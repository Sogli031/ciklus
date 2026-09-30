package com.injekcije.app

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// =============================================================================
// UI DIMENSIONS
// =============================================================================

object UI {
    val buttonHeight = 48.dp
    val cardRadius = 16.dp
    val cardPadding = 12.dp
    val headerHeight = 60.dp
    val statusCardHeight = 140.dp
    val weekCardWidth = 70.dp
    val bottomBarHeight = 70.dp
    val dialogWidth = 0.92f
    val smallSpacing = 4.dp
    val defaultSpacing = 8.dp
    val mediumSpacing = 12.dp
    val largeSpacing = 16.dp
    val xlSpacing = 24.dp

    val titleSize = 20.sp
    val subtitleSize = 16.sp
    val bodySize = 14.sp
    val captionSize = 12.sp
    val smallSize = 10.sp
}

// =============================================================================
// THEME COLORS
// =============================================================================

data class AppTheme(
    // Background
    val bgDark: Color = Color(0xFF0B0F14),
    val bgPrimary: Color = Color(0xFF101826),
    val bgCard: Color = Color(0xFF151D2B),
    val bgCardElevated: Color = Color(0xFF1A2435),
    val bgInput: Color = Color(0xFF0D1219),

    // Borders
    val glassBorder: Color = Color(0xFF2A3446),
    val divider: Color = Color(0xFF1E2A3A),

    // Accent colors
    val accentPrimary: Color = Color(0xFF2DE2E6),    // Cyan
    val accentSecondary: Color = Color(0xFFFCA311),  // Orange

    // Status colors
    val success: Color = Color(0xFF00C853),           // Green - On Track
    val warning: Color = Color(0xFFFFB300),           // Yellow - Due Soon
    val danger: Color = Color(0xFFFF5252),            // Red - Overdue
    val info: Color = Color(0xFF40C4FF),

    // Text
    val textPrimary: Color = Color(0xFFF8FAFC),
    val textSecondary: Color = Color(0xFFC7CEDB),
    val textMuted: Color = Color(0xFF7F8AA3),
    val textDark: Color = Color(0xFF0B0F14),

    // Category colors (for substance categories)
    val testosterone: Color = Color(0xFF00C2FF),
    val trenbolone: Color = Color(0xFFFF7AB6),
    val nandrolone: Color = Color(0xFF7AE582),
    val masteron: Color = Color(0xFFFFB300),
    val boldenone: Color = Color(0xFF9C7AFF),
    val primobolan: Color = Color(0xFFFF9E64),
    val other: Color = Color(0xFFA78BFA)
) {
    fun getCategoryColor(category: String): Color = when (category.lowercase()) {
        "testosterone" -> testosterone
        "trenbolone" -> trenbolone
        "nandrolone" -> nandrolone
        "masteron" -> masteron
        "boldenone" -> boldenone
        "primobolan" -> primobolan
        else -> other
    }
}

val THEME = AppTheme()

// =============================================================================
// DATE/TIME FORMATTING
// =============================================================================

object DateFormat {
    const val DATE_PATTERN = "dd.MM.yyyy"
    const val TIME_PATTERN = "HH:mm"
    const val DATETIME_PATTERN = "dd.MM.yyyy HH:mm"
    const val SHORT_DATE_PATTERN = "dd.MM"
    const val DAY_MONTH_PATTERN = "d. MMMM"
}

// =============================================================================
// SERBIAN LOCALE STRINGS
// =============================================================================

val WEEKDAY_NAMES_SHORT = listOf("PON", "UTO", "SRE", "ČET", "PET", "SUB", "NED")
val WEEKDAY_NAMES_FULL = listOf(
    "Ponedeljak", "Utorak", "Sreda", "Četvrtak", "Petak", "Subota", "Nedelja"
)

val MONTH_NAMES = listOf(
    "", "Januar", "Februar", "Mart", "April", "Maj", "Jun",
    "Jul", "Avgust", "Septembar", "Oktobar", "Novembar", "Decembar"
)

// =============================================================================
// TIPS
// =============================================================================

/**
 * Injection tips based on PubMed clinical guidelines.
 *
 * Key research findings:
 * - Ventrogluteal is safest (minimal nerves/vessels), can handle up to 5mL
 * - Dorsogluteal: 1-2% risk of sciatic nerve damage, being phased out
 * - Site rotation every 2-4 weeks prevents fibrosis ("needle myopathy")
 * - Injection rate <1mL/10sec reduces post-injection pain (PIP)
 * - Warming solution to body temperature reduces PIP incidence
 */
object InjectionTips {
    val siteTips = mapOf(
        "glute" to listOf(
            "Gornji spoljni kvadrant",
            "Do 4 mL po injekciji",
            "1-2% rizik od išijatički nerv - VG bezbedniji"
        ),
        "vg" to listOf(
            "NAJBEZBEDNIJE mesto (PubMed)",
            "Minimalno nerava/krvnih sudova",
            "Do 4-5 mL po injekciji"
        ),
        "delt" to listOf(
            "Spoljasnja strana ramena",
            "Max 1.5-2 mL",
            "Igla 0.5\" (12.7mm) dovoljna"
        ),
        "quad" to listOf(
            "Spoljna sredina butine",
            "Do 3 mL (do 5mL kod razvijene muskulature)",
            "Lako za samoadministraciju"
        ),
        "pec" to listOf(
            "Sredina grudi",
            "Max 1-2 mL",
            "Oprez: blizina neurovaskularnih struktura"
        )
    )

    val generalTips = listOf(
        "Rotiraj mesta - min 14 dana odmora po mestu",
        "Aspiracija pre injekcije (opcionalno po novijim smernicama)",
        "Brzina: <1mL/10 sekundi smanjuje bol",
        "Zagrej rastvor na telesnu temperaturu",
        "Igla 1-1.5\" (25-38mm), 22-25G za IM"
    )
}
