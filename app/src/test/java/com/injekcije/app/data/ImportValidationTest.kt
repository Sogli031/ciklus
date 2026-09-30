package com.injekcije.app.data

import org.json.JSONArray
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.LocalDateTime

/**
 * Unit testovi za validaciju importovanog JSON-a.
 * Pokriva scenarije sa nevalidnim substanceId-ovima, nedostajućim poljima,
 * pogrešnim tipovima i koruptiranim podacima.
 *
 * Napomena: Ne testira InjectionRepository.importJson() direktno jer zahteva
 * Android Context. Umesto toga, testira fromJson() metode modela koje su
 * ključne za import validaciju.
 */
class ImportValidationTest {

    // =========================================================================
    // CycleCompound sa nevalidnim substanceId-ovima
    // =========================================================================

    @Test
    fun `CycleCompound fromJson - nepostojeci substanceId se čuva`() {
        val json = JSONObject().apply {
            put("substance_id", "nepostojeci_steroid_xyz")
            put("weekly_dose_mg", 500.0)
            put("frequency", "e3.5d")
            put("concentration_mg_ml", 250.0)
        }
        val compound = CycleCompound.fromJson(json)
        assertEquals("nepostojeci_steroid_xyz", compound.substanceId)
    }

    @Test
    fun `CycleCompound fromJson - prazan substanceId`() {
        val json = JSONObject().apply {
            put("substance_id", "")
            put("weekly_dose_mg", 500.0)
            put("frequency", "e3.5d")
            put("concentration_mg_ml", 250.0)
        }
        val compound = CycleCompound.fromJson(json)
        assertEquals("", compound.substanceId)
    }

    @Test
    fun `CycleCompound fromJson - substanceId sa specijalnim karakterima`() {
        val json = JSONObject().apply {
            put("substance_id", "custom/blend 123!@#")
            put("weekly_dose_mg", 300.0)
            put("frequency", "weekly")
            put("concentration_mg_ml", 150.0)
        }
        val compound = CycleCompound.fromJson(json)
        assertEquals("custom/blend 123!@#", compound.substanceId)
    }

    // =========================================================================
    // Cycle sa nevalidnim compound ID-ovima
    // =========================================================================

    @Test
    fun `Cycle fromJson - compounds sa nevalidnim substanceId-ovima`() {
        val json = JSONObject().apply {
            put("id", "cycle_invalid_subs")
            put("name", "Ciklus sa nevalidnim supstancama")
            put("start_date", "2025-05-01")
            put("duration_weeks", 8)
            put("compounds", JSONArray().apply {
                put(JSONObject().apply {
                    put("substance_id", "nepostojeci_1")
                    put("weekly_dose_mg", 500.0)
                    put("frequency", "e3.5d")
                    put("concentration_mg_ml", 250.0)
                })
                put(JSONObject().apply {
                    put("substance_id", "nepostojeci_2")
                    put("weekly_dose_mg", 400.0)
                    put("frequency", "weekly")
                    put("concentration_mg_ml", 200.0)
                })
            })
        }
        val cycle = Cycle.fromJson(json)
        assertEquals(2, cycle.compounds.size)
        assertEquals("nepostojeci_1", cycle.compounds[0].substanceId)
        assertEquals("nepostojeci_2", cycle.compounds[1].substanceId)
    }

    @Test
    fun `Cycle fromJson - mesavina validnih i nevalidnih substanceId-ova`() {
        val json = JSONObject().apply {
            put("id", "cycle_mixed")
            put("name", "Mešovit ciklus")
            put("start_date", "2025-05-01")
            put("duration_weeks", 12)
            put("compounds", JSONArray().apply {
                put(JSONObject().apply {
                    put("substance_id", "test_e")
                    put("weekly_dose_mg", 500.0)
                    put("frequency", "e3.5d")
                    put("concentration_mg_ml", 250.0)
                })
                put(JSONObject().apply {
                    put("substance_id", "mystery_compound")
                    put("weekly_dose_mg", 200.0)
                    put("frequency", "weekly")
                    put("concentration_mg_ml", 100.0)
                })
            })
        }
        val cycle = Cycle.fromJson(json)
        assertEquals(2, cycle.compounds.size)
        assertEquals("test_e", cycle.compounds[0].substanceId)
        assertEquals("mystery_compound", cycle.compounds[1].substanceId)
    }

    // =========================================================================
    // InjectionRecord sa nevalidnim substanceId
    // =========================================================================

    @Test
    fun `InjectionRecord fromJson - nepostojeci substanceId`() {
        val json = JSONObject().apply {
            put("id", "inj_invalid")
            put("cycle_id", "cycle_abc")
            put("substance_id", "nepostojeci_steroid")
            put("date_time", "2025-06-01T10:00:00")
            put("site", "glute_l")
            put("dose_mg", 250.0)
            put("volume_ml", 1.0)
            put("week", 1)
        }
        val record = InjectionRecord.fromJson(json)
        assertEquals("nepostojeci_steroid", record.substanceId)
    }

    @Test
    fun `InjectionRecord fromJson - nepostojeci cycleId`() {
        val json = JSONObject().apply {
            put("id", "inj_orphan")
            put("cycle_id", "nepostojeci_ciklus_xyz")
            put("substance_id", "test_e")
            put("date_time", "2025-06-01T10:00:00")
            put("site", "vg_r")
            put("dose_mg", 250.0)
            put("volume_ml", 1.0)
            put("week", 1)
        }
        val record = InjectionRecord.fromJson(json)
        assertEquals("nepostojeci_ciklus_xyz", record.cycleId)
    }

    // =========================================================================
    // CycleCompound sa nevalidnom frekvencijom
    // =========================================================================

    @Test
    fun `CycleCompound fromJson - nevalidna frekvencija daje E3_5D`() {
        val json = JSONObject().apply {
            put("substance_id", "test_e")
            put("weekly_dose_mg", 500.0)
            put("frequency", "nepostojeci_kljuc")
            put("concentration_mg_ml", 250.0)
        }
        val compound = CycleCompound.fromJson(json)
        assertEquals(InjectionFrequency.E3_5D, compound.frequency)
    }

    @Test
    fun `CycleCompound fromJson - prazna frekvencija daje E3_5D`() {
        val json = JSONObject().apply {
            put("substance_id", "test_e")
            put("weekly_dose_mg", 500.0)
            put("frequency", "")
            put("concentration_mg_ml", 250.0)
        }
        val compound = CycleCompound.fromJson(json)
        assertEquals(InjectionFrequency.E3_5D, compound.frequency)
    }

    // =========================================================================
    // InjectionRecord sa nevalidnim site ključem
    // =========================================================================

    @Test
    fun `InjectionRecord fromJson - nevalidan site daje GLUTE_L`() {
        val json = JSONObject().apply {
            put("substance_id", "test_e")
            put("site", "nepostojeci_sajt")
            put("dose_mg", 250.0)
        }
        val record = InjectionRecord.fromJson(json)
        assertEquals(InjectionSite.GLUTE_L, record.site)
    }

    // =========================================================================
    // PCTData sa nevalidnim esterom i protokolom
    // =========================================================================

    @Test
    fun `PCTData fromJson - nevalidan ester daje ENANTHATE`() {
        val json = JSONObject().apply {
            put("cycle_id", "cycle_abc")
            put("last_injection_date", "2025-06-01")
            put("longest_ester", "nepostojeci_ester")
        }
        val pctData = PCTData.fromJson(json)
        assertEquals(EsterType.ENANTHATE, pctData.longestEster)
    }

    @Test
    fun `PCTData fromJson - nevalidan protokol daje CLOMIPHENE_ONLY`() {
        val json = JSONObject().apply {
            put("cycle_id", "cycle_abc")
            put("last_injection_date", "2025-06-01")
            put("protocol", "nepostojeci_protokol")
        }
        val pctData = PCTData.fromJson(json)
        assertEquals(PCTProtocolType.CLOMIPHENE_ONLY, pctData.protocol)
    }

    // =========================================================================
    // Substance sa nevalidnim poljima
    // =========================================================================

    @Test
    fun `Substance fromJson - nevalidna recommended_frequency daje E3_5D`() {
        val json = JSONObject().apply {
            put("id", "custom")
            put("name", "Custom")
            put("short_name", "C")
            put("default_concentration", 250.0)
            put("recommended_frequency", "nepostojeci")
        }
        val substance = Substance.fromJson(json)
        assertEquals(InjectionFrequency.E3_5D, substance.recommendedFrequency)
    }

    @Test
    fun `Substance fromJson - nevalidan ester_type daje ENANTHATE`() {
        val json = JSONObject().apply {
            put("id", "custom")
            put("name", "Custom")
            put("short_name", "C")
            put("default_concentration", 250.0)
            put("ester_type", "nepostojeci_ester")
        }
        val substance = Substance.fromJson(json)
        assertEquals(EsterType.ENANTHATE, substance.esterType)
    }

    // =========================================================================
    // Pogrešan format datuma
    // =========================================================================

    @Test
    fun `InjectionRecord fromJson - prazan date_time koristi fallback`() {
        val json = JSONObject().apply {
            put("id", "inj_no_date")
            put("cycle_id", "cycle_abc")
            put("substance_id", "test_e")
            put("date_time", "")
            put("site", "glute_l")
            put("dose_mg", 250.0)
        }
        val record = InjectionRecord.fromJson(json)
        // Prazan string koristi LocalDateTime.now() kao fallback
        assertNotNull(record.dateTime)
    }

    @Test
    fun `PCTData fromJson - prazan last_injection_date koristi fallback`() {
        val json = JSONObject().apply {
            put("cycle_id", "cycle_abc")
            put("last_injection_date", "")
        }
        val pctData = PCTData.fromJson(json)
        // Prazan string koristi LocalDate.now() kao fallback
        assertNotNull(pctData.lastInjectionDate)
    }

    // =========================================================================
    // Kompletan import JSON sa svim sekcijama
    // =========================================================================

    @Test
    fun `kompletna parsirana struktura - svi tipovi podataka`() {
        // Simulira kompletan JSON koji bi Repository.importJson() primio
        val substances = JSONArray().apply {
            put(JSONObject().apply {
                put("id", "test_e")
                put("name", "Testosteron Enantat")
                put("short_name", "Test E")
                put("default_concentration", 250.0)
            })
        }

        val cycles = JSONArray().apply {
            put(JSONObject().apply {
                put("id", "cycle_1")
                put("name", "Osnovni ciklus")
                put("start_date", "2025-03-01")
                put("duration_weeks", 12)
                put("compounds", JSONArray().apply {
                    put(JSONObject().apply {
                        put("substance_id", "test_e")
                        put("weekly_dose_mg", 500.0)
                        put("frequency", "e3.5d")
                        put("concentration_mg_ml", 250.0)
                    })
                })
            })
        }

        val injections = JSONArray().apply {
            put(JSONObject().apply {
                put("id", "inj_1")
                put("cycle_id", "cycle_1")
                put("substance_id", "test_e")
                put("date_time", "2025-03-01T10:00:00")
                put("site", "glute_l")
                put("dose_mg", 250.0)
                put("volume_ml", 1.0)
                put("week", 1)
            })
        }

        val oralDoses = JSONArray().apply {
            put(JSONObject().apply {
                put("id", "oral_1")
                put("cycle_id", "cycle_1")
                put("substance_id", "methandienone")
                put("date_time", "2025-03-01T08:00:00")
                put("dose_mg", 10.0)
                put("week", 1)
            })
        }

        // Parsiraj svaku sekciju
        val parsedSubstance = Substance.fromJson(substances.getJSONObject(0))
        assertEquals("test_e", parsedSubstance.id)

        val parsedCycle = Cycle.fromJson(cycles.getJSONObject(0))
        assertEquals("cycle_1", parsedCycle.id)
        assertEquals(1, parsedCycle.compounds.size)

        val parsedInjection = InjectionRecord.fromJson(injections.getJSONObject(0))
        assertEquals("inj_1", parsedInjection.id)
        assertEquals("cycle_1", parsedInjection.cycleId)

        val parsedOral = OralDoseRecord.fromJson(oralDoses.getJSONObject(0))
        assertEquals("oral_1", parsedOral.id)
        assertEquals("methandienone", parsedOral.substanceId)
    }

    @Test
    fun `import JSON sa nevalidnim elementima - ostali elementi se parsiraju`() {
        // Simulira niz gde su neki elementi nevalidni
        val compounds = JSONArray().apply {
            // Validan compound
            put(JSONObject().apply {
                put("substance_id", "test_e")
                put("weekly_dose_mg", 500.0)
                put("frequency", "e3.5d")
                put("concentration_mg_ml", 250.0)
            })
            // Compound sa nevalidnom frekvencijom - parsira se sa default
            put(JSONObject().apply {
                put("substance_id", "deca")
                put("weekly_dose_mg", 400.0)
                put("frequency", "nevalidno")
                put("concentration_mg_ml", 200.0)
            })
        }

        val parsed = (0 until compounds.length()).map {
            CycleCompound.fromJson(compounds.getJSONObject(it))
        }

        assertEquals(2, parsed.size)
        assertEquals("test_e", parsed[0].substanceId)
        assertEquals(InjectionFrequency.E3_5D, parsed[0].frequency)
        assertEquals("deca", parsed[1].substanceId)
        // Nevalidna frekvencija pada na default E3_5D
        assertEquals(InjectionFrequency.E3_5D, parsed[1].frequency)
    }

    // =========================================================================
    // Substance isOral i detekcija sa nevalidnim ID-ovima
    // =========================================================================

    @Test
    fun `Substance isOral - nepostojeci ID NIJE oralni`() {
        val substance = Substance(
            id = "nepostojeci_xyz",
            name = "Nepoznato",
            shortName = "N",
            defaultConcentration = 100.0,
            category = "AAS"
        )
        assertTrue(!substance.isOral)
    }

    @Test
    fun `Substance isOral - methandienone JE oralni`() {
        val substance = Substance(
            id = "methandienone",
            name = "Dianabol",
            shortName = "Dbol",
            defaultConcentration = 1.0,
            category = "Oralni"
        )
        assertTrue(substance.isOral)
    }

    @Test
    fun `Substance recommendedWeeklyDose - oralni konvertuje dnevnu u nedeljnu`() {
        val substance = Substance(
            id = "oxandrolone",
            name = "Anavar",
            shortName = "Var",
            defaultConcentration = 1.0,
            category = "Oralni",
            recommendedDose = 20.0  // 20mg dnevno
        )
        // Oralni: recommendedDose je dnevna, weeklyDose = 20 * 7 = 140
        assertEquals(140.0, substance.recommendedWeeklyDose, 0.001)
    }

    @Test
    fun `Substance recommendedDailyDose - injectable konvertuje nedeljnu u dnevnu`() {
        val substance = Substance(
            id = "test_e",
            name = "Testosteron Enantat",
            shortName = "Test E",
            defaultConcentration = 250.0,
            category = "AAS",
            recommendedDose = 500.0  // 500mg nedeljno
        )
        // Injectable: recommendedDose je nedeljna, dailyDose = 500 / 7
        assertEquals(500.0 / 7, substance.recommendedDailyDose, 0.001)
    }

    // =========================================================================
    // EsterType.fromSubstance - eksplicitni vs. parsirani ester
    // =========================================================================

    @Test
    fun `EsterType fromSubstance - koristi eksplicitni esterType ako postoji`() {
        val substance = Substance(
            id = "custom_blend",
            name = "Custom Blend",
            shortName = "CB",
            defaultConcentration = 250.0,
            category = "AAS",
            esterType = EsterType.CYPIONATE
        )
        assertEquals(EsterType.CYPIONATE, EsterType.fromSubstance(substance))
    }

    @Test
    fun `EsterType fromSubstance - parsira iz ID-ja ako esterType je null`() {
        val substance = Substance(
            id = "test_e",
            name = "Testosteron Enantat",
            shortName = "Test E",
            defaultConcentration = 250.0,
            category = "AAS",
            esterType = null
        )
        assertEquals(EsterType.ENANTHATE, EsterType.fromSubstance(substance))
    }
}
