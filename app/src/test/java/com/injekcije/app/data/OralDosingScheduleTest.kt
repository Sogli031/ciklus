package com.injekcije.app.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit testovi za OralDosingSchedule.
 * Pokriva prepoznavanje oralnih steroida, rasporede doziranja i edge case-ove.
 */
class OralDosingScheduleTest {

    // =========================================================================
    // isOralSteroid - exact match detekcija oralnih steroida
    // =========================================================================

    @Test
    fun `isOralSteroid - methandienone je oralni`() {
        assertTrue(OralDosingSchedule.isOralSteroid("methandienone"))
    }

    @Test
    fun `isOralSteroid - oxandrolone je oralni`() {
        assertTrue(OralDosingSchedule.isOralSteroid("oxandrolone"))
    }

    @Test
    fun `isOralSteroid - stanozolol_oral je oralni`() {
        assertTrue(OralDosingSchedule.isOralSteroid("stanozolol_oral"))
    }

    @Test
    fun `isOralSteroid - oxymetholone je oralni`() {
        assertTrue(OralDosingSchedule.isOralSteroid("oxymetholone"))
    }

    @Test
    fun `isOralSteroid - turinabol je oralni`() {
        assertTrue(OralDosingSchedule.isOralSteroid("turinabol"))
    }

    @Test
    fun `isOralSteroid - case insensitive`() {
        assertTrue(OralDosingSchedule.isOralSteroid("METHANDIENONE"))
        assertTrue(OralDosingSchedule.isOralSteroid("Oxandrolone"))
        assertTrue(OralDosingSchedule.isOralSteroid("TURINABOL"))
    }

    // =========================================================================
    // isOralSteroid - injectable steroidi NISU oralni
    // =========================================================================

    @Test
    fun `isOralSteroid - test_e NIJE oralni`() {
        assertFalse(OralDosingSchedule.isOralSteroid("test_e"))
    }

    @Test
    fun `isOralSteroid - test_c NIJE oralni`() {
        assertFalse(OralDosingSchedule.isOralSteroid("test_c"))
    }

    @Test
    fun `isOralSteroid - tren_a NIJE oralni`() {
        assertFalse(OralDosingSchedule.isOralSteroid("tren_a"))
    }

    @Test
    fun `isOralSteroid - tren_e NIJE oralni`() {
        assertFalse(OralDosingSchedule.isOralSteroid("tren_e"))
    }

    @Test
    fun `isOralSteroid - deca NIJE oralni`() {
        assertFalse(OralDosingSchedule.isOralSteroid("deca"))
    }

    @Test
    fun `isOralSteroid - npp NIJE oralni`() {
        assertFalse(OralDosingSchedule.isOralSteroid("npp"))
    }

    @Test
    fun `isOralSteroid - eq NIJE oralni`() {
        assertFalse(OralDosingSchedule.isOralSteroid("eq"))
    }

    @Test
    fun `isOralSteroid - masteron NIJE oralni`() {
        assertFalse(OralDosingSchedule.isOralSteroid("masteron"))
    }

    @Test
    fun `isOralSteroid - sustanon NIJE oralni`() {
        assertFalse(OralDosingSchedule.isOralSteroid("sustanon"))
    }

    @Test
    fun `isOralSteroid - hcg NIJE oralni`() {
        assertFalse(OralDosingSchedule.isOralSteroid("hcg"))
    }

    @Test
    fun `isOralSteroid - hgh NIJE oralni`() {
        assertFalse(OralDosingSchedule.isOralSteroid("hgh"))
    }

    // =========================================================================
    // isOralSteroid - edge case nazivi (exact match sprečava lažne pozitive)
    // =========================================================================

    @Test
    fun `isOralSteroid - prazan string NIJE oralni`() {
        assertFalse(OralDosingSchedule.isOralSteroid(""))
    }

    @Test
    fun `isOralSteroid - anavar NIJE u ORAL_STEROID_IDS (exact match)`() {
        // "anavar" nije u setu, samo "oxandrolone" - exact match dizajn
        assertFalse(OralDosingSchedule.isOralSteroid("anavar"))
    }

    @Test
    fun `isOralSteroid - dianabol NIJE u ORAL_STEROID_IDS (exact match)`() {
        // "dianabol" nije u setu, samo "methandienone" - exact match dizajn
        assertFalse(OralDosingSchedule.isOralSteroid("dianabol"))
    }

    @Test
    fun `isOralSteroid - anadrol NIJE u ORAL_STEROID_IDS (exact match)`() {
        // "anadrol" nije u setu, samo "oxymetholone" - exact match dizajn
        assertFalse(OralDosingSchedule.isOralSteroid("anadrol"))
    }

    @Test
    fun `isOralSteroid - stanozolol bez _oral NIJE u setu`() {
        // Samo "stanozolol_oral" je u setu, ne "stanozolol"
        assertFalse(OralDosingSchedule.isOralSteroid("stanozolol"))
    }

    // =========================================================================
    // forSubstance - raspored doziranja za poznate supstance
    // =========================================================================

    @Test
    fun `forSubstance - methandienone 3x dnevno`() {
        val schedule = OralDosingSchedule.forSubstance("methandienone")
        assertNotNull(schedule)
        assertEquals(3, schedule!!.dosesPerDay)
        assertEquals(listOf(8, 14, 20), schedule.dosingTimes)
        assertEquals(5.0, schedule.halfLifeHours, 0.001)
    }

    @Test
    fun `forSubstance - oxandrolone 2x dnevno`() {
        val schedule = OralDosingSchedule.forSubstance("oxandrolone")
        assertNotNull(schedule)
        assertEquals(2, schedule!!.dosesPerDay)
        assertEquals(listOf(8, 20), schedule.dosingTimes)
        assertEquals(9.5, schedule.halfLifeHours, 0.001)
    }

    @Test
    fun `forSubstance - oxymetholone 2x dnevno`() {
        val schedule = OralDosingSchedule.forSubstance("oxymetholone")
        assertNotNull(schedule)
        assertEquals(2, schedule!!.dosesPerDay)
        assertEquals(listOf(8, 20), schedule.dosingTimes)
        assertEquals(8.5, schedule.halfLifeHours, 0.001)
    }

    @Test
    fun `forSubstance - stanozolol_oral 2x dnevno`() {
        val schedule = OralDosingSchedule.forSubstance("stanozolol_oral")
        assertNotNull(schedule)
        assertEquals(2, schedule!!.dosesPerDay)
        assertEquals(listOf(8, 20), schedule.dosingTimes)
        assertEquals(9.0, schedule.halfLifeHours, 0.001)
    }

    @Test
    fun `forSubstance - turinabol 1x dnevno`() {
        val schedule = OralDosingSchedule.forSubstance("turinabol")
        assertNotNull(schedule)
        assertEquals(1, schedule!!.dosesPerDay)
        assertEquals(listOf(8), schedule.dosingTimes)
        assertEquals(16.0, schedule.halfLifeHours, 0.001)
    }

    @Test
    fun `forSubstance - case insensitive`() {
        val schedule = OralDosingSchedule.forSubstance("METHANDIENONE")
        assertNotNull(schedule)
        assertEquals(3, schedule!!.dosesPerDay)
    }

    // =========================================================================
    // forSubstance - nepoznate/injectable supstance vracaju null
    // =========================================================================

    @Test
    fun `forSubstance - test_e vraca null`() {
        assertNull(OralDosingSchedule.forSubstance("test_e"))
    }

    @Test
    fun `forSubstance - tren_a vraca null`() {
        assertNull(OralDosingSchedule.forSubstance("tren_a"))
    }

    @Test
    fun `forSubstance - deca vraca null`() {
        assertNull(OralDosingSchedule.forSubstance("deca"))
    }

    @Test
    fun `forSubstance - prazan string vraca null`() {
        assertNull(OralDosingSchedule.forSubstance(""))
    }

    @Test
    fun `forSubstance - nepostojeci ID vraca null`() {
        assertNull(OralDosingSchedule.forSubstance("potpuno_nepoznato"))
    }

    // =========================================================================
    // getAllowedFrequencies - dozvoljene frekvencije po supstanci
    // =========================================================================

    @Test
    fun `getAllowedFrequencies - oralni steroidi samo ED`() {
        val freqs = OralDosingSchedule.getAllowedFrequencies("methandienone")
        assertEquals(listOf(InjectionFrequency.ED), freqs)
    }

    @Test
    fun `getAllowedFrequencies - kratki esteri ED EOD E3D`() {
        val freqs = OralDosingSchedule.getAllowedFrequencies("test_p")
        assertEquals(
            listOf(InjectionFrequency.ED, InjectionFrequency.EOD, InjectionFrequency.E3D),
            freqs
        )
    }

    @Test
    fun `getAllowedFrequencies - srednji esteri E3_5D E5D WEEKLY`() {
        val freqs = OralDosingSchedule.getAllowedFrequencies("test_e")
        assertEquals(
            listOf(InjectionFrequency.E3_5D, InjectionFrequency.E5D, InjectionFrequency.WEEKLY),
            freqs
        )
    }

    @Test
    fun `getAllowedFrequencies - dugi esteri E3_5D WEEKLY E10D`() {
        val freqs = OralDosingSchedule.getAllowedFrequencies("deca")
        assertEquals(
            listOf(InjectionFrequency.E3_5D, InjectionFrequency.WEEKLY, InjectionFrequency.E10D),
            freqs
        )
    }

    @Test
    fun `getAllowedFrequencies - HCG samo E3D`() {
        val freqs = OralDosingSchedule.getAllowedFrequencies("hcg")
        assertEquals(listOf(InjectionFrequency.E3D), freqs)
    }

    @Test
    fun `getAllowedFrequencies - HGH samo ED`() {
        val freqs = OralDosingSchedule.getAllowedFrequencies("hgh")
        assertEquals(listOf(InjectionFrequency.ED), freqs)
    }

    @Test
    fun `getAllowedFrequencies - nepoznata supstanca daje sve frekvencije`() {
        val freqs = OralDosingSchedule.getAllowedFrequencies("nepoznato_xyz")
        assertEquals(InjectionFrequency.entries.toList(), freqs)
    }

    // =========================================================================
    // OralDosingSchedule predefinisane konstante
    // =========================================================================

    @Test
    fun `predefinisani raspored - METHANDIENONE poluživot 5h`() {
        assertEquals(5.0, OralDosingSchedule.METHANDIENONE.halfLifeHours, 0.001)
        assertEquals(3, OralDosingSchedule.METHANDIENONE.dosesPerDay)
    }

    @Test
    fun `predefinisani raspored - OXANDROLONE poluživot 9_5h`() {
        assertEquals(9.5, OralDosingSchedule.OXANDROLONE.halfLifeHours, 0.001)
        assertEquals(2, OralDosingSchedule.OXANDROLONE.dosesPerDay)
    }

    @Test
    fun `predefinisani raspored - TURINABOL poluživot 16h`() {
        assertEquals(16.0, OralDosingSchedule.TURINABOL.halfLifeHours, 0.001)
        assertEquals(1, OralDosingSchedule.TURINABOL.dosesPerDay)
    }

    @Test
    fun `predefinisani raspored - svi imaju vremena u validnom opsegu 0-23`() {
        val schedules = listOf(
            OralDosingSchedule.METHANDIENONE,
            OralDosingSchedule.OXANDROLONE,
            OralDosingSchedule.OXYMETHOLONE,
            OralDosingSchedule.STANOZOLOL,
            OralDosingSchedule.TURINABOL
        )
        for (schedule in schedules) {
            for (time in schedule.dosingTimes) {
                assertTrue("Vreme $time van opsega za ${schedule.description}", time in 0..23)
            }
            assertEquals(
                "Broj vremena ne odgovara dosesPerDay za ${schedule.description}",
                schedule.dosesPerDay,
                schedule.dosingTimes.size
            )
        }
    }
}
