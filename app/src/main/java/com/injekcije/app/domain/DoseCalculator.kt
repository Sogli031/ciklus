package com.injekcije.app.domain

import com.injekcije.app.data.InjectionFrequency
import java.util.Locale
import kotlin.math.roundToInt

/**
 * Rezultat kalkulacije doze - bezbedno rukovanje greškama umesto bacanja exception-a.
 */
sealed class DoseResult {
    data class Success(val value: Double) : DoseResult()
    data class Error(val message: String) : DoseResult()

    fun getOrDefault(default: Double): Double = when (this) {
        is Success -> value
        is Error -> default
    }

    fun getOrNull(): Double? = when (this) {
        is Success -> value
        is Error -> null
    }
}

object DoseCalculator {

    /**
     * Pretvara miligrame u milillitre na osnovu koncentracije rastvora.
     * Pretpostavlja da je koncentracija u mg/mL (standardna jedinica za injekcione preparate).
     *
     * Formula: volumen (mL) = doza (mg) / koncentracija (mg/mL)
     *
     * Vraća DoseResult.Error umesto bacanja exception-a za nevalidan input.
     */
    fun mgToMl(doseMg: Double, concentrationMgMl: Double): DoseResult {
        if (concentrationMgMl <= 0) return DoseResult.Error("Koncentracija mora biti pozitivna")
        if (doseMg < 0) return DoseResult.Error("Doza ne može biti negativna")
        // mg / (mg/mL) = mL
        return DoseResult.Success(doseMg / concentrationMgMl)
    }

    /**
     * Pretvara mililitre u miligrame na osnovu koncentracije rastvora.
     * Pretpostavlja da je koncentracija u mg/mL.
     *
     * Formula: doza (mg) = volumen (mL) * koncentracija (mg/mL)
     *
     * Vraća DoseResult.Error umesto bacanja exception-a za nevalidan input.
     */
    fun mlToMg(volumeMl: Double, concentrationMgMl: Double): DoseResult {
        if (concentrationMgMl <= 0) return DoseResult.Error("Koncentracija mora biti pozitivna")
        if (volumeMl < 0) return DoseResult.Error("Volumen ne može biti negativan")
        // mL * (mg/mL) = mg
        return DoseResult.Success(volumeMl * concentrationMgMl)
    }

    /**
     * Računa dozu po injekciji na osnovu nedeljne doze i frekvencije.
     * Pretpostavlja ravnomerno raspoređivanje nedeljne doze na sve injekcije.
     *
     * Formula: doza_po_injekciji = nedeljna_doza / (7 / interval_u_danima)
     *
     * Vraća DoseResult.Error umesto bacanja exception-a za nevalidan input.
     */
    fun calculateDosePerInjection(
        weeklyDoseMg: Double,
        frequency: InjectionFrequency
    ): DoseResult {
        if (weeklyDoseMg < 0) return DoseResult.Error("Nedeljna doza ne može biti negativna")
        // Broj injekcija nedeljno = 7 / interval u danima
        // Doza po injekciji = nedeljna doza / broj injekcija nedeljno
        return DoseResult.Success(weeklyDoseMg / frequency.injectionsPerWeek)
    }

    /**
     * Računa volumen po injekciji na osnovu nedeljne doze, frekvencije i koncentracije.
     * Pretpostavlja da je koncentracija u mg/mL.
     *
     * Formula: volumen = (nedeljna_doza / injekcije_nedeljno) / koncentracija
     *
     * Vraća DoseResult.Error umesto bacanja exception-a za nevalidan input.
     */
    fun calculateVolumePerInjection(
        weeklyDoseMg: Double,
        frequency: InjectionFrequency,
        concentrationMgMl: Double
    ): DoseResult {
        if (weeklyDoseMg < 0) return DoseResult.Error("Nedeljna doza ne može biti negativna")
        if (concentrationMgMl <= 0) return DoseResult.Error("Koncentracija mora biti pozitivna")
        val doseResult = calculateDosePerInjection(weeklyDoseMg, frequency)
        return when (doseResult) {
            is DoseResult.Error -> doseResult
            is DoseResult.Success -> mgToMl(doseResult.value, concentrationMgMl)
        }
    }

    /**
     * Formatira volumen za prikaz (npr. "0.5 mL" ili "1.25 mL").
     * Koristi 3 decimale za veoma male volumene (<0.1 mL), inače 2 decimale.
     * Negativne vrednosti se tretiraju kao 0.
     */
    fun formatVolume(volumeMl: Double): String {
        val safe = volumeMl.coerceAtLeast(0.0)
        return when {
            safe < 0.1 -> String.format(Locale.US, "%.3f mL", safe)
            safe < 1.0 -> String.format(Locale.US, "%.2f mL", safe)
            else -> String.format(Locale.US, "%.2f mL", safe)
        }
    }

    /**
     * Formatira dozu za prikaz (npr. "125 mg" ili "62.5 mg").
     * Celobrojne doze prikazuje bez decimala.
     * Negativne vrednosti se tretiraju kao 0.
     */
    fun formatDose(doseMg: Double): String {
        val safe = doseMg.coerceAtLeast(0.0)
        return if (safe == safe.toLong().toDouble()) {
            "${safe.toLong()} mg"
        } else {
            String.format(Locale.US, "%.1f mg", safe)
        }
    }

    /**
     * Formatira dozu zajedno sa volumenom (npr. "125mg / 0.5mL").
     */
    fun formatDoseWithVolume(doseMg: Double, volumeMl: Double): String {
        val doseStr = formatDose(doseMg)
        val volStr = formatVolume(volumeMl)
        return "$doseStr / $volStr"
    }

    /**
     * Prikazuje pomoć za očitavanje šprica za dati volumen.
     * 1 mL = 1 cc. Insulinski špric: 100 jedinica = 1 mL.
     * Negativne vrednosti se tretiraju kao 0.
     */
    fun getSyringeHelp(volumeMl: Double): String {
        val safe = volumeMl.coerceAtLeast(0.0)
        val cc = safe // 1 mL = 1 cc (kubni centimetar)
        // roundToInt umesto toInt - truncate bi npr. za 0.29 mL prikazao 28 umesto 29 jedinica
        val insulinUnits = (safe * 100).roundToInt() // insulinski špric: 100 IU = 1 mL

        return buildString {
            append("Volume: ${formatVolume(safe)}\n")
            append("= ${"%.2f".format(cc)} cc\n")
            if (safe <= 1.0) {
                append("= $insulinUnits jedinica na insulinskom špricu")
            }
        }
    }

    /**
     * Proverava da li je volumen bezbedan za dato mesto injekcije.
     */
    fun isVolumeSafeForSite(volumeMl: Double, maxSiteVolume: Double): Boolean {
        return volumeMl in 0.0..maxSiteVolume
    }

    /**
     * Vraća upozorenje ako volumen prekoračuje kapacitet mesta injekcije.
     */
    fun getVolumeWarning(volumeMl: Double, maxSiteVolume: Double, siteName: String): String? {
        if (volumeMl <= 0) return null
        return if (volumeMl > maxSiteVolume) {
            "Upozorenje: ${formatVolume(volumeMl)} je više od preporučenog za $siteName (max ${formatVolume(maxSiteVolume)})"
        } else null
    }
}
