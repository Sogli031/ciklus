package com.injekcije.app.domain

import com.injekcije.app.data.InjectionFrequency
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit testovi za DoseCalculator.
 * Pokriva kalkulacije doza, konverzije volumena, formatiranje i edge case-ove.
 * Ažurirano za DoseResult sealed class API.
 */
class DoseCalculatorTest {

    // =========================================================================
    // Pomoćne metode za DoseResult asercije
    // =========================================================================

    private fun assertSuccess(expected: Double, result: DoseResult, delta: Double = 0.001) {
        assertTrue("Očekivan DoseResult.Success ali dobijen: $result", result is DoseResult.Success)
        assertEquals(expected, (result as DoseResult.Success).value, delta)
    }

    private fun assertError(result: DoseResult) {
        assertTrue("Očekivan DoseResult.Error ali dobijen: $result", result is DoseResult.Error)
    }

    // =========================================================================
    // mgToMl - konverzija miligrami u mililitre
    // =========================================================================

    @Test
    fun `mgToMl - standardna konverzija 250mg na 250mgml`() {
        val result = DoseCalculator.mgToMl(250.0, 250.0)
        assertSuccess(1.0, result)
    }

    @Test
    fun `mgToMl - pola viale 125mg na 250mgml`() {
        val result = DoseCalculator.mgToMl(125.0, 250.0)
        assertSuccess(0.5, result)
    }

    @Test
    fun `mgToMl - visoka koncentracija 300mgml`() {
        val result = DoseCalculator.mgToMl(300.0, 300.0)
        assertSuccess(1.0, result)
    }

    @Test
    fun `mgToMl - mala doza 50mg na 250mgml`() {
        val result = DoseCalculator.mgToMl(50.0, 250.0)
        assertSuccess(0.2, result)
    }

    @Test
    fun `mgToMl - nula mg daje nula ml`() {
        val result = DoseCalculator.mgToMl(0.0, 250.0)
        assertSuccess(0.0, result)
    }

    @Test
    fun `mgToMl - nula koncentracija vraca Error`() {
        val result = DoseCalculator.mgToMl(100.0, 0.0)
        assertError(result)
    }

    @Test
    fun `mgToMl - negativna koncentracija vraca Error`() {
        val result = DoseCalculator.mgToMl(100.0, -50.0)
        assertError(result)
    }

    @Test
    fun `mgToMl - negativna doza vraca Error`() {
        val result = DoseCalculator.mgToMl(-10.0, 250.0)
        assertError(result)
    }

    // =========================================================================
    // mlToMg - konverzija mililitri u miligrame
    // =========================================================================

    @Test
    fun `mlToMg - standardna konverzija 1ml na 250mgml`() {
        val result = DoseCalculator.mlToMg(1.0, 250.0)
        assertSuccess(250.0, result)
    }

    @Test
    fun `mlToMg - pola ml na 250mgml`() {
        val result = DoseCalculator.mlToMg(0.5, 250.0)
        assertSuccess(125.0, result)
    }

    @Test
    fun `mlToMg - nula ml daje nula mg`() {
        val result = DoseCalculator.mlToMg(0.0, 250.0)
        assertSuccess(0.0, result)
    }

    @Test
    fun `mlToMg - nula koncentracija vraca Error`() {
        val result = DoseCalculator.mlToMg(1.0, 0.0)
        assertError(result)
    }

    @Test
    fun `mlToMg - negativna koncentracija vraca Error`() {
        val result = DoseCalculator.mlToMg(1.0, -100.0)
        assertError(result)
    }

    @Test
    fun `mlToMg - negativan volumen vraca Error`() {
        val result = DoseCalculator.mlToMg(-1.0, 250.0)
        assertError(result)
    }

    // =========================================================================
    // calculateDosePerInjection - doza po injekciji
    // =========================================================================

    @Test
    fun `doza po injekciji - 500mg nedeljno E3_5D daje 250mg`() {
        val result = DoseCalculator.calculateDosePerInjection(500.0, InjectionFrequency.E3_5D)
        assertSuccess(250.0, result)
    }

    @Test
    fun `doza po injekciji - 500mg nedeljno WEEKLY daje 500mg`() {
        val result = DoseCalculator.calculateDosePerInjection(500.0, InjectionFrequency.WEEKLY)
        assertSuccess(500.0, result)
    }

    @Test
    fun `doza po injekciji - 350mg nedeljno ED`() {
        val result = DoseCalculator.calculateDosePerInjection(350.0, InjectionFrequency.ED)
        assertSuccess(50.0, result)
    }

    @Test
    fun `doza po injekciji - 350mg nedeljno EOD`() {
        val result = DoseCalculator.calculateDosePerInjection(350.0, InjectionFrequency.EOD)
        assertSuccess(100.0, result)
    }

    @Test
    fun `doza po injekciji - 500mg nedeljno E3D`() {
        val result = DoseCalculator.calculateDosePerInjection(500.0, InjectionFrequency.E3D)
        val expected = 500.0 / (7.0 / 3.0)
        assertSuccess(expected, result)
    }

    @Test
    fun `doza po injekciji - 250mg nedeljno E14D`() {
        val result = DoseCalculator.calculateDosePerInjection(250.0, InjectionFrequency.E14D)
        assertSuccess(500.0, result)
    }

    @Test
    fun `doza po injekciji - nula nedeljne doze`() {
        val result = DoseCalculator.calculateDosePerInjection(0.0, InjectionFrequency.WEEKLY)
        assertSuccess(0.0, result)
    }

    @Test
    fun `doza po injekciji - negativna nedeljna doza vraca Error`() {
        val result = DoseCalculator.calculateDosePerInjection(-100.0, InjectionFrequency.WEEKLY)
        assertError(result)
    }

    // =========================================================================
    // calculateVolumePerInjection - volumen po injekciji
    // =========================================================================

    @Test
    fun `volumen po injekciji - 500mg nedeljno E3_5D na 250mgml`() {
        val result = DoseCalculator.calculateVolumePerInjection(500.0, InjectionFrequency.E3_5D, 250.0)
        assertSuccess(1.0, result)
    }

    @Test
    fun `volumen po injekciji - 300mg nedeljno WEEKLY na 300mgml`() {
        val result = DoseCalculator.calculateVolumePerInjection(300.0, InjectionFrequency.WEEKLY, 300.0)
        assertSuccess(1.0, result)
    }

    @Test
    fun `volumen po injekciji - 500mg nedeljno ED na 100mgml`() {
        val result = DoseCalculator.calculateVolumePerInjection(500.0, InjectionFrequency.ED, 100.0)
        val expected = (500.0 / 7.0) / 100.0
        assertSuccess(expected, result)
    }

    @Test
    fun `volumen po injekciji - nula koncentracija vraca Error`() {
        val result = DoseCalculator.calculateVolumePerInjection(500.0, InjectionFrequency.E3_5D, 0.0)
        assertError(result)
    }

    @Test
    fun `volumen po injekciji - negativna koncentracija vraca Error`() {
        val result = DoseCalculator.calculateVolumePerInjection(500.0, InjectionFrequency.E3_5D, -100.0)
        assertError(result)
    }

    @Test
    fun `volumen po injekciji - negativna doza vraca Error`() {
        val result = DoseCalculator.calculateVolumePerInjection(-500.0, InjectionFrequency.E3_5D, 250.0)
        assertError(result)
    }

    // =========================================================================
    // DoseResult.getOrDefault i getOrNull
    // =========================================================================

    @Test
    fun `getOrDefault - Success vraca vrednost`() {
        val result = DoseResult.Success(42.0)
        assertEquals(42.0, result.getOrDefault(0.0), 0.001)
    }

    @Test
    fun `getOrDefault - Error vraca podrazumevanu vrednost`() {
        val result = DoseResult.Error("greška")
        assertEquals(0.0, result.getOrDefault(0.0), 0.001)
    }

    @Test
    fun `getOrNull - Success vraca vrednost`() {
        val result = DoseResult.Success(42.0)
        assertEquals(42.0, result.getOrNull()!!, 0.001)
    }

    @Test
    fun `getOrNull - Error vraca null`() {
        val result = DoseResult.Error("greška")
        assertEquals(null, result.getOrNull())
    }

    // =========================================================================
    // formatVolume - formatiranje volumena za prikaz
    // =========================================================================

    @Test
    fun `formatVolume - mala vrednost ispod 0_1ml`() {
        val result = DoseCalculator.formatVolume(0.05)
        assertEquals("0.050 mL", result)
    }

    @Test
    fun `formatVolume - srednja vrednost 0_5ml`() {
        val result = DoseCalculator.formatVolume(0.5)
        assertEquals("0.50 mL", result)
    }

    @Test
    fun `formatVolume - 1ml`() {
        val result = DoseCalculator.formatVolume(1.0)
        assertEquals("1.00 mL", result)
    }

    @Test
    fun `formatVolume - veca vrednost 2_5ml`() {
        val result = DoseCalculator.formatVolume(2.5)
        assertEquals("2.50 mL", result)
    }

    @Test
    fun `formatVolume - negativan input daje 0_000`() {
        val result = DoseCalculator.formatVolume(-1.5)
        assertEquals("0.000 mL", result)
    }

    @Test
    fun `formatVolume - nula`() {
        val result = DoseCalculator.formatVolume(0.0)
        assertEquals("0.000 mL", result)
    }

    // =========================================================================
    // formatDose - formatiranje doze za prikaz
    // =========================================================================

    @Test
    fun `formatDose - ceo broj 250mg`() {
        val result = DoseCalculator.formatDose(250.0)
        assertEquals("250 mg", result)
    }

    @Test
    fun `formatDose - decimalni broj 62_5mg`() {
        val result = DoseCalculator.formatDose(62.5)
        assertEquals("62.5 mg", result)
    }

    @Test
    fun `formatDose - nula`() {
        val result = DoseCalculator.formatDose(0.0)
        assertEquals("0 mg", result)
    }

    @Test
    fun `formatDose - negativna vrednost daje 0`() {
        val result = DoseCalculator.formatDose(-50.0)
        assertEquals("0 mg", result)
    }

    // =========================================================================
    // formatDoseWithVolume - kombinovano formatiranje
    // =========================================================================

    @Test
    fun `formatDoseWithVolume - standardna kombinacija`() {
        val result = DoseCalculator.formatDoseWithVolume(250.0, 1.0)
        assertEquals("250 mg / 1.00 mL", result)
    }

    @Test
    fun `formatDoseWithVolume - mala doza i volumen`() {
        val result = DoseCalculator.formatDoseWithVolume(62.5, 0.25)
        assertEquals("62.5 mg / 0.25 mL", result)
    }

    // =========================================================================
    // getSyringeHelp - pomoć za špric
    // =========================================================================

    @Test
    fun `getSyringeHelp - 1ml sadrzi insulinske jedinice`() {
        val result = DoseCalculator.getSyringeHelp(1.0)
        assertTrue(result.contains("100 jedinica na insulinskom špricu"))
    }

    @Test
    fun `getSyringeHelp - 0_5ml sadrzi 50 insulinskih jedinica`() {
        val result = DoseCalculator.getSyringeHelp(0.5)
        assertTrue(result.contains("50 jedinica na insulinskom špricu"))
    }

    @Test
    fun `getSyringeHelp - volumen preko 1ml nema insulinski deo`() {
        val result = DoseCalculator.getSyringeHelp(1.5)
        assertTrue(!result.contains("insulinskom špricu"))
    }

    @Test
    fun `getSyringeHelp - negativan volumen koristi 0`() {
        val result = DoseCalculator.getSyringeHelp(-1.0)
        assertTrue(result.contains("0 jedinica na insulinskom špricu"))
    }

    // =========================================================================
    // isVolumeSafeForSite - bezbednost volumena za sajt
    // =========================================================================

    @Test
    fun `isVolumeSafeForSite - volumen ispod limita je bezbedan`() {
        assertTrue(DoseCalculator.isVolumeSafeForSite(1.0, 4.0))
    }

    @Test
    fun `isVolumeSafeForSite - volumen jednak limitu je bezbedan`() {
        assertTrue(DoseCalculator.isVolumeSafeForSite(4.0, 4.0))
    }

    @Test
    fun `isVolumeSafeForSite - volumen preko limita nije bezbedan`() {
        assertTrue(!DoseCalculator.isVolumeSafeForSite(5.0, 4.0))
    }

    // =========================================================================
    // getVolumeWarning - upozorenje za volumen
    // =========================================================================

    @Test
    fun `getVolumeWarning - nema upozorenja kada je volumen bezbedan`() {
        val result = DoseCalculator.getVolumeWarning(1.0, 4.0, "Glute (L)")
        assertEquals(null, result)
    }

    @Test
    fun `getVolumeWarning - ima upozorenje kada volumen prelazi limit`() {
        val result = DoseCalculator.getVolumeWarning(5.0, 4.0, "Delt (L)")
        assertTrue(result != null)
        assertTrue(result!!.contains("Upozorenje"))
        assertTrue(result.contains("Delt (L)"))
    }

    // =========================================================================
    // InjectionFrequency.injectionsPerWeek - verifikacija vrednosti
    // =========================================================================

    @Test
    fun `frekvencija - ED je 7x nedeljno`() {
        assertEquals(7.0, InjectionFrequency.ED.injectionsPerWeek, 0.001)
    }

    @Test
    fun `frekvencija - EOD je 3_5x nedeljno`() {
        assertEquals(3.5, InjectionFrequency.EOD.injectionsPerWeek, 0.001)
    }

    @Test
    fun `frekvencija - E3_5D je 2x nedeljno`() {
        assertEquals(2.0, InjectionFrequency.E3_5D.injectionsPerWeek, 0.001)
    }

    @Test
    fun `frekvencija - WEEKLY je 1x nedeljno`() {
        assertEquals(1.0, InjectionFrequency.WEEKLY.injectionsPerWeek, 0.001)
    }

    @Test
    fun `frekvencija - E14D je 0_5x nedeljno`() {
        assertEquals(0.5, InjectionFrequency.E14D.injectionsPerWeek, 0.001)
    }

    // =========================================================================
    // HCG/HGH - specijalni slučajevi sa IU
    // =========================================================================

    @Test
    fun `HCG doza - 1500IU E3D kao nedeljni ekvivalent`() {
        val weeklyIU = 3500.0
        val result = DoseCalculator.calculateDosePerInjection(weeklyIU, InjectionFrequency.E3D)
        assertSuccess(1500.0, result, 1.0)
    }

    @Test
    fun `HGH doza - ED sa nedeljnom dozom`() {
        val weeklyIU = 28.0
        val result = DoseCalculator.calculateDosePerInjection(weeklyIU, InjectionFrequency.ED)
        assertSuccess(4.0, result)
    }

    @Test
    fun `HCG volumen - 5000IU u 2ml bakteriostatske vode`() {
        val result = DoseCalculator.mgToMl(1500.0, 2500.0)
        assertSuccess(0.6, result)
    }

    // =========================================================================
    // Edge case-ovi - ekstremne vrednosti
    // =========================================================================

    @Test
    fun `mgToMl - ekstremno visoka koncentracija 1000mgml`() {
        // Npr. Testosterone Undecanoate 1000mg/4ml = 250mg/ml, ali testiramo 1000mg/ml
        val result = DoseCalculator.mgToMl(500.0, 1000.0)
        assertSuccess(0.5, result)
    }

    @Test
    fun `mgToMl - sicusan volumen ispod 0_01ml`() {
        // 1mg na 250mg/ml = 0.004ml
        val result = DoseCalculator.mgToMl(1.0, 250.0)
        assertSuccess(0.004, result)
    }

    @Test
    fun `mgToMl - veoma velika doza 5000mg na 250mgml`() {
        val result = DoseCalculator.mgToMl(5000.0, 250.0)
        assertSuccess(20.0, result)
    }

    @Test
    fun `calculateVolumePerInjection - sicusan volumen manje od 0_01ml`() {
        // 7mg nedeljno, ED, 250mg/ml = 1mg po injekciji / 250 = 0.004ml
        val result = DoseCalculator.calculateVolumePerInjection(7.0, InjectionFrequency.ED, 250.0)
        assertSuccess(0.004, result)
    }

    @Test
    fun `calculateVolumePerInjection - ekstremno visoka koncentracija`() {
        // 500mg nedeljno, E3.5D, 1000mg/ml = 250mg / 1000 = 0.25ml
        val result = DoseCalculator.calculateVolumePerInjection(500.0, InjectionFrequency.E3_5D, 1000.0)
        assertSuccess(0.25, result)
    }

    @Test
    fun `calculateDosePerInjection - sva frekvencija E3D frakcija`() {
        // E3D = 7/3 = 2.333 injekcije nedeljno
        // 700mg / 2.333 = 300mg po injekciji
        val result = DoseCalculator.calculateDosePerInjection(700.0, InjectionFrequency.E3D)
        assertSuccess(300.0, result)
    }

    @Test
    fun `calculateDosePerInjection - E5D frakcija`() {
        // E5D = 7/5 = 1.4 injekcije nedeljno
        // 700mg / 1.4 = 500mg po injekciji
        val result = DoseCalculator.calculateDosePerInjection(700.0, InjectionFrequency.E5D)
        assertSuccess(500.0, result)
    }

    @Test
    fun `calculateDosePerInjection - E10D frakcija`() {
        // E10D = 7/10 = 0.7 injekcije nedeljno
        // 250mg / 0.7 = ~357.14mg po injekciji
        val result = DoseCalculator.calculateDosePerInjection(250.0, InjectionFrequency.E10D)
        val expected = 250.0 / (7.0 / 10.0)
        assertSuccess(expected, result)
    }

    @Test
    fun `formatVolume - sicusan volumen 0_004ml`() {
        val result = DoseCalculator.formatVolume(0.004)
        assertEquals("0.004 mL", result)
    }

    @Test
    fun `formatVolume - velik volumen 10ml`() {
        val result = DoseCalculator.formatVolume(10.0)
        assertEquals("10.00 mL", result)
    }

    @Test
    fun `formatDose - velika doza 1000mg`() {
        val result = DoseCalculator.formatDose(1000.0)
        assertEquals("1000 mg", result)
    }

    @Test
    fun `formatDose - mala doza 0_5mg`() {
        val result = DoseCalculator.formatDose(0.5)
        assertEquals("0.5 mg", result)
    }

    @Test
    fun `isVolumeSafeForSite - nula volumen je bezbedan`() {
        assertTrue(DoseCalculator.isVolumeSafeForSite(0.0, 4.0))
    }

    @Test
    fun `isVolumeSafeForSite - negativan volumen NIJE bezbedan`() {
        assertTrue(!DoseCalculator.isVolumeSafeForSite(-1.0, 4.0))
    }

    @Test
    fun `getVolumeWarning - nula volumen nema upozorenje`() {
        val result = DoseCalculator.getVolumeWarning(0.0, 4.0, "Glute (L)")
        assertEquals(null, result)
    }
}
