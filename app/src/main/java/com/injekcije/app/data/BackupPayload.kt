package com.injekcije.app.data

import org.json.JSONArray
import org.json.JSONObject

/** Backup ne može da se pročita ili sadrži neispravan zapis. Poruka je za korisnika. */
class BackupPayloadException(message: String) : Exception(message)

/**
 * Potpuno parsiran sadržaj backup-a / lokalnog fajla, spreman za primenu.
 * Čist Kotlin (bez Androida), da parsiranje i round-trip mogu da se testiraju na JVM-u.
 */
data class BackupPayload(
    val substances: Map<String, Substance>,
    val cycles: Map<String, Cycle>,
    val injections: List<InjectionRecord>,
    val oralDoses: List<OralDoseRecord>,
    val pctData: Map<String, PCTData>,
    val siteRotationIndex: Int,
    /** null = ključ ne postoji u JSON-u (zadrži postojeće stanje). */
    val enabledSites: Set<String>?,
    val notificationSettings: NotificationSettings,
    /** Opisi preskočenih zapisa — popunjava se samo u tolerantnom režimu. */
    val skipped: List<String> = emptyList()
) {
    /** Nema korisničkih unosa (podrazumevane supstance se ne broje). */
    val isEmpty: Boolean
        get() = cycles.isEmpty() && injections.isEmpty() && oralDoses.isEmpty() && pctData.isEmpty()

    fun summary(): String =
        "ciklusa: ${cycles.size}, injekcija: ${injections.size}, " +
            "oralnih doza: ${oralDoses.size}, PCT zapisa: ${pctData.size}"
}

object BackupPayloadParser {

    const val KEY_SUBSTANCES = "substances"
    const val KEY_CYCLES = "cycles"
    const val KEY_INJECTIONS = "injections"
    const val KEY_ORAL_DOSES = "oral_doses"
    const val KEY_SITE_INDEX = "site_rotation_index"
    const val KEY_ENABLED_SITES = "enabled_sites"
    const val KEY_PCT_DATA = "pct_data"
    const val KEY_NOTIFICATION_SETTINGS = "notification_settings"
    const val KEY_EXPORTED_AT = "exported_at"

    /**
     * Parsira ceo JSON PRE bilo kakve izmene stanja.
     *
     * [strict] = true (vraćanje backup-a): bilo koji neispravan ili nepotpun zapis je greška
     * celog vraćanja — nikad delimičan "uspeh". Stariji backup-i bez novijih sekcija
     * (oral_doses, pct_data, notification_settings, enabled_sites) i dalje prolaze.
     *
     * [strict] = false (učitavanje lokalnog fajla pri startu): neispravan zapis se preskače
     * i beleži u [BackupPayload.skipped], da jedna loša stavka ne bi ostavila aplikaciju praznom.
     */
    fun parse(jsonText: String, strict: Boolean): BackupPayload {
        val json = try {
            JSONObject(jsonText)
        } catch (e: Exception) {
            throw BackupPayloadException("Nevažeći JSON format: ${e.message}")
        }

        if (strict) {
            val known = listOf(KEY_SUBSTANCES, KEY_CYCLES, KEY_INJECTIONS)
            if (known.none { json.has(it) }) {
                throw BackupPayloadException("Fajl nije Ciklus backup.")
            }
            val arrayKeys = listOf(
                KEY_SUBSTANCES, KEY_CYCLES, KEY_INJECTIONS, KEY_ORAL_DOSES, KEY_PCT_DATA, KEY_ENABLED_SITES
            )
            for (key in arrayKeys) {
                if (json.has(key) && json.optJSONArray(key) == null) {
                    throw BackupPayloadException("Backup je oštećen: polje „$key“ mora biti niz.")
                }
            }
            if (json.has(KEY_NOTIFICATION_SETTINGS) && json.optJSONObject(KEY_NOTIFICATION_SETTINGS) == null) {
                throw BackupPayloadException("Backup je oštećen: polje „$KEY_NOTIFICATION_SETTINGS“ mora biti objekat.")
            }
        }

        val skipped = mutableListOf<String>()

        fun <T> parseArray(
            key: String,
            label: String,
            required: List<String>,
            parse: (JSONObject) -> T
        ): List<T> {
            val arr: JSONArray = json.optJSONArray(key) ?: return emptyList()
            val out = ArrayList<T>(arr.length())
            for (i in 0 until arr.length()) {
                try {
                    val obj = arr.getJSONObject(i)
                    if (strict) {
                        val missing = required.filter { !obj.has(it) || obj.isNull(it) }
                        if (missing.isNotEmpty()) {
                            throw IllegalArgumentException("nedostaje: ${missing.joinToString(", ")}")
                        }
                    }
                    out.add(parse(obj))
                } catch (e: Exception) {
                    val description = "$label, stavka ${i + 1} (${e.message})"
                    if (strict) throw BackupPayloadException("Backup je oštećen: $description.")
                    skipped.add(description)
                }
            }
            return out
        }

        fun <T> toUniqueMap(items: List<T>, label: String, idOf: (T) -> String): MutableMap<String, T> {
            val map = LinkedHashMap<String, T>()
            for (item in items) {
                val id = idOf(item)
                if (map.containsKey(id)) {
                    val description = "$label — dupli identifikator „$id“"
                    if (strict) throw BackupPayloadException("Backup je oštećen: $description.")
                    skipped.add(description)
                }
                map[id] = item
            }
            return map
        }

        val substances = toUniqueMap(
            parseArray(KEY_SUBSTANCES, "supstance", listOf("id", "name", "short_name", "default_concentration")) {
                Substance.fromJson(it)
            },
            "supstance"
        ) { it.id }

        val cycles = toUniqueMap(
            parseArray(KEY_CYCLES, "ciklusi", listOf("id", "start_date", "duration_weeks", "compounds")) {
                Cycle.fromJson(it)
            },
            "ciklusi"
        ) { it.id }

        // Validacija substance ID-ova u ciklusima - za nepostojeće supstance kreiraj placeholder
        val defaultSubstanceIds = SampleData.getDefaultSubstances().map { it.id }.toSet()
        for (cycle in cycles.values) {
            for (compound in cycle.compounds) {
                if (compound.substanceId !in substances && compound.substanceId !in defaultSubstanceIds) {
                    substances[compound.substanceId] = Substance(
                        id = compound.substanceId,
                        name = "Nepoznato (${compound.substanceId})",
                        shortName = compound.substanceId.take(8),
                        defaultConcentration = 250.0,
                        category = "Nepoznato"
                    )
                }
            }
        }

        val siteKeys = InjectionSite.entries.map { it.key }.toSet()
        val injections = parseArray(
            KEY_INJECTIONS, "injekcije",
            listOf("id", "cycle_id", "substance_id", "date_time", "site", "dose_mg")
        ) { obj ->
            if (strict && obj.optString("site", "") !in siteKeys) {
                throw IllegalArgumentException("nepoznato mesto injekcije „${obj.optString("site", "")}“")
            }
            InjectionRecord.fromJson(obj)
        }

        val oralDoses = parseArray(
            KEY_ORAL_DOSES, "oralne doze",
            listOf("id", "cycle_id", "substance_id", "date_time", "dose_mg")
        ) { OralDoseRecord.fromJson(it) }

        val pctData = toUniqueMap(
            parseArray(KEY_PCT_DATA, "PCT podaci", listOf("cycle_id", "last_injection_date")) {
                PCTData.fromJson(it)
            },
            "PCT podaci"
        ) { it.cycleId }

        val enabledSites: Set<String>? = json.optJSONArray(KEY_ENABLED_SITES)?.let { arr ->
            val sites = LinkedHashSet<String>()
            for (i in 0 until arr.length()) {
                val site = arr.optString(i, "")
                if (site.isNotEmpty()) sites.add(site)
            }
            sites
        }

        val notificationSettings = try {
            json.optJSONObject(KEY_NOTIFICATION_SETTINGS)?.let { NotificationSettings.fromJson(it) }
                ?: NotificationSettings()
        } catch (e: Exception) {
            if (strict) throw BackupPayloadException("Backup je oštećen: podešavanja notifikacija (${e.message}).")
            skipped.add("podešavanja notifikacija (${e.message})")
            NotificationSettings()
        }

        return BackupPayload(
            substances = substances,
            cycles = cycles,
            injections = injections,
            oralDoses = oralDoses,
            pctData = pctData,
            siteRotationIndex = json.optInt(KEY_SITE_INDEX, 0).coerceAtLeast(0),
            enabledSites = enabledSites,
            notificationSettings = notificationSettings,
            skipped = skipped
        )
    }

    /** Isti oblik JSON-a koji piše Repository — koristi se i za round-trip proveru. */
    fun toJson(payload: BackupPayload, exportedAt: Long): JSONObject = JSONObject().apply {
        put(KEY_SUBSTANCES, JSONArray().apply { payload.substances.values.forEach { put(it.toJson()) } })
        put(KEY_CYCLES, JSONArray().apply { payload.cycles.values.forEach { put(it.toJson()) } })
        put(KEY_INJECTIONS, JSONArray().apply { payload.injections.forEach { put(it.toJson()) } })
        put(KEY_ORAL_DOSES, JSONArray().apply { payload.oralDoses.forEach { put(it.toJson()) } })
        put(KEY_SITE_INDEX, payload.siteRotationIndex)
        put(KEY_ENABLED_SITES, JSONArray().apply { (payload.enabledSites ?: emptySet()).forEach { put(it) } })
        put(KEY_PCT_DATA, JSONArray().apply { payload.pctData.values.forEach { put(it.toJson()) } })
        put(KEY_NOTIFICATION_SETTINGS, payload.notificationSettings.toJson())
        put(KEY_EXPORTED_AT, exportedAt)
    }
}
