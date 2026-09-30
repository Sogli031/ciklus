package com.injekcije.app.ui

import com.injekcije.app.R
import com.injekcije.app.ui.components.GlassCard
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.ln
import kotlin.math.roundToInt
import com.injekcije.app.LocalStrings
import com.injekcije.app.Strings
import com.injekcije.app.THEME
import com.injekcije.app.UI
import com.injekcije.app.data.LearningArticleData
import com.injekcije.app.data.LearningCategoryData
import com.injekcije.app.data.getLearningArticle
import com.injekcije.app.data.getLearningCategory
import com.injekcije.app.data.learningCategories
import java.util.Locale

private val learningArticleImages = mapOf(
    "chart_nandrolone" to R.drawable.chart_nandrolone
)

// =============================================================================
// LEARNING CATEGORIES SCREEN
// =============================================================================

@Composable
fun LearningCategoriesScreen(
    strings: Strings,
    onDismiss: () -> Unit,
    onCategoryClick: (String) -> Unit
) {
    BackHandler { onDismiss() }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(THEME.bgDark, THEME.bgPrimary)))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(UI.mediumSpacing)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = strings.learning,
                        color = THEME.textPrimary,
                        fontSize = UI.titleSize,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = strings.learningSubtitle,
                        color = THEME.textMuted,
                        fontSize = UI.captionSize
                    )
                }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = null, tint = THEME.textMuted)
                }
            }

            Spacer(modifier = Modifier.height(UI.mediumSpacing))

            // Categories list
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(UI.defaultSpacing)
            ) {
                items(learningCategories) { category ->
                    CategoryCard(
                        category = category,
                        onClick = { onCategoryClick(category.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun CategoryCard(
    category: LearningCategoryData,
    onClick: () -> Unit
) {
    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(UI.cardPadding),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(THEME.accentSecondary.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = category.icon,
                    contentDescription = null,
                    tint = THEME.accentSecondary,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(UI.mediumSpacing))

            // Title and article count
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = category.title,
                    color = THEME.textPrimary,
                    fontSize = UI.subtitleSize,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "${category.articles.size} ${if (category.articles.size == 1) "članak" else "članaka"}",
                    color = THEME.textMuted,
                    fontSize = UI.captionSize
                )
            }

            // Arrow
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = null,
                tint = THEME.textMuted,
                modifier = Modifier.size(20.dp).rotate(180f)
            )
        }
    }
}

// =============================================================================
// LEARNING CATEGORY SCREEN (Articles list)
// =============================================================================

@Composable
fun LearningCategoryScreen(
    categoryId: String,
    @Suppress("UNUSED_PARAMETER") strings: Strings,
    onDismiss: () -> Unit,
    onArticleClick: (String) -> Unit
) {
    BackHandler { onDismiss() }

    val category = remember { getLearningCategory(categoryId) }

    if (category == null) {
        onDismiss()
        return
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(THEME.bgDark, THEME.bgPrimary)))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(UI.mediumSpacing)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onDismiss) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = null,
                        tint = THEME.textPrimary
                    )
                }
                Spacer(modifier = Modifier.width(UI.defaultSpacing))
                Column {
                    Text(
                        text = category.title,
                        color = THEME.textPrimary,
                        fontSize = UI.titleSize,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${category.articles.size} ${if (category.articles.size == 1) "članak" else "članaka"}",
                        color = THEME.textMuted,
                        fontSize = UI.captionSize
                    )
                }
            }

            Spacer(modifier = Modifier.height(UI.mediumSpacing))

            // Articles list
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(UI.defaultSpacing)
            ) {
                items(category.articles) { article ->
                    ArticleCard(
                        article = article,
                        onClick = { onArticleClick(article.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun ArticleCard(
    article: LearningArticleData,
    onClick: () -> Unit
) {
    val content = article.content
    val preview = content.take(150).replace("**", "").replace("•", "-")

    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(UI.cardPadding)
        ) {
            Text(
                text = article.title,
                color = THEME.textPrimary,
                fontSize = UI.subtitleSize,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(UI.smallSpacing))
            Text(
                text = preview + if (content.length > 150) "..." else "",
                color = THEME.textMuted,
                fontSize = UI.captionSize,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

// =============================================================================
// LEARNING ARTICLE SCREEN
// =============================================================================

@Composable
fun LearningArticleScreen(
    categoryId: String,
    articleId: String,
    @Suppress("UNUSED_PARAMETER") strings: Strings,
    onDismiss: () -> Unit
) {
    BackHandler { onDismiss() }

    val article = remember { getLearningArticle(categoryId, articleId) }

    if (article == null) {
        onDismiss()
        return
    }

    val content = article.content

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(THEME.bgDark, THEME.bgPrimary)))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = UI.mediumSpacing, vertical = UI.defaultSpacing),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onDismiss) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = null,
                        tint = THEME.textPrimary
                    )
                }
                Spacer(modifier = Modifier.width(UI.defaultSpacing))
                Text(
                    text = article.title,
                    color = THEME.textPrimary,
                    fontSize = UI.subtitleSize,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
            }

            HorizontalDivider(color = THEME.divider, thickness = 1.dp)

            // Content
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(UI.mediumSpacing)
            ) {
                FormattedContent(content = content)
            }
        }
    }
}

@Composable
private fun FormattedContent(content: String) {
    val lines = content.split("\n")
    var i = 0
    val context = LocalContext.current

    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        while (i < lines.size) {
            val line = lines[i]
            when {
                // Full line separator (━━━)
                line.trim().all { it == '━' } && line.trim().length >= 10 -> {
                    Spacer(modifier = Modifier.height(UI.defaultSpacing))
                    HorizontalDivider(
                        modifier = Modifier.fillMaxWidth(),
                        thickness = 2.dp,
                        color = THEME.glassBorder
                    )
                    Spacer(modifier = Modifier.height(UI.defaultSpacing))
                    i++
                }
                // Dashed line separator (─ ─ ─)
                line.trim().replace(" ", "").all { it == '─' } && line.contains("─") && line.trim().length >= 10 -> {
                    Spacer(modifier = Modifier.height(UI.smallSpacing))
                    HorizontalDivider(
                        modifier = Modifier.fillMaxWidth(),
                        thickness = 1.dp,
                        color = THEME.divider.copy(alpha = 0.5f)
                    )
                    Spacer(modifier = Modifier.height(UI.smallSpacing))
                    i++
                }
                line.startsWith("[SIMULATOR:") && line.endsWith("]") -> {
                    // Simulator tag: [SIMULATOR:type]
                    val simType = line.removePrefix("[SIMULATOR:").removeSuffix("]")
                    when (simType) {
                        "testosterone" -> {
                            Spacer(modifier = Modifier.height(UI.defaultSpacing))
                            TestosteroneDoseSimulator()
                            Spacer(modifier = Modifier.height(UI.defaultSpacing))
                        }
                    }
                    i++
                }
                line.startsWith("[IMAGE:") && line.endsWith("]") -> {
                    // Image tag: [IMAGE:drawable_name]
                    val imageName = line.removePrefix("[IMAGE:").removeSuffix("]")
                    val resourceId = learningArticleImages[imageName] ?: 0
                    if (resourceId != 0) {
                        Spacer(modifier = Modifier.height(UI.defaultSpacing))
                        Image(
                            painter = painterResource(id = resourceId),
                            contentDescription = imageName,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp)),
                            contentScale = ContentScale.FillWidth
                        )
                        Spacer(modifier = Modifier.height(UI.defaultSpacing))
                    }
                    i++
                }
                line.startsWith("**") && line.endsWith("**") -> {
                    // Bold header
                    Spacer(modifier = Modifier.height(UI.defaultSpacing))
                    Text(
                        text = line.removePrefix("**").removeSuffix("**"),
                        color = THEME.accentPrimary,
                        fontSize = UI.subtitleSize,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(UI.smallSpacing))
                    i++
                }
                line.startsWith("• ") || line.startsWith("- ") -> {
                    // Bullet point
                    Row(modifier = Modifier.padding(start = UI.defaultSpacing)) {
                        Text(
                            text = "•",
                            color = THEME.accentSecondary,
                            fontSize = UI.bodySize
                        )
                        Spacer(modifier = Modifier.width(UI.defaultSpacing))
                        Text(
                            text = line.removePrefix("• ").removePrefix("- ").formatInlineBold(),
                            color = THEME.textSecondary,
                            fontSize = UI.bodySize
                        )
                    }
                    i++
                }
                line.startsWith("|") -> {
                    // Collect all consecutive table rows
                    val tableRows = mutableListOf<String>()
                    while (i < lines.size && lines[i].startsWith("|")) {
                        tableRows.add(lines[i])
                        i++
                    }
                    if (tableRows.isNotEmpty()) {
                        DoseTable(tableRows)
                    }
                }
                line.isBlank() -> {
                    Spacer(modifier = Modifier.height(UI.smallSpacing))
                    i++
                }
                else -> {
                    // Regular text with possible inline bold
                    Text(
                        text = line.formatInlineBold(),
                        color = THEME.textSecondary,
                        fontSize = UI.bodySize
                    )
                    i++
                }
            }
        }
    }
}

@Composable
private fun DoseTable(rows: List<String>) {
    // Parse table rows
    val parsedRows = rows.mapNotNull { row ->
        val cells = row.split("|").filter { it.isNotBlank() }.map { it.trim() }
        if (cells.isNotEmpty() && !cells.all { it.all { c -> c == '-' || c == ' ' } }) {
            cells
        } else {
            null
        }
    }

    if (parsedRows.isEmpty()) return

    val headerRow = parsedRows.firstOrNull() ?: return
    val dataRows = parsedRows.drop(1)
    val numColumns = headerRow.size
    val minColumnWidth = 100.dp

    Spacer(modifier = Modifier.height(8.dp))

    // Use BoxWithConstraints to determine if table fits
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .border(1.dp, THEME.glassBorder, RoundedCornerShape(12.dp))
    ) {
        val availableWidth = maxWidth
        val totalMinWidth = minColumnWidth * numColumns
        val fitsInWidth = totalMinWidth <= availableWidth

        val tableContent: @Composable (useWeight: Boolean) -> Unit = { useWeight ->
            Column(
                modifier = if (fitsInWidth) Modifier.fillMaxWidth() else Modifier.horizontalScroll(rememberScrollState())
            ) {
                // Header row
                Row(
                    modifier = Modifier
                        .then(if (fitsInWidth) Modifier.fillMaxWidth() else Modifier)
                        .background(THEME.accentPrimary.copy(alpha = 0.15f))
                        .padding(vertical = 10.dp)
                ) {
                    headerRow.forEach { cell ->
                        Box(
                            modifier = Modifier
                                .then(if (useWeight) Modifier.weight(1f) else Modifier.width(minColumnWidth))
                                .padding(horizontal = 8.dp),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            Text(
                                text = cell.replace("**", ""),
                                color = THEME.accentPrimary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                // Divider after header
                HorizontalDivider(color = THEME.glassBorder, thickness = 1.dp)

                // Data rows
                dataRows.forEachIndexed { index, cells ->
                    val isOptimal = cells.any { it.contains("OPTIMALNO", ignoreCase = true) }
                    val isBoldRow = cells.any { it.startsWith("**") && it.endsWith("**") }
                    val rowBgColor = when {
                        isOptimal || isBoldRow -> THEME.success.copy(alpha = 0.12f)
                        index % 2 == 0 -> THEME.bgCard.copy(alpha = 0.5f)
                        else -> THEME.bgCardElevated.copy(alpha = 0.3f)
                    }

                    Row(
                        modifier = Modifier
                            .then(if (fitsInWidth) Modifier.fillMaxWidth() else Modifier)
                            .background(rowBgColor)
                            .padding(vertical = 8.dp)
                    ) {
                        cells.forEachIndexed { cellIndex, cell ->
                            val cleanCell = cell.replace("**", "")
                            val isCellBold = cell.startsWith("**") && cell.endsWith("**")
                            val isStatusCell = cleanCell.contains("OPTIMALNO", ignoreCase = true)

                            Box(
                                modifier = Modifier
                                    .then(if (useWeight) Modifier.weight(1f) else Modifier.width(minColumnWidth))
                                    .padding(horizontal = 8.dp),
                                contentAlignment = Alignment.CenterStart
                            ) {
                                Text(
                                    text = cleanCell,
                                    color = when {
                                        isStatusCell -> THEME.success
                                        isCellBold || isOptimal -> THEME.textPrimary
                                        cellIndex == 0 -> THEME.textPrimary
                                        else -> THEME.textSecondary
                                    },
                                    fontSize = 11.sp,
                                    fontWeight = if (isCellBold || isStatusCell) FontWeight.Bold else FontWeight.Normal,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }

                    // Divider between rows (except after last)
                    if (index < dataRows.size - 1) {
                        HorizontalDivider(color = THEME.divider.copy(alpha = 0.5f), thickness = 0.5.dp)
                    }
                }
            }
        }

        tableContent(fitsInWidth)
    }

    Spacer(modifier = Modifier.height(8.dp))
}

// Helper extension to handle inline bold (removes ** markers for now)
private fun String.formatInlineBold(): String {
    return this.replace("**", "")
}

// =============================================================================
// TESTOSTERONE DOSE SIMULATOR
// Podaci: Bhasin S, et al. "Testosterone dose-response relationships in healthy
// young men." Am J Physiol Endocrinol Metab 2001;281(6):E1172-81. PMID 11701431.
// DOI 10.1152/ajpendo.2001.281.6.E1172
// =============================================================================

// Jedini TVRDI podaci: 5 stvarno testiranih doza iz studije.
//  serumTNgDl = NADIR (trough) serumska koncentracija T, izmerena pred sledeću
//               nedeljnu injekciju (ng/dL) — nije prosek ni pik.
//  ffmKg      = promena fat-free mase. Na 25 i 50 mg promena NIJE bila značajna
//               (ns) — umesto FFM rasla je masna masa. Značajna tek od 125 mg.
private data class TestoStudyPoint(
    val doseMg: Int,
    val serumTNgDl: Int,
    val ffmKg: Double,
    val ffmSignificant: Boolean
)

private val BHASIN_2001_POINTS = listOf(
    TestoStudyPoint(25, 253, 0.0, false),
    TestoStudyPoint(50, 306, 0.0, false),
    TestoStudyPoint(125, 542, 3.4, true),
    TestoStudyPoint(300, 1345, 5.2, true),
    TestoStudyPoint(600, 2370, 7.9, true)
)

private const val TESTO_MIN_DOSE = 25
private const val TESTO_MAX_DOSE = 600 // gornja granica studije — iznad NEMA podataka

private fun isTestedDose(dose: Int): Boolean = BHASIN_2001_POINTS.any { it.doseMg == dose }

// doza -> serum T: piecewise-linearno KROZ stvarne tačke. Prolazi tačno kroz
// svaku izmerenu vrednost; sve između je model procena (ne podatak studije).
private fun interpolateSerumT(dose: Int): Double {
    val pts = BHASIN_2001_POINTS
    if (dose <= pts.first().doseMg) return pts.first().serumTNgDl.toDouble()
    if (dose >= pts.last().doseMg) return pts.last().serumTNgDl.toDouble()
    for (i in 0 until pts.size - 1) {
        val a = pts[i]; val b = pts[i + 1]
        if (dose >= a.doseMg && dose <= b.doseMg) {
            val r = (dose - a.doseMg).toDouble() / (b.doseMg - a.doseMg)
            return a.serumTNgDl + (b.serumTNgDl - a.serumTNgDl) * r
        }
    }
    return pts.last().serumTNgDl.toDouble()
}

// FFM se u studiji vezuje za log(T) (FFM korelira sa log koncentracijom T,
// r=0.73). Zato interpoliramo piecewise-linearno u ln(serum T) kroz stvarne
// tačke, umesto linearno po dozi.
private fun interpolateFfm(serumT: Double): Double {
    val pts = BHASIN_2001_POINTS
    if (serumT <= pts.first().serumTNgDl) return pts.first().ffmKg
    if (serumT >= pts.last().serumTNgDl) return pts.last().ffmKg
    for (i in 0 until pts.size - 1) {
        val a = pts[i]; val b = pts[i + 1]
        if (serumT >= a.serumTNgDl && serumT <= b.serumTNgDl) {
            val r = (ln(serumT) - ln(a.serumTNgDl.toDouble())) /
                (ln(b.serumTNgDl.toDouble()) - ln(a.serumTNgDl.toDouble()))
            return a.ffmKg + (b.ffmKg - a.ffmKg) * r
        }
    }
    return pts.last().ffmKg
}

// Dozno-zavisni SMER nuspojava iz Bhasin 2001 (hematokrit/Hb rastu, HDL pada).
// Vraća nivo 0..3. DISKRETAN nivo je heuristika aplikacije nad smerom iz studije
// — NIJE numerički podatak studije (tačne po-dozi cifre nisu javno dostupne).
private fun doseRiskLevel(dose: Int): Int = when {
    dose <= 50 -> 0
    dose <= 125 -> 1
    dose <= 300 -> 2
    else -> 3
}

@Composable
private fun TestosteroneDoseSimulator() {
    // Default = 125 mg (tačka infleksije i stvarno testirana doza)
    var dose by remember { mutableIntStateOf(125) }

    val tested = isTestedDose(dose)
    val serumT = remember(dose) { interpolateSerumT(dose) }
    val ffm = remember(dose) { interpolateFfm(serumT) }
    // Studija: FFM promena je značajna tek od 125 mg; na 25/50 mg = ns.
    val ffmNs = dose <= 50
    val gainsPercent = ((ffm / BHASIN_2001_POINTS.last().ffmKg) * 100).roundToInt().coerceIn(0, 100)

    val riskLevel = doseRiskLevel(dose)
    val hctLabel = when (riskLevel) { 0 -> "nizak"; 1 -> "blago povišen"; 2 -> "umeren-povišen"; else -> "visok" }
    val hdlLabel = when (riskLevel) { 0 -> "≈ bez pada"; 1 -> "blag pad"; 2 -> "umeren pad"; else -> "izražen pad" }
    val riskColor = when (riskLevel) { 0 -> THEME.success; 1 -> THEME.accentSecondary; 2 -> THEME.warning; else -> THEME.danger }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(THEME.bgCard)
            .border(1.dp, THEME.glassBorder, RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        // Title
        Text(
            text = "🧪 SIMULATOR DOZE",
            color = THEME.accentPrimary,
            fontSize = UI.subtitleSize,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Bhasin 2001 (PMID 11701431) · 5 testiranih doza · n≈12/grupa",
            color = THEME.textMuted,
            fontSize = 10.sp
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Doza + oznaka izvora (podatak iz studije vs procena)
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "Nedeljno:", color = THEME.textSecondary, fontSize = UI.bodySize)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "$dose mg",
                color = THEME.textPrimary,
                fontSize = UI.subtitleSize,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.weight(1f))
            SourceBadge(tested = tested)
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Slider 25..600 — cap na 600 (gornja granica studije)
        Slider(
            value = dose.toFloat(),
            onValueChange = { dose = it.roundToInt().coerceIn(TESTO_MIN_DOSE, TESTO_MAX_DOSE) },
            valueRange = TESTO_MIN_DOSE.toFloat()..TESTO_MAX_DOSE.toFloat(),
            colors = SliderDefaults.colors(
                thumbColor = THEME.accentPrimary,
                activeTrackColor = THEME.accentPrimary,
                inactiveTrackColor = THEME.glassBorder
            )
        )
        // Brzi izbor stvarno testiranih doza
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            BHASIN_2001_POINTS.forEach { p ->
                Text(
                    text = "${p.doseMg}",
                    color = if (dose == p.doseMg) THEME.accentPrimary else THEME.textMuted,
                    fontSize = 11.sp,
                    fontWeight = if (dose == p.doseMg) FontWeight.Bold else FontWeight.Normal,
                    modifier = Modifier.clickable { dose = p.doseMg }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Rezultati: Serum T (nadir) + FFM
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            ResultBox(
                label = "Serum T (nadir)",
                value = "${serumT.roundToInt()} ng/dL",
                subValue = if (tested) "podatak iz studije" else "procena (model)",
                color = THEME.accentSecondary
            )
            ResultBox(
                label = "FFM promena",
                value = if (ffmNs) "≈ 0" else "+${String.format(Locale.getDefault(), "%.1f", ffm)} kg",
                subValue = when {
                    ffmNs -> "bez zn. promene (ns)"
                    tested -> "$gainsPercent% od 600mg"
                    else -> "procena · $gainsPercent% od 600mg"
                },
                color = if (ffmNs) THEME.textMuted else THEME.success
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Nuspojave — dozno-zavisni SMER (Bhasin 2001), bez izmišljenog jedinstvenog %
        Text(
            text = "NUSPOJAVE (smer, dozno-zavisno)",
            color = THEME.textSecondary,
            fontSize = UI.captionSize,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(6.dp))
        RiskIndicatorRow(label = "Hematokrit / Hb ↑  (rizik eritrocitoze)", levelLabel = hctLabel, color = riskColor)
        Spacer(modifier = Modifier.height(4.dp))
        RiskIndicatorRow(label = "HDL holesterol ↓", levelLabel = hdlLabel, color = riskColor)
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "PSA, raspoloženje i seksualna funkcija: bez značajne promene ni na jednoj dozi (Bhasin 2001).",
            color = THEME.textMuted,
            fontSize = 10.sp
        )
        Text(
            text = "Nivo rizika je heuristika aplikacije nad smerom iz studije — nije numerička cifra iz studije.",
            color = THEME.textMuted,
            fontSize = 10.sp
        )

        // Upozorenje na granicu validiranog opsega
        if (dose >= TESTO_MAX_DOSE) {
            Spacer(modifier = Modifier.height(8.dp))
            WarnBox("600 mg/ned je gornja granica studije. Za veće doze NEMA podataka — vrednosti se ne ekstrapoliraju.")
        }

        Spacer(modifier = Modifier.height(10.dp))
        // Napomena o populaciji (ne prevodi se 1:1)
        Text(
            text = "⚠️ Ispitanici: mladi zdravi muškarci (18–35 g) sa POTPUNO ugašenim endogenim T (GnRH agonist) pre početka. Rezultati se ne prevode 1:1 na TRT kod starijih, druge estre/protokole, ni na osobe sa komorbiditetima.",
            color = THEME.textMuted,
            fontSize = 10.sp
        )
    }
}

@Composable
private fun SourceBadge(tested: Boolean) {
    val color = if (tested) THEME.success else THEME.warning
    Text(
        text = if (tested) "IZ STUDIJE" else "PROCENA (MODEL)",
        color = color,
        fontSize = 10.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(color.copy(alpha = 0.15f))
            .padding(horizontal = 8.dp, vertical = 3.dp)
    )
}

@Composable
private fun RiskIndicatorRow(
    label: String,
    levelLabel: String,
    color: androidx.compose.ui.graphics.Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            color = THEME.textSecondary,
            fontSize = UI.captionSize,
            modifier = Modifier.weight(1f)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = levelLabel,
            color = color,
            fontSize = UI.captionSize,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .clip(RoundedCornerShape(6.dp))
                .background(color.copy(alpha = 0.15f))
                .padding(horizontal = 8.dp, vertical = 3.dp)
        )
    }
}

@Composable
private fun WarnBox(text: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(THEME.danger.copy(alpha = 0.15f))
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Text(
            text = "⚠️ $text",
            color = THEME.danger,
            fontSize = UI.captionSize,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun ResultBox(
    label: String,
    value: String,
    subValue: String?,
    color: androidx.compose.ui.graphics.Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(THEME.bgCardElevated)
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Text(
            text = label,
            color = THEME.textMuted,
            fontSize = 10.sp
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            color = color,
            fontSize = UI.subtitleSize,
            fontWeight = FontWeight.Bold
        )
        if (subValue != null) {
            Text(
                text = subValue,
                color = THEME.textMuted,
                fontSize = 10.sp
            )
        }
    }
}
