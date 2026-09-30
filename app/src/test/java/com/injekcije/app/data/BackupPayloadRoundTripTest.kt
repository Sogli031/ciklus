package com.injekcije.app.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.LocalDateTime

/**
 * Backup -> reinstalacija -> vraćanje mora dati IDENTIČNO stanje.
 * Testira se BackupPayloadParser (čist Kotlin) kroz koji ide i vraćanje iz oblaka
 * i učitavanje lokalnog fajla.
 */
class BackupPayloadRoundTripTest {

    private fun populatedPayload(): BackupPayload {
        val custom = Substance(
            id = "moj_blend",
            name = "Moj blend šđčćž",
            shortName = "Blend",
            defaultConcentration = 300.0,
            category = "AAS",
            recommendedFrequency = InjectionFrequency.E5D,
            recommendedDose = 450.0,
            recommendedMaxWeeks = 14,
            esterType = EsterType.CYPIONATE
        )
        val cycle = Cycle(
            id = "cycle_1",
            name = "Prolećni ciklus",
            startDate = LocalDate.of(2026, 3, 2),
            durationWeeks = 14,
            compounds = listOf(
                CycleCompound(
                    substanceId = "moj_blend",
                    weeklyDoseMg = 450.0,
                    frequency = InjectionFrequency.E3_5D,
                    concentrationMgMl = 300.0,
                    phases = listOf(DosePhase(1, 4, 300.0), DosePhase(5, 14, 450.0)),
                    startWeek = 1,
                    endWeek = 12
                ),
                CycleCompound("methandienone", 140.0, InjectionFrequency.ED, 10.0)
            ),
            isActive = true
        )
        val oldCycle = cycle.copy(id = "cycle_0", name = "Stari", startDate = LocalDate.of(2025, 1, 6), isActive = false)
        return BackupPayload(
            substances = linkedMapOf(custom.id to custom),
            cycles = linkedMapOf(cycle.id to cycle, oldCycle.id to oldCycle),
            injections = listOf(
                InjectionRecord("inj_1", "cycle_1", "moj_blend", LocalDateTime.of(2026, 3, 2, 9, 15, 30), InjectionSite.VG_L, 225.0, 0.75, 1, "prva, bez bola"),
                InjectionRecord("inj_2", "cycle_1", "moj_blend", LocalDateTime.of(2026, 3, 5, 21, 0), InjectionSite.QUAD_R, 225.0, 0.75, 1, "")
            ),
            oralDoses = listOf(
                OralDoseRecord("oral_1", "cycle_1", "methandienone", LocalDateTime.of(2026, 3, 2, 8, 0), 5.0, 1)
            ),
            pctData = linkedMapOf(
                "cycle_0" to PCTData(
                    cycleId = "cycle_0",
                    lastInjectionDate = LocalDate.of(2025, 4, 10),
                    longestEster = EsterType.CYPIONATE,
                    protocol = PCTProtocolType.CLOMIPHENE_HCG,
                    pctStartDate = LocalDate.of(2025, 5, 30),
                    pctEndDate = LocalDate.of(2025, 9, 1),
                    bloodworkDates = listOf(LocalDate.of(2025, 7, 11), LocalDate.of(2025, 8, 8)),
                    notes = "LH u opsegu"
                )
            ),
            siteRotationIndex = 3,
            enabledSites = linkedSetOf("glute_l", "glute_r", "vg_l", "quad_r"),
            notificationSettings = NotificationSettings(
                injectionReminder5h = false,
                injectionOverdueIntervalMinutes = 120,
                oralEnabled = false,
                oralOverdueIntervalMinutes = 45,
                pctBloodworkReminder = false
            )
        )
    }

    @Test
    fun `round trip - export, strict import, export again is identical`() {
        val original = populatedPayload()

        val json = BackupPayloadParser.toJson(original, exportedAt = 1_700_000_000_000L).toString(2)
        val restored = BackupPayloadParser.parse(json, strict = true)

        assertEquals(original, restored)
        assertTrue(restored.skipped.isEmpty())
        assertEquals(json, BackupPayloadParser.toJson(restored, exportedAt = 1_700_000_000_000L).toString(2))
        assertFalse(restored.isEmpty)
    }

    @Test
    fun `old backup without newer sections still restores in strict mode`() {
        // Format iz starijih verzija: bez oral_doses, pct_data, enabled_sites,
        // notification_settings i exported_at; supstanca bez novijih polja.
        val old = """
            {
              "substances": [
                {"id": "test_e", "name": "Testosterone Enanthate", "short_name": "Test E", "default_concentration": 250}
              ],
              "cycles": [
                {"id": "c1", "name": "Prvi", "start_date": "2024-02-05", "duration_weeks": 12, "is_active": true,
                 "compounds": [{"substance_id": "test_e", "weekly_dose_mg": 500, "frequency": "e3.5d", "concentration_mg_ml": 250}]}
              ],
              "injections": [
                {"id": "i1", "cycle_id": "c1", "substance_id": "test_e", "date_time": "2024-02-05T10:00:00",
                 "site": "glute_l", "dose_mg": 250, "volume_ml": 1.0, "week": 1, "notes": ""}
              ],
              "site_rotation_index": 1
            }
        """.trimIndent()

        val restored = BackupPayloadParser.parse(old, strict = true)

        assertEquals(1, restored.cycles.size)
        assertEquals(1, restored.injections.size)
        assertEquals(LocalDateTime.of(2024, 2, 5, 10, 0), restored.injections[0].dateTime)
        assertEquals(1, restored.siteRotationIndex)
        assertTrue(restored.oralDoses.isEmpty())
        assertTrue(restored.pctData.isEmpty())
        assertNull(restored.enabledSites)
        assertEquals(NotificationSettings(), restored.notificationSettings)
    }

    @Test
    fun `strict import fails on any invalid record instead of skipping it`() {
        val good = BackupPayloadParser.toJson(populatedPayload(), exportedAt = 1L).toString()

        val badDate = good.replace("2026-03-05T21:00:00", "nije-datum")
        assertThrows(BackupPayloadException::class.java) { BackupPayloadParser.parse(badDate, strict = true) }

        val unknownSite = good.replace("\"quad_r\",\"dose_mg\"", "\"koleno\",\"dose_mg\"")
            .replace("\"site\":\"quad_r\"", "\"site\":\"koleno\"")
        assertThrows(BackupPayloadException::class.java) { BackupPayloadParser.parse(unknownSite, strict = true) }

        // Redosled ključeva u org.json nije garantovan, pa se polje uklanja kroz JSONObject
        val missingId = org.json.JSONObject(good).apply {
            getJSONArray("oral_doses").getJSONObject(0).remove("id")
        }.toString()
        assertThrows(BackupPayloadException::class.java) { BackupPayloadParser.parse(missingId, strict = true) }

        val zeroWeeks = good.replace("\"duration_weeks\":14", "\"duration_weeks\":0")
        assertThrows(BackupPayloadException::class.java) { BackupPayloadParser.parse(zeroWeeks, strict = true) }
    }

    @Test
    fun `lenient local load skips the bad record and reports it`() {
        val good = BackupPayloadParser.toJson(populatedPayload(), exportedAt = 1L).toString()
        val badDate = good.replace("2026-03-05T21:00:00", "nije-datum")

        val loaded = BackupPayloadParser.parse(badDate, strict = false)

        assertEquals(1, loaded.injections.size)
        assertEquals(1, loaded.skipped.size)
    }

    @Test
    fun `strict import rejects files that are not a Ciklus backup`() {
        assertThrows(BackupPayloadException::class.java) { BackupPayloadParser.parse("{\"foo\": 1}", strict = true) }
        assertThrows(BackupPayloadException::class.java) { BackupPayloadParser.parse("[1,2,3]", strict = true) }
        assertThrows(BackupPayloadException::class.java) { BackupPayloadParser.parse("{\"cycles\": 5}", strict = true) }
    }

    @Test
    fun `payload with only default substances counts as empty`() {
        val emptyJson = """{"substances":[{"id":"test_e","name":"T","short_name":"T","default_concentration":250}],
            "cycles":[],"injections":[],"oral_doses":[],"pct_data":[]}"""
        assertTrue(BackupPayloadParser.parse(emptyJson, strict = true).isEmpty)
    }
}
