package com.injekcije.app.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDateTime

/**
 * Unit testovi za rotaciju sajtova za injekcije.
 * Pokriva redosled rotacije, max volumene, SiteHistory i edge case-ove.
 */
class SiteRotationTest {

    // =========================================================================
    // InjectionSite.rotationOrder - redosled rotacije
    // =========================================================================

    @Test
    fun `rotationOrder - sadrzi svih 10 sajtova`() {
        val order = InjectionSite.rotationOrder()
        assertEquals(10, order.size)
    }

    @Test
    fun `rotationOrder - pocinje sa Glute L`() {
        val order = InjectionSite.rotationOrder()
        assertEquals(InjectionSite.GLUTE_L, order.first())
    }

    @Test
    fun `rotationOrder - zavrsava sa Pec R`() {
        val order = InjectionSite.rotationOrder()
        assertEquals(InjectionSite.PEC_R, order.last())
    }

    @Test
    fun `rotationOrder - pravilni parovi L i R`() {
        val order = InjectionSite.rotationOrder()
        // Svaki par je L pa R
        assertEquals(InjectionSite.GLUTE_L, order[0])
        assertEquals(InjectionSite.GLUTE_R, order[1])
        assertEquals(InjectionSite.VG_L, order[2])
        assertEquals(InjectionSite.VG_R, order[3])
        assertEquals(InjectionSite.DELT_L, order[4])
        assertEquals(InjectionSite.DELT_R, order[5])
        assertEquals(InjectionSite.QUAD_L, order[6])
        assertEquals(InjectionSite.QUAD_R, order[7])
        assertEquals(InjectionSite.PEC_L, order[8])
        assertEquals(InjectionSite.PEC_R, order[9])
    }

    // =========================================================================
    // InjectionSite.maxVolumeMl - bezbedni volumeni po sajtu
    // =========================================================================

    @Test
    fun `max volumen - Glute 4ml`() {
        assertEquals(4.0, InjectionSite.GLUTE_L.maxVolumeMl, 0.001)
        assertEquals(4.0, InjectionSite.GLUTE_R.maxVolumeMl, 0.001)
    }

    @Test
    fun `max volumen - VG 4ml`() {
        assertEquals(4.0, InjectionSite.VG_L.maxVolumeMl, 0.001)
        assertEquals(4.0, InjectionSite.VG_R.maxVolumeMl, 0.001)
    }

    @Test
    fun `max volumen - Delt 1_5ml`() {
        assertEquals(1.5, InjectionSite.DELT_L.maxVolumeMl, 0.001)
        assertEquals(1.5, InjectionSite.DELT_R.maxVolumeMl, 0.001)
    }

    @Test
    fun `max volumen - Quad 3ml`() {
        assertEquals(3.0, InjectionSite.QUAD_L.maxVolumeMl, 0.001)
        assertEquals(3.0, InjectionSite.QUAD_R.maxVolumeMl, 0.001)
    }

    @Test
    fun `max volumen - Pec 1_5ml`() {
        assertEquals(1.5, InjectionSite.PEC_L.maxVolumeMl, 0.001)
        assertEquals(1.5, InjectionSite.PEC_R.maxVolumeMl, 0.001)
    }

    // =========================================================================
    // InjectionSite.fromKey - parsiranje kljuca
    // =========================================================================

    @Test
    fun `fromKey - validan kljuc`() {
        assertEquals(InjectionSite.GLUTE_L, InjectionSite.fromKey("glute_l"))
        assertEquals(InjectionSite.VG_R, InjectionSite.fromKey("vg_r"))
        assertEquals(InjectionSite.DELT_L, InjectionSite.fromKey("delt_l"))
        assertEquals(InjectionSite.QUAD_R, InjectionSite.fromKey("quad_r"))
        assertEquals(InjectionSite.PEC_L, InjectionSite.fromKey("pec_l"))
    }

    @Test
    fun `fromKey - nevalidan kljuc daje GLUTE_L kao podrazumevano`() {
        assertEquals(InjectionSite.GLUTE_L, InjectionSite.fromKey("nepostojeci_sajt"))
    }

    @Test
    fun `fromKey - prazan string daje GLUTE_L`() {
        assertEquals(InjectionSite.GLUTE_L, InjectionSite.fromKey(""))
    }

    // =========================================================================
    // InjectionSite.isLeft - strana tela
    // =========================================================================

    @Test
    fun `isLeft - levi sajtovi su oznaceni ispravno`() {
        assertTrue(InjectionSite.GLUTE_L.isLeft)
        assertTrue(InjectionSite.VG_L.isLeft)
        assertTrue(InjectionSite.DELT_L.isLeft)
        assertTrue(InjectionSite.QUAD_L.isLeft)
        assertTrue(InjectionSite.PEC_L.isLeft)
    }

    @Test
    fun `isLeft - desni sajtovi su oznaceni ispravno`() {
        assertFalse(InjectionSite.GLUTE_R.isLeft)
        assertFalse(InjectionSite.VG_R.isLeft)
        assertFalse(InjectionSite.DELT_R.isLeft)
        assertFalse(InjectionSite.QUAD_R.isLeft)
        assertFalse(InjectionSite.PEC_R.isLeft)
    }

    // =========================================================================
    // SiteHistory - dostupnost sajta na osnovu odmora
    // =========================================================================

    @Test
    fun `SiteHistory - sajt bez prethodne upotrebe je dostupan`() {
        val history = SiteHistory(
            site = InjectionSite.GLUTE_L,
            lastUsed = null,
            totalInjections = 0,
            daysSinceLastUse = null
        )
        assertTrue(history.isAvailable)
    }

    @Test
    fun `SiteHistory - sajt koriscen pre 14 dana je dostupan`() {
        val history = SiteHistory(
            site = InjectionSite.GLUTE_L,
            lastUsed = LocalDateTime.now().minusDays(14),
            totalInjections = 5,
            daysSinceLastUse = 14
        )
        assertTrue(history.isAvailable)
    }

    @Test
    fun `SiteHistory - sajt koriscen pre 20 dana je dostupan`() {
        val history = SiteHistory(
            site = InjectionSite.VG_R,
            lastUsed = LocalDateTime.now().minusDays(20),
            totalInjections = 3,
            daysSinceLastUse = 20
        )
        assertTrue(history.isAvailable)
    }

    @Test
    fun `SiteHistory - sajt koriscen pre 13 dana NIJE dostupan`() {
        val history = SiteHistory(
            site = InjectionSite.DELT_L,
            lastUsed = LocalDateTime.now().minusDays(13),
            totalInjections = 2,
            daysSinceLastUse = 13
        )
        assertFalse(history.isAvailable)
    }

    @Test
    fun `SiteHistory - sajt koriscen pre 1 dan NIJE dostupan`() {
        val history = SiteHistory(
            site = InjectionSite.QUAD_R,
            lastUsed = LocalDateTime.now().minusDays(1),
            totalInjections = 10,
            daysSinceLastUse = 1
        )
        assertFalse(history.isAvailable)
    }

    @Test
    fun `SiteHistory - sajt koriscen danas NIJE dostupan`() {
        val history = SiteHistory(
            site = InjectionSite.PEC_L,
            lastUsed = LocalDateTime.now(),
            totalInjections = 1,
            daysSinceLastUse = 0
        )
        assertFalse(history.isAvailable)
    }

    @Test
    fun `SiteHistory - MIN_REST_DAYS je 14`() {
        assertEquals(14, SiteHistory.MIN_REST_DAYS)
    }

    // =========================================================================
    // InjectionSite entries - kompletnost
    // =========================================================================

    @Test
    fun `svi sajtovi - ima tacno 10 sajtova`() {
        assertEquals(10, InjectionSite.entries.size)
    }

    @Test
    fun `svi sajtovi - 5 levih i 5 desnih`() {
        val leftCount = InjectionSite.entries.count { it.isLeft }
        val rightCount = InjectionSite.entries.count { !it.isLeft }
        assertEquals(5, leftCount)
        assertEquals(5, rightCount)
    }

    @Test
    fun `svi sajtovi - imaju jedinstvene kljuceve`() {
        val keys = InjectionSite.entries.map { it.key }
        assertEquals(keys.size, keys.distinct().size)
    }

    @Test
    fun `svi sajtovi - imaju jedinstvene shortName`() {
        val shortNames = InjectionSite.entries.map { it.shortName }
        assertEquals(shortNames.size, shortNames.distinct().size)
    }

    // =========================================================================
    // CycleCompound - aktivnost po nedelji
    // =========================================================================

    @Test
    fun `compound aktivan - ceo ciklus kada endWeek je null`() {
        val compound = CycleCompound(
            substanceId = "test_e",
            weeklyDoseMg = 500.0,
            frequency = InjectionFrequency.E3_5D,
            concentrationMgMl = 250.0,
            startWeek = 1,
            endWeek = null
        )
        assertTrue(compound.isActiveInWeek(1, 12))
        assertTrue(compound.isActiveInWeek(6, 12))
        assertTrue(compound.isActiveInWeek(12, 12))
    }

    @Test
    fun `compound aktivan - samo u definisanom opsegu`() {
        val compound = CycleCompound(
            substanceId = "dianabol",
            weeklyDoseMg = 210.0,
            frequency = InjectionFrequency.ED,
            concentrationMgMl = 1.0,
            startWeek = 1,
            endWeek = 4
        )
        assertTrue(compound.isActiveInWeek(1, 12))
        assertTrue(compound.isActiveInWeek(4, 12))
        assertFalse(compound.isActiveInWeek(5, 12))
        assertFalse(compound.isActiveInWeek(12, 12))
    }

    @Test
    fun `compound aktivan - pocinje u sredini ciklusa`() {
        val compound = CycleCompound(
            substanceId = "test_e",
            weeklyDoseMg = 500.0,
            frequency = InjectionFrequency.E3_5D,
            concentrationMgMl = 250.0,
            startWeek = 5,
            endWeek = 12
        )
        assertFalse(compound.isActiveInWeek(1, 12))
        assertFalse(compound.isActiveInWeek(4, 12))
        assertTrue(compound.isActiveInWeek(5, 12))
        assertTrue(compound.isActiveInWeek(12, 12))
    }

    // =========================================================================
    // CycleCompound - doza po injekciji
    // =========================================================================

    @Test
    fun `compound doza po injekciji - 500mg E3_5D`() {
        val compound = CycleCompound(
            substanceId = "test_e",
            weeklyDoseMg = 500.0,
            frequency = InjectionFrequency.E3_5D,
            concentrationMgMl = 250.0
        )
        assertEquals(250.0, compound.dosePerInjectionMg, 0.001)
    }

    @Test
    fun `compound volumen po injekciji - 500mg E3_5D na 250mgml`() {
        val compound = CycleCompound(
            substanceId = "test_e",
            weeklyDoseMg = 500.0,
            frequency = InjectionFrequency.E3_5D,
            concentrationMgMl = 250.0
        )
        assertEquals(1.0, compound.volumePerInjectionMl, 0.001)
    }

    // =========================================================================
    // CycleCompound - doze po fazama
    // =========================================================================

    @Test
    fun `compound faze - razlicita doza po nedelji`() {
        val compound = CycleCompound(
            substanceId = "test_e",
            weeklyDoseMg = 500.0,
            frequency = InjectionFrequency.E3_5D,
            concentrationMgMl = 250.0,
            phases = listOf(
                DosePhase(startWeek = 1, endWeek = 2, weeklyDoseMg = 750.0),  // front-load
                DosePhase(startWeek = 3, endWeek = 10, weeklyDoseMg = 500.0),
                DosePhase(startWeek = 11, endWeek = 12, weeklyDoseMg = 250.0) // taper
            )
        )
        assertEquals(750.0, compound.getDoseForWeek(1), 0.001)
        assertEquals(750.0, compound.getDoseForWeek(2), 0.001)
        assertEquals(500.0, compound.getDoseForWeek(5), 0.001)
        assertEquals(250.0, compound.getDoseForWeek(12), 0.001)
    }

    @Test
    fun `compound faze - podrazumevana doza ako nedelja nije u fazi`() {
        val compound = CycleCompound(
            substanceId = "test_e",
            weeklyDoseMg = 500.0,
            frequency = InjectionFrequency.E3_5D,
            concentrationMgMl = 250.0,
            phases = listOf(
                DosePhase(startWeek = 1, endWeek = 2, weeklyDoseMg = 750.0)
            )
        )
        // Nedelja 5 nije pokrivena fazom, koristi se podrazumevana doza
        assertEquals(500.0, compound.getDoseForWeek(5), 0.001)
    }

    @Test
    fun `compound getDosePerInjectionForWeek - sa fazom`() {
        val compound = CycleCompound(
            substanceId = "test_e",
            weeklyDoseMg = 500.0,
            frequency = InjectionFrequency.E3_5D,
            concentrationMgMl = 250.0,
            phases = listOf(
                DosePhase(startWeek = 1, endWeek = 2, weeklyDoseMg = 750.0)
            )
        )
        // Nedelja 1: 750 / 2 = 375mg po injekciji
        assertEquals(375.0, compound.getDosePerInjectionForWeek(1), 0.001)
        // Nedelja 5: 500 / 2 = 250mg po injekciji
        assertEquals(250.0, compound.getDosePerInjectionForWeek(5), 0.001)
    }

    @Test
    fun `compound getVolumePerInjectionForWeek - sa fazom`() {
        val compound = CycleCompound(
            substanceId = "test_e",
            weeklyDoseMg = 500.0,
            frequency = InjectionFrequency.E3_5D,
            concentrationMgMl = 250.0,
            phases = listOf(
                DosePhase(startWeek = 1, endWeek = 2, weeklyDoseMg = 750.0)
            )
        )
        // Nedelja 1: 375mg / 250mg/ml = 1.5ml
        assertEquals(1.5, compound.getVolumePerInjectionForWeek(1), 0.001)
        // Nedelja 5: 250mg / 250mg/ml = 1.0ml
        assertEquals(1.0, compound.getVolumePerInjectionForWeek(5), 0.001)
    }

    // =========================================================================
    // InjectionFrequency.fromKey - parsiranje
    // =========================================================================

    @Test
    fun `frekvencija fromKey - validni kljucevi`() {
        assertEquals(InjectionFrequency.ED, InjectionFrequency.fromKey("ed"))
        assertEquals(InjectionFrequency.EOD, InjectionFrequency.fromKey("eod"))
        assertEquals(InjectionFrequency.E3D, InjectionFrequency.fromKey("e3d"))
        assertEquals(InjectionFrequency.E3_5D, InjectionFrequency.fromKey("e3.5d"))
        assertEquals(InjectionFrequency.E5D, InjectionFrequency.fromKey("e5d"))
        assertEquals(InjectionFrequency.WEEKLY, InjectionFrequency.fromKey("weekly"))
        assertEquals(InjectionFrequency.E10D, InjectionFrequency.fromKey("e10d"))
        assertEquals(InjectionFrequency.E14D, InjectionFrequency.fromKey("e14d"))
    }

    @Test
    fun `frekvencija fromKey - nevalidan kljuc daje E3_5D`() {
        assertEquals(InjectionFrequency.E3_5D, InjectionFrequency.fromKey("nepostojeci"))
    }

    // =========================================================================
    // NextInjectionInfo.status - odredjivanje statusa
    // =========================================================================

    @Test
    fun `NextInjectionInfo status - prva injekcija`() {
        val info = NextInjectionInfo(
            substanceId = "test_e",
            substanceName = "Test E",
            scheduledDateTime = LocalDateTime.now(),
            site = InjectionSite.GLUTE_L,
            doseMg = 250.0,
            volumeMl = 1.0,
            hoursRemaining = 48.0,
            isFirstInjection = true
        )
        assertEquals(InjectionStatus.AWAITING_FIRST, info.status)
    }

    @Test
    fun `NextInjectionInfo status - na vreme`() {
        val info = NextInjectionInfo(
            substanceId = "test_e",
            substanceName = "Test E",
            scheduledDateTime = LocalDateTime.now().plusHours(48),
            site = InjectionSite.GLUTE_L,
            doseMg = 250.0,
            volumeMl = 1.0,
            hoursRemaining = 48.0
        )
        assertEquals(InjectionStatus.ON_TRACK, info.status)
    }

    @Test
    fun `NextInjectionInfo status - uskoro (ispod 12h)`() {
        val info = NextInjectionInfo(
            substanceId = "test_e",
            substanceName = "Test E",
            scheduledDateTime = LocalDateTime.now().plusHours(6),
            site = InjectionSite.GLUTE_L,
            doseMg = 250.0,
            volumeMl = 1.0,
            hoursRemaining = 6.0
        )
        assertEquals(InjectionStatus.DUE_SOON, info.status)
    }

    @Test
    fun `NextInjectionInfo status - kasni (negativno vreme)`() {
        val info = NextInjectionInfo(
            substanceId = "test_e",
            substanceName = "Test E",
            scheduledDateTime = LocalDateTime.now().minusHours(3),
            site = InjectionSite.GLUTE_L,
            doseMg = 250.0,
            volumeMl = 1.0,
            hoursRemaining = -3.0
        )
        assertEquals(InjectionStatus.OVERDUE, info.status)
    }

    // =========================================================================
    // OralDosingSchedule - prepoznavanje oralnih steroida (exact match)
    // =========================================================================

    @Test
    fun `isOralSteroid - prepoznaje oralne po exact match`() {
        assertTrue(OralDosingSchedule.isOralSteroid("oxandrolone"))
        assertTrue(OralDosingSchedule.isOralSteroid("methandienone"))
        assertTrue(OralDosingSchedule.isOralSteroid("stanozolol_oral"))
        assertTrue(OralDosingSchedule.isOralSteroid("turinabol"))
        assertTrue(OralDosingSchedule.isOralSteroid("oxymetholone"))
    }

    @Test
    fun `isOralSteroid - brendirani nazivi NISU u exact match setu`() {
        // Exact match dizajn: samo genericka imena iz ORAL_STEROID_IDS
        assertFalse(OralDosingSchedule.isOralSteroid("anavar"))    // treba "oxandrolone"
        assertFalse(OralDosingSchedule.isOralSteroid("dianabol"))  // treba "methandienone"
        assertFalse(OralDosingSchedule.isOralSteroid("anadrol"))   // treba "oxymetholone"
    }

    @Test
    fun `isOralSteroid - ne prepoznaje injekcione`() {
        assertFalse(OralDosingSchedule.isOralSteroid("test_e"))
        assertFalse(OralDosingSchedule.isOralSteroid("test_c"))
        assertFalse(OralDosingSchedule.isOralSteroid("tren_a"))
        assertFalse(OralDosingSchedule.isOralSteroid("deca"))
    }

    @Test
    fun `forSubstance - vraca pravilan raspored`() {
        val dbol = OralDosingSchedule.forSubstance("methandienone")
        assertEquals(3, dbol?.dosesPerDay)
        assertEquals(listOf(8, 14, 20), dbol?.dosingTimes)

        val anavar = OralDosingSchedule.forSubstance("oxandrolone")
        assertEquals(2, anavar?.dosesPerDay)

        val tbol = OralDosingSchedule.forSubstance("turinabol")
        assertEquals(1, tbol?.dosesPerDay)
    }

    @Test
    fun `forSubstance - nepoznata supstanca vraca null`() {
        assertNull(OralDosingSchedule.forSubstance("test_e"))
        assertNull(OralDosingSchedule.forSubstance("tren_a"))
    }

    // =========================================================================
    // SiteHistory - razne kombinacije odmora i korišćenja
    // =========================================================================

    @Test
    fun `SiteHistory - tacno na granici 14 dana je dostupan`() {
        val history = SiteHistory(
            site = InjectionSite.QUAD_L,
            lastUsed = LocalDateTime.now().minusDays(14),
            totalInjections = 8,
            daysSinceLastUse = 14
        )
        assertTrue(history.isAvailable)
    }

    @Test
    fun `SiteHistory - svi sajtovi bez istorije su dostupni`() {
        for (site in InjectionSite.entries) {
            val history = SiteHistory(
                site = site,
                lastUsed = null,
                totalInjections = 0,
                daysSinceLastUse = null
            )
            assertTrue("$site treba da je dostupan bez istorije", history.isAvailable)
        }
    }

    @Test
    fun `SiteHistory - visok broj injekcija ne utice na dostupnost`() {
        val history = SiteHistory(
            site = InjectionSite.VG_L,
            lastUsed = LocalDateTime.now().minusDays(30),
            totalInjections = 100,
            daysSinceLastUse = 30
        )
        assertTrue(history.isAvailable)
    }

    @Test
    fun `SiteHistory - E3_5D protokol sa 4 sajta daje 14 dana odmora`() {
        // E3.5D = 2 injekcije nedeljno
        // Sa 4 sajta u rotaciji: svaki sajt se koristi jednom u 2 nedelje = 14 dana
        // Ovo verifikuje da je 14 dana minimum dovoljan za E3.5D sa 4 sajta
        val rotationSize = 4
        val injectionsPerWeek = 2
        val restDays = (rotationSize.toDouble() / injectionsPerWeek) * 7
        assertTrue(restDays >= SiteHistory.MIN_REST_DAYS)
    }

    @Test
    fun `SiteHistory - ED protokol zahteva vise sajtova`() {
        // ED = 7 injekcija nedeljno
        // Za 14 dana odmora: potrebno minimum 14 sajtova
        // Sa 10 sajtova, odmor je 10/7*7 = 10 dana (manje od 14!)
        val rotationSize = 10
        val injectionsPerWeek = 7
        val restDays = (rotationSize.toDouble() / injectionsPerWeek) * 7
        assertTrue(
            "ED protokol sa 10 sajtova ne dostiže minimum odmor od 14 dana",
            restDays < SiteHistory.MIN_REST_DAYS
        )
    }

    // =========================================================================
    // InjectionSite - tip informacije
    // =========================================================================

    @Test
    fun `svi sajtovi imaju neprazan tip`() {
        for (site in InjectionSite.entries) {
            assertTrue("$site nema tip", site.tip.isNotEmpty())
        }
    }

    @Test
    fun `VG sajt ima NAJBEZBEDNIJE u tipu`() {
        assertTrue(InjectionSite.VG_L.tip.contains("NAJBEZBEDNIJE"))
        assertTrue(InjectionSite.VG_R.tip.contains("NAJBEZBEDNIJE"))
    }

    @Test
    fun `svi sajtovi imaju neprazan displayName`() {
        for (site in InjectionSite.entries) {
            assertTrue("$site nema displayName", site.displayName.isNotEmpty())
        }
    }
}
