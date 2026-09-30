package com.injekcije.app.data

import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.LocalDateTime

/**
 * Unit testovi za JSON serijalizaciju/deserijalizaciju (round-trip).
 * Proverava da svi modeli mogu preživeti toJson() → fromJson() bez gubitka podataka.
 *
 * Napomena: Zahteva testImplementation("org.json:json:20231013") u build.gradle.kts
 */
class JsonRoundTripTest {

    // =========================================================================
    // Cycle - round-trip
    // =========================================================================

    @Test
    fun `Cycle round-trip - sva polja sacuvana`() {
        val original = Cycle(
            id = "cycle_abc123",
            name = "Testosteron ciklus",
            startDate = LocalDate.of(2025, 3, 1),
            durationWeeks = 12,
            compounds = listOf(
                CycleCompound(
                    substanceId = "test_e",
                    weeklyDoseMg = 500.0,
                    frequency = InjectionFrequency.E3_5D,
                    concentrationMgMl = 250.0,
                    startWeek = 1,
                    endWeek = 12
                )
            ),
            isActive = true
        )

        val json = original.toJson()
        val restored = Cycle.fromJson(json)

        assertEquals(original.id, restored.id)
        assertEquals(original.name, restored.name)
        assertEquals(original.startDate, restored.startDate)
        assertEquals(original.durationWeeks, restored.durationWeeks)
        assertEquals(original.isActive, restored.isActive)
        assertEquals(original.compounds.size, restored.compounds.size)
        assertEquals(original.compounds[0].substanceId, restored.compounds[0].substanceId)
        assertEquals(original.compounds[0].weeklyDoseMg, restored.compounds[0].weeklyDoseMg, 0.001)
        assertEquals(original.compounds[0].frequency, restored.compounds[0].frequency)
        assertEquals(original.compounds[0].concentrationMgMl, restored.compounds[0].concentrationMgMl, 0.001)
    }

    @Test
    fun `Cycle round-trip - sa vise kompounada i fazama`() {
        val original = Cycle(
            id = "cycle_multi",
            name = "Test + Deca ciklus",
            startDate = LocalDate.of(2025, 1, 15),
            durationWeeks = 16,
            compounds = listOf(
                CycleCompound(
                    substanceId = "test_e",
                    weeklyDoseMg = 500.0,
                    frequency = InjectionFrequency.E3_5D,
                    concentrationMgMl = 250.0,
                    phases = listOf(
                        DosePhase(startWeek = 1, endWeek = 2, weeklyDoseMg = 750.0),
                        DosePhase(startWeek = 3, endWeek = 14, weeklyDoseMg = 500.0),
                        DosePhase(startWeek = 15, endWeek = 16, weeklyDoseMg = 250.0)
                    )
                ),
                CycleCompound(
                    substanceId = "deca",
                    weeklyDoseMg = 400.0,
                    frequency = InjectionFrequency.WEEKLY,
                    concentrationMgMl = 200.0,
                    startWeek = 1,
                    endWeek = 14
                )
            ),
            isActive = false
        )

        val json = original.toJson()
        val restored = Cycle.fromJson(json)

        assertEquals(original.compounds.size, restored.compounds.size)
        assertEquals(original.isActive, restored.isActive)

        // Proveri faze prvog kompaunda
        val phases = restored.compounds[0].phases
        assertEquals(3, phases.size)
        assertEquals(750.0, phases[0].weeklyDoseMg, 0.001)
        assertEquals(1, phases[0].startWeek)
        assertEquals(2, phases[0].endWeek)

        // Proveri drugi kompaund
        assertEquals("deca", restored.compounds[1].substanceId)
        assertEquals(14, restored.compounds[1].endWeek)
    }

    // =========================================================================
    // InjectionRecord - round-trip
    // =========================================================================

    @Test
    fun `InjectionRecord round-trip - sva polja sacuvana`() {
        val dateTime = LocalDateTime.of(2025, 3, 15, 10, 30, 0)
        val original = InjectionRecord(
            id = "inj_001",
            cycleId = "cycle_abc",
            substanceId = "test_e",
            dateTime = dateTime,
            site = InjectionSite.VG_L,
            doseMg = 250.0,
            volumeMl = 1.0,
            week = 3,
            notes = "Bez bola"
        )

        val json = original.toJson()
        val restored = InjectionRecord.fromJson(json)

        assertEquals(original.id, restored.id)
        assertEquals(original.cycleId, restored.cycleId)
        assertEquals(original.substanceId, restored.substanceId)
        assertEquals(original.dateTime, restored.dateTime)
        assertEquals(original.site, restored.site)
        assertEquals(original.doseMg, restored.doseMg, 0.001)
        assertEquals(original.volumeMl, restored.volumeMl, 0.001)
        assertEquals(original.week, restored.week)
        assertEquals(original.notes, restored.notes)
    }

    @Test
    fun `InjectionRecord round-trip - prazan notes`() {
        val dateTime = LocalDateTime.of(2025, 6, 1, 8, 0, 0)
        val original = InjectionRecord(
            id = "inj_002",
            cycleId = "cycle_abc",
            substanceId = "tren_a",
            dateTime = dateTime,
            site = InjectionSite.DELT_R,
            doseMg = 100.0,
            volumeMl = 1.0,
            week = 5,
            notes = ""
        )

        val json = original.toJson()
        val restored = InjectionRecord.fromJson(json)

        assertEquals(original.notes, restored.notes)
    }

    // =========================================================================
    // OralDoseRecord - round-trip
    // =========================================================================

    @Test
    fun `OralDoseRecord round-trip - sva polja sacuvana`() {
        val dateTime = LocalDateTime.of(2025, 4, 10, 8, 0, 0)
        val original = OralDoseRecord(
            id = "oral_001",
            cycleId = "cycle_abc",
            substanceId = "methandienone",
            dateTime = dateTime,
            doseMg = 10.0,
            week = 2
        )

        val json = original.toJson()
        val restored = OralDoseRecord.fromJson(json)

        assertEquals(original.id, restored.id)
        assertEquals(original.cycleId, restored.cycleId)
        assertEquals(original.substanceId, restored.substanceId)
        assertEquals(original.dateTime, restored.dateTime)
        assertEquals(original.doseMg, restored.doseMg, 0.001)
        assertEquals(original.week, restored.week)
    }

    // =========================================================================
    // PCTData - round-trip
    // =========================================================================

    @Test
    fun `PCTData round-trip - sva polja sacuvana`() {
        val original = PCTData(
            cycleId = "cycle_abc",
            lastInjectionDate = LocalDate.of(2025, 6, 1),
            longestEster = EsterType.ENANTHATE,
            protocol = PCTProtocolType.CLOMIPHENE_HCG,
            pctStartDate = LocalDate.of(2025, 7, 16),
            pctEndDate = LocalDate.of(2025, 12, 16),
            bloodworkDates = listOf(
                LocalDate.of(2025, 8, 27),
                LocalDate.of(2025, 10, 15)
            ),
            notes = "Krvna slika uredna"
        )

        val json = original.toJson()
        val restored = PCTData.fromJson(json)

        assertEquals(original.cycleId, restored.cycleId)
        assertEquals(original.lastInjectionDate, restored.lastInjectionDate)
        assertEquals(original.longestEster, restored.longestEster)
        assertEquals(original.protocol, restored.protocol)
        assertEquals(original.pctStartDate, restored.pctStartDate)
        assertEquals(original.pctEndDate, restored.pctEndDate)
        assertEquals(original.bloodworkDates, restored.bloodworkDates)
        assertEquals(original.notes, restored.notes)
    }

    @Test
    fun `PCTData round-trip - bez opcionalnih polja`() {
        val original = PCTData(
            cycleId = "cycle_abc",
            lastInjectionDate = LocalDate.of(2025, 6, 1),
            longestEster = EsterType.CYPIONATE
        )

        val json = original.toJson()
        val restored = PCTData.fromJson(json)

        assertEquals(original.cycleId, restored.cycleId)
        assertEquals(original.lastInjectionDate, restored.lastInjectionDate)
        assertEquals(original.longestEster, restored.longestEster)
        assertEquals(null, restored.pctStartDate)
        assertEquals(null, restored.pctEndDate)
        assertTrue(restored.bloodworkDates.isEmpty())
    }

    // =========================================================================
    // CycleCompound - round-trip
    // =========================================================================

    @Test
    fun `CycleCompound round-trip - bez faza`() {
        val original = CycleCompound(
            substanceId = "test_c",
            weeklyDoseMg = 300.0,
            frequency = InjectionFrequency.E3_5D,
            concentrationMgMl = 200.0,
            startWeek = 1,
            endWeek = null
        )

        val json = original.toJson()
        val restored = CycleCompound.fromJson(json)

        assertEquals(original.substanceId, restored.substanceId)
        assertEquals(original.weeklyDoseMg, restored.weeklyDoseMg, 0.001)
        assertEquals(original.frequency, restored.frequency)
        assertEquals(original.concentrationMgMl, restored.concentrationMgMl, 0.001)
        assertEquals(original.startWeek, restored.startWeek)
        assertEquals(original.endWeek, restored.endWeek)
        assertTrue(restored.phases.isEmpty())
    }

    @Test
    fun `CycleCompound round-trip - sa fazama`() {
        val original = CycleCompound(
            substanceId = "test_e",
            weeklyDoseMg = 500.0,
            frequency = InjectionFrequency.E3_5D,
            concentrationMgMl = 250.0,
            phases = listOf(
                DosePhase(1, 2, 750.0),
                DosePhase(3, 10, 500.0)
            ),
            startWeek = 1,
            endWeek = 12
        )

        val json = original.toJson()
        val restored = CycleCompound.fromJson(json)

        assertEquals(2, restored.phases.size)
        assertEquals(750.0, restored.phases[0].weeklyDoseMg, 0.001)
        assertEquals(12, restored.endWeek)
    }

    // =========================================================================
    // Stari JSON format - nedostajuća opcionalna polja (backward compatibility)
    // =========================================================================

    @Test
    fun `Cycle fromJson - nedostaje id polje`() {
        val json = JSONObject().apply {
            put("name", "Test ciklus")
            put("start_date", "2025-03-01")
            put("duration_weeks", 12)
        }

        val cycle = Cycle.fromJson(json)
        assertTrue("ID treba biti generisan", cycle.id.isNotEmpty())
        assertEquals("Test ciklus", cycle.name)
        assertEquals(12, cycle.durationWeeks)
    }

    @Test
    fun `Cycle fromJson - nedostaju sva opcionalna polja`() {
        val json = JSONObject().apply {
            put("duration_weeks", 8)
        }

        val cycle = Cycle.fromJson(json)
        assertTrue(cycle.id.isNotEmpty())
        assertEquals("Neimenovan ciklus", cycle.name)
        assertEquals(8, cycle.durationWeeks)
        assertTrue(cycle.isActive)
        assertTrue(cycle.compounds.isEmpty())
    }

    @Test
    fun `InjectionRecord fromJson - minimalan JSON`() {
        val json = JSONObject()

        val record = InjectionRecord.fromJson(json)
        assertTrue(record.id.isNotEmpty())
        assertEquals("", record.cycleId)
        assertEquals("", record.substanceId)
        assertEquals(InjectionSite.GLUTE_L, record.site)
        assertEquals(0.0, record.doseMg, 0.001)
        assertEquals(0.0, record.volumeMl, 0.001)
        assertEquals(1, record.week)
        assertEquals("", record.notes)
    }

    @Test
    fun `OralDoseRecord fromJson - minimalan JSON`() {
        val json = JSONObject()

        val record = OralDoseRecord.fromJson(json)
        assertTrue(record.id.isNotEmpty())
        assertEquals("", record.cycleId)
        assertEquals("", record.substanceId)
        assertEquals(0.0, record.doseMg, 0.001)
        assertEquals(1, record.week)
    }

    @Test
    fun `PCTData fromJson - minimalan JSON`() {
        val json = JSONObject()

        val pctData = PCTData.fromJson(json)
        assertEquals("", pctData.cycleId)
        assertEquals(EsterType.ENANTHATE, pctData.longestEster)
        assertEquals(PCTProtocolType.CLOMIPHENE_ONLY, pctData.protocol)
        assertEquals(null, pctData.pctStartDate)
        assertEquals(null, pctData.pctEndDate)
        assertTrue(pctData.bloodworkDates.isEmpty())
        assertEquals("", pctData.notes)
    }

    // =========================================================================
    // NotificationSettings - round-trip
    // =========================================================================

    @Test
    fun `NotificationSettings round-trip - podrazumevane vrednosti`() {
        val original = NotificationSettings()

        val json = original.toJson()
        val restored = NotificationSettings.fromJson(json)

        assertEquals(original.injectionEnabled, restored.injectionEnabled)
        assertEquals(original.injectionReminder5h, restored.injectionReminder5h)
        assertEquals(original.injectionReminder3h, restored.injectionReminder3h)
        assertEquals(original.oralEnabled, restored.oralEnabled)
        assertEquals(original.pctEnabled, restored.pctEnabled)
    }

    @Test
    fun `NotificationSettings round-trip - prilagodjena podesavanja`() {
        val original = NotificationSettings(
            injectionEnabled = false,
            injectionReminder5h = false,
            pctEnabled = false
        )

        val json = original.toJson()
        val restored = NotificationSettings.fromJson(json)

        assertEquals(false, restored.injectionEnabled)
        assertEquals(false, restored.injectionReminder5h)
        assertEquals(false, restored.pctEnabled)
    }

    @Test
    fun `NotificationSettings fromJson - prazan JSON koristi podrazumevane`() {
        val json = JSONObject()
        val settings = NotificationSettings.fromJson(json)

        assertEquals(true, settings.injectionEnabled)
        assertEquals(true, settings.oralEnabled)
        assertEquals(true, settings.pctEnabled)
    }

    // =========================================================================
    // Substance - round-trip
    // =========================================================================

    @Test
    fun `Substance round-trip - sva polja sacuvana`() {
        val original = Substance(
            id = "test_e",
            name = "Testosteron Enantat",
            shortName = "Test E",
            defaultConcentration = 250.0,
            category = "AAS",
            recommendedFrequency = InjectionFrequency.E3_5D,
            recommendedDose = 500.0,
            recommendedMaxWeeks = 16,
            esterType = EsterType.ENANTHATE
        )

        val json = original.toJson()
        val restored = Substance.fromJson(json)

        assertEquals(original.id, restored.id)
        assertEquals(original.name, restored.name)
        assertEquals(original.shortName, restored.shortName)
        assertEquals(original.defaultConcentration, restored.defaultConcentration, 0.001)
        assertEquals(original.category, restored.category)
        assertEquals(original.recommendedFrequency, restored.recommendedFrequency)
        assertEquals(original.recommendedDose, restored.recommendedDose, 0.001)
        assertEquals(original.recommendedMaxWeeks, restored.recommendedMaxWeeks)
        assertEquals(original.esterType, restored.esterType)
    }

    @Test
    fun `Substance round-trip - bez esterType`() {
        val original = Substance(
            id = "custom",
            name = "Custom",
            shortName = "C",
            defaultConcentration = 100.0,
            category = "AAS"
        )

        val json = original.toJson()
        val restored = Substance.fromJson(json)

        assertEquals(null, restored.esterType)
    }

    // =========================================================================
    // DosePhase - round-trip
    // =========================================================================

    @Test
    fun `DosePhase round-trip - sva polja sacuvana`() {
        val original = DosePhase(
            startWeek = 1,
            endWeek = 4,
            weeklyDoseMg = 750.0
        )

        val json = original.toJson()
        val restored = DosePhase.fromJson(json)

        assertEquals(original.startWeek, restored.startWeek)
        assertEquals(original.endWeek, restored.endWeek)
        assertEquals(original.weeklyDoseMg, restored.weeklyDoseMg, 0.001)
    }

    // =========================================================================
    // JSON sa ekstra poljima - forward compatibility
    // =========================================================================

    @Test
    fun `Cycle fromJson - ignoriše nepoznata polja`() {
        val json = JSONObject().apply {
            put("id", "cycle_extra")
            put("name", "Test ciklus")
            put("start_date", "2025-03-01")
            put("duration_weeks", 12)
            put("is_active", true)
            put("unknown_field_xyz", "neka vrednost")
            put("extra_number", 42)
            put("nested_extra", JSONObject().apply { put("a", 1) })
        }

        val cycle = Cycle.fromJson(json)
        assertEquals("cycle_extra", cycle.id)
        assertEquals("Test ciklus", cycle.name)
        assertEquals(12, cycle.durationWeeks)
    }

    @Test
    fun `InjectionRecord fromJson - ignoriše nepoznata polja`() {
        val json = JSONObject().apply {
            put("id", "inj_extra")
            put("cycle_id", "cycle_abc")
            put("substance_id", "test_e")
            put("date_time", "2025-03-15T10:30:00")
            put("site", "vg_l")
            put("dose_mg", 250.0)
            put("volume_ml", 1.0)
            put("week", 3)
            put("notes", "Test")
            put("future_field", true)
        }

        val record = InjectionRecord.fromJson(json)
        assertEquals("inj_extra", record.id)
        assertEquals(250.0, record.doseMg, 0.001)
    }

    @Test
    fun `OralDoseRecord fromJson - ignoriše nepoznata polja`() {
        val json = JSONObject().apply {
            put("id", "oral_extra")
            put("cycle_id", "cycle_abc")
            put("substance_id", "methandienone")
            put("date_time", "2025-04-10T08:00:00")
            put("dose_mg", 10.0)
            put("week", 2)
            put("extra_data", "nesto")
        }

        val record = OralDoseRecord.fromJson(json)
        assertEquals("oral_extra", record.id)
        assertEquals(10.0, record.doseMg, 0.001)
    }

    // =========================================================================
    // Type mismatch u JSON-u - robusnost parsiranja
    // =========================================================================

    @Test
    fun `Cycle fromJson - duration_weeks kao string se parsira sa default`() {
        // JSONObject.optInt vraca default ako tip ne odgovara
        val json = JSONObject().apply {
            put("duration_weeks", "dvanaest")  // string umesto int
        }
        // optInt ce vratiti default 12 za nevalidni string
        val cycle = Cycle.fromJson(json)
        assertTrue(cycle.durationWeeks > 0)
    }

    @Test
    fun `InjectionRecord fromJson - dose_mg kao string se parsira sa default`() {
        val json = JSONObject().apply {
            put("dose_mg", "sto")  // string umesto double
        }
        // optDouble vraca default za nevalidni string
        val record = InjectionRecord.fromJson(json)
        assertEquals(0.0, record.doseMg, 0.001)
    }

    @Test
    fun `CycleCompound fromJson - bez phases polja`() {
        val json = JSONObject().apply {
            put("substance_id", "test_e")
            put("weekly_dose_mg", 500.0)
            put("frequency", "e3.5d")
            put("concentration_mg_ml", 250.0)
        }
        val compound = CycleCompound.fromJson(json)
        assertTrue(compound.phases.isEmpty())
        assertEquals(1, compound.startWeek)
        assertEquals(null, compound.endWeek)
    }

    @Test
    fun `NotificationSettings fromJson - boolean kao string koristi default`() {
        val json = JSONObject().apply {
            put("injection_enabled", "da")  // string umesto boolean
        }
        val settings = NotificationSettings.fromJson(json)
        // optBoolean sa default true ce vratiti true za nevalidni format
        assertTrue(settings.injectionEnabled)
    }

    // =========================================================================
    // Round-trip sa Unicode i specijalnim karakterima
    // =========================================================================

    @Test
    fun `Cycle round-trip - srpski dijakritici u nazivu`() {
        val original = Cycle(
            id = "cycle_dijakritici",
            name = "Čisti ciklus sa šđžć",
            startDate = LocalDate.of(2025, 1, 1),
            durationWeeks = 8,
            compounds = emptyList()
        )

        val json = original.toJson()
        val restored = Cycle.fromJson(json)

        assertEquals("Čisti ciklus sa šđžć", restored.name)
    }

    @Test
    fun `InjectionRecord round-trip - notes sa dijakriticima`() {
        val dateTime = LocalDateTime.of(2025, 3, 15, 10, 30, 0)
        val original = InjectionRecord(
            id = "inj_dij",
            cycleId = "cycle_abc",
            substanceId = "test_e",
            dateTime = dateTime,
            site = InjectionSite.GLUTE_L,
            doseMg = 250.0,
            volumeMl = 1.0,
            week = 1,
            notes = "Mala oteklina, prošla nakon 2 dana. Šta li će sledeći put?"
        )

        val json = original.toJson()
        val restored = InjectionRecord.fromJson(json)

        assertEquals(original.notes, restored.notes)
    }

    // =========================================================================
    // Nevalidni substanceId u ciklusu
    // =========================================================================

    @Test
    fun `CycleCompound round-trip - nepostojeci substanceId`() {
        val original = CycleCompound(
            substanceId = "nepostojeci_steroid_xyz",
            weeklyDoseMg = 500.0,
            frequency = InjectionFrequency.E3_5D,
            concentrationMgMl = 250.0
        )

        val json = original.toJson()
        val restored = CycleCompound.fromJson(json)

        assertEquals("nepostojeci_steroid_xyz", restored.substanceId)
        assertEquals(500.0, restored.weeklyDoseMg, 0.001)
    }

    @Test
    fun `Cycle round-trip - compound sa nepostojecim substanceId`() {
        val original = Cycle(
            id = "cycle_unknown",
            name = "Ciklus sa nepoznatom supstancom",
            startDate = LocalDate.of(2025, 5, 1),
            durationWeeks = 8,
            compounds = listOf(
                CycleCompound(
                    substanceId = "custom_blend_123",
                    weeklyDoseMg = 300.0,
                    frequency = InjectionFrequency.WEEKLY,
                    concentrationMgMl = 150.0
                )
            )
        )

        val json = original.toJson()
        val restored = Cycle.fromJson(json)

        assertEquals(1, restored.compounds.size)
        assertEquals("custom_blend_123", restored.compounds[0].substanceId)
    }
}
