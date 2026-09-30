package com.injekcije.app.data

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicInteger

/**
 * Rezultat import operacije.
 * [Success] - svi podaci uspešno parsirani i primenjeni.
 * [Error] - parsiranje nije uspelo, stanje repozitorijuma je nepromenjeno.
 */
sealed class ImportResult {
    /** [skipped] > 0 je moguće samo u tolerantnom režimu (učitavanje lokalnog fajla). */
    data class Success(val skipped: Int = 0, val summary: String = "") : ImportResult()
    data class Error(val message: String) : ImportResult()
}

class InjectionRepository {

    companion object {
        private const val TAG = "InjectionRepository"
        private const val LOCAL_BACKUP_FILE = "ciklus_local_data.json"

        private const val KEY_SUBSTANCES = "substances"
        private const val KEY_CYCLES = "cycles"
        private const val KEY_INJECTIONS = "injections"
        private const val KEY_ORAL_DOSES = "oral_doses"
        private const val KEY_SITE_INDEX = "site_rotation_index"
        private const val KEY_ENABLED_SITES = "enabled_sites"
        private const val KEY_PCT_DATA = "pct_data"
        private const val KEY_NOTIFICATION_SETTINGS = "notification_settings"
        private const val KEY_EXPORTED_AT = "exported_at"
        private const val SAVE_DELAY_MS = 3000L // 3 sekunde debounce za batch čuvanje

        // Memoizovana fallback instanca za cold-start pozive (BroadcastReceiver-i
        // pozivaju getInstance() više puta po događaju - JSON se parsira samo jednom)
        @Volatile
        private var fallbackInstance: InjectionRepository? = null

        /**
         * Dohvata Repository singleton iz Application konteksta.
         * Ako proces nije živ (cold start iz BroadcastReceiver-a),
         * kreira privremenu instancu, učitava iz lokalne memorije
         * i memoizira je za naredne pozive.
         */
        fun getInstance(context: Context): InjectionRepository {
            val app = context.applicationContext
            if (app is com.injekcije.app.InjekcijeApp && app.isInitialized) {
                return app.repository
            }
            fallbackInstance?.let { return it }
            return synchronized(this) {
                fallbackInstance ?: InjectionRepository().also {
                    it.loadFromLocal(app)
                    fallbackInstance = it
                }
            }
        }

        /**
         * Čita "exported_at" timestamp (epoch millis) iz backup JSON-a bez menjanja stanja.
         * Vraća null ako polje ne postoji, nije validno ili JSON ne može da se parsira.
         * UI sloj ovo koristi za upozorenje pri vraćanju backup-a starijeg od lokalnih podataka.
         */
        fun peekExportedAt(json: String): Long? = try {
            val obj = JSONObject(json)
            if (obj.has(KEY_EXPORTED_AT)) obj.optLong(KEY_EXPORTED_AT).takeIf { it > 0 } else null
        } catch (_: Exception) {
            null
        }
    }

    // Thread safety
    private val lock = Any()

    // In-memory cache
    private val substances = mutableMapOf<String, Substance>()
    private val cycles = mutableMapOf<String, Cycle>()
    private val injections = mutableListOf<InjectionRecord>()
    private val oralDoses = mutableListOf<OralDoseRecord>()
    private var siteRotationIndex = 0
    private var enabledSites = mutableSetOf<String>().apply {
        addAll(InjectionSite.entries.map { it.key })
    }
    private val pctDataMap = mutableMapOf<String, PCTData>()
    private var notificationSettings = NotificationSettings()

    // Keš za getUpcomingInjections() - invalidira se kad se promene relevantni podaci.
    // Keširaju se samo vremenski NEZAVISNI delovi (scheduledDateTime, doza, sajt);
    // hoursRemaining se računa sveže pri svakom pozivu.
    // Čita se i piše isključivo pod [lock].
    private var upcomingInjectionsCache: List<NextInjectionInfo>? = null
    private var upcomingInjectionsCacheDirty = true
    private var upcomingInjectionsCacheWeek = -1

    // Dirty flag praćenje - označava koje kolekcije su promenjene od poslednjeg čuvanja
    private val dirtyCollections = mutableSetOf<String>()
    private val saveHandler = Handler(Looper.getMainLooper())
    @Volatile
    private var pendingSaveContext: Context? = null
    // Jednonitni executor - garantuje da je najviše JEDAN upis fajla u toku
    private val saveExecutor = Executors.newSingleThreadExecutor { r ->
        Thread(r, "ciklus-save").apply { isDaemon = true }
    }
    private val saveRunnable = Runnable {
        pendingSaveContext?.let { ctx -> saveExecutor.execute { saveToLocal(ctx) } }
    }

    // Callbacks for UI updates and cloud sync.
    // ConcurrentHashMap + AtomicInteger - registracija sa UI niti i notifikacije
    // sa proizvoljnih niti ne smeju da izazovu ConcurrentModificationException.
    private val onChangeCallbacks = ConcurrentHashMap<Int, () -> Unit>()
    private val nextCallbackId = AtomicInteger(0)

    init {
        initializeDefaultSubstances()
    }

    // =========================================================================
    // CHANGE CALLBACKS
    // =========================================================================

    fun onChange(callback: () -> Unit): Int {
        val id = nextCallbackId.getAndIncrement()
        onChangeCallbacks[id] = callback
        return id
    }

    fun removeOnChange(token: Int) {
        onChangeCallbacks.remove(token)
    }

    private fun notifyChange() {
        // Iteracija preko snapshot-a - callback može da registruje/ukloni callback-ove
        val snapshot = onChangeCallbacks.values.toList()
        for (cb in snapshot) {
            try {
                cb()
            } catch (e: Exception) {
                Log.w(TAG, "onChange callback failed", e)
            }
        }
    }

    fun close() {
        onChangeCallbacks.clear()
        saveHandler.removeCallbacks(saveRunnable)
    }

    /**
     * Označava kolekciju kao promenjenu (dirty).
     * Koristi se za optimizovano čuvanje - samo promenjeni delovi se serijalizuju.
     */
    private fun markDirty(collection: String) {
        synchronized(lock) {
            dirtyCollections.add(collection)
        }
    }

    /**
     * Invalidira keš za getUpcomingInjections().
     * Poziva se kad se promene supstance, ciklusi ili injekcije.
     */
    private fun invalidateUpcomingInjectionsCache() {
        synchronized(lock) {
            upcomingInjectionsCacheDirty = true
            upcomingInjectionsCache = null
            upcomingInjectionsCacheWeek = -1
        }
    }

    /**
     * Zakazuje odloženo čuvanje sa debounce-om od 3 sekunde.
     * Ako se više promena desi u kratkom periodu, čuva se samo jednom (batch).
     * Sam upis se izvršava na pozadinskoj niti, serijalizovano (jedan po jedan).
     */
    fun scheduleSave(context: Context) {
        pendingSaveContext = context.applicationContext
        saveHandler.removeCallbacks(saveRunnable)
        saveHandler.postDelayed(saveRunnable, SAVE_DELAY_MS)
    }

    /**
     * Vraća skup dirty kolekcija i čisti ga.
     * Ako je prazan, znači da je potreban pun save.
     */
    private fun consumeDirtyCollections(): Set<String> {
        return synchronized(lock) {
            val copy = dirtyCollections.toSet()
            dirtyCollections.clear()
            copy
        }
    }

    /**
     * Resetuje indeks rotacije sajtova na 0.
     * Poziva se kada se promene aktivni sajtovi ili kada indeks postane nevalidan.
     */
    fun resetSiteRotation() {
        synchronized(lock) {
            siteRotationIndex = 0
        }
        notifyChange()
    }

    // =========================================================================
    // SUBSTANCE OPERATIONS
    // =========================================================================

    fun getSubstances(): List<Substance> = synchronized(lock) {
        substances.values.toList()
    }

    fun getSubstance(id: String): Substance? = synchronized(lock) {
        substances[id] ?: SampleData.getSubstanceById(id)
    }

    fun addSubstance(substance: Substance) {
        synchronized(lock) { substances[substance.id] = substance }
        markDirty(KEY_SUBSTANCES)
        invalidateUpcomingInjectionsCache()
        notifyChange()
    }

    // =========================================================================
    // CYCLE OPERATIONS
    // =========================================================================

    fun getActiveCycle(): Cycle? = synchronized(lock) {
        cycles.values.firstOrNull { it.isActive && !it.isFinished() }
    }

    fun getAllCycles(): List<Cycle> = synchronized(lock) {
        cycles.values.toList().sortedByDescending { it.startDate }
    }

    fun getCycle(id: String): Cycle? = synchronized(lock) {
        cycles[id]
    }

    fun addCycle(cycle: Cycle) {
        synchronized(lock) {
            cycles.values.filter { it.isActive }.forEach {
                cycles[it.id] = it.copy(isActive = false)
            }
            cycles[cycle.id] = cycle
        }
        markDirty(KEY_CYCLES)
        invalidateUpcomingInjectionsCache()
        notifyChange()
    }

    fun updateCycle(cycle: Cycle) {
        synchronized(lock) { cycles[cycle.id] = cycle }
        markDirty(KEY_CYCLES)
        invalidateUpcomingInjectionsCache()
        notifyChange()
    }

    fun updateCompoundDose(substanceId: String, newWeeklyDoseMg: Double, newFrequency: InjectionFrequency? = null) {
        synchronized(lock) {
            val cycle = cycles.values.firstOrNull { it.isActive && !it.isFinished() } ?: return
            val updatedCompounds = cycle.compounds.map { compound ->
                if (compound.substanceId == substanceId) {
                    compound.copy(
                        weeklyDoseMg = newWeeklyDoseMg,
                        frequency = newFrequency ?: compound.frequency
                    )
                } else compound
            }
            cycles[cycle.id] = cycle.copy(compounds = updatedCompounds)
        }
        markDirty(KEY_CYCLES)
        invalidateUpcomingInjectionsCache()
        notifyChange()
    }

    fun updateCompoundPhases(substanceId: String, phases: List<DosePhase>) {
        synchronized(lock) {
            val cycle = cycles.values.firstOrNull { it.isActive && !it.isFinished() } ?: return
            val updatedCompounds = cycle.compounds.map { compound ->
                if (compound.substanceId == substanceId) {
                    compound.copy(phases = phases)
                } else compound
            }
            cycles[cycle.id] = cycle.copy(compounds = updatedCompounds)
        }
        markDirty(KEY_CYCLES)
        invalidateUpcomingInjectionsCache()
        notifyChange()
    }

    fun addCompoundToCycle(compound: CycleCompound) {
        synchronized(lock) {
            val cycle = cycles.values.firstOrNull { it.isActive && !it.isFinished() } ?: return
            if (cycle.compounds.any { it.substanceId == compound.substanceId }) return
            val updatedCompounds = cycle.compounds + compound
            cycles[cycle.id] = cycle.copy(compounds = updatedCompounds)
        }
        markDirty(KEY_CYCLES)
        invalidateUpcomingInjectionsCache()
        notifyChange()
    }

    /**
     * Uklanja jedinjenje iz aktivnog ciklusa.
     * @return true ako je jedinjenje uklonjeno, false ako je to poslednje jedinjenje u ciklusu.
     * Ne dozvoljava brisanje poslednjeg jedinjenja jer bi ciklus ostao prazan.
     */
    fun removeCompoundFromCycle(substanceId: String): Boolean {
        val removed = synchronized(lock) {
            val cycle = cycles.values.firstOrNull { it.isActive && !it.isFinished() } ?: return false
            // Sprečavanje brisanja poslednjeg jedinjenja - ciklus mora imati bar jedno
            if (cycle.compounds.size <= 1) {
                Log.w(TAG, "Ne možete obrisati poslednje jedinjenje iz ciklusa")
                return false
            }
            val updatedCompounds = cycle.compounds.filter { it.substanceId != substanceId }
            cycles[cycle.id] = cycle.copy(compounds = updatedCompounds)
            true
        }
        if (removed) {
            markDirty(KEY_CYCLES)
            invalidateUpcomingInjectionsCache()
            notifyChange()
        }
        return removed
    }

    fun deleteCycle(id: String) {
        synchronized(lock) {
            cycles.remove(id)
            injections.removeAll { it.cycleId == id }
            // Ukloni i povezane oralne doze i PCT podatke - inače ostaju
            // "siročići" (PCT kartica za nepostojeći ciklus, zastarele oralne doze)
            oralDoses.removeAll { it.cycleId == id }
            pctDataMap.remove(id)
        }
        markDirty(KEY_CYCLES)
        markDirty(KEY_INJECTIONS)
        markDirty(KEY_ORAL_DOSES)
        markDirty(KEY_PCT_DATA)
        invalidateUpcomingInjectionsCache()
        notifyChange()
    }

    // =========================================================================
    // INJECTION OPERATIONS
    // =========================================================================

    fun getInjections(): List<InjectionRecord> = synchronized(lock) {
        injections.toList().sortedByDescending { it.dateTime }
    }

    fun getInjectionsForCycle(cycleId: String): List<InjectionRecord> = synchronized(lock) {
        injections.filter { it.cycleId == cycleId }.sortedByDescending { it.dateTime }
    }

    fun getInjectionsForSubstance(substanceId: String): List<InjectionRecord> = synchronized(lock) {
        injections.filter { it.substanceId == substanceId }.sortedByDescending { it.dateTime }
    }

    fun getLastInjectionForSubstance(substanceId: String): InjectionRecord? = synchronized(lock) {
        injections
            .filter { it.substanceId == substanceId }
            .maxByOrNull { it.dateTime }
    }

    fun getInjectionsForSite(site: InjectionSite): List<InjectionRecord> = synchronized(lock) {
        injections.filter { it.site == site }.sortedByDescending { it.dateTime }
    }

    fun addInjection(record: InjectionRecord) {
        synchronized(lock) {
            injections.add(record)
            val rotation = getEnabledRotationOrder()
            if (rotation.isNotEmpty()) {
                // Validacija pre inkrementiranja - indeks može biti nevalidan posle importa/promene sajtova
                val safeIndex = siteRotationIndex % rotation.size
                siteRotationIndex = (safeIndex + 1) % rotation.size
            }
        }
        markDirty(KEY_INJECTIONS)
        markDirty(KEY_SITE_INDEX)
        invalidateUpcomingInjectionsCache()
        notifyChange()
    }

    fun addCombinedInjections(records: List<InjectionRecord>) {
        if (records.isEmpty()) return
        synchronized(lock) {
            records.forEach { injections.add(it) }
            val rotation = getEnabledRotationOrder()
            if (rotation.isNotEmpty()) {
                val safeIndex = siteRotationIndex % rotation.size
                siteRotationIndex = (safeIndex + 1) % rotation.size
            }
        }
        markDirty(KEY_INJECTIONS)
        markDirty(KEY_SITE_INDEX)
        invalidateUpcomingInjectionsCache()
        notifyChange()
    }

    fun updateInjection(record: InjectionRecord) {
        synchronized(lock) {
            val index = injections.indexOfFirst { it.id == record.id }
            if (index >= 0) {
                injections[index] = record
            }
        }
        markDirty(KEY_INJECTIONS)
        invalidateUpcomingInjectionsCache()
        notifyChange()
    }

    fun deleteInjection(id: String): Boolean {
        val removed = synchronized(lock) {
            val index = injections.indexOfFirst { it.id == id }
            if (index >= 0) {
                injections.removeAt(index)
                true
            } else false
        }
        if (removed) {
            markDirty(KEY_INJECTIONS)
            invalidateUpcomingInjectionsCache()
            notifyChange()
        }
        return removed
    }

    // =========================================================================
    // ORAL DOSE RECORDS
    // =========================================================================

    fun getOralDoses(): List<OralDoseRecord> = synchronized(lock) {
        oralDoses.toList()
    }

    fun getOralDosesForSubstance(substanceId: String): List<OralDoseRecord> = synchronized(lock) {
        oralDoses.filter { it.substanceId == substanceId }
    }

    fun getLastOralDoseForSubstance(substanceId: String): OralDoseRecord? = synchronized(lock) {
        oralDoses.filter { it.substanceId == substanceId }
            .maxByOrNull { it.dateTime }
    }

    fun getTodayOralDosesForSubstance(substanceId: String): List<OralDoseRecord> = synchronized(lock) {
        val today = java.time.LocalDate.now()
        oralDoses.filter { it.substanceId == substanceId && it.dateTime.toLocalDate() == today }
    }

    fun addOralDose(record: OralDoseRecord) {
        synchronized(lock) {
            oralDoses.add(record)
        }
        markDirty(KEY_ORAL_DOSES)
        notifyChange()
    }

    fun deleteOralDose(id: String): Boolean {
        val removed = synchronized(lock) {
            val index = oralDoses.indexOfFirst { it.id == id }
            if (index >= 0) {
                oralDoses.removeAt(index)
                true
            } else false
        }
        if (removed) {
            markDirty(KEY_ORAL_DOSES)
            notifyChange()
        }
        return removed
    }

    // =========================================================================
    // SITE ROTATION
    // =========================================================================

    fun getEnabledSites(): Set<InjectionSite> = synchronized(lock) {
        InjectionSite.entries.filter { it.key in enabledSites }.toSet()
    }

    /**
     * Postavlja aktivne sajtove za rotaciju.
     * Nakon svake promene, validira siteRotationIndex:
     * - Ako je indeks >= broja aktivnih sajtova, resetuje se na 0
     * - Ako nema aktivnih sajtova, indeks ostaje 0
     */
    fun setEnabledSites(sites: Set<InjectionSite>) {
        synchronized(lock) {
            enabledSites.clear()
            enabledSites.addAll(sites.map { it.key })
            val enabledRotation = getEnabledRotationOrder()
            if (enabledRotation.isEmpty() || siteRotationIndex >= enabledRotation.size) {
                // Indeks je nevalidan za novi skup sajtova - resetuj na 0
                siteRotationIndex = 0
            }
        }
        markDirty(KEY_ENABLED_SITES)
        markDirty(KEY_SITE_INDEX)
        notifyChange()
    }

    fun isSiteEnabled(site: InjectionSite): Boolean = synchronized(lock) {
        site.key in enabledSites
    }

    private fun getEnabledRotationOrder(): List<InjectionSite> = synchronized(lock) {
        InjectionSite.rotationOrder().filter { it.key in enabledSites }
    }

    fun getNextSite(): InjectionSite = synchronized(lock) {
        val rotation = getEnabledRotationOrder()
        if (rotation.isEmpty()) return InjectionSite.GLUTE_L
        rotation[siteRotationIndex % rotation.size]
    }

    fun getSiteHistory(): List<SiteHistory> = synchronized(lock) {
        val now = LocalDateTime.now()
        InjectionSite.entries.map { site ->
            val siteInjections = injections.filter { it.site == site }
            val lastUsed = siteInjections.maxByOrNull { it.dateTime }?.dateTime
            val daysSince = lastUsed?.let {
                ChronoUnit.DAYS.between(it.toLocalDate(), now.toLocalDate()).toInt()
            }
            SiteHistory(
                site = site,
                lastUsed = lastUsed,
                totalInjections = siteInjections.size,
                daysSinceLastUse = daysSince
            )
        }
    }

    fun getRecommendedSite(): InjectionSite {
        val history = getSiteHistory()
        val rotation = getEnabledRotationOrder()

        if (rotation.isEmpty()) return InjectionSite.GLUTE_L

        for (site in rotation) {
            val siteHist = history.find { it.site == site }
            if (siteHist?.isAvailable == true) {
                return site
            }
        }

        return history
            .filter { it.site in rotation }
            .maxByOrNull { it.daysSinceLastUse ?: Int.MAX_VALUE }
            ?.site ?: getNextSite()
    }

    // =========================================================================
    // SCHEDULING
    // =========================================================================

    fun getUpcomingInjections(): List<NextInjectionInfo> {
        val cycle = getActiveCycle()
        if (cycle == null) {
            synchronized(lock) {
                upcomingInjectionsCache = emptyList()
                upcomingInjectionsCacheDirty = false
                upcomingInjectionsCacheWeek = -1
            }
            return emptyList()
        }

        val now = LocalDateTime.now()
        val today = now.toLocalDate()
        val currentWeek = cycle.currentWeek()

        // Vraćaj keširani rezultat ako podaci nisu promenjeni i nedelja ciklusa je ista.
        // hoursRemaining se UVEK računa sveže - zavisi od trenutnog vremena, a keš
        // može da bude star satima (npr. UI koji dugo stoji otvoren).
        val cached = synchronized(lock) {
            if (!upcomingInjectionsCacheDirty && upcomingInjectionsCacheWeek == currentWeek) {
                upcomingInjectionsCache
            } else null
        }
        if (cached != null) {
            return cached.map {
                it.copy(hoursRemaining = ChronoUnit.MINUTES.between(now, it.scheduledDateTime) / 60.0)
            }
        }

        val results = mutableListOf<NextInjectionInfo>()

        for (compound in cycle.compounds) {
            // Skip if not active this week
            if (!compound.isActiveInWeek(currentWeek, cycle.durationWeeks)) continue

            // Skip oral steroids (they have a separate notification system)
            if (OralDosingSchedule.isOralSteroid(compound.substanceId)) continue

            val substance = getSubstance(compound.substanceId) ?: continue
            // Poslednja injekcija SAMO iz aktivnog ciklusa - injekcija iste supstance
            // iz starog ciklusa ne sme da pomera raspored novog (prikazivala bi
            // OVERDUE mesecima umesto AWAITING_FIRST)
            val lastInjection = synchronized(lock) {
                injections
                    .filter { it.substanceId == compound.substanceId && it.cycleId == cycle.id }
                    .maxByOrNull { it.dateTime }
            }
            val isFirstInjection = lastInjection == null

            val nextDateTime = when {
                lastInjection != null ->
                    lastInjection.dateTime.plusMinutes((compound.frequency.days * 24 * 60).toLong())
                // Ciklus još nije počeo - prva injekcija se zakazuje za datum početka,
                // ne za "sada" (inače bi se prikazivala kao dospela pre početka ciklusa)
                today.isBefore(cycle.startDate) -> cycle.startDate.atTime(9, 0)
                else -> now
            }

            val hoursRemaining = ChronoUnit.MINUTES.between(now, nextDateTime) / 60.0

            val doseMg = compound.getDosePerInjectionForWeek(currentWeek)
            val volumeMl = compound.getVolumePerInjectionForWeek(currentWeek)

            results.add(
                NextInjectionInfo(
                    substanceId = compound.substanceId,
                    substanceName = substance.shortName,
                    scheduledDateTime = nextDateTime,
                    site = getRecommendedSite(),
                    doseMg = doseMg,
                    volumeMl = volumeMl,
                    hoursRemaining = hoursRemaining,
                    isFirstInjection = isFirstInjection
                )
            )
        }

        val sorted = results.sortedBy { it.scheduledDateTime }
        synchronized(lock) {
            upcomingInjectionsCache = sorted
            upcomingInjectionsCacheDirty = false
            upcomingInjectionsCacheWeek = currentWeek
        }
        return sorted
    }

    fun getMostUrgentInjection(): NextInjectionInfo? {
        return getUpcomingInjections().minByOrNull { it.hoursRemaining }
    }

    fun getWeeklySchedule(): List<WeekSchedule> {
        val cycle = getActiveCycle() ?: return emptyList()
        val cycleInjections = getInjectionsForCycle(cycle.id)

        return (1..cycle.durationWeeks).map { week ->
            val weekInjections = cycleInjections.filter { it.week == week }
            WeekSchedule(
                week = week,
                isCurrent = week == cycle.currentWeek(),
                compounds = cycle.compounds
                    .filter { it.isActiveInWeek(week, cycle.durationWeeks) }
                    .map { compound ->
                        val substance = getSubstance(compound.substanceId)
                        val injCount = weekInjections.count { it.substanceId == compound.substanceId }
                        val expectedCount = compound.frequency.injectionsPerWeek.toInt()
                        CompoundWeekStatus(
                            substanceId = compound.substanceId,
                            substanceName = substance?.shortName ?: compound.substanceId,
                            weeklyDoseMg = compound.getDoseForWeek(week),
                            injectionsCompleted = injCount,
                            injectionsExpected = expectedCount
                        )
                    }
            )
        }
    }

    // =========================================================================
    // PCT (Post-Cycle Therapy) OPERATIONS
    //
    // Zasnovano na kliničkim studijama:
    // - Wenker et al. "A Systematic Approach to Recovery from AAS Use"
    //   Fertility & Sterility, 2023 (PubMed: PMID 36898456)
    // - Rahnema et al. "Clomiphene citrate in male infertility"
    //   J Urol, 2024 (PubMed: PMID 25264337) - 69.2% normozoospermija samo Clomiphene
    // - Ko et al. "Empirical medical therapy for idiopathic male infertility"
    //   Urology, 2024 (PubMed: PMID 22516357) - 87.5% sa Clomiphene+hCG
    //
    // Nivo supresije HPG ose zavisi od:
    // 1. Tipa jedinjenja (19-nor steroidi poput Nandrolona/Trenbolona su najsupresivniji)
    // 2. Trajanja ciklusa (duži = teži oporavak)
    // 3. Doze (veća = veća supresija)
    //
    // Periodi čekanja pre PCT-a bazirani na poluživotima estera:
    // - Propionat/Acetat: 3 dana (kratki estar, t½ ~0.8 dana)
    // - Enanthate/Cypionat: 14 dana (srednji estar, t½ ~4.5 dana)
    // - Undecanoat/Dekanoat: 21-28 dana (dugi estar, t½ ~15 dana)
    // Princip: čekati ~5 poluživota da ester potpuno napusti organizam
    // =========================================================================

    /**
     * Dohvata PCT podatke za određeni ciklus.
     */
    fun getPCTData(cycleId: String): PCTData? = synchronized(lock) {
        pctDataMap[cycleId]
    }

    /**
     * Get PCT data for the most recently finished cycle that needs PCT.
     */
    fun getActivePCTData(): PCTData? = synchronized(lock) {
        val today = java.time.LocalDate.now()
        pctDataMap.values
            .filter { pct ->
                val phase = pct.currentPhase(today)
                phase != PCTPhase.COMPLETED
            }
            .maxByOrNull { it.lastInjectionDate }
    }

    /**
     * Kreira PCT podatke kada se ciklus završi.
     *
     * Automatski detektuje najduži estar korišćen u ciklusu da bi izračunao
     * period čekanja pre početka PCT-a. Ovo je kritično jer počinjanje PCT-a
     * dok su egzogeni androgeni još u cirkulaciji smanjuje efikasnost SERM-ova.
     *
     * Logika nivoa supresije (iz CyclePCTAdvice):
     * - Blag: samo testosteron, kratki estar, ≤12 nedelja
     * - Umeren: testosteron srednji/dugi estar, ≤16 nedelja
     * - Težak: 19-nor steroidi (Nandrolon, Trenbolon), oralni steroidi, >16 nedelja
     * - Ekstreman: 19-nor + dugi estar + >20 nedelja
     *
     * Reference: Jarow & Lipshultz, "Anabolic steroid-induced hypogonadism"
     * (PubMed: PMID 2210116)
     */
    fun createPCTData(cycleId: String, lastInjectionDate: java.time.LocalDate? = null): PCTData? {
        val cycle = getCycle(cycleId) ?: return null
        val today = java.time.LocalDate.now()

        // Validacija datuma ciklusa
        if (cycle.startDate.isAfter(cycle.endDate)) {
            Log.w(TAG, "createPCTData: datum početka ciklusa (${cycle.startDate}) je posle datuma kraja (${cycle.endDate})")
            return null
        }
        if (cycle.durationWeeks <= 0) {
            Log.w(TAG, "createPCTData: trajanje ciklusa mora biti pozitivno (${cycle.durationWeeks})")
            return null
        }

        val cycleInjections = getInjectionsForCycle(cycleId)

        // Find the last injection date
        val rawLastDate = lastInjectionDate
            ?: cycleInjections.maxByOrNull { it.dateTime }?.dateTime?.toLocalDate()
            ?: cycle.endDate

        // Validacija da datum poslednje injekcije nije u budućnosti
        val lastDate = if (rawLastDate.isAfter(today)) {
            Log.w(TAG, "createPCTData: datum poslednje injekcije ($rawLastDate) je u budućnosti, koristim današnji datum")
            today
        } else {
            rawLastDate
        }

        // Određivanje najdužeg estera iz korišćenih jedinjenja.
        // Koristi eksplicitni Substance.esterType kada postoji (EsterType.fromSubstance),
        // a nesupresivni pomoćni preparati (hCG, hGH, HMG) se PRESKAČU -
        // hCG se koristi i tokom ciklusa i ne sme da produžava čekanje na PCT.
        val longestEster = cycle.compounds
            .mapNotNull { compound ->
                val substance = getSubstance(compound.substanceId)
                when {
                    isNonSuppressiveAncillary(compound.substanceId, substance) -> null
                    substance != null -> EsterType.fromSubstance(substance)
                    else -> EsterType.fromSubstanceId(compound.substanceId)
                }
            }
            .maxByOrNull { it.pctWaitDays }
            ?: EsterType.ENANTHATE

        val pctData = PCTData(
            cycleId = cycleId,
            lastInjectionDate = lastDate,
            longestEster = longestEster,
            protocol = PCTProtocolType.CLOMIPHENE_ONLY
        )

        synchronized(lock) {
            pctDataMap[cycleId] = pctData
        }
        markDirty(KEY_PCT_DATA)
        notifyChange()
        return pctData
    }

    /**
     * Nesupresivni pomoćni preparati (hCG, hGH, HMG) - ne gase HPG osu
     * i ne ulaze u računanje perioda čekanja pre PCT-a.
     */
    private fun isNonSuppressiveAncillary(substanceId: String, substance: Substance?): Boolean {
        val lower = substanceId.lowercase()
        if (lower == "hcg" || lower == "hgh" || lower == "hmg") return true
        return substance?.category == "PCT" || substance?.category == "Peptidi"
    }

    /**
     * Update PCT data (e.g., when user starts PCT, changes protocol, adds bloodwork).
     */
    fun updatePCTData(pctData: PCTData) {
        synchronized(lock) {
            pctDataMap[pctData.cycleId] = pctData
        }
        markDirty(KEY_PCT_DATA)
        notifyChange()
    }

    /**
     * Mark PCT as started.
     */
    fun startPCT(cycleId: String, startDate: java.time.LocalDate = java.time.LocalDate.now()): PCTData? {
        val pct = getPCTData(cycleId) ?: return null
        val updated = pct.copy(pctStartDate = startDate)
        updatePCTData(updated)
        return updated
    }

    /**
     * Mark PCT as completed.
     */
    fun completePCT(cycleId: String, endDate: java.time.LocalDate = java.time.LocalDate.now()): PCTData? {
        val pct = getPCTData(cycleId) ?: return null
        val updated = pct.copy(pctEndDate = endDate)
        updatePCTData(updated)
        return updated
    }

    /**
     * Add bloodwork date to PCT tracking.
     */
    fun addPCTBloodwork(cycleId: String, date: java.time.LocalDate = java.time.LocalDate.now()): PCTData? {
        val pct = getPCTData(cycleId) ?: return null
        val updated = pct.copy(bloodworkDates = pct.bloodworkDates + date)
        updatePCTData(updated)
        return updated
    }

    /**
     * Change PCT protocol.
     */
    fun setPCTProtocol(cycleId: String, protocol: PCTProtocolType): PCTData? {
        val pct = getPCTData(cycleId) ?: return null
        val updated = pct.copy(protocol = protocol)
        updatePCTData(updated)
        return updated
    }

    /**
     * Generiše kompletne PCT informacije za prikaz u UI.
     *
     * Uključuje:
     * - Trenutnu fazu PCT-a (čekanje, spreman, aktivan, praćenje, završen)
     * - Progres na osnovu minimalnog trajanja za korišćena jedinjenja
     * - Datum sledeće krvne slike (6 nedelja posle početka, zatim svake 4 nedelje)
     * - Detalje protokola sa dozama i stopama uspešnosti iz kliničkih studija
     * - Specifične savete za jedinjenja (npr. Nandrolon/Trenbolon zahtevaju hCG)
     *
     * Protokoli (na osnovu PubMed meta-analiza):
     * - Samo Clomiphene 25mg/dan: 69.2% normozoospermija (Rahnema et al.)
     * - Clomiphene 25mg/dan + hCG 1500IU 3x/ned: 87.5% normozoospermija (Ko et al.)
     * - Bez PCT (spontani oporavak): 58.6% normozoospermija, ali 6-12 meseci
     */
    fun getPCTGuideInfo(cycleId: String): PCTGuideInfo? {
        val pct = getPCTData(cycleId) ?: return null
        val today = java.time.LocalDate.now()
        val status = pct.currentStatus(today)
        val phase = pct.currentPhase(today)
        val daysUntilStart = pct.daysUntilPCT(today)

        val daysSinceStart = pct.pctStartDate?.let {
            ChronoUnit.DAYS.between(it, today)
        }

        // Generate compound-specific advice based on cycle compounds
        val cycle = getCycle(cycleId)
        val compoundAdvice = cycle?.let {
            val substanceIds = it.compounds.map { compound -> compound.substanceId }
            if (substanceIds.isNotEmpty()) {
                CyclePCTAdvice.fromSubstanceIds(substanceIds)
            } else null
        }

        // Progress based on compound-specific minimum duration
        val minDurationDays = (compoundAdvice?.recommendedDurationWeeks ?: 12) * 7
        val progressPercent = if (daysSinceStart != null) {
            (daysSinceStart / minDurationDays.toDouble()).toFloat().coerceIn(0f, 1f)
        } else 0f

        // Next bloodwork date (6 weeks after start, then every 4 weeks)
        val nextBloodworkDate = if (pct.pctStartDate != null && phase == PCTPhase.ACTIVE) {
            val lastBloodwork = pct.bloodworkDates.maxOrNull()
            if (lastBloodwork != null) {
                lastBloodwork.plusWeeks(4)
            } else {
                pct.pctStartDate.plusWeeks(6)
            }
        } else null

        // Protocol details based on selected protocol and compound duration
        val minWeeks = compoundAdvice?.recommendedDurationWeeks ?: 12
        val minMonths = "%.0f".format(minWeeks / 4.3)
        val optMonths = "%.0f".format(minWeeks * 1.5 / 4.3)
        val protocolDetails = when (pct.protocol) {
            PCTProtocolType.CLOMIPHENE_ONLY -> """
                Clomiphene Citrate 25mg dnevno
                Trajanje: minimum $minWeeks nedelja (~$minMonths mes.), optimalno ~$optMonths mes.
                Uspešnost: 69.2% normozoospermija
                Prekid na osnovu laboratorija (TT, LH, FSH)
            """.trimIndent()
            PCTProtocolType.CLOMIPHENE_HCG -> """
                Clomiphene Citrate 25mg dnevno
                + hCG 1500 IU subkutano 3x nedeljno (tokom celog PCT-a)
                Trajanje: minimum $minWeeks nedelja (~$minMonths mes.), optimalno ~$optMonths mes.
                Uspešnost: 87.5% normozoospermija
                Prekid na osnovu laboratorija (TT, LH, FSH)
            """.trimIndent()
            PCTProtocolType.NO_PCT -> """
                Spontani oporavak bez terapije
                Trajanje: 6-12 meseci za testosteron
                Uspešnost: 58.6% normozoospermija
            """.trimIndent()
        }

        return PCTGuideInfo(
            pctData = pct,
            status = status,
            phase = phase,
            daysUntilStart = daysUntilStart,
            daysSinceStart = daysSinceStart,
            progressPercent = progressPercent,
            nextBloodworkDate = nextBloodworkDate,
            protocolDetails = protocolDetails,
            compoundAdvice = compoundAdvice
        )
    }

    /**
     * Get PCT guide info for the active/most recent PCT.
     */
    fun getActivePCTGuideInfo(): PCTGuideInfo? {
        val pct = getActivePCTData() ?: return null
        return getPCTGuideInfo(pct.cycleId)
    }

    /**
     * Check if there's a finished cycle that needs PCT setup.
     */
    fun getFinishedCycleNeedingPCT(): Cycle? = synchronized(lock) {
        val today = java.time.LocalDate.now()
        cycles.values
            .filter { it.isFinished(today) || !it.isActive }
            .filter { cycle -> !pctDataMap.containsKey(cycle.id) }
            // Ciklus bez ijednog unosa (npr. odmah zamenjen novim) ne traži PCT
            .filter { cycle ->
                injections.any { it.cycleId == cycle.id } ||
                    oralDoses.any { it.cycleId == cycle.id }
            }
            .maxByOrNull { it.endDate }
    }

    /**
     * Delete PCT data for a cycle.
     */
    fun deletePCTData(cycleId: String) {
        synchronized(lock) {
            pctDataMap.remove(cycleId)
        }
        markDirty(KEY_PCT_DATA)
        notifyChange()
    }

    // =========================================================================
    // NOTIFICATION SETTINGS
    // =========================================================================

    fun getNotificationSettings(): NotificationSettings = synchronized(lock) {
        notificationSettings
    }

    fun updateNotificationSettings(settings: NotificationSettings) {
        synchronized(lock) {
            notificationSettings = settings
        }
        markDirty(KEY_NOTIFICATION_SETTINGS)
        notifyChange()
    }

    // =========================================================================
    // EXPORT/IMPORT (for cloud sync)
    // =========================================================================

    private fun buildJson(): JSONObject = synchronized(lock) {
        JSONObject().apply {
            put(KEY_SUBSTANCES, JSONArray().apply {
                substances.values.forEach { put(it.toJson()) }
            })
            put(KEY_CYCLES, JSONArray().apply {
                cycles.values.forEach { put(it.toJson()) }
            })
            put(KEY_INJECTIONS, JSONArray().apply {
                injections.forEach { put(it.toJson()) }
            })
            put(KEY_ORAL_DOSES, JSONArray().apply {
                oralDoses.forEach { put(it.toJson()) }
            })
            put(KEY_SITE_INDEX, siteRotationIndex)
            put(KEY_ENABLED_SITES, JSONArray().apply {
                enabledSites.forEach { put(it) }
            })
            put(KEY_PCT_DATA, JSONArray().apply {
                pctDataMap.values.forEach { put(it.toJson()) }
            })
            put(KEY_NOTIFICATION_SETTINGS, notificationSettings.toJson())
            // Vreme izvoza - UI koristi za upozorenje pri vraćanju starog backup-a
            put(KEY_EXPORTED_AT, System.currentTimeMillis())
        }
    }

    fun exportJson(): String = buildJson().toString(2)

    /**
     * Atomski uvoz JSON podataka.
     *
     * Sve podatke parsira u privremene varijable PRE nego što promeni stanje repozitorijuma.
     * Ako bilo koji deo parsiranja ne uspe, vraća grešku BEZ menjanja internog stanja.
     * Tek kada je sve uspešno parsirano, primenjuje promene u jednom synchronized bloku.
     *
     * @return [ImportResult.Success] ako je uvoz uspešan, [ImportResult.Error] sa porukom ako nije.
     */
    fun importJson(jsonText: String, strict: Boolean = true): ImportResult {
        // Faza 1: Parsiranje i validacija CELOG sadržaja (BackupPayloadParser, bez izmene stanja).
        // Ako bilo šta ovde padne, stanje repozitorijuma ostaje netaknuto.
        // strict = true: neispravan/preskočen zapis je greška celog vraćanja (nikad delimičan uspeh).
        val payload = try {
            BackupPayloadParser.parse(jsonText, strict)
        } catch (e: BackupPayloadException) {
            Log.e(TAG, "Import odbijen - stanje repozitorijuma nepromenjeno: ${e.message}")
            return ImportResult.Error(e.message ?: "Backup je oštećen.")
        } catch (e: Exception) {
            Log.e(TAG, "Import parsiranje neuspešno - stanje repozitorijuma nepromenjeno", e)
            return ImportResult.Error("Parsiranje neuspešno: ${e.message}")
        }
        payload.skipped.forEach { Log.w(TAG, "Import: preskočeno - $it") }

        val tempSubstances = payload.substances
        val tempCycles = payload.cycles
        val tempInjections = payload.injections
        val tempOralDoses = payload.oralDoses
        val tempPctDataMap = payload.pctData
        val tempSiteRotationIndex = payload.siteRotationIndex
        // null = ključ ne postoji u JSON-u (zadrži postojeće stanje);
        // prazan set = ključ postoji ali je prazan (resetuj na podrazumevano)
        val tempEnabledSites = payload.enabledSites
        val tempNotificationSettings = payload.notificationSettings

        // Faza 2: Atomska primena - sve promene u jednom synchronized bloku
        synchronized(lock) {
            substances.clear()
            substances.putAll(tempSubstances)
            // Backup iz starije verzije nema supstance dodate kasnije - dopuni podrazumevane
            // (supstance se u aplikaciji ne brišu, pa ovo ne vraća ništa što je korisnik uklonio)
            SampleData.getDefaultSubstances().forEach { default ->
                if (!substances.containsKey(default.id)) substances[default.id] = default
            }

            cycles.clear()
            cycles.putAll(tempCycles)

            injections.clear()
            injections.addAll(tempInjections)

            oralDoses.clear()
            oralDoses.addAll(tempOralDoses)

            pctDataMap.clear()
            pctDataMap.putAll(tempPctDataMap)


            siteRotationIndex = tempSiteRotationIndex

            // enabled_sites round-trip: ključ odsutan → zadrži postojeće stanje;
            // ključ prisutan ali prazan → determinističko resetovanje na
            // podrazumevano (svi sajtovi aktivni)
            if (tempEnabledSites != null) {
                enabledSites.clear()
                if (tempEnabledSites.isEmpty()) {
                    enabledSites.addAll(InjectionSite.entries.map { it.key })
                } else {
                    enabledSites.addAll(tempEnabledSites)
                }
            }

            // Validacija siteRotationIndex posle importa
            val enabledRotation = getEnabledRotationOrder()
            if (enabledRotation.isEmpty() || siteRotationIndex >= enabledRotation.size) {
                siteRotationIndex = 0
            }

            notificationSettings = tempNotificationSettings

            // Sve kolekcije su zamenjene - obriši dirty flag
            dirtyCollections.clear()
        }
        invalidateUpcomingInjectionsCache()
        notifyChange()
        return ImportResult.Success(skipped = payload.skipped.size, summary = payload.summary())
    }

    /** Da li postoje korisnički unosi (podrazumevane supstance se ne broje). */
    fun hasUserData(): Boolean = synchronized(lock) {
        cycles.isNotEmpty() || injections.isNotEmpty() || oralDoses.isNotEmpty() || pctDataMap.isNotEmpty()
    }

    // =========================================================================
    // LOCAL STORAGE
    // =========================================================================

    // Odvojen lock samo za upis fajla - serijalizuje upise sa različitih niti
    // bez blokiranja čitanja podataka pod glavnim [lock]-om
    private val saveFileLock = Any()

    /**
     * Čuva podatke u lokalni fajl ATOMSKI: piše u temp fajl u istom direktorijumu,
     * pa ga preimenuje preko ciljnog fajla. Prekinut upis tako nikad ne ostavlja
     * polupisan/oštećen glavni fajl. Upisi su serijalizovani preko [saveFileLock].
     */
    fun saveToLocal(context: Context): Boolean {
        return try {
            val dirty = consumeDirtyCollections()
            val jsonString = buildJson().toString()
            val file = File(context.filesDir, LOCAL_BACKUP_FILE)
            synchronized(saveFileLock) {
                val tempFile = File(context.filesDir, "$LOCAL_BACKUP_FILE.tmp")
                tempFile.writeText(jsonString)
                if (!tempFile.renameTo(file)) {
                    // Na nekim sistemima rename ne uspeva ako cilj postoji - obriši pa ponovi
                    file.delete()
                    if (!tempFile.renameTo(file)) {
                        throw java.io.IOException("Preimenovanje temp fajla u ${file.name} nije uspelo")
                    }
                }
            }
            Log.d(
                TAG,
                "Sačuvano u lokalni storage: ${file.absolutePath} (${jsonString.length} bajtova, " +
                    "promenjeno: ${if (dirty.isEmpty()) "sve" else dirty.joinToString()})"
            )
            true
        } catch (e: java.io.IOException) {
            Log.e(TAG, "I/O greška pri čuvanju u lokalni storage: ${e.message}", e)
            false
        } catch (e: SecurityException) {
            Log.e(TAG, "Nema dozvole za pisanje u lokalni storage: ${e.message}", e)
            false
        } catch (e: Exception) {
            Log.e(TAG, "Neočekivana greška pri čuvanju u lokalni storage: ${e.javaClass.simpleName} - ${e.message}", e)
            false
        }
    }

    fun loadFromLocal(context: Context): Boolean {
        return try {
            val file = File(context.filesDir, LOCAL_BACKUP_FILE)
            if (!file.exists()) {
                Log.d(TAG, "Lokalni backup fajl ne postoji: ${file.absolutePath}")
                return false
            }
            if (file.length() == 0L) {
                Log.d(TAG, "Lokalni backup fajl je prazan: ${file.absolutePath}")
                return false
            }
            val json = file.readText()
            if (json.isBlank()) {
                Log.d(TAG, "Lokalni backup fajl sadrži samo prazan tekst")
                return false
            }
            Log.d(TAG, "Učitavam iz lokalnog storage-a: ${file.absolutePath} (${json.length} bajtova)")
            // Tolerantan režim: jedan loš zapis ne sme da ostavi aplikaciju praznom pri startu.
            val result = importJson(json, strict = false)
            when (result) {
                is ImportResult.Success -> {
                    if (result.skipped > 0) {
                        // Sačuvaj original pre nego što ga sledeći auto-save pregazi bez
                        // preskočenih zapisa - možda mogu ručno da se povrate.
                        Log.w(TAG, "Preskočeno ${result.skipped} neispravnih zapisa - čuvam kopiju originala")
                        preserveFileWithSkippedRecords(file)
                    }
                    Log.d(TAG, "Uspešno učitano iz lokalnog storage-a")
                    true
                }
                is ImportResult.Error -> {
                    Log.e(TAG, "Neuspešno učitavanje iz lokalnog storage-a: ${result.message}")
                    // Skloni oštećen fajl u stranu da ga sledeći auto-save ne bi
                    // pregazio - podaci možda mogu ručno da se povrate
                    quarantineCorruptFile(file)
                    false
                }
            }
        } catch (e: java.io.IOException) {
            Log.e(TAG, "I/O greška pri čitanju lokalnog storage-a: ${e.message}", e)
            false
        } catch (e: SecurityException) {
            Log.e(TAG, "Nema dozvole za čitanje lokalnog storage-a: ${e.message}", e)
            false
        } catch (e: Exception) {
            Log.e(TAG, "Neočekivana greška pri čitanju lokalnog storage-a: ${e.javaClass.simpleName} - ${e.message}", e)
            false
        }
    }

    /** Kopira fajl u "<ime>.skipped" (ne dira original) kada su pri učitavanju preskočeni zapisi. */
    private fun preserveFileWithSkippedRecords(file: File) {
        try {
            file.copyTo(File(file.parentFile, "${file.name}.skipped"), overwrite = true)
        } catch (e: Exception) {
            Log.w(TAG, "Kopija fajla sa preskočenim zapisima nije uspela: ${e.message}", e)
        }
    }

    /**
     * Preimenuje oštećen backup fajl u "<ime>.corrupt" (sa numeričkim sufiksom
     * ako .corrupt već postoji) kako sledeći auto-save ne bi pregazio podatke
     * koji možda mogu da se povrate.
     */
    private fun quarantineCorruptFile(file: File) {
        try {
            var target = File(file.parentFile, "${file.name}.corrupt")
            var suffix = 1
            while (target.exists()) {
                target = File(file.parentFile, "${file.name}.corrupt$suffix")
                suffix++
            }
            if (file.renameTo(target)) {
                Log.w(TAG, "Oštećen fajl premešten u: ${target.name}")
            } else {
                Log.w(TAG, "Premeštanje oštećenog fajla nije uspelo: ${file.name}")
            }
        } catch (e: Exception) {
            Log.w(TAG, "Greška pri premeštanju oštećenog fajla: ${e.message}", e)
        }
    }

    fun hasLocalData(context: Context): Boolean {
        val file = File(context.filesDir, LOCAL_BACKUP_FILE)
        return file.exists() && file.length() > 0
    }

    // =========================================================================
    // INITIALIZATION
    // =========================================================================

    private fun initializeDefaultSubstances() {
        val defaults = SampleData.getDefaultSubstances()
        synchronized(lock) {
            if (substances.isEmpty()) {
                defaults.forEach { substances[it.id] = it }
            }
        }
    }
}
