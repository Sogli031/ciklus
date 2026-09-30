package com.injekcije.app.ui

import com.injekcije.app.ui.components.GlassCard
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.injekcije.app.THEME
import com.injekcije.app.UI
import com.injekcije.app.Strings
import com.injekcije.app.data.*
import com.injekcije.app.notifications.schedulePCTNotifications
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

private val DATE_FORMAT: DateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")

/**
 * PCT Guide Card - shown on MainScreen when cycle is finished or PCT is active.
 */
@Composable
fun PCTGuideCard(
    pctInfo: PCTGuideInfo?,
    finishedCycle: Cycle?,
    modifier: Modifier = Modifier,
    onSetupPCT: (String) -> Unit,
    onViewPCT: (PCTGuideInfo) -> Unit
) {
    if (pctInfo == null && finishedCycle == null) return

    val borderColor by animateColorAsState(
        targetValue = pctInfo?.status?.color ?: THEME.accentSecondary,
        label = "pct_border"
    )

    GlassCard(
        modifier = modifier.fillMaxWidth(),
        borderColor = borderColor
    ) {
        Column(Modifier.padding(UI.cardPadding)) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Refresh,
                        contentDescription = null,
                        tint = pctInfo?.status?.color ?: THEME.accentSecondary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "PCT Vodič",
                        color = THEME.textPrimary,
                        fontSize = UI.subtitleSize,
                        fontWeight = FontWeight.Bold
                    )
                }

                if (pctInfo != null) {
                    PCTStatusBadge(pctInfo.status)
                }
            }

            Spacer(Modifier.height(12.dp))

            if (pctInfo != null) {
                // Active PCT info
                PCTActiveContent(pctInfo, onViewPCT)
            } else if (finishedCycle != null) {
                // Setup PCT prompt
                PCTSetupPrompt(finishedCycle, onSetupPCT)
            }
        }
    }
}

@Composable
private fun PCTActiveContent(info: PCTGuideInfo, onViewDetails: (PCTGuideInfo) -> Unit) {
    val today = LocalDate.now()

    when (info.status) {
        PCTStatus.WAITING -> {
            val daysLeft = info.daysUntilStart
            Text(
                "Čekaj još $daysLeft dana pre početka PCT-a",
                color = THEME.textSecondary,
                fontSize = UI.bodySize
            )
            Text(
                "Ester: ${info.pctData.longestEster.displayName}",
                color = THEME.textMuted,
                fontSize = UI.smallSize
            )
            Spacer(Modifier.height(8.dp))

            // Progress bar for waiting period
            val totalWait = info.pctData.longestEster.pctWaitDays
            val elapsed = totalWait - daysLeft.toInt()
            val progress = (elapsed.toFloat() / totalWait).coerceIn(0f, 1f)

            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                color = THEME.accentPrimary,
                trackColor = THEME.glassBorder
            )

            Text(
                "Početak PCT: ${info.pctData.recommendedStartDate.format(DATE_FORMAT)}",
                color = THEME.accentPrimary,
                fontSize = UI.smallSize,
                modifier = Modifier.padding(top = 4.dp)
            )
        }

        PCTStatus.START_SOON -> {
            Text(
                "PCT počinje za ${info.daysUntilStart} dana",
                color = THEME.warning,
                fontSize = UI.bodySize,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(4.dp))
            Text(
                "Pripremi: ${info.pctData.protocol.displayName}",
                color = THEME.textSecondary,
                fontSize = UI.smallSize
            )
        }

        PCTStatus.START_NOW -> {
            Text(
                "Vreme je za PCT",
                color = THEME.success,
                fontSize = UI.bodySize,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(8.dp))
            ProtocolSummary(info.pctData.protocol)
        }

        PCTStatus.IN_PROGRESS -> {
            val daysSince = info.daysSinceStart ?: 0
            val weeksIn = (daysSince / 7).toInt()

            Text(
                "PCT u toku: Nedelja ${weeksIn + 1}",
                color = THEME.accentPrimary,
                fontSize = UI.bodySize,
                fontWeight = FontWeight.Medium
            )

            Spacer(Modifier.height(8.dp))

            // Progress bar (3 months = 100%)
            LinearProgressIndicator(
                progress = { info.progressPercent },
                modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                color = THEME.accentPrimary,
                trackColor = THEME.glassBorder
            )

            val minWeeks = info.compoundAdvice?.recommendedDurationWeeks ?: 12
            val minMonths = "%.0f".format(minWeeks / 4.3)
            Text(
                "${(info.progressPercent * 100).toInt()}% od minimalnog trajanja (~$minMonths meseci)",
                color = THEME.textMuted,
                fontSize = UI.smallSize,
                modifier = Modifier.padding(top = 4.dp)
            )
        }

        PCTStatus.BLOODWORK_DUE -> {
            Text(
                "Vreme za krvnu sliku",
                color = THEME.warning,
                fontSize = UI.bodySize,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(4.dp))
            Text(
                "Testiraj: Testosteron, LH, FSH, Estradiol",
                color = THEME.textSecondary,
                fontSize = UI.smallSize
            )
        }

        PCTStatus.COMPLETED -> {
            Text(
                "PCT završen",
                color = THEME.success,
                fontSize = UI.bodySize,
                fontWeight = FontWeight.Medium
            )
        }

        else -> {}
    }

    Spacer(Modifier.height(12.dp))

    Button(
        onClick = { onViewDetails(info) },
        modifier = Modifier.fillMaxWidth(),
        colors = ButtonDefaults.buttonColors(
            containerColor = THEME.accentPrimary.copy(alpha = 0.2f),
            contentColor = THEME.accentPrimary
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Icon(Icons.Default.Info, null, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(8.dp))
        Text("Detalji i protokol")
    }
}

@Composable
private fun PCTSetupPrompt(cycle: Cycle, onSetup: (String) -> Unit) {
    Text(
        "Ciklus \"${cycle.name}\" je završen",
        color = THEME.textSecondary,
        fontSize = UI.bodySize
    )
    Text(
        "Završen: ${cycle.endDate.format(DATE_FORMAT)}",
        color = THEME.textMuted,
        fontSize = UI.smallSize
    )

    Spacer(Modifier.height(12.dp))

    Button(
        onClick = { onSetup(cycle.id) },
        modifier = Modifier.fillMaxWidth(),
        colors = ButtonDefaults.buttonColors(THEME.accentSecondary, THEME.textDark),
        shape = RoundedCornerShape(8.dp)
    ) {
        Icon(Icons.Default.PlayArrow, null, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(8.dp))
        Text("Pokreni PCT vodič")
    }
}

@Composable
private fun ProtocolSummary(protocol: PCTProtocolType) {
    Surface(
        color = THEME.bgCard,
        shape = RoundedCornerShape(8.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, THEME.glassBorder)
    ) {
        Column(Modifier.padding(12.dp)) {
            Text(
                protocol.displayName,
                color = THEME.textPrimary,
                fontSize = UI.bodySize,
                fontWeight = FontWeight.Medium
            )
            Spacer(Modifier.height(4.dp))
            Text(
                "Uspešnost: ${(protocol.successRate * 100).toInt()}%",
                color = THEME.success,
                fontSize = UI.smallSize
            )
        }
    }
}

@Composable
private fun PCTStatusBadge(status: PCTStatus) {
    val text = when (status) {
        PCTStatus.NOT_NEEDED -> "Nije potrebno"
        PCTStatus.WAITING -> "Čekanje"
        PCTStatus.START_SOON -> "Uskoro"
        PCTStatus.START_NOW -> "Počni"
        PCTStatus.IN_PROGRESS -> "U toku"
        PCTStatus.BLOODWORK_DUE -> "Krvna slika"
        PCTStatus.COMPLETED -> "Završeno"
    }

    Box(
        Modifier
            .background(status.color.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text,
            color = status.color,
            fontSize = UI.smallSize,
            fontWeight = FontWeight.Medium
        )
    }
}

/**
 * PCT Setup Dialog - shown when user sets up PCT for a finished cycle.
 */
@Composable
fun PCTSetupDialog(
    cycleId: String,
    repo: InjectionRepository,
    onDismiss: () -> Unit,
    onPCTCreated: (PCTData) -> Unit
) {
    val context = LocalContext.current
    val cycle = remember { repo.getCycle(cycleId) }
    val injections = remember { repo.getInjectionsForCycle(cycleId) }
    val lastInjection = injections.maxByOrNull { it.dateTime }

    // Generate compound-specific advice
    val compoundAdvice = remember {
        cycle?.compounds?.map { it.substanceId }?.let { ids ->
            if (ids.isNotEmpty()) CyclePCTAdvice.fromSubstanceIds(ids) else null
        }
    }

    // Set protocol based on compound advice
    var selectedProtocol by remember {
        mutableStateOf(compoundAdvice?.recommendedProtocol ?: PCTProtocolType.CLOMIPHENE_ONLY)
    }
    var lastInjectionDate by remember {
        mutableStateOf(
            lastInjection?.dateTime?.toLocalDate()
                ?: cycle?.endDate
                ?: LocalDate.now()
        )
    }

    // Detect longest ester
    val longestEster = remember {
        cycle?.compounds
            ?.map { EsterType.fromSubstanceId(it.substanceId) }
            ?.maxByOrNull { it.pctWaitDays }
            ?: EsterType.ENANTHATE
    }

    val recommendedStart = lastInjectionDate.plusDays(longestEster.pctWaitDays.toLong())
    val daysUntilPCT = ChronoUnit.DAYS.between(LocalDate.now(), recommendedStart)

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.9f),
            shape = RoundedCornerShape(16.dp),
            color = THEME.bgPrimary
        ) {
            Column(
                Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Podešavanje PCT vodiča",
                        color = THEME.textPrimary,
                        fontSize = UI.titleSize,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Zatvori PCT podešavanje", tint = THEME.textMuted)
                    }
                }

                Spacer(Modifier.height(16.dp))

                Column(
                    Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                ) {
                    // Cycle info
                    GlassCard(Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(16.dp)) {
                            Text("Ciklus", color = THEME.textMuted, fontSize = UI.captionSize)
                            Text(
                                cycle?.name ?: "Nepoznat",
                                color = THEME.textPrimary,
                                fontSize = UI.subtitleSize,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    Spacer(Modifier.height(16.dp))

                    // Compound-specific warnings
                    compoundAdvice?.let { advice ->
                        if (advice.warnings.isNotEmpty()) {
                            Surface(
                                color = THEME.danger.copy(alpha = 0.1f),
                                shape = RoundedCornerShape(12.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, THEME.danger.copy(alpha = 0.5f))
                            ) {
                                Column(Modifier.padding(16.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            Icons.Default.Warning,
                                            null,
                                            tint = THEME.danger,
                                            modifier = Modifier.size(24.dp)
                                        )
                                        Spacer(Modifier.width(8.dp))
                                        Text(
                                            "Upozorenja za tvoj ciklus",
                                            color = THEME.danger,
                                            fontSize = UI.bodySize,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    Spacer(Modifier.height(12.dp))
                                    advice.warnings.forEach { warning ->
                                        Text(
                                            warning,
                                            color = THEME.textPrimary,
                                            fontSize = UI.smallSize,
                                            modifier = Modifier.padding(vertical = 4.dp)
                                        )
                                    }
                                }
                            }
                            Spacer(Modifier.height(16.dp))
                        }

                        // Compound analysis
                        GlassCard(
                            Modifier.fillMaxWidth(),
                            borderColor = when (advice.worstSuppression) {
                                SuppressionLevel.EXTREME -> THEME.danger
                                SuppressionLevel.SEVERE -> THEME.warning
                                else -> THEME.accentPrimary
                            }
                        ) {
                            Column(Modifier.padding(16.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Default.Analytics,
                                        null,
                                        tint = THEME.accentPrimary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Text(
                                        "Analiza ciklusa",
                                        color = THEME.accentPrimary,
                                        fontSize = UI.bodySize,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                                Spacer(Modifier.height(12.dp))

                                Text("Detektovane komponente:", color = THEME.textMuted, fontSize = UI.captionSize)
                                advice.compounds.forEach { compound ->
                                    Row(
                                        Modifier.padding(vertical = 2.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            Modifier
                                                .size(8.dp)
                                                .background(
                                                    when (compound.suppressionLevel) {
                                                        SuppressionLevel.EXTREME -> THEME.danger
                                                        SuppressionLevel.SEVERE -> THEME.warning
                                                        SuppressionLevel.MODERATE -> THEME.accentPrimary
                                                        SuppressionLevel.MILD -> THEME.success
                                                    },
                                                    CircleShape
                                                )
                                        )
                                        Spacer(Modifier.width(8.dp))
                                        Text(
                                            compound.displayName,
                                            color = THEME.textPrimary,
                                            fontSize = UI.smallSize
                                        )
                                        Spacer(Modifier.width(8.dp))
                                        Text(
                                            "(${compound.suppressionLevel.displayName})",
                                            color = THEME.textMuted,
                                            fontSize = UI.captionSize
                                        )
                                    }
                                }

                                Spacer(Modifier.height(12.dp))
                                HorizontalDivider(color = THEME.glassBorder)
                                Spacer(Modifier.height(12.dp))

                                Row {
                                    Column(Modifier.weight(1f)) {
                                        Text("Ukupna supresija", color = THEME.textMuted, fontSize = UI.captionSize)
                                        Text(
                                            advice.worstSuppression.displayName,
                                            color = when (advice.worstSuppression) {
                                                SuppressionLevel.EXTREME -> THEME.danger
                                                SuppressionLevel.SEVERE -> THEME.warning
                                                else -> THEME.textPrimary
                                            },
                                            fontSize = UI.smallSize,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                    Column(Modifier.weight(1f)) {
                                        Text("hCG potreban", color = THEME.textMuted, fontSize = UI.captionSize)
                                        Text(
                                            if (advice.requiresHCG) "DA" else "Ne",
                                            color = if (advice.requiresHCG) THEME.warning else THEME.textSecondary,
                                            fontSize = UI.smallSize,
                                            fontWeight = if (advice.requiresHCG) FontWeight.Bold else FontWeight.Normal
                                        )
                                    }
                                }
                            }
                        }
                        Spacer(Modifier.height(16.dp))
                    }

                    // Last injection date
                    GlassCard(Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(16.dp)) {
                            Text("Poslednja injekcija", color = THEME.textMuted, fontSize = UI.captionSize)
                            Text(
                                lastInjectionDate.format(DATE_FORMAT),
                                color = THEME.textPrimary,
                                fontSize = UI.subtitleSize,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                "Detektovan ester: ${longestEster.displayName} (${longestEster.pctWaitDays} dana čekanja)",
                                color = THEME.accentPrimary,
                                fontSize = UI.smallSize
                            )
                        }
                    }

                    Spacer(Modifier.height(16.dp))

                    // PCT Start date calculation
                    GlassCard(
                        Modifier.fillMaxWidth(),
                        borderColor = if (daysUntilPCT <= 0) THEME.success else THEME.accentPrimary
                    ) {
                        Column(Modifier.padding(16.dp)) {
                            Text("Preporučeni početak PCT-a", color = THEME.textMuted, fontSize = UI.captionSize)
                            Text(
                                recommendedStart.format(DATE_FORMAT),
                                color = if (daysUntilPCT <= 0) THEME.success else THEME.accentPrimary,
                                fontSize = UI.subtitleSize,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                if (daysUntilPCT > 0) "Za $daysUntilPCT dana"
                                else if (daysUntilPCT == 0L) "Danas"
                                else "Pre ${-daysUntilPCT} dana",
                                color = if (daysUntilPCT <= 0) THEME.success else THEME.textSecondary,
                                fontSize = UI.smallSize
                            )
                        }
                    }

                    Spacer(Modifier.height(24.dp))

                    // Protocol selection
                    Text(
                        "Izaberi protokol",
                        color = THEME.textPrimary,
                        fontSize = UI.subtitleSize,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        "Preporuka nauke: Clomiphene + hCG (87.5% uspešnost)",
                        color = THEME.textMuted,
                        fontSize = UI.smallSize
                    )

                    Spacer(Modifier.height(12.dp))

                    PCTProtocolType.entries.filter { it != PCTProtocolType.NO_PCT }.forEach { protocol ->
                        ProtocolOption(
                            protocol = protocol,
                            isSelected = selectedProtocol == protocol,
                            onSelect = { selectedProtocol = protocol }
                        )
                        Spacer(Modifier.height(8.dp))
                    }

                    Spacer(Modifier.height(16.dp))

                    // Evidence box
                    Surface(
                        color = THEME.bgCard,
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, THEME.glassBorder)
                    ) {
                        Column(Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.Science,
                                    null,
                                    tint = THEME.accentPrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    "Naučna osnova",
                                    color = THEME.accentPrimary,
                                    fontSize = UI.bodySize,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                            Spacer(Modifier.height(8.dp))
                            Text(
                                """
                                BJU International 2026 studija:
                                • 79 bodibildera praćeno 12 meseci
                                • CC + hCG: 87.5% normozoospermija
                                • CC samo: 69.2% normozoospermija
                                • Bez PCT: 58.6% normozoospermija

                                Clomiphene 25mg dnevno je naučno dokazan standard.
                                """.trimIndent(),
                                color = THEME.textSecondary,
                                fontSize = UI.smallSize
                            )
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))

                // Confirm button
                Button(
                    onClick = {
                        val pctData = PCTData(
                            cycleId = cycleId,
                            lastInjectionDate = lastInjectionDate,
                            longestEster = longestEster,
                            protocol = selectedProtocol
                        )
                        repo.updatePCTData(pctData)
                        // Schedule PCT notifications
                        schedulePCTNotifications(context, repo)
                        onPCTCreated(pctData)
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    colors = ButtonDefaults.buttonColors(THEME.accentPrimary, THEME.textDark),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Check, null, modifier = Modifier.size(24.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Pokreni PCT praćenje", fontSize = UI.subtitleSize, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun ProtocolOption(
    protocol: PCTProtocolType,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    val borderColor = if (isSelected) THEME.accentPrimary else THEME.glassBorder
    val bgColor = if (isSelected) THEME.accentPrimary.copy(alpha = 0.1f) else THEME.bgCard

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelect() },
        color = bgColor,
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(
            width = if (isSelected) 2.dp else 1.dp,
            color = borderColor
        )
    ) {
        Row(
            Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RadioButton(
                selected = isSelected,
                onClick = onSelect,
                colors = RadioButtonDefaults.colors(
                    selectedColor = THEME.accentPrimary,
                    unselectedColor = THEME.textMuted
                )
            )
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    protocol.displayName,
                    color = THEME.textPrimary,
                    fontSize = UI.bodySize,
                    fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal
                )
                Text(
                    "Uspešnost: ${(protocol.successRate * 100).toInt()}%",
                    color = if (protocol == PCTProtocolType.CLOMIPHENE_HCG) THEME.success else THEME.textMuted,
                    fontSize = UI.smallSize
                )
            }
            if (protocol == PCTProtocolType.CLOMIPHENE_HCG) {
                Text(
                    "PREPORUKA",
                    color = THEME.success,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

/**
 * PCT Details Dialog - shows full PCT info and allows actions.
 */
@Composable
fun PCTDetailsDialog(
    info: PCTGuideInfo,
    repo: InjectionRepository,
    onDismiss: () -> Unit
) {
    var currentInfo by remember { mutableStateOf(info) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.9f),
            shape = RoundedCornerShape(16.dp),
            color = THEME.bgPrimary
        ) {
            Column(
                Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Refresh,
                            null,
                            tint = currentInfo.status.color,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(Modifier.width(12.dp))
                        Text(
                            "PCT detalji",
                            color = THEME.textPrimary,
                            fontSize = UI.titleSize,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Zatvori PCT detalje", tint = THEME.textMuted)
                    }
                }

                Spacer(Modifier.height(16.dp))

                Column(
                    Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                ) {
                    // Status card
                    GlassCard(
                        Modifier.fillMaxWidth(),
                        borderColor = currentInfo.status.color
                    ) {
                        Column(Modifier.padding(16.dp)) {
                            Row(
                                Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Status", color = THEME.textMuted, fontSize = UI.captionSize)
                                PCTStatusBadge(currentInfo.status)
                            }
                            Spacer(Modifier.height(8.dp))
                            Text(
                                currentInfo.phase.displayName,
                                color = THEME.textPrimary,
                                fontSize = UI.subtitleSize,
                                fontWeight = FontWeight.Bold
                            )

                            if (currentInfo.daysSinceStart != null) {
                                Spacer(Modifier.height(8.dp))
                                LinearProgressIndicator(
                                    progress = { currentInfo.progressPercent },
                                    modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                                    color = THEME.accentPrimary,
                                    trackColor = THEME.glassBorder
                                )
                                val minDays = (currentInfo.compoundAdvice?.recommendedDurationWeeks ?: 12) * 7
                                Text(
                                    "Dan ${currentInfo.daysSinceStart} / $minDays (minimum)",
                                    color = THEME.textMuted,
                                    fontSize = UI.smallSize,
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(16.dp))

                    // Protocol details
                    GlassCard(Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(16.dp)) {
                            Text("Protokol", color = THEME.textMuted, fontSize = UI.captionSize)
                            Text(
                                currentInfo.pctData.protocol.displayName,
                                color = THEME.textPrimary,
                                fontSize = UI.subtitleSize,
                                fontWeight = FontWeight.Medium
                            )
                            Spacer(Modifier.height(8.dp))
                            Text(
                                currentInfo.protocolDetails,
                                color = THEME.textSecondary,
                                fontSize = UI.smallSize
                            )
                        }
                    }

                    Spacer(Modifier.height(16.dp))

                    // Compound-specific advice section
                    currentInfo.compoundAdvice?.let { advice ->
                        // Warnings first
                        if (advice.warnings.isNotEmpty()) {
                            Surface(
                                color = THEME.danger.copy(alpha = 0.1f),
                                shape = RoundedCornerShape(12.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, THEME.danger.copy(alpha = 0.5f))
                            ) {
                                Column(Modifier.padding(16.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            Icons.Default.Warning,
                                            null,
                                            tint = THEME.danger,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(Modifier.width(8.dp))
                                        Text(
                                            "Upozorenja",
                                            color = THEME.danger,
                                            fontSize = UI.bodySize,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    Spacer(Modifier.height(8.dp))
                                    advice.warnings.forEach { warning ->
                                        Text(
                                            warning,
                                            color = THEME.textPrimary,
                                            fontSize = UI.smallSize,
                                            modifier = Modifier.padding(vertical = 2.dp)
                                        )
                                    }
                                }
                            }
                            Spacer(Modifier.height(16.dp))
                        }

                        // Personalized advice
                        GlassCard(
                            Modifier.fillMaxWidth(),
                            borderColor = THEME.accentSecondary
                        ) {
                            Column(Modifier.padding(16.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Default.Lightbulb,
                                        null,
                                        tint = THEME.accentSecondary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Text(
                                        "Personalizovani saveti",
                                        color = THEME.accentSecondary,
                                        fontSize = UI.bodySize,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    "Na osnovu komponenti u tvom ciklusu",
                                    color = THEME.textMuted,
                                    fontSize = UI.captionSize
                                )
                                Spacer(Modifier.height(12.dp))

                                // Detected compounds
                                Text("Komponente:", color = THEME.textMuted, fontSize = UI.captionSize)
                                advice.compounds.forEach { compound ->
                                    Row(
                                        Modifier.padding(vertical = 2.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            Modifier
                                                .size(6.dp)
                                                .background(
                                                    when (compound.suppressionLevel) {
                                                        SuppressionLevel.EXTREME -> THEME.danger
                                                        SuppressionLevel.SEVERE -> THEME.warning
                                                        SuppressionLevel.MODERATE -> THEME.accentPrimary
                                                        SuppressionLevel.MILD -> THEME.success
                                                    },
                                                    CircleShape
                                                )
                                        )
                                        Spacer(Modifier.width(8.dp))
                                        Text(
                                            compound.displayName,
                                            color = THEME.textPrimary,
                                            fontSize = UI.smallSize
                                        )
                                    }
                                }

                                Spacer(Modifier.height(12.dp))
                                HorizontalDivider(color = THEME.glassBorder)
                                Spacer(Modifier.height(12.dp))

                                // Main recommendations
                                advice.advice.take(6).forEach { adviceItem ->
                                    if (adviceItem.startsWith("\n")) {
                                        Spacer(Modifier.height(8.dp))
                                        Text(
                                            adviceItem.trim(),
                                            color = THEME.accentPrimary,
                                            fontSize = UI.smallSize,
                                            fontWeight = FontWeight.Medium
                                        )
                                    } else {
                                        Text(
                                            adviceItem,
                                            color = THEME.textSecondary,
                                            fontSize = UI.smallSize,
                                            modifier = Modifier.padding(vertical = 2.dp)
                                        )
                                    }
                                }
                            }
                        }
                        Spacer(Modifier.height(16.dp))
                    }

                    // Timeline
                    GlassCard(Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(16.dp)) {
                            Text("Vremenski okvir", color = THEME.textMuted, fontSize = UI.captionSize)
                            Spacer(Modifier.height(8.dp))

                            TimelineItem(
                                "Poslednja injekcija",
                                currentInfo.pctData.lastInjectionDate.format(DATE_FORMAT),
                                true
                            )

                            TimelineItem(
                                "Preporučeni početak PCT-a",
                                currentInfo.pctData.recommendedStartDate.format(DATE_FORMAT),
                                currentInfo.pctData.pctStartDate != null || LocalDate.now() >= currentInfo.pctData.recommendedStartDate
                            )

                            currentInfo.pctData.pctStartDate?.let { startDate ->
                                TimelineItem(
                                    "Stvarni početak PCT-a",
                                    startDate.format(DATE_FORMAT),
                                    true
                                )
                            }

                            currentInfo.nextBloodworkDate?.let { bloodworkDate ->
                                TimelineItem(
                                    "Sledeća krvna slika",
                                    bloodworkDate.format(DATE_FORMAT),
                                    false,
                                    highlight = true
                                )
                            }

                            val minWeeksTimeline = currentInfo.compoundAdvice?.recommendedDurationWeeks ?: 12
                            val minMonthsTimeline = "%.0f".format(minWeeksTimeline / 4.3)
                            TimelineItem(
                                "Minimum trajanje (~$minMonthsTimeline mes.)",
                                currentInfo.pctData.pctStartDate?.plusWeeks(minWeeksTimeline.toLong())?.format(DATE_FORMAT) ?: "-",
                                currentInfo.progressPercent >= 1f
                            )
                        }
                    }

                    Spacer(Modifier.height(16.dp))

                    // Bloodwork history
                    if (currentInfo.pctData.bloodworkDates.isNotEmpty()) {
                        GlassCard(Modifier.fillMaxWidth()) {
                            Column(Modifier.padding(16.dp)) {
                                Text("Krvne slike", color = THEME.textMuted, fontSize = UI.captionSize)
                                Spacer(Modifier.height(8.dp))
                                currentInfo.pctData.bloodworkDates.forEach { date ->
                                    Row(
                                        Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            Icons.Default.Check,
                                            null,
                                            tint = THEME.success,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(Modifier.width(8.dp))
                                        Text(
                                            date.format(DATE_FORMAT),
                                            color = THEME.textSecondary,
                                            fontSize = UI.smallSize
                                        )
                                    }
                                }
                            }
                        }
                        Spacer(Modifier.height(16.dp))
                    }

                    // Action buttons based on status
                    when (currentInfo.status) {
                        PCTStatus.START_NOW, PCTStatus.START_SOON -> {
                            Button(
                                onClick = {
                                    repo.startPCT(currentInfo.pctData.cycleId)
                                    repo.getPCTGuideInfo(currentInfo.pctData.cycleId)?.let {
                                        currentInfo = it
                                    }
                                },
                                modifier = Modifier.fillMaxWidth().height(56.dp),
                                colors = ButtonDefaults.buttonColors(THEME.success, THEME.textDark),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.PlayArrow, null, modifier = Modifier.size(24.dp))
                                Spacer(Modifier.width(8.dp))
                                Text("Počni PCT danas", fontSize = UI.subtitleSize, fontWeight = FontWeight.Bold)
                            }
                        }

                        PCTStatus.IN_PROGRESS, PCTStatus.BLOODWORK_DUE -> {
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                Button(
                                    onClick = {
                                        repo.addPCTBloodwork(currentInfo.pctData.cycleId)
                                        repo.getPCTGuideInfo(currentInfo.pctData.cycleId)?.let {
                                            currentInfo = it
                                        }
                                    },
                                    modifier = Modifier.weight(1f).height(48.dp),
                                    colors = ButtonDefaults.buttonColors(THEME.accentSecondary, THEME.textDark),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Icon(Icons.Default.Bloodtype, null, Modifier.size(18.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text("Dodaj krvnu sliku", fontSize = UI.smallSize)
                                }

                                if (currentInfo.progressPercent >= 1f) {
                                    Button(
                                        onClick = {
                                            repo.completePCT(currentInfo.pctData.cycleId)
                                            repo.getPCTGuideInfo(currentInfo.pctData.cycleId)?.let {
                                                currentInfo = it
                                            }
                                        },
                                        modifier = Modifier.weight(1f).height(48.dp),
                                        colors = ButtonDefaults.buttonColors(THEME.success, THEME.textDark),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Icon(Icons.Default.Check, null, Modifier.size(18.dp))
                                        Spacer(Modifier.width(4.dp))
                                        Text("Završi PCT", fontSize = UI.smallSize)
                                    }
                                }
                            }
                        }

                        else -> {}
                    }
                }
            }
        }
    }
}

@Composable
private fun TimelineItem(
    label: String,
    date: String,
    completed: Boolean,
    highlight: Boolean = false
) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            Modifier
                .size(12.dp)
                .background(
                    if (completed) THEME.success
                    else if (highlight) THEME.warning
                    else THEME.glassBorder,
                    CircleShape
                )
        )
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(
                label,
                color = if (highlight) THEME.warning else THEME.textSecondary,
                fontSize = UI.smallSize
            )
            Text(
                date,
                color = if (highlight) THEME.warning else THEME.textPrimary,
                fontSize = UI.bodySize,
                fontWeight = if (highlight) FontWeight.Medium else FontWeight.Normal
            )
        }
        if (completed) {
            Icon(Icons.Default.Check, null, tint = THEME.success, modifier = Modifier.size(16.dp))
        }
    }
}
