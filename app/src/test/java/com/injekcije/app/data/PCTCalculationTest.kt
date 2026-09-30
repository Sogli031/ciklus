package com.injekcije.app.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

/**
 * Unit testovi za PCT (Post-Cycle Therapy) kalkulacije.
 * Pokriva izracunavanje datuma pocetka, nivo supresije, protokol preporuke.
 */
class PCTCalculationTest {

    // =========================================================================
    // EsterType.fromSubstanceId - detekcija estera po supstanci
    // =========================================================================

    @Test
    fun `ester detekcija - testosteron enantat`() {
        val ester = EsterType.fromSubstanceId("test_e")
        assertEquals(EsterType.ENANTHATE, ester)
    }

    @Test
    fun `ester detekcija - testosteron cipionat`() {
        val ester = EsterType.fromSubstanceId("test_c")
        assertEquals(EsterType.CYPIONATE, ester)
    }

    @Test
    fun `ester detekcija - testosteron propionat`() {
        val ester = EsterType.fromSubstanceId("test_p")
        assertEquals(EsterType.PROPIONATE, ester)
    }

    @Test
    fun `ester detekcija - trenbolon acetat`() {
        val ester = EsterType.fromSubstanceId("tren_a")
        assertEquals(EsterType.ACETATE, ester)
    }

    @Test
    fun `ester detekcija - trenbolon enantat`() {
        val ester = EsterType.fromSubstanceId("tren_e")
        assertEquals(EsterType.ENANTHATE, ester)
    }

    @Test
    fun `ester detekcija - deca durabolin`() {
        val ester = EsterType.fromSubstanceId("deca")
        assertEquals(EsterType.DECANOATE, ester)
    }

    @Test
    fun `ester detekcija - NPP fenilpropionat`() {
        val ester = EsterType.fromSubstanceId("npp")
        assertEquals(EsterType.PHENYLPROPIONATE, ester)
    }

    @Test
    fun `ester detekcija - boldenon undecilenat`() {
        val ester = EsterType.fromSubstanceId("eq")
        assertEquals(EsterType.UNDECYLENATE, ester)
    }

    @Test
    fun `ester detekcija - sustanon kao enantat`() {
        val ester = EsterType.fromSubstanceId("sustanon")
        assertEquals(EsterType.ENANTHATE, ester)
    }

    @Test
    fun `ester detekcija - oralni anavar`() {
        val ester = EsterType.fromSubstanceId("oxandrolone")
        assertEquals(EsterType.ORAL, ester)
    }

    @Test
    fun `ester detekcija - oralni dianabol`() {
        val ester = EsterType.fromSubstanceId("dianabol")
        assertEquals(EsterType.ORAL, ester)
    }

    @Test
    fun `ester detekcija - oralni turinabol`() {
        val ester = EsterType.fromSubstanceId("turinabol")
        assertEquals(EsterType.ORAL, ester)
    }

    @Test
    fun `ester detekcija - nepoznata supstanca daje enantat kao podrazumevano`() {
        val ester = EsterType.fromSubstanceId("nepoznato_nesto")
        assertEquals(EsterType.ENANTHATE, ester)
    }

    // =========================================================================
    // EsterType.pctWaitDays - dani cekanja pre PCT-a
    // =========================================================================

    @Test
    fun `PCT cekanje - propionat 10 dana`() {
        assertEquals(10, EsterType.PROPIONATE.pctWaitDays)
    }

    @Test
    fun `PCT cekanje - enantat 45 dana`() {
        assertEquals(45, EsterType.ENANTHATE.pctWaitDays)
    }

    @Test
    fun `PCT cekanje - cipionat 50 dana`() {
        assertEquals(50, EsterType.CYPIONATE.pctWaitDays)
    }

    @Test
    fun `PCT cekanje - dekanoat 55 dana`() {
        assertEquals(55, EsterType.DECANOATE.pctWaitDays)
    }

    @Test
    fun `PCT cekanje - undecilenat 70 dana`() {
        assertEquals(70, EsterType.UNDECYLENATE.pctWaitDays)
    }

    @Test
    fun `PCT cekanje - oralni 5 dana`() {
        assertEquals(5, EsterType.ORAL.pctWaitDays)
    }

    // =========================================================================
    // PCTData.recommendedStartDate - izracunavanje datuma pocetka PCT-a
    // =========================================================================

    @Test
    fun `preporuceni datum PCT - enantat ester`() {
        val lastInjection = LocalDate.of(2025, 1, 1)
        val pctData = PCTData(
            cycleId = "test_cycle",
            lastInjectionDate = lastInjection,
            longestEster = EsterType.ENANTHATE
        )
        val expected = lastInjection.plusDays(45)
        assertEquals(expected, pctData.recommendedStartDate)
    }

    @Test
    fun `preporuceni datum PCT - cipionat ester`() {
        val lastInjection = LocalDate.of(2025, 3, 15)
        val pctData = PCTData(
            cycleId = "test_cycle",
            lastInjectionDate = lastInjection,
            longestEster = EsterType.CYPIONATE
        )
        val expected = lastInjection.plusDays(50)
        assertEquals(expected, pctData.recommendedStartDate)
    }

    @Test
    fun `preporuceni datum PCT - oralni ester kratko cekanje`() {
        val lastInjection = LocalDate.of(2025, 6, 1)
        val pctData = PCTData(
            cycleId = "test_cycle",
            lastInjectionDate = lastInjection,
            longestEster = EsterType.ORAL
        )
        val expected = lastInjection.plusDays(5)
        assertEquals(expected, pctData.recommendedStartDate)
    }

    @Test
    fun `preporuceni datum PCT - undecilenat dugacko cekanje`() {
        val lastInjection = LocalDate.of(2025, 1, 1)
        val pctData = PCTData(
            cycleId = "test_cycle",
            lastInjectionDate = lastInjection,
            longestEster = EsterType.UNDECYLENATE
        )
        val expected = lastInjection.plusDays(70)
        assertEquals(expected, pctData.recommendedStartDate)
    }

    // =========================================================================
    // PCTData.daysUntilPCT - dani do pocetka PCT-a
    // =========================================================================

    @Test
    fun `dani do PCT - buducnost`() {
        val lastInjection = LocalDate.of(2025, 1, 1)
        val today = LocalDate.of(2025, 1, 20)
        val pctData = PCTData(
            cycleId = "test_cycle",
            lastInjectionDate = lastInjection,
            longestEster = EsterType.ENANTHATE
        )
        // recommendedStartDate = Jan 1 + 45 = Feb 15
        // daysUntil = days between Jan 20 and Feb 15 = 26
        assertEquals(26, pctData.daysUntilPCT(today))
    }

    @Test
    fun `dani do PCT - vec prosao datum`() {
        val lastInjection = LocalDate.of(2025, 1, 1)
        val today = LocalDate.of(2025, 3, 1)
        val pctData = PCTData(
            cycleId = "test_cycle",
            lastInjectionDate = lastInjection,
            longestEster = EsterType.ENANTHATE
        )
        // recommendedStartDate = Feb 15, today = Mar 1
        // daysUntil = -14 (negativno, znaci da treba da je poceo)
        assertTrue(pctData.daysUntilPCT(today) < 0)
    }

    // =========================================================================
    // PCTData.currentPhase - odredjivanje trenutne faze
    // =========================================================================

    @Test
    fun `PCT faza - WAITING kada ceka na ciscenje`() {
        val lastInjection = LocalDate.of(2025, 1, 1)
        val today = LocalDate.of(2025, 1, 10)
        val pctData = PCTData(
            cycleId = "test_cycle",
            lastInjectionDate = lastInjection,
            longestEster = EsterType.ENANTHATE
        )
        assertEquals(PCTPhase.WAITING, pctData.currentPhase(today))
    }

    @Test
    fun `PCT faza - READY_TO_START kada je prosao datum cekanja`() {
        val lastInjection = LocalDate.of(2025, 1, 1)
        val today = LocalDate.of(2025, 2, 20) // posle 45 dana
        val pctData = PCTData(
            cycleId = "test_cycle",
            lastInjectionDate = lastInjection,
            longestEster = EsterType.ENANTHATE
        )
        assertEquals(PCTPhase.READY_TO_START, pctData.currentPhase(today))
    }

    @Test
    fun `PCT faza - READY_TO_START na tacan datum pocetka`() {
        val lastInjection = LocalDate.of(2025, 1, 1)
        val recommendedStart = lastInjection.plusDays(45)
        val pctData = PCTData(
            cycleId = "test_cycle",
            lastInjectionDate = lastInjection,
            longestEster = EsterType.ENANTHATE
        )
        assertEquals(PCTPhase.READY_TO_START, pctData.currentPhase(recommendedStart))
    }

    @Test
    fun `PCT faza - ACTIVE kada je PCT zapocet`() {
        val lastInjection = LocalDate.of(2025, 1, 1)
        val pctStart = LocalDate.of(2025, 2, 16)
        val today = LocalDate.of(2025, 3, 1)
        val pctData = PCTData(
            cycleId = "test_cycle",
            lastInjectionDate = lastInjection,
            longestEster = EsterType.ENANTHATE,
            pctStartDate = pctStart
        )
        assertEquals(PCTPhase.ACTIVE, pctData.currentPhase(today))
    }

    @Test
    fun `PCT faza - COMPLETED kada je PCT zavrsen`() {
        val lastInjection = LocalDate.of(2025, 1, 1)
        val pctStart = LocalDate.of(2025, 2, 16)
        val pctEnd = LocalDate.of(2025, 6, 16)
        val today = LocalDate.of(2025, 7, 1)
        val pctData = PCTData(
            cycleId = "test_cycle",
            lastInjectionDate = lastInjection,
            longestEster = EsterType.ENANTHATE,
            pctStartDate = pctStart,
            pctEndDate = pctEnd
        )
        assertEquals(PCTPhase.COMPLETED, pctData.currentPhase(today))
    }

    // =========================================================================
    // PCTData.currentStatus - detaljniji status za UI
    // =========================================================================

    @Test
    fun `PCT status - WAITING daleko od datuma`() {
        val lastInjection = LocalDate.of(2025, 1, 1)
        val today = LocalDate.of(2025, 1, 10)
        val pctData = PCTData(
            cycleId = "test_cycle",
            lastInjectionDate = lastInjection,
            longestEster = EsterType.ENANTHATE
        )
        assertEquals(PCTStatus.WAITING, pctData.currentStatus(today))
    }

    @Test
    fun `PCT status - START_SOON 3 dana pre pocetka`() {
        val lastInjection = LocalDate.of(2025, 1, 1)
        // recommendedStart = Jan 1 + 45 = Feb 15
        val today = LocalDate.of(2025, 2, 13) // 2 dana pre
        val pctData = PCTData(
            cycleId = "test_cycle",
            lastInjectionDate = lastInjection,
            longestEster = EsterType.ENANTHATE
        )
        assertEquals(PCTStatus.START_SOON, pctData.currentStatus(today))
    }

    @Test
    fun `PCT status - START_NOW na datum ili posle`() {
        val lastInjection = LocalDate.of(2025, 1, 1)
        val today = LocalDate.of(2025, 2, 20)
        val pctData = PCTData(
            cycleId = "test_cycle",
            lastInjectionDate = lastInjection,
            longestEster = EsterType.ENANTHATE
        )
        assertEquals(PCTStatus.START_NOW, pctData.currentStatus(today))
    }

    @Test
    fun `PCT status - IN_PROGRESS dok je aktivan`() {
        val lastInjection = LocalDate.of(2025, 1, 1)
        val pctStart = LocalDate.of(2025, 2, 16)
        val today = LocalDate.of(2025, 3, 1) // 13 dana od pocetka
        val pctData = PCTData(
            cycleId = "test_cycle",
            lastInjectionDate = lastInjection,
            longestEster = EsterType.ENANTHATE,
            pctStartDate = pctStart
        )
        assertEquals(PCTStatus.IN_PROGRESS, pctData.currentStatus(today))
    }

    @Test
    fun `PCT status - BLOODWORK_DUE posle 6 nedelja bez krvne slike`() {
        val lastInjection = LocalDate.of(2025, 1, 1)
        val pctStart = LocalDate.of(2025, 2, 16)
        // 42+ dana od pocetka PCT, bez krvne slike
        val today = pctStart.plusDays(50)
        val pctData = PCTData(
            cycleId = "test_cycle",
            lastInjectionDate = lastInjection,
            longestEster = EsterType.ENANTHATE,
            pctStartDate = pctStart
        )
        assertEquals(PCTStatus.BLOODWORK_DUE, pctData.currentStatus(today))
    }

    @Test
    fun `PCT status - IN_PROGRESS ako je krvna slika uradjena skoro`() {
        val lastInjection = LocalDate.of(2025, 1, 1)
        val pctStart = LocalDate.of(2025, 2, 16)
        val bloodworkDate = pctStart.plusDays(43)
        val today = pctStart.plusDays(50) // 7 dana posle krvne slike
        val pctData = PCTData(
            cycleId = "test_cycle",
            lastInjectionDate = lastInjection,
            longestEster = EsterType.ENANTHATE,
            pctStartDate = pctStart,
            bloodworkDates = listOf(bloodworkDate)
        )
        assertEquals(PCTStatus.IN_PROGRESS, pctData.currentStatus(today))
    }

    @Test
    fun `PCT status - COMPLETED`() {
        val pctData = PCTData(
            cycleId = "test_cycle",
            lastInjectionDate = LocalDate.of(2025, 1, 1),
            longestEster = EsterType.ENANTHATE,
            pctStartDate = LocalDate.of(2025, 2, 16),
            pctEndDate = LocalDate.of(2025, 6, 16)
        )
        val today = LocalDate.of(2025, 7, 1)
        assertEquals(PCTStatus.COMPLETED, pctData.currentStatus(today))
    }

    // =========================================================================
    // CompoundType.fromSubstanceId - detekcija tipa komponente
    // =========================================================================

    @Test
    fun `tip komponente - testosteron enantat`() {
        assertEquals(CompoundType.TESTOSTERONE, CompoundType.fromSubstanceId("test_e"))
    }

    @Test
    fun `tip komponente - testosteron cipionat`() {
        assertEquals(CompoundType.TESTOSTERONE, CompoundType.fromSubstanceId("test_c"))
    }

    @Test
    fun `tip komponente - sustanon`() {
        assertEquals(CompoundType.TESTOSTERONE, CompoundType.fromSubstanceId("sustanon"))
    }

    @Test
    fun `tip komponente - nandrolon deca`() {
        assertEquals(CompoundType.NANDROLONE, CompoundType.fromSubstanceId("deca"))
    }

    @Test
    fun `tip komponente - NPP`() {
        assertEquals(CompoundType.NANDROLONE, CompoundType.fromSubstanceId("npp"))
    }

    @Test
    fun `tip komponente - trenbolon acetat`() {
        assertEquals(CompoundType.TRENBOLONE, CompoundType.fromSubstanceId("tren_a"))
    }

    @Test
    fun `tip komponente - trenbolon enantat`() {
        assertEquals(CompoundType.TRENBOLONE, CompoundType.fromSubstanceId("tren_e"))
    }

    @Test
    fun `tip komponente - boldenon eq`() {
        assertEquals(CompoundType.BOLDENONE, CompoundType.fromSubstanceId("eq"))
    }

    @Test
    fun `tip komponente - masteron`() {
        assertEquals(CompoundType.MASTERON, CompoundType.fromSubstanceId("masteron"))
    }

    @Test
    fun `tip komponente - oralni anavar`() {
        assertEquals(CompoundType.ORAL_17AA, CompoundType.fromSubstanceId("oxandrolone"))
    }

    @Test
    fun `tip komponente - oralni dianabol`() {
        assertEquals(CompoundType.ORAL_17AA, CompoundType.fromSubstanceId("dianabol"))
    }

    @Test
    fun `tip komponente - oralni turinabol`() {
        assertEquals(CompoundType.ORAL_17AA, CompoundType.fromSubstanceId("turinabol"))
    }

    @Test
    fun `tip komponente - nepoznato`() {
        assertEquals(CompoundType.UNKNOWN, CompoundType.fromSubstanceId("random_xyz"))
    }

    // =========================================================================
    // SuppressionLevel po CompoundType
    // =========================================================================

    @Test
    fun `supresija - testosteron je umerena`() {
        assertEquals(SuppressionLevel.MODERATE, CompoundType.TESTOSTERONE.suppressionLevel)
    }

    @Test
    fun `supresija - nandrolon je jaka`() {
        assertEquals(SuppressionLevel.SEVERE, CompoundType.NANDROLONE.suppressionLevel)
    }

    @Test
    fun `supresija - trenbolon je ekstremna`() {
        assertEquals(SuppressionLevel.EXTREME, CompoundType.TRENBOLONE.suppressionLevel)
    }

    @Test
    fun `supresija - masteron je blaga`() {
        assertEquals(SuppressionLevel.MILD, CompoundType.MASTERON.suppressionLevel)
    }

    @Test
    fun `supresija - boldenon je umerena`() {
        assertEquals(SuppressionLevel.MODERATE, CompoundType.BOLDENONE.suppressionLevel)
    }

    // =========================================================================
    // CompoundType.requiresHCG
    // =========================================================================

    @Test
    fun `hCG potreban - trenbolon zahteva`() {
        assertTrue(CompoundType.TRENBOLONE.requiresHCG)
    }

    @Test
    fun `hCG potreban - nandrolon zahteva`() {
        assertTrue(CompoundType.NANDROLONE.requiresHCG)
    }

    @Test
    fun `hCG potreban - testosteron ne zahteva`() {
        assertFalse(CompoundType.TESTOSTERONE.requiresHCG)
    }

    @Test
    fun `hCG potreban - masteron ne zahteva`() {
        assertFalse(CompoundType.MASTERON.requiresHCG)
    }

    // =========================================================================
    // CyclePCTAdvice.fromSubstanceIds - savet za PCT po ciklusu
    // =========================================================================

    @Test
    fun `PCT savet - samo testosteron`() {
        val advice = CyclePCTAdvice.fromSubstanceIds(listOf("test_e"))
        assertEquals(SuppressionLevel.MODERATE, advice.worstSuppression)
        assertFalse(advice.requiresHCG)
        assertEquals(PCTProtocolType.CLOMIPHENE_ONLY, advice.recommendedProtocol)
    }

    @Test
    fun `PCT savet - testosteron i nandrolon`() {
        val advice = CyclePCTAdvice.fromSubstanceIds(listOf("test_e", "deca"))
        assertEquals(SuppressionLevel.SEVERE, advice.worstSuppression)
        assertTrue(advice.requiresHCG)
        assertEquals(PCTProtocolType.CLOMIPHENE_HCG, advice.recommendedProtocol)
    }

    @Test
    fun `PCT savet - testosteron i trenbolon`() {
        val advice = CyclePCTAdvice.fromSubstanceIds(listOf("test_e", "tren_a"))
        assertEquals(SuppressionLevel.EXTREME, advice.worstSuppression)
        assertTrue(advice.requiresHCG)
        assertEquals(PCTProtocolType.CLOMIPHENE_HCG, advice.recommendedProtocol)
    }

    @Test
    fun `PCT savet - trenbolon i nandrolon upozorenje`() {
        val advice = CyclePCTAdvice.fromSubstanceIds(listOf("tren_a", "deca"))
        assertTrue(advice.warnings.any { it.contains("19-NOR") })
    }

    @Test
    fun `PCT savet - trenbolon upozorenje`() {
        val advice = CyclePCTAdvice.fromSubstanceIds(listOf("test_e", "tren_e"))
        assertTrue(advice.warnings.any { it.contains("TRENBOLON") })
    }

    @Test
    fun `PCT savet - samo oralni kratka PCT`() {
        val advice = CyclePCTAdvice.fromSubstanceIds(listOf("oxandrolone"))
        assertEquals(CompoundType.ORAL_17AA, advice.compounds.first())
        assertEquals(SuppressionLevel.MODERATE, advice.worstSuppression)
        assertFalse(advice.requiresHCG)
    }

    @Test
    fun `PCT savet - najduzi PCT se koristi`() {
        // Trenbolon ima 26 nedelja, testosteron 16
        val advice = CyclePCTAdvice.fromSubstanceIds(listOf("test_e", "tren_a"))
        assertEquals(26, advice.recommendedDurationWeeks)
    }

    @Test
    fun `PCT savet - masteron kratak PCT`() {
        val advice = CyclePCTAdvice.fromSubstanceIds(listOf("masteron"))
        assertEquals(10, advice.recommendedDurationWeeks)
    }

    // =========================================================================
    // PCTProtocolType - uspesnost
    // =========================================================================

    @Test
    fun `protokol uspesnost - clomiphene only 69_2 procenata`() {
        assertEquals(0.692, PCTProtocolType.CLOMIPHENE_ONLY.successRate, 0.001)
    }

    @Test
    fun `protokol uspesnost - clomiphene i hCG 87_5 procenata`() {
        assertEquals(0.875, PCTProtocolType.CLOMIPHENE_HCG.successRate, 0.001)
    }

    @Test
    fun `protokol uspesnost - bez PCT 58_6 procenata`() {
        assertEquals(0.586, PCTProtocolType.NO_PCT.successRate, 0.001)
    }

    // =========================================================================
    // PCTData - edge case scenarija
    // =========================================================================

    @Test
    fun `PCT sa datumom poslednje injekcije u buducnosti`() {
        val futureDate = LocalDate.of(2026, 6, 1)
        val today = LocalDate.of(2025, 12, 1)
        val pctData = PCTData(
            cycleId = "test_cycle",
            lastInjectionDate = futureDate,
            longestEster = EsterType.ENANTHATE
        )
        // Preporučeni start = futureDate + 45 dana = Jul 16, 2026
        val expectedStart = futureDate.plusDays(45)
        assertEquals(expectedStart, pctData.recommendedStartDate)

        // Faza treba biti WAITING jer je daleko u budućnosti
        assertEquals(PCTPhase.WAITING, pctData.currentPhase(today))

        // daysUntilPCT treba biti pozitivan (daleko u budućnosti)
        assertTrue(pctData.daysUntilPCT(today) > 0)
    }

    @Test
    fun `PCT sa ciklusom duzim od 52 nedelje - undecilenat`() {
        // Ciklus od 60 nedelja sa boldenonom - ekstremno dug ciklus
        val lastInjection = LocalDate.of(2025, 1, 1)
        val pctData = PCTData(
            cycleId = "long_cycle",
            lastInjectionDate = lastInjection,
            longestEster = EsterType.UNDECYLENATE  // 70 dana čekanja
        )
        val expectedStart = lastInjection.plusDays(70)
        assertEquals(expectedStart, pctData.recommendedStartDate)

        // Proveri da se faza pravilno izračunava i posle godinu dana
        val wayLater = LocalDate.of(2026, 6, 1)
        assertEquals(PCTPhase.READY_TO_START, pctData.currentPhase(wayLater))
    }

    @Test
    fun `PCT sa injekcijom istog dana`() {
        val today = LocalDate.of(2025, 6, 15)
        val pctData = PCTData(
            cycleId = "test_cycle",
            lastInjectionDate = today,
            longestEster = EsterType.PROPIONATE  // 10 dana čekanja
        )
        // Preporučeni start = danas + 10 dana
        val expectedStart = today.plusDays(10)
        assertEquals(expectedStart, pctData.recommendedStartDate)
        assertEquals(10L, pctData.daysUntilPCT(today))
        assertEquals(PCTPhase.WAITING, pctData.currentPhase(today))
    }

    @Test
    fun `PCT sa injekcijom istog dana - oralni ester kratak period`() {
        val today = LocalDate.of(2025, 6, 15)
        val pctData = PCTData(
            cycleId = "oral_cycle",
            lastInjectionDate = today,
            longestEster = EsterType.ORAL  // 5 dana čekanja
        )
        assertEquals(5L, pctData.daysUntilPCT(today))
        assertEquals(PCTPhase.WAITING, pctData.currentPhase(today))

        // Posle 5 dana treba biti READY_TO_START
        val fiveDaysLater = today.plusDays(5)
        assertEquals(PCTPhase.READY_TO_START, pctData.currentPhase(fiveDaysLater))
    }

    @Test
    fun `PCT faza ACTIVE ali bez datuma krvne slike treba biti BLOODWORK_DUE posle 42 dana`() {
        val lastInjection = LocalDate.of(2025, 1, 1)
        val pctStart = LocalDate.of(2025, 2, 16)
        val pctData = PCTData(
            cycleId = "test_cycle",
            lastInjectionDate = lastInjection,
            longestEster = EsterType.ENANTHATE,
            pctStartDate = pctStart
        )
        // 42 dana posle početka PCT-a
        val after42days = pctStart.plusDays(42)
        assertEquals(PCTStatus.BLOODWORK_DUE, pctData.currentStatus(after42days))
    }

    @Test
    fun `PCT sa kratkim esterom - acetat 7 dana cekanja`() {
        val lastInjection = LocalDate.of(2025, 3, 1)
        val pctData = PCTData(
            cycleId = "tren_a_cycle",
            lastInjectionDate = lastInjection,
            longestEster = EsterType.ACETATE
        )
        assertEquals(7, EsterType.ACETATE.pctWaitDays)
        val expectedStart = lastInjection.plusDays(7)
        assertEquals(expectedStart, pctData.recommendedStartDate)
    }

    @Test
    fun `PCT sa undekanoatom - najduze cekanje 80 dana`() {
        val lastInjection = LocalDate.of(2025, 1, 1)
        val pctData = PCTData(
            cycleId = "undecanoate_cycle",
            lastInjectionDate = lastInjection,
            longestEster = EsterType.UNDECANOATE
        )
        assertEquals(80, EsterType.UNDECANOATE.pctWaitDays)
        val expectedStart = lastInjection.plusDays(80)
        assertEquals(expectedStart, pctData.recommendedStartDate)
    }

    // =========================================================================
    // EsterType.fromKey - parsiranje ključeva
    // =========================================================================

    @Test
    fun `EsterType fromKey - svi validni kljucevi`() {
        assertEquals(EsterType.ACETATE, EsterType.fromKey("acetate"))
        assertEquals(EsterType.PROPIONATE, EsterType.fromKey("propionate"))
        assertEquals(EsterType.PHENYLPROPIONATE, EsterType.fromKey("phenylpropionate"))
        assertEquals(EsterType.ENANTHATE, EsterType.fromKey("enanthate"))
        assertEquals(EsterType.CYPIONATE, EsterType.fromKey("cypionate"))
        assertEquals(EsterType.DECANOATE, EsterType.fromKey("decanoate"))
        assertEquals(EsterType.UNDECYLENATE, EsterType.fromKey("undecylenate"))
        assertEquals(EsterType.UNDECANOATE, EsterType.fromKey("undecanoate"))
        assertEquals(EsterType.ORAL, EsterType.fromKey("oral"))
    }

    @Test
    fun `EsterType fromKey - nepoznat kljuc daje ENANTHATE`() {
        assertEquals(EsterType.ENANTHATE, EsterType.fromKey("nepostojeci"))
    }

    // =========================================================================
    // PCTProtocolType.fromKey - parsiranje ključeva
    // =========================================================================

    @Test
    fun `PCTProtocolType fromKey - svi validni kljucevi`() {
        assertEquals(PCTProtocolType.CLOMIPHENE_ONLY, PCTProtocolType.fromKey("cc_only"))
        assertEquals(PCTProtocolType.CLOMIPHENE_HCG, PCTProtocolType.fromKey("cc_hcg"))
        assertEquals(PCTProtocolType.NO_PCT, PCTProtocolType.fromKey("none"))
    }

    @Test
    fun `PCTProtocolType fromKey - nepoznat kljuc daje CLOMIPHENE_ONLY`() {
        assertEquals(PCTProtocolType.CLOMIPHENE_ONLY, PCTProtocolType.fromKey("nepostojeci"))
    }

    // =========================================================================
    // CyclePCTAdvice - edge case-ovi
    // =========================================================================

    @Test
    fun `PCT savet - prazan spisak supstanci`() {
        val advice = CyclePCTAdvice.fromSubstanceIds(emptyList())
        assertEquals(SuppressionLevel.MODERATE, advice.worstSuppression)
        assertFalse(advice.requiresHCG)
    }

    @Test
    fun `PCT savet - nepoznata supstanca`() {
        val advice = CyclePCTAdvice.fromSubstanceIds(listOf("potpuno_nepoznato"))
        assertEquals(CompoundType.UNKNOWN, advice.compounds.first())
        assertEquals(SuppressionLevel.MODERATE, advice.worstSuppression)
        assertFalse(advice.requiresHCG)
    }

    @Test
    fun `PCT savet - duplikati supstanci ne uvecanvaju listu`() {
        val advice = CyclePCTAdvice.fromSubstanceIds(listOf("test_e", "test_e", "test_c"))
        // Svi testosteroni se mapiraju na CompoundType.TESTOSTERONE
        assertEquals(1, advice.compounds.distinct().size)
    }

    // =========================================================================
    // PCT sa prestupnom godinom
    // =========================================================================

    @Test
    fun `PCT datum preko prestupne godine - feb 29`() {
        // 2024 je prestupna godina
        val lastInjection = LocalDate.of(2024, 1, 15)
        val pctData = PCTData(
            cycleId = "leap_year_cycle",
            lastInjectionDate = lastInjection,
            longestEster = EsterType.ENANTHATE  // 45 dana čekanja
        )
        // Jan 15 + 45 = Mar 1, 2024 (jer Feb ima 29 dana)
        val expectedStart = lastInjection.plusDays(45)
        assertEquals(LocalDate.of(2024, 2, 29), expectedStart)
        assertEquals(expectedStart, pctData.recommendedStartDate)
    }

    @Test
    fun `PCT datum sa poslednjom injekcijom na feb 29 prestupne godine`() {
        // Poslednja injekcija na Feb 29, 2024
        val lastInjection = LocalDate.of(2024, 2, 29)
        val pctData = PCTData(
            cycleId = "leap_day_cycle",
            lastInjectionDate = lastInjection,
            longestEster = EsterType.PROPIONATE  // 10 dana čekanja
        )
        // Feb 29 + 10 = Mar 10, 2024
        val expectedStart = LocalDate.of(2024, 3, 10)
        assertEquals(expectedStart, pctData.recommendedStartDate)
    }

    @Test
    fun `PCT datum preko granice godine - dec 15 sa undecilenat`() {
        val lastInjection = LocalDate.of(2025, 12, 15)
        val pctData = PCTData(
            cycleId = "year_boundary",
            lastInjectionDate = lastInjection,
            longestEster = EsterType.UNDECYLENATE  // 70 dana čekanja
        )
        // Dec 15 + 70 = Feb 23, 2026
        val expectedStart = LocalDate.of(2026, 2, 23)
        assertEquals(expectedStart, pctData.recommendedStartDate)
    }

    // =========================================================================
    // Cycle.currentWeek i isFinished edge case-ovi
    // =========================================================================

    @Test
    fun `Cycle currentWeek - prvi dan ciklusa je nedelja 1`() {
        val cycle = Cycle(
            name = "Test",
            startDate = LocalDate.of(2025, 1, 1),
            durationWeeks = 12,
            compounds = emptyList()
        )
        assertEquals(1, cycle.currentWeek(LocalDate.of(2025, 1, 1)))
    }

    @Test
    fun `Cycle currentWeek - osmi dan ciklusa je nedelja 2`() {
        val cycle = Cycle(
            name = "Test",
            startDate = LocalDate.of(2025, 1, 1),
            durationWeeks = 12,
            compounds = emptyList()
        )
        assertEquals(2, cycle.currentWeek(LocalDate.of(2025, 1, 8)))
    }

    @Test
    fun `Cycle currentWeek - poslednji dan ciklusa`() {
        val cycle = Cycle(
            name = "Test",
            startDate = LocalDate.of(2025, 1, 1),
            durationWeeks = 12,
            compounds = emptyList()
        )
        // Nedelja 12 = dani 77-84
        val lastDayOfWeek12 = LocalDate.of(2025, 1, 1).plusDays(83)
        assertEquals(12, cycle.currentWeek(lastDayOfWeek12))
    }

    @Test
    fun `Cycle currentWeek - posle kraja ciklusa vraca durationWeeks`() {
        val cycle = Cycle(
            name = "Test",
            startDate = LocalDate.of(2025, 1, 1),
            durationWeeks = 12,
            compounds = emptyList()
        )
        // Veoma daleko u budućnosti - treba biti clamped na 12
        assertEquals(12, cycle.currentWeek(LocalDate.of(2026, 1, 1)))
    }

    @Test
    fun `Cycle currentWeek - pre pocetka vraca 1`() {
        val cycle = Cycle(
            name = "Test",
            startDate = LocalDate.of(2025, 3, 1),
            durationWeeks = 12,
            compounds = emptyList()
        )
        // Pre početka ciklusa
        assertEquals(1, cycle.currentWeek(LocalDate.of(2025, 1, 1)))
    }

    @Test
    fun `Cycle isFinished - na dan zavrsetka NIJE zavrsen`() {
        val cycle = Cycle(
            name = "Test",
            startDate = LocalDate.of(2025, 1, 1),
            durationWeeks = 12,
            compounds = emptyList()
        )
        // endDate = Jan 1 + 12 nedelja = Mar 26
        assertFalse(cycle.isFinished(cycle.endDate))
    }

    @Test
    fun `Cycle isFinished - dan posle zavrsetka JE zavrsen`() {
        val cycle = Cycle(
            name = "Test",
            startDate = LocalDate.of(2025, 1, 1),
            durationWeeks = 12,
            compounds = emptyList()
        )
        assertTrue(cycle.isFinished(cycle.endDate.plusDays(1)))
    }

    // =========================================================================
    // CompoundType - ostali tipovi
    // =========================================================================

    @Test
    fun `tip komponente - primobolan`() {
        assertEquals(CompoundType.PRIMOBOLAN, CompoundType.fromSubstanceId("primobolan"))
    }

    @Test
    fun `supresija - primobolan je blaga`() {
        assertEquals(SuppressionLevel.MILD, CompoundType.PRIMOBOLAN.suppressionLevel)
    }

    @Test
    fun `hCG potreban - primobolan ne zahteva`() {
        assertFalse(CompoundType.PRIMOBOLAN.requiresHCG)
    }

    @Test
    fun `hCG potreban - boldenon ne zahteva`() {
        assertFalse(CompoundType.BOLDENONE.requiresHCG)
    }

    @Test
    fun `hCG potreban - oralni ne zahtevaju`() {
        assertFalse(CompoundType.ORAL_17AA.requiresHCG)
    }

    // =========================================================================
    // CyclePCTAdvice - summaryText
    // =========================================================================

    @Test
    fun `PCT savet summaryText - sadrži nivo supresije`() {
        val advice = CyclePCTAdvice.fromSubstanceIds(listOf("test_e"))
        assertTrue(advice.summaryText.contains("Umerena supresija"))
    }

    @Test
    fun `PCT savet summaryText - hCG preporuka za nandrolon`() {
        val advice = CyclePCTAdvice.fromSubstanceIds(listOf("deca"))
        assertTrue(advice.summaryText.contains("hCG"))
    }
}
