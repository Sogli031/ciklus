package com.injekcije.app.data

import androidx.compose.ui.graphics.Color
import com.injekcije.app.Strings
import org.json.JSONArray
import org.json.JSONObject
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.UUID

// =============================================================================
// ENUMS
// =============================================================================

enum class InjectionFrequency(
    val key: String,
    val days: Double,
    val displayName: String
) {
    ED("ed", 1.0, "Svaki dan"),
    EOD("eod", 2.0, "Svaki drugi dan"),
    E3D("e3d", 3.0, "Svaka 3 dana"),
    E3_5D("e3.5d", 3.5, "2x nedeljno"),
    E5D("e5d", 5.0, "Svaka 5 dana"),
    WEEKLY("weekly", 7.0, "Nedeljno"),
    E10D("e10d", 10.0, "Svaka 10 dana"),
    E14D("e14d", 14.0, "Svake 2 nedelje");

    val injectionsPerWeek: Double
        get() = 7.0 / days

    companion object {
        fun fromKey(key: String): InjectionFrequency =
            entries.firstOrNull { it.key == key } ?: E3_5D
    }
}

/**
 * Injection sites with maximum safe volumes.
 *
 * Research basis (PubMed clinical guidelines):
 * - Ventrogluteal: up to 5 mL, SAFEST site (minimal nerves/vessels) - using 4.0 conservative
 * - Dorsogluteal: up to 4 mL, avoid due to sciatic nerve proximity
 * - Vastus lateralis: 2-5 mL, using 3.0 conservative for routine use
 * - Deltoid: 1.5-2 mL max, reduced for patients with less muscle mass
 * - Pectoralis: 1-2 mL due to neurovascular structure proximity
 *
 * Site rotation: minimum 2-week interval per site for fibrosis prevention.
 * For E3.5D protocol with 4 sites, this achieves 2-week rest per location.
 */
enum class InjectionSite(
    val key: String,
    val displayName: String,
    val shortName: String,
    val maxVolumeMl: Double,
    val isLeft: Boolean
) {
    GLUTE_L("glute_l", "Glute (L)", "GL", 4.0, true),
    GLUTE_R("glute_r", "Glute (R)", "GR", 4.0, false),
    VG_L("vg_l", "Ventrogluteal (L)", "VGL", 4.0, true),  // Research: up to 5mL, safest site
    VG_R("vg_r", "Ventrogluteal (R)", "VGR", 4.0, false), // Research: up to 5mL, safest site
    DELT_L("delt_l", "Delt (L)", "DL", 1.5, true),
    DELT_R("delt_r", "Delt (R)", "DR", 1.5, false),
    QUAD_L("quad_l", "Quad (L)", "QL", 3.0, true),
    QUAD_R("quad_r", "Quad (R)", "QR", 3.0, false),
    PEC_L("pec_l", "Pec (L)", "PL", 1.5, true),
    PEC_R("pec_r", "Pec (R)", "PR", 1.5, false);

    val tip: String
        get() = when {
            this in listOf(GLUTE_L, GLUTE_R) -> "Do 4mL, 1-2% rizik išijatički nerv"
            this in listOf(VG_L, VG_R) -> "NAJBEZBEDNIJE - do 4mL, minimalno nerava"
            this in listOf(QUAD_L, QUAD_R) -> "Lako za samoadministraciju (do 3mL)"
            this in listOf(DELT_L, DELT_R) -> "Za manje količine (1-1.5mL)"
            this in listOf(PEC_L, PEC_R) -> "Za manje količine (1-2mL)"
            else -> ""
        }

    companion object {
        fun fromKey(key: String): InjectionSite =
            entries.firstOrNull { it.key == key } ?: GLUTE_L

        fun rotationOrder(): List<InjectionSite> = listOf(
            GLUTE_L, GLUTE_R, VG_L, VG_R, DELT_L, DELT_R,
            QUAD_L, QUAD_R, PEC_L, PEC_R
        )
    }
}

enum class InjectionStatus {
    ON_TRACK,
    DUE_SOON,
    OVERDUE,
    AWAITING_FIRST;

    val displayName: String
        get() = when (this) {
            ON_TRACK -> "Na vreme"
            DUE_SOON -> "Uskoro"
            OVERDUE -> "Kasni"
            AWAITING_FIRST -> "Čeka se"
        }

    val color: Color
        get() = when (this) {
            ON_TRACK -> Color(0xFF00C853)
            DUE_SOON -> Color(0xFFFFB300)
            OVERDUE -> Color(0xFFFF5252)
            AWAITING_FIRST -> Color(0xFF2196F3)
        }
}

// =============================================================================
// DATA CLASSES
// =============================================================================

/**
 * Oral steroid dosing schedule based on pharmacokinetics (half-life).
 * Determines how many times per day to take the oral and at what times.
 */
data class OralDosingSchedule(
    val halfLifeHours: Double,
    val dosesPerDay: Int,
    val dosingTimes: List<Int>,  // Hours (0-23)
    val description: String
) {
    companion object {
        val METHANDIENONE = OralDosingSchedule(5.0, 3, listOf(8, 14, 20), "3x dnevno")
        val OXANDROLONE = OralDosingSchedule(9.5, 2, listOf(8, 20), "2x dnevno")
        val OXYMETHOLONE = OralDosingSchedule(8.5, 2, listOf(8, 20), "2x dnevno")
        val STANOZOLOL = OralDosingSchedule(9.0, 2, listOf(8, 20), "2x dnevno")
        val TURINABOL = OralDosingSchedule(16.0, 1, listOf(8), "1x dnevno")

        // Eksplicitna lista oralnih steroid ID-ova za exact match
        private val ORAL_STEROID_IDS = setOf(
            "methandienone", "oxandrolone", "oxymetholone",
            "stanozolol_oral", "turinabol"
        )

        // Mapiranje oralnih ID-ova na odgovarajuće rasporede doziranja
        private val ORAL_SCHEDULE_MAP = mapOf(
            "methandienone" to METHANDIENONE,
            "oxandrolone" to OXANDROLONE,
            "oxymetholone" to OXYMETHOLONE,
            "stanozolol_oral" to STANOZOLOL,
            "turinabol" to TURINABOL
        )

        /**
         * Get the dosing schedule for an oral steroid based on substance ID.
         * Koristi exact match umesto substring pretrage.
         */
        fun forSubstance(substanceId: String): OralDosingSchedule? {
            return ORAL_SCHEDULE_MAP[substanceId.lowercase()]
        }

        /**
         * Check if a substance is an oral steroid (needs multiple daily doses).
         * Koristi exact match umesto substring pretrage.
         */
        fun isOralSteroid(substanceId: String): Boolean {
            return substanceId.lowercase() in ORAL_STEROID_IDS
        }

        /**
         * Get allowed injection/dosing frequencies for a substance.
         * Oral steroids are locked to ED, different esters have different optimal frequencies.
         */
        fun getAllowedFrequencies(substanceId: String): List<InjectionFrequency> {
            val lower = substanceId.lowercase()
            return when {
                // Oral steroids - only ED
                isOralSteroid(substanceId) ->
                    listOf(InjectionFrequency.ED)

                // Short esters - ED, EOD, E3D
                lower.contains("test_p") || lower.contains("tren_a") ||
                lower.contains("mast_p") || lower.contains("npp") ->
                    listOf(InjectionFrequency.ED, InjectionFrequency.EOD, InjectionFrequency.E3D)

                // Medium esters - E3.5D, E5D, WEEKLY
                lower.contains("test_e") || lower.contains("test_c") ||
                lower.contains("sustanon") || lower.contains("tren_e") ||
                lower.contains("mast_e") ->
                    listOf(InjectionFrequency.E3_5D, InjectionFrequency.E5D, InjectionFrequency.WEEKLY)

                // Long esters - E3.5D, WEEKLY, E10D
                lower.contains("deca") || lower.contains("eq") ||
                lower.contains("tren_hex") || lower.contains("bold_c") ->
                    listOf(InjectionFrequency.E3_5D, InjectionFrequency.WEEKLY, InjectionFrequency.E10D)

                // HCG - only E3D
                lower.contains("hcg") ->
                    listOf(InjectionFrequency.E3D)

                // HGH - only ED
                lower.contains("hgh") ->
                    listOf(InjectionFrequency.ED)

                // Default - all frequencies
                else -> InjectionFrequency.entries.toList()
            }
        }
    }
}

data class Substance(
    val id: String,
    val name: String,
    val shortName: String,
    val defaultConcentration: Double,
    val category: String,
    val recommendedFrequency: InjectionFrequency = InjectionFrequency.E3_5D,
    val recommendedDose: Double = 0.0,  // Daily dose for orals, weekly for injectables
    val recommendedMaxWeeks: Int = 12,  // Max recommended cycle duration for this substance
    val esterType: EsterType? = null    // Eksplicitni tip estera; ako je null, određuje se iz ID-ja
) {
    /**
     * Returns dosing timing recommendation for this substance.
     * Particularly useful for oral steroids where timing matters.
     */
    val dosingTip: String?
        get() = getDosingTip(id)

    /**
     * Check if this substance is an oral steroid.
     */
    val isOral: Boolean
        get() = OralDosingSchedule.isOralSteroid(id)

    /**
     * Get the recommended weekly dose (converts daily to weekly for orals).
     */
    val recommendedWeeklyDose: Double
        get() = if (isOral) recommendedDose * 7 else recommendedDose

    /**
     * Get the recommended daily dose (converts weekly to daily for injectables).
     */
    val recommendedDailyDose: Double
        get() = if (isOral) recommendedDose else recommendedDose / 7

    fun toJson(): JSONObject = JSONObject().apply {
        put("id", id)
        put("name", name)
        put("short_name", shortName)
        put("default_concentration", defaultConcentration)
        put("category", category)
        put("recommended_frequency", recommendedFrequency.key)
        put("recommended_dose", recommendedDose)
        put("recommended_max_weeks", recommendedMaxWeeks)
        esterType?.let { put("ester_type", it.key) }
    }

    companion object {
        fun fromJson(obj: JSONObject): Substance = Substance(
            id = obj.getString("id"),
            name = obj.getString("name"),
            shortName = obj.getString("short_name"),
            defaultConcentration = obj.getDouble("default_concentration"),
            category = obj.optString("category", "AAS"),
            recommendedFrequency = InjectionFrequency.fromKey(
                obj.optString("recommended_frequency", "e3.5d")
            ),
            recommendedDose = obj.optDouble("recommended_dose", 0.0),
            recommendedMaxWeeks = obj.optInt("recommended_max_weeks", 12),
            esterType = obj.optString("ester_type", "").takeIf { it.isNotEmpty() }
                ?.let { EsterType.fromKey(it) }
        )

        /**
         * Returns dosing timing recommendation based on substance ID.
         * Based on pharmacokinetics (half-life) of each compound.
         */
        fun getDosingTip(substanceId: String): String? {
            val lower = substanceId.lowercase()
            return when {
                // Oxandrolone (Anavar) - poluživot 9.4-10.4h
                lower.contains("oxandrolone") || lower.contains("anavar") ->
                    """
                    ⏰ DOZIRANJE: Podeli u 2-3 doze dnevno

                    Poluživot: 9.4-10.4h

                    Primer za 20mg/dan:
                    • 10mg ujutru (sa doručkom)
                    • 10mg uveče (sa večerom)

                    Ili za stabilnije nivoe:
                    • 7mg ujutru
                    • 7mg popodne
                    • 6mg uveče

                    💡 Uzimaj sa hranom za bolju apsorpciju.
                    ✓ Najsigurniji oralni - bubrežni metabolizam.
                    """.trimIndent()

                // Oxymetholone (Anadrol) - poluživot 8-9h
                lower.contains("oxymetholone") || lower.contains("anadrol") ->
                    """
                    ⏰ DOZIRANJE: Podeli u 2 doze dnevno

                    Poluživot: 8-9h

                    Primer za 50mg/dan:
                    • 25mg ujutru (sa doručkom)
                    • 25mg uveče (sa večerom)

                    ⚠️ NAJHEPATOTOKSIČNIJI oralni steroid!
                    • Maksimum 4-6 nedelja
                    • Jetreni enzimi svakih 2 nedelje
                    • Uzimaj sa hranom da smanjiš GI tegobe
                    """.trimIndent()

                // Stanozolol oral - poluživot 9h (NE winstrol depot - to je injectable)
                lower.contains("stanozolol_oral") ->
                    """
                    ⏰ DOZIRANJE: Podeli u 2 doze dnevno

                    Poluživot: 9h

                    Primer za 10mg/dan:
                    • 5mg ujutru
                    • 5mg uveče

                    ⚠️ UPOZORENJA:
                    • HDL supresija 49-90% - najgori lipidni profil
                    • Fatalni slučajevi dokumentovani na visokim dozama
                    • Preporučeno SAMO za HAE (0.5-2mg/dan)
                    """.trimIndent()

                // Methandienone (Dianabol) - poluživot 3-6h (NAJKRAĆI!)
                lower.contains("methandienone") || lower.contains("dianabol") || lower.contains("dbol") ->
                    """
                    ⏰ DOZIRANJE: Podeli u 3-4 doze dnevno

                    Poluživot: 3-6h (NAJKRAĆI među oralnim!)

                    Primer za 20mg/dan:
                    • 5mg ujutru (7:00)
                    • 5mg pre treninga (12:00)
                    • 5mg posle treninga (15:00)
                    • 5mg uveče (20:00)

                    💡 Zbog kratkog poluživota, češće doze = stabilniji nivoi

                    ⚠️ UPOZORENJA:
                    • Jako aromatizuje - prati estradiol
                    • Maksimum 4 nedelje
                    • OBAVEZNO sa testosteron bazom!
                    """.trimIndent()

                // Turinabol - poluživot 16h (NAJDUŽI!)
                lower.contains("turinabol") ->
                    """
                    ⏰ DOZIRANJE: Jednom ili 2x dnevno

                    Poluživot: 16h (NAJDUŽI među oralnim!)

                    Opcija 1 - Jednom dnevno:
                    • Cela doza ujutru sa doručkom

                    Opcija 2 - Podeljeno:
                    • Pola ujutru
                    • Pola uveče

                    💡 Zbog dugog poluživota, doziranje je fleksibilnije.

                    ⚠️ UPOZORENJA:
                    • Najizraženija HPTA supresija među oralnim
                    • Čekaj 2 NEDELJE pre PCT-a (dugo čišćenje)
                    • Detekcija metabolita do 12 meseci
                    """.trimIndent()

                // HCG
                lower.contains("hcg") ->
                    """
                    ⏰ DOZIRANJE: 3x nedeljno (pon/sre/pet)

                    Poluživot: ~33h

                    PCT protokol:
                    • 1500 IU subkutano 3x nedeljno
                    • Uz Clomiphene 25mg dnevno

                    On-cycle (održavanje testisa):
                    • 250-500 IU 2-3x nedeljno

                    💡 Mešanje: 5000 IU + 2mL bakt. vode = 250 IU/0.1mL
                    📦 Čuvanje: Frižider, traje ~60 dana
                    """.trimIndent()

                // HGH
                lower.contains("hgh") ->
                    """
                    ⏰ DOZIRANJE: Jednom dnevno ujutru

                    Poluživot: 2-3h (ali IGF-1 efekat traje duže)

                    Preporuka:
                    • Ujutru natašte (30min pre doručka)
                    • ILI pre spavanja (ako fokus na oporavak)

                    💡 Subkutano u abdomen za najbolju apsorpciju
                    ⚠️ Ne kombinuj sa ugljenim hidratima (insulin spike)
                    """.trimIndent()

                else -> null
            }
        }
    }
}

data class DosePhase(
    val startWeek: Int,
    val endWeek: Int,
    val weeklyDoseMg: Double
) {
    fun toJson(): JSONObject = JSONObject().apply {
        put("start_week", startWeek)
        put("end_week", endWeek)
        put("weekly_dose_mg", weeklyDoseMg)
    }

    companion object {
        fun fromJson(obj: JSONObject): DosePhase = DosePhase(
            startWeek = obj.getInt("start_week"),
            endWeek = obj.getInt("end_week"),
            weeklyDoseMg = obj.getDouble("weekly_dose_mg")
        )
    }
}

data class CycleCompound(
    val substanceId: String,
    val weeklyDoseMg: Double,
    val frequency: InjectionFrequency,
    val concentrationMgMl: Double,
    val phases: List<DosePhase> = emptyList(),
    val startWeek: Int = 1,
    val endWeek: Int? = null  // null = duration of cycle
) {
    val dosePerInjectionMg: Double
        get() = weeklyDoseMg / frequency.injectionsPerWeek

    val volumePerInjectionMl: Double
        get() = dosePerInjectionMg / concentrationMgMl

    /**
     * Check if this compound is active in a given week.
     * @param week The week number to check
     * @param cycleDuration Total weeks in the cycle (used when endWeek is null)
     */
    fun isActiveInWeek(week: Int, cycleDuration: Int): Boolean {
        val effectiveEnd = endWeek ?: cycleDuration
        return week in startWeek..effectiveEnd
    }

    fun getDoseForWeek(week: Int): Double {
        val phase = phases.find { week in it.startWeek..it.endWeek }
        return phase?.weeklyDoseMg ?: weeklyDoseMg
    }

    fun getDosePerInjectionForWeek(week: Int): Double {
        return getDoseForWeek(week) / frequency.injectionsPerWeek
    }

    fun getVolumePerInjectionForWeek(week: Int): Double {
        return getDosePerInjectionForWeek(week) / concentrationMgMl
    }

    fun toJson(): JSONObject = JSONObject().apply {
        put("substance_id", substanceId)
        put("weekly_dose_mg", weeklyDoseMg)
        put("frequency", frequency.key)
        put("concentration_mg_ml", concentrationMgMl)
        put("start_week", startWeek)
        endWeek?.let { put("end_week", it) }
        if (phases.isNotEmpty()) {
            put("phases", JSONArray().apply {
                phases.forEach { put(it.toJson()) }
            })
        }
    }

    companion object {
        fun fromJson(obj: JSONObject): CycleCompound = CycleCompound(
            substanceId = obj.getString("substance_id"),
            weeklyDoseMg = obj.getDouble("weekly_dose_mg"),
            frequency = InjectionFrequency.fromKey(obj.getString("frequency")),
            concentrationMgMl = obj.getDouble("concentration_mg_ml"),
            phases = obj.optJSONArray("phases")?.let { arr ->
                (0 until arr.length()).map { DosePhase.fromJson(arr.getJSONObject(it)) }
            } ?: emptyList(),
            startWeek = obj.optInt("start_week", 1),
            endWeek = if (obj.has("end_week")) obj.getInt("end_week") else null
        )
    }
}

data class Cycle(
    val id: String = generateId(),
    val name: String,
    val startDate: LocalDate,
    val durationWeeks: Int,
    val compounds: List<CycleCompound>,
    val isActive: Boolean = true
) {
    init {
        require(durationWeeks > 0) { "Trajanje ciklusa mora biti pozitivno" }
    }

    val endDate: LocalDate
        get() = startDate.plusWeeks(durationWeeks.toLong())

    fun currentWeek(today: LocalDate = LocalDate.now()): Int {
        val daysSinceStart = ChronoUnit.DAYS.between(startDate, today)
        return ((daysSinceStart / 7) + 1).toInt().coerceIn(1, durationWeeks)
    }

    fun isFinished(today: LocalDate = LocalDate.now()): Boolean =
        today.isAfter(endDate)

    fun toJson(): JSONObject = JSONObject().apply {
        put("id", id)
        put("name", name)
        put("start_date", startDate.format(DateTimeFormatter.ISO_LOCAL_DATE))
        put("duration_weeks", durationWeeks)
        put("compounds", JSONArray().apply {
            compounds.forEach { put(it.toJson()) }
        })
        put("is_active", isActive)
    }

    companion object {
        fun fromJson(obj: JSONObject): Cycle = Cycle(
            id = obj.optString("id", generateId()),
            name = obj.optString("name", "Neimenovan ciklus"),
            startDate = obj.optString("start_date", "").let {
                if (it.isNotEmpty()) LocalDate.parse(it) else LocalDate.now()
            },
            durationWeeks = obj.optInt("duration_weeks", 12),
            compounds = obj.optJSONArray("compounds")?.let { arr ->
                (0 until arr.length()).map { CycleCompound.fromJson(arr.getJSONObject(it)) }
            } ?: emptyList(),
            isActive = obj.optBoolean("is_active", true)
        )
    }
}

data class InjectionRecord(
    val id: String = generateId(),
    val cycleId: String,
    val substanceId: String,
    val dateTime: LocalDateTime,
    val site: InjectionSite,
    val doseMg: Double,
    val volumeMl: Double,
    val week: Int,
    val notes: String = ""
) {
    fun toJson(): JSONObject = JSONObject().apply {
        put("id", id)
        put("cycle_id", cycleId)
        put("substance_id", substanceId)
        put("date_time", dateTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
        put("site", site.key)
        put("dose_mg", doseMg)
        put("volume_ml", volumeMl)
        put("week", week)
        put("notes", notes)
    }

    companion object {
        fun fromJson(obj: JSONObject): InjectionRecord = InjectionRecord(
            id = obj.optString("id", generateId()),
            cycleId = obj.optString("cycle_id", ""),
            substanceId = obj.optString("substance_id", ""),
            dateTime = obj.optString("date_time", "").let {
                if (it.isNotEmpty()) LocalDateTime.parse(it) else LocalDateTime.now()
            },
            site = InjectionSite.fromKey(obj.optString("site", "glute_l")),
            doseMg = obj.optDouble("dose_mg", 0.0),
            volumeMl = obj.optDouble("volume_ml", 0.0),
            week = obj.optInt("week", 1),
            notes = obj.optString("notes", "")
        )
    }
}

/**
 * Record of an oral steroid dose taken
 */
data class OralDoseRecord(
    val id: String = generateId(),
    val cycleId: String,
    val substanceId: String,
    val dateTime: LocalDateTime,
    val doseMg: Double,
    val week: Int
) {
    fun toJson(): JSONObject = JSONObject().apply {
        put("id", id)
        put("cycle_id", cycleId)
        put("substance_id", substanceId)
        put("date_time", dateTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
        put("dose_mg", doseMg)
        put("week", week)
    }

    companion object {
        fun fromJson(obj: JSONObject): OralDoseRecord = OralDoseRecord(
            id = obj.optString("id", generateId()),
            cycleId = obj.optString("cycle_id", ""),
            substanceId = obj.optString("substance_id", ""),
            dateTime = obj.optString("date_time", "").let {
                if (it.isNotEmpty()) LocalDateTime.parse(it) else LocalDateTime.now()
            },
            doseMg = obj.optDouble("dose_mg", 0.0),
            week = obj.optInt("week", 1)
        )
    }
}

// =============================================================================
// COMPUTED/VIEW MODELS (not persisted)
// =============================================================================

data class NextInjectionInfo(
    val substanceId: String,
    val substanceName: String,
    val scheduledDateTime: LocalDateTime,
    val site: InjectionSite,
    val doseMg: Double,
    val volumeMl: Double,
    val hoursRemaining: Double,
    val isFirstInjection: Boolean = false
) {
    val status: InjectionStatus
        get() = when {
            isFirstInjection -> InjectionStatus.AWAITING_FIRST
            hoursRemaining < 0 -> InjectionStatus.OVERDUE
            hoursRemaining < 12 -> InjectionStatus.DUE_SOON
            else -> InjectionStatus.ON_TRACK
        }

    val timeDisplay: String
        get() {
            val absMinutes = kotlin.math.abs(hoursRemaining * 60).toInt()
            val days = absMinutes / (24 * 60)
            val hours = (absMinutes % (24 * 60)) / 60
            val minutes = absMinutes % 60

            val timeStr = buildString {
                if (days > 0) append("${days}d ")
                if (hours > 0 || days > 0) append("${hours}h ")
                append("${minutes}m")
            }

            return if (hoursRemaining < 0) "$timeStr kasni" else "$timeStr preostalo"
        }
}

/**
 * Site usage history for rotation tracking.
 *
 * Research basis (PubMed):
 * - Repetitive trauma causes "needle myopathy" with local muscle fiber necrosis
 * - Gluteal muscle contracture incidence: 1-2.5% in pediatric populations with poor rotation
 * - Optimal rotation interval: 2-4 weeks per site to allow regeneration
 * - For E3.5D protocol: rotate through 4+ sites (L/R glute, L/R quad) for 2-week rest
 */
data class SiteHistory(
    val site: InjectionSite,
    val lastUsed: LocalDateTime?,
    val totalInjections: Int,
    val daysSinceLastUse: Int?
) {
    val isAvailable: Boolean
        get() = daysSinceLastUse == null || daysSinceLastUse >= MIN_REST_DAYS

    companion object {
        /**
         * Minimum days between injections at same site.
         * Research: 2-4 weeks optimal. Using 14 days (2 weeks) as minimum.
         */
        const val MIN_REST_DAYS = 14
    }
}

data class WeekSchedule(
    val week: Int,
    val isCurrent: Boolean,
    val compounds: List<CompoundWeekStatus>
)

data class CompoundWeekStatus(
    val substanceId: String,
    val substanceName: String,
    val weeklyDoseMg: Double,
    val injectionsCompleted: Int,
    val injectionsExpected: Int
) {
    val isComplete: Boolean
        get() = injectionsCompleted >= injectionsExpected

    val progress: Float
        get() = if (injectionsExpected > 0) {
            (injectionsCompleted.toFloat() / injectionsExpected).coerceIn(0f, 1f)
        } else 0f
}

// =============================================================================
// PCT (Post-Cycle Therapy) - Evidence-Based Models
// Based on BJU International 2026 study and PubMed meta-analyses
// =============================================================================

/**
 * HPTA Suppression level - how much a compound suppresses natural testosterone.
 */
enum class SuppressionLevel(val displayName: String) {
    MILD("Blaga supresija"),
    MODERATE("Umerena supresija"),
    SEVERE("Jaka supresija"),
    EXTREME("Ekstremna supresija")
}

/**
 * Compound types (base molecule) with PCT-specific properties.
 * Based on clinical studies and pharmacological profiles.
 *
 * Nivoi supresije HPTA ose:
 * - MILD: DHT derivati (Masteron, Primobolan) - blaga supresija, brži oporavak
 *   Ref: Drostanolone/Methenolon klinički podaci; brzo čišćenje iz sistema
 * - MODERATE: Testosteron, Boldenon, oralni 17-aa steroidi
 *   Ref: Bhasin S, et al. NEJM 2001;345:507-514 (potpuna supresija na suprafiziološkim dozama)
 * - SEVERE: Nandrolon (19-nor) - 19-nor metaboliti detektabilni ~9-12 meseci
 *   Ref: Sattler FR, et al. JCEM 1999; Grunfeld C, et al. Ann Intern Med 2006
 * - EXTREME: Trenbolon (19-nor) - najsupresivniji, nema humanih RCT podataka
 *   Ref: Samo životinjske studije; 19-nor metaboliti detektabilni godinu+ dana
 */
enum class CompoundType(
    val key: String,
    val displayName: String,
    val suppressionLevel: SuppressionLevel,
    val requiresHCG: Boolean,           // hCG strongly recommended
    val recommendedProtocol: PCTProtocolType,
    val pctDurationWeeks: Int,          // Minimum recommended PCT duration
    val specialNotes: String            // Compound-specific advice
) {
    TESTOSTERONE(
        key = "testosterone",
        displayName = "Testosteron",
        suppressionLevel = SuppressionLevel.MODERATE,
        requiresHCG = false,
        recommendedProtocol = PCTProtocolType.CLOMIPHENE_ONLY,
        pctDurationWeeks = 16,
        specialNotes = "Standardni PCT protokol. Clomiphene 25mg dnevno minimum 3-4 meseca. " +
                "CloTASH protokol: 25mg svaki drugi dan 16 nedelja. " +
                "Očekuje se potpuni oporavak u roku od 3-6 meseci kod većine korisnika. " +
                "Prekid na osnovu laboratorija (TT, LH, FSH u referentnom opsegu na 2 kontrole)."
    ),

    NANDROLONE(
        key = "nandrolone",
        displayName = "Nandrolon (Deca/NPP)",
        suppressionLevel = SuppressionLevel.SEVERE,
        requiresHCG = true,
        recommendedProtocol = PCTProtocolType.CLOMIPHENE_HCG,
        pctDurationWeeks = 26,
        specialNotes = "⚠️ EKSTREMNA SUPRESIJA HPTA! Nandrolon je jedan od najsupresivnijih steroida. " +
                "OBAVEZNO koristi hCG 1500IU 3x nedeljno TOKOM CELOG PCT-a. " +
                "Očekuj produženi oporavak (6-12 meseci za potpunu normalizaciju sperme). " +
                "19-nor struktura se dugo zadržava u organizmu. Minimum 6 meseci PCT-a."
    ),

    TRENBOLONE(
        key = "trenbolone",
        displayName = "Trenbolon",
        suppressionLevel = SuppressionLevel.EXTREME,
        requiresHCG = true,
        recommendedProtocol = PCTProtocolType.CLOMIPHENE_HCG,
        pctDurationWeeks = 26,
        specialNotes = "⚠️ NAJSUPRESIVNIJI STEROID! Trenbolon potpuno gasi HPTA. " +
                "OBAVEZNO koristi hCG 1500IU 3x nedeljno TOKOM CELOG PCT-a. " +
                "Razmotri veću dozu CC (37.5mg). " +
                "Očekuj NAJDUŽI oporavak (do 12+ meseci). " +
                "19-nor metaboliti se detektuju i godinu dana nakon upotrebe. Minimum 6 meseci PCT-a."
    ),

    BOLDENONE(
        key = "boldenone",
        displayName = "Boldenon (Equipoise)",
        suppressionLevel = SuppressionLevel.MODERATE,
        requiresHCG = false,
        recommendedProtocol = PCTProtocolType.CLOMIPHENE_ONLY,
        pctDurationWeeks = 16,
        specialNotes = "Undecylenate ester ima veoma dug poluživot (~14 dana). " +
                "Čekaj minimum 6-8 nedelja pre PCT! " +
                "Standardni CC protokol je dovoljan, minimum 4 meseca. " +
                "Oporavak sličan testosteronu, ali duže čekanje zbog estera."
    ),

    MASTERON(
        key = "masteron",
        displayName = "Masteron (Drostanolon)",
        suppressionLevel = SuppressionLevel.MILD,
        requiresHCG = false,
        recommendedProtocol = PCTProtocolType.CLOMIPHENE_ONLY,
        pctDurationWeeks = 10,
        specialNotes = "Relativno blaga supresija HPTA. " +
                "Kratak poluživot (propionat), brzo izlazi iz sistema. " +
                "Standardni CC protokol, minimum 2-3 meseca. " +
                "Prekid na osnovu laboratorija."
    ),

    PRIMOBOLAN(
        key = "primobolan",
        displayName = "Primobolan (Methenolon)",
        suppressionLevel = SuppressionLevel.MILD,
        requiresHCG = false,
        recommendedProtocol = PCTProtocolType.CLOMIPHENE_ONLY,
        pctDurationWeeks = 10,
        specialNotes = "Blaga supresija. Često korišćen u blagim ciklusima. " +
                "Standardni CC protokol, minimum 2-3 meseca. " +
                "Prekid na osnovu laboratorija."
    ),

    ORAL_17AA(
        key = "oral_17aa",
        displayName = "Oralni (17-alpha-alkilovani)",
        suppressionLevel = SuppressionLevel.MODERATE,
        requiresHCG = false,
        recommendedProtocol = PCTProtocolType.CLOMIPHENE_ONLY,
        pctDurationWeeks = 10,
        specialNotes = "Oralni steroidi (Anavar, Winstrol, Dianabol, Turinabol) " +
                "imaju kratak poluživot i brzo izlaze iz sistema. " +
                "Ako su korišćeni SAMO oralni, PCT može početi 3-5 dana nakon poslednje doze. " +
                "Standardni CC protokol, minimum 2-3 meseca."
    ),

    UNKNOWN(
        key = "unknown",
        displayName = "Nepoznata komponenta",
        suppressionLevel = SuppressionLevel.MODERATE,
        requiresHCG = false,
        recommendedProtocol = PCTProtocolType.CLOMIPHENE_ONLY,
        pctDurationWeeks = 12,
        specialNotes = "Nije prepoznata komponenta. Primeni standardni PCT protokol, minimum 3 meseca. " +
                "Prekid na osnovu laboratorija."
    );

    companion object {
        fun fromSubstanceId(substanceId: String): CompoundType {
            val lower = substanceId.lowercase()
            return when {
                // Nandrolone compounds
                lower.contains("deca") || lower.contains("nandrolon") ||
                lower.contains("npp") || lower.contains("nand") -> NANDROLONE

                // Trenbolone compounds
                lower.contains("tren") -> TRENBOLONE

                // Boldenone compounds
                lower.contains("boldenon") || lower.contains("eq") ||
                lower.contains("equipoise") -> BOLDENONE

                // Masteron compounds
                lower.contains("masteron") || lower.contains("drostanolon") ||
                lower.contains("drost") -> MASTERON

                // Primobolan compounds
                lower.contains("primo") || lower.contains("methenolon") -> PRIMOBOLAN

                // Oral steroids - mapira na ORAL_17AA
                lower.contains("anavar") || lower.contains("oxandrolon") ||
                lower.contains("oxandrolone") ||  // dodato
                lower.contains("winstrol") || lower.contains("stanozolol") ||
                lower.contains("stanozolol_oral") ||  // dodato
                lower.contains("dianabol") || lower.contains("dbol") ||
                lower.contains("metandienon") || lower.contains("methandienone") ||  // dodato
                lower.contains("turinabol") ||
                lower.contains("anadrol") || lower.contains("oxymetholon") ||
                lower.contains("oxymetholone") -> ORAL_17AA  // dodato

                // Testosterone compounds (default for most injectables)
                lower.contains("test") || lower.contains("sust") ||
                lower.contains("enan") || lower.contains("cyp") ||
                lower.contains("prop") -> TESTOSTERONE

                else -> UNKNOWN
            }
        }
    }
}

/**
 * Ester types with their half-lives and PCT waiting periods.
 * Based on pharmacokinetic studies (PubMed).
 *
 * The waiting period is NOT simply 5x half-life because:
 * 1. Pituitary desensitization takes 40-50 days to reverse
 * 2. Estradiol metabolites have slower clearance
 * 3. SHBG recycling extends biological effects
 * 4. Leydig cells need time to recover from suppression
 */
enum class EsterType(
    val key: String,
    val displayName: String,
    val halfLifeDays: Double,
    val pctWaitDays: Int
) {
    // Short esters
    ACETATE("acetate", "Acetat", 1.0, 7),
    PROPIONATE("propionate", "Propionat", 0.8, 10),
    PHENYLPROPIONATE("phenylpropionate", "Phenylpropionat", 2.7, 30),

    // Medium esters
    ENANTHATE("enanthate", "Enanthate", 4.5, 45),
    CYPIONATE("cypionate", "Cypionate", 8.0, 50),

    // Long esters
    DECANOATE("decanoate", "Decanoate", 7.5, 55),
    UNDECYLENATE("undecylenate", "Undecylenate", 14.0, 70), // Boldenone EQ
    UNDECANOATE("undecanoate", "Undecanoate", 20.9, 80),

    // Oral (no ester)
    ORAL("oral", "Oralni (bez estera)", 0.3, 5);

    companion object {
        fun fromKey(key: String): EsterType =
            entries.firstOrNull { it.key == key } ?: ENANTHATE

        /**
         * Određuje tip estera na osnovu Substance objekta.
         * Ako supstanca ima eksplicitno postavljen esterType, koristi ga;
         * u suprotnom, parsira iz ID-ja.
         */
        fun fromSubstance(substance: Substance): EsterType {
            return substance.esterType ?: fromSubstanceId(substance.id)
        }

        fun fromSubstanceId(substanceId: String): EsterType {
            val lower = substanceId.lowercase()
            return when {
                // Acetate (Trenbolone Acetate)
                lower.contains("acet") || lower.contains("tren_a") ||
                (lower.contains("tren") && lower.contains("a")) -> ACETATE

                // Propionate (test_s = suspenzija bez estera, t½ ~1 dan - tretira se kao propionat)
                lower == "test_p" || lower == "mast_p" || lower == "test_s" ||
                lower.contains("prop") -> PROPIONATE

                // Phenylpropionate (NPP)
                lower.contains("phenyl") || lower.contains("npp") -> PHENYLPROPIONATE

                // Enanthate
                lower == "test_e" || lower.contains("enan") || lower.contains("tren_e") ||
                (lower.contains("tren") && lower.contains("e") && !lower.contains("acet")) -> ENANTHATE

                // Cypionate (dhb = Dihydroboldenone / 1-Test Cypionate)
                lower == "test_c" || lower == "bold_c" || lower == "dhb" ||
                lower.contains("cyp") || lower.contains("cip") -> CYPIONATE

                // Decanoate (Deca-Durabolin)
                lower.contains("deca") && !lower.contains("undeca") -> DECANOATE

                // Undecylenate (Boldenone/EQ)
                lower.contains("undecylen") || lower.contains("eq") ||
                lower.contains("equipoise") || lower.contains("boldenon") -> UNDECYLENATE

                // Undecanoate
                lower.contains("und") -> UNDECANOATE

                // Winstrol Depot (injectable suspension) - kratko poluvreme ~24h, slično propionatu
                lower == "winstrol" -> PROPIONATE

                // Oral steroids (NE winstrol depot)
                lower.contains("anavar") ||
                lower.contains("dianabol") || lower.contains("dbol") ||
                lower.contains("turinabol") || lower.contains("anadrol") ||
                lower.contains("oxandrolon") || lower.contains("oxandrolone") ||
                lower.contains("stanozolol_oral") ||
                lower.contains("oxymetholone") || lower.contains("methandienone") -> ORAL

                // Sustanon (blend, treat as enanthate)
                lower.contains("sust") -> ENANTHATE

                // Nesupresivni pomoćni preparati bez estera (hCG, hGH, HMG) -
                // NE smeju da padnu na ENANTHATE i produže čekanje na PCT na 45 dana
                lower == "hcg" || lower == "hgh" || lower == "hmg" -> ORAL

                else -> ENANTHATE // Default
            }
        }
    }
}

/**
 * PCT Protocol types based on BJU International 2026 study.
 */
enum class PCTProtocolType(
    val key: String,
    val displayName: String,
    val successRate: Double // Based on 12-month normozoospermia rates
) {
    CLOMIPHENE_ONLY("cc_only", "Clomiphene 25mg dnevno", 0.692),
    CLOMIPHENE_HCG("cc_hcg", "Clomiphene 25mg + hCG 1500IU 3x ned.", 0.875),
    NO_PCT("none", "Bez PCT (spontani oporavak)", 0.586);

    companion object {
        fun fromKey(key: String): PCTProtocolType =
            entries.firstOrNull { it.key == key } ?: CLOMIPHENE_ONLY
    }
}

/**
 * PCT Phase status for tracking recovery progress.
 */
enum class PCTPhase(val displayName: String) {
    WAITING("Čekanje na PCT"),
    READY_TO_START("Spreman za PCT"),
    ACTIVE("PCT u toku"),
    MONITORING("Praćenje oporavka"),
    COMPLETED("PCT završen")
}

/**
 * PCT Status indicator with colors.
 */
enum class PCTStatus {
    NOT_NEEDED,      // No finished cycle
    WAITING,         // Waiting period (clearance)
    START_SOON,      // 3 days before PCT start
    START_NOW,       // Time to start PCT
    IN_PROGRESS,     // PCT is active
    BLOODWORK_DUE,   // Time for blood test
    COMPLETED;       // Recovery complete

    val color: Color
        get() = when (this) {
            NOT_NEEDED -> Color(0xFF607D8B)
            WAITING -> Color(0xFF2196F3)
            START_SOON -> Color(0xFFFFB300)
            START_NOW -> Color(0xFF00C853)
            IN_PROGRESS -> Color(0xFF2DE2E6)
            BLOODWORK_DUE -> Color(0xFFFCA311)
            COMPLETED -> Color(0xFF4CAF50)
        }
}

/**
 * PCT tracking data - persisted with cycle.
 */
data class PCTData(
    val cycleId: String,
    val lastInjectionDate: LocalDate,
    val longestEster: EsterType,
    val protocol: PCTProtocolType = PCTProtocolType.CLOMIPHENE_ONLY,
    val pctStartDate: LocalDate? = null,           // When user actually started
    val pctEndDate: LocalDate? = null,             // When PCT ended
    val bloodworkDates: List<LocalDate> = emptyList(),
    val notes: String = ""
) {
    /**
     * Calculated earliest date to start PCT.
     * Based on ester clearance + pituitary recovery time.
     */
    val recommendedStartDate: LocalDate
        get() = lastInjectionDate.plusDays(longestEster.pctWaitDays.toLong())

    /**
     * Days remaining until PCT should start.
     */
    fun daysUntilPCT(today: LocalDate = LocalDate.now()): Long {
        return ChronoUnit.DAYS.between(today, recommendedStartDate)
    }

    /**
     * Current PCT phase based on dates.
     */
    fun currentPhase(today: LocalDate = LocalDate.now()): PCTPhase = when {
        // !isBefore umesto isAfter - completePCT() postavlja pctEndDate na današnji
        // datum, pa PCT mora biti COMPLETED već ISTOG dana, ne tek sutradan
        pctEndDate != null && !today.isBefore(pctEndDate) -> PCTPhase.COMPLETED
        pctStartDate != null -> PCTPhase.ACTIVE
        today.isAfter(recommendedStartDate) || today.isEqual(recommendedStartDate) -> PCTPhase.READY_TO_START
        else -> PCTPhase.WAITING
    }

    /**
     * Current PCT status for UI display.
     */
    fun currentStatus(today: LocalDate = LocalDate.now()): PCTStatus {
        val daysUntil = daysUntilPCT(today)
        val phase = currentPhase(today)

        return when {
            phase == PCTPhase.COMPLETED -> PCTStatus.COMPLETED
            phase == PCTPhase.ACTIVE -> {
                // Check if bloodwork is due (every 4 weeks after first 6 weeks)
                val pctStart = pctStartDate ?: return PCTStatus.IN_PROGRESS
                val daysSinceStart = ChronoUnit.DAYS.between(pctStart, today)
                val lastBloodwork = bloodworkDates.maxOrNull()
                val daysSinceBloodwork = lastBloodwork?.let { ChronoUnit.DAYS.between(it, today) }

                when {
                    daysSinceStart >= 42 && (daysSinceBloodwork == null || daysSinceBloodwork >= 28) ->
                        PCTStatus.BLOODWORK_DUE
                    else -> PCTStatus.IN_PROGRESS
                }
            }
            daysUntil <= 0 -> PCTStatus.START_NOW
            daysUntil <= 3 -> PCTStatus.START_SOON
            else -> PCTStatus.WAITING
        }
    }

    /**
     * Recommended minimum PCT duration (3 months for hormones, 6 for sperm).
     */
    val recommendedDurationMonths: Int = 3
    val optimalDurationMonths: Int = 6

    fun toJson(): JSONObject = JSONObject().apply {
        put("cycle_id", cycleId)
        put("last_injection_date", lastInjectionDate.format(DateTimeFormatter.ISO_LOCAL_DATE))
        put("longest_ester", longestEster.key)
        put("protocol", protocol.key)
        pctStartDate?.let { put("pct_start_date", it.format(DateTimeFormatter.ISO_LOCAL_DATE)) }
        pctEndDate?.let { put("pct_end_date", it.format(DateTimeFormatter.ISO_LOCAL_DATE)) }
        if (bloodworkDates.isNotEmpty()) {
            put("bloodwork_dates", JSONArray().apply {
                bloodworkDates.forEach { put(it.format(DateTimeFormatter.ISO_LOCAL_DATE)) }
            })
        }
        put("notes", notes)
    }

    companion object {
        fun fromJson(obj: JSONObject): PCTData = PCTData(
            cycleId = obj.optString("cycle_id", ""),
            lastInjectionDate = obj.optString("last_injection_date", "").let {
                if (it.isNotEmpty()) LocalDate.parse(it) else LocalDate.now()
            },
            longestEster = EsterType.fromKey(obj.optString("longest_ester", "enanthate")),
            protocol = PCTProtocolType.fromKey(obj.optString("protocol", "cc_only")),
            pctStartDate = obj.optString("pct_start_date", "").takeIf { it.isNotEmpty() }
                ?.let { LocalDate.parse(it) },
            pctEndDate = obj.optString("pct_end_date", "").takeIf { it.isNotEmpty() }
                ?.let { LocalDate.parse(it) },
            bloodworkDates = obj.optJSONArray("bloodwork_dates")?.let { arr ->
                (0 until arr.length()).map { LocalDate.parse(arr.getString(it)) }
            } ?: emptyList(),
            notes = obj.optString("notes", "")
        )
    }
}

/**
 * PCT Guide info for UI display (computed, not persisted).
 */
data class PCTGuideInfo(
    val pctData: PCTData,
    val status: PCTStatus,
    val phase: PCTPhase,
    val daysUntilStart: Long,
    val daysSinceStart: Long?,
    val progressPercent: Float, // 0-1 based on compound-specific minimum duration
    val nextBloodworkDate: LocalDate?,
    val protocolDetails: String,
    val compoundAdvice: CyclePCTAdvice? = null  // Compound-specific advice
) {
    val isActive: Boolean
        get() = status in listOf(PCTStatus.START_NOW, PCTStatus.IN_PROGRESS, PCTStatus.BLOODWORK_DUE, PCTStatus.START_SOON)
}

/**
 * Compound-specific PCT advice generated from cycle compounds.
 */
data class CyclePCTAdvice(
    val compounds: List<CompoundType>,
    val worstSuppression: SuppressionLevel,
    val requiresHCG: Boolean,
    val recommendedProtocol: PCTProtocolType,
    val recommendedDurationWeeks: Int,
    val warnings: List<String>,
    val advice: List<String>
) {
    /**
     * Get a summary text for the advice.
     */
    val summaryText: String
        get() = buildString {
            append("Na osnovu komponenti u ciklusu:\n")
            compounds.distinct().forEach { compound ->
                append("• ${compound.displayName}\n")
            }
            append("\n")
            append("Nivo supresije: ${worstSuppression.displayName}\n")
            if (requiresHCG) {
                append("⚠️ hCG JE PREPORUČEN za ovaj ciklus!\n")
            }
            append("\nPreporučeni protokol: ${recommendedProtocol.displayName}\n")
            val months = "%.0f".format(recommendedDurationWeeks / 4.3)
            append("Preporučeno trajanje: $recommendedDurationWeeks nedelja (~$months meseci)\n")
            append("Prekid na osnovu laboratorija (TT, LH, FSH)\n")
        }

    companion object {
        /**
         * Generate PCT advice from a list of substance IDs.
         */
        fun fromSubstanceIds(substanceIds: List<String>): CyclePCTAdvice {
            // Detect all compound types
            val compounds = substanceIds.map { CompoundType.fromSubstanceId(it) }
            val distinctCompounds = compounds.distinct()

            // Find the worst suppression level
            val suppressionOrder = listOf(
                SuppressionLevel.MILD,
                SuppressionLevel.MODERATE,
                SuppressionLevel.SEVERE,
                SuppressionLevel.EXTREME
            )
            val worstSuppression = distinctCompounds
                .map { it.suppressionLevel }
                .maxByOrNull { suppressionOrder.indexOf(it) }
                ?: SuppressionLevel.MODERATE

            // Check if any compound requires hCG
            val requiresHCG = distinctCompounds.any { it.requiresHCG }

            // Determine recommended protocol (use most aggressive if hCG needed)
            val recommendedProtocol = if (requiresHCG) {
                PCTProtocolType.CLOMIPHENE_HCG
            } else {
                distinctCompounds
                    .map { it.recommendedProtocol }
                    .maxByOrNull {
                        when (it) {
                            PCTProtocolType.NO_PCT -> 0
                            PCTProtocolType.CLOMIPHENE_ONLY -> 1
                            PCTProtocolType.CLOMIPHENE_HCG -> 2
                        }
                    } ?: PCTProtocolType.CLOMIPHENE_ONLY
            }

            // Use longest recommended duration
            val recommendedDurationWeeks = distinctCompounds.maxOfOrNull { it.pctDurationWeeks }
                ?: CompoundType.UNKNOWN.pctDurationWeeks

            // Generate warnings
            val warnings = mutableListOf<String>()

            if (distinctCompounds.contains(CompoundType.TRENBOLONE)) {
                warnings.add("⚠️ TRENBOLON: Najsupresivniji steroid! Očekuj produženi oporavak (6-12+ meseci).")
            }

            if (distinctCompounds.contains(CompoundType.NANDROLONE)) {
                warnings.add("⚠️ NANDROLON: 19-nor struktura se dugo zadržava. hCG je OBAVEZAN!")
            }

            if (distinctCompounds.contains(CompoundType.TRENBOLONE) &&
                distinctCompounds.contains(CompoundType.NANDROLONE)) {
                warnings.add("⚠️ KOMBINACIJA 19-NOR STEROIDA: Ekstremno teška za oporavak! Razmotri TRT ako plodnost nije prioritet.")
            }

            if (worstSuppression == SuppressionLevel.EXTREME) {
                warnings.add("⚠️ EKSTREMNA SUPRESIJA: Preporučuje se kontrola hormona pre i tokom PCT-a.")
            }

            // Generate advice
            val advice = mutableListOf<String>()

            // Protocol-specific advice
            when (recommendedProtocol) {
                PCTProtocolType.CLOMIPHENE_HCG -> {
                    advice.add("✓ Koristi Clomiphene Citrate (CC) 25mg dnevno")
                    advice.add("✓ Dodaj hCG 1500IU 3x nedeljno (pon/sre/pet)")
                    advice.add("✓ hCG koristi TOKOM CELOG PCT-a zajedno sa CC (BJU 2026)")
                }
                PCTProtocolType.CLOMIPHENE_ONLY -> {
                    advice.add("✓ Koristi Clomiphene Citrate (CC) 25mg dnevno")
                    advice.add("✓ Uzimaj uvek u isto vreme")
                }
                PCTProtocolType.NO_PCT -> {
                    advice.add("• Spontani oporavak bez medikamenata")
                    advice.add("• Prati vrednosti hormona")
                }
            }

            // Duration advice
            val minMonths = (recommendedDurationWeeks / 4.3).let { "%.0f".format(it) }
            val optMonths = (recommendedDurationWeeks * 1.5 / 4.3).let { "%.0f".format(it) }
            advice.add("✓ Minimum trajanje: $recommendedDurationWeeks nedelja (~$minMonths meseci)")
            advice.add("✓ Optimalno trajanje: ${(recommendedDurationWeeks * 1.5).toInt()} nedelja (~$optMonths meseci)")
            advice.add("✓ Prekid SAMO na osnovu laboratorija — TT, LH, FSH stabilno u referentnom opsegu na 2 uzastopne kontrole")

            // Bloodwork advice
            advice.add("\n📊 KONTROLA KRVNE SLIKE:")
            advice.add("• Pre PCT-a: LH, FSH, Testosteron, Estradiol")
            advice.add("• 6 nedelja nakon početka PCT-a")
            advice.add("• 3 meseca nakon završetka PCT-a")
            if (worstSuppression in listOf(SuppressionLevel.SEVERE, SuppressionLevel.EXTREME)) {
                advice.add("• Spermiogram nakon 6-12 meseci (ako je plodnost bitna)")
            }

            // Compound-specific notes
            distinctCompounds.forEach { compound ->
                if (compound.specialNotes.isNotBlank() && compound != CompoundType.UNKNOWN) {
                    advice.add("\n📋 ${compound.displayName.uppercase()}:")
                    advice.add(compound.specialNotes)
                }
            }

            return CyclePCTAdvice(
                compounds = distinctCompounds,
                worstSuppression = worstSuppression,
                requiresHCG = requiresHCG,
                recommendedProtocol = recommendedProtocol,
                recommendedDurationWeeks = recommendedDurationWeeks,
                warnings = warnings,
                advice = advice
            )
        }
    }
}

// =============================================================================
// NOTIFICATION SETTINGS
// =============================================================================

data class NotificationSettings(
    // Injekcije
    val injectionEnabled: Boolean = true,
    val injectionReminder5h: Boolean = true,
    val injectionReminder3h: Boolean = true,
    val injectionReminder30min: Boolean = true,
    val injectionReminderAtTime: Boolean = true,
    val injectionOverdueEnabled: Boolean = true,
    val injectionOverdueIntervalMinutes: Int = 60,

    // Oralni steroidi
    val oralEnabled: Boolean = true,
    val oralReminder30min: Boolean = true,
    val oralReminderAtTime: Boolean = true,
    val oralOverdueEnabled: Boolean = true,
    val oralOverdueIntervalMinutes: Int = 60,

    // PCT
    val pctEnabled: Boolean = true,
    val pctAdvanceWarning: Boolean = true,
    val pctStartNotification: Boolean = true,
    val pctBloodworkReminder: Boolean = true
) {
    fun toJson(): JSONObject = JSONObject().apply {
        put("injection_enabled", injectionEnabled)
        put("injection_reminder_5h", injectionReminder5h)
        put("injection_reminder_3h", injectionReminder3h)
        put("injection_reminder_30min", injectionReminder30min)
        put("injection_reminder_at_time", injectionReminderAtTime)
        put("injection_overdue_enabled", injectionOverdueEnabled)
        put("injection_overdue_interval_minutes", injectionOverdueIntervalMinutes)

        put("oral_enabled", oralEnabled)
        put("oral_reminder_30min", oralReminder30min)
        put("oral_reminder_at_time", oralReminderAtTime)
        put("oral_overdue_enabled", oralOverdueEnabled)
        put("oral_overdue_interval_minutes", oralOverdueIntervalMinutes)

        put("pct_enabled", pctEnabled)
        put("pct_advance_warning", pctAdvanceWarning)
        put("pct_start_notification", pctStartNotification)
        put("pct_bloodwork_reminder", pctBloodworkReminder)
    }

    companion object {
        fun fromJson(obj: JSONObject): NotificationSettings = NotificationSettings(
            injectionEnabled = obj.optBoolean("injection_enabled", true),
            injectionReminder5h = obj.optBoolean("injection_reminder_5h", true),
            injectionReminder3h = obj.optBoolean("injection_reminder_3h", true),
            injectionReminder30min = obj.optBoolean("injection_reminder_30min", true),
            injectionReminderAtTime = obj.optBoolean("injection_reminder_at_time", true),
            injectionOverdueEnabled = obj.optBoolean("injection_overdue_enabled", true),
            injectionOverdueIntervalMinutes = obj.optInt("injection_overdue_interval_minutes", 60),

            oralEnabled = obj.optBoolean("oral_enabled", true),
            oralReminder30min = obj.optBoolean("oral_reminder_30min", true),
            oralReminderAtTime = obj.optBoolean("oral_reminder_at_time", true),
            oralOverdueEnabled = obj.optBoolean("oral_overdue_enabled", true),
            oralOverdueIntervalMinutes = obj.optInt("oral_overdue_interval_minutes", 60),

            pctEnabled = obj.optBoolean("pct_enabled", true),
            pctAdvanceWarning = obj.optBoolean("pct_advance_warning", true),
            pctStartNotification = obj.optBoolean("pct_start_notification", true),
            pctBloodworkReminder = obj.optBoolean("pct_bloodwork_reminder", true)
        )
    }
}

// =============================================================================
// UTILITY
// =============================================================================

fun generateId(): String = UUID.randomUUID().toString().replace("-", "").take(12)

// =============================================================================
// LOCALIZED DISPLAY NAMES
// =============================================================================

fun InjectionFrequency.getDisplayName(strings: Strings): String = when (this) {
    InjectionFrequency.ED -> strings.freqDaily
    InjectionFrequency.EOD -> strings.freqEod
    InjectionFrequency.E3D -> strings.freqE3d
    InjectionFrequency.E3_5D -> strings.freqE35d
    InjectionFrequency.E5D -> strings.freqE5d
    InjectionFrequency.WEEKLY -> strings.freqWeekly
    InjectionFrequency.E10D -> strings.freqE10d
    InjectionFrequency.E14D -> strings.freqBiweekly
}

fun InjectionSite.getDisplayName(strings: Strings): String = when (this) {
    InjectionSite.GLUTE_L -> strings.gluteLeft
    InjectionSite.GLUTE_R -> strings.gluteRight
    InjectionSite.VG_L -> strings.vgLeft
    InjectionSite.VG_R -> strings.vgRight
    InjectionSite.DELT_L -> strings.deltLeft
    InjectionSite.DELT_R -> strings.deltRight
    InjectionSite.QUAD_L -> strings.quadLeft
    InjectionSite.QUAD_R -> strings.quadRight
    InjectionSite.PEC_L -> strings.pecLeft
    InjectionSite.PEC_R -> strings.pecRight
}

fun InjectionSite.getTip(strings: Strings): String = when (this) {
    InjectionSite.GLUTE_L, InjectionSite.GLUTE_R -> strings.gluteTip
    InjectionSite.VG_L, InjectionSite.VG_R -> strings.vgTip
    InjectionSite.DELT_L, InjectionSite.DELT_R -> strings.deltTip
    InjectionSite.QUAD_L, InjectionSite.QUAD_R -> strings.quadTip
    InjectionSite.PEC_L, InjectionSite.PEC_R -> strings.pecTip
}

fun InjectionStatus.getDisplayName(strings: Strings): String = when (this) {
    InjectionStatus.ON_TRACK -> strings.onTrack
    InjectionStatus.DUE_SOON -> strings.dueSoon
    InjectionStatus.OVERDUE -> strings.overdue
    InjectionStatus.AWAITING_FIRST -> strings.awaitingFirst
}

fun NextInjectionInfo.getTimeDisplay(strings: Strings): String {
    // If it's the first injection, show special text
    if (isFirstInjection) {
        return strings.firstInjectionPending
    }

    val absMinutes = kotlin.math.abs(hoursRemaining * 60).toInt()
    val days = absMinutes / (24 * 60)
    val hours = (absMinutes % (24 * 60)) / 60
    val minutes = absMinutes % 60

    val timeStr = buildString {
        if (days > 0) append("${days}d ")
        if (hours > 0 || days > 0) append("${hours}h ")
        if (minutes > 0) append("${minutes}m")
    }.trim()

    return if (hoursRemaining < 0) {
        strings.hoursAgo.replace("%s", timeStr)
    } else {
        strings.inHours.replace("%s", timeStr)
    }
}

fun NextInjectionInfo.getScheduledDateTimeDisplay(): String {
    val now = LocalDateTime.now()
    val formatter = java.time.format.DateTimeFormatter.ofPattern("HH:mm")
    val dateFormatter = java.time.format.DateTimeFormatter.ofPattern("dd.MM HH:mm")

    return when {
        scheduledDateTime.toLocalDate() == now.toLocalDate() ->
            scheduledDateTime.format(formatter)
        scheduledDateTime.toLocalDate() == now.toLocalDate().plusDays(1) ->
            "sutra ${scheduledDateTime.format(formatter)}"
        else ->
            scheduledDateTime.format(dateFormatter)
    }
}
