package com.injekcije.app.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.injekcije.app.THEME
import com.injekcije.app.UI
import com.injekcije.app.Strings
import com.injekcije.app.data.*
import com.injekcije.app.domain.DoseCalculator
import com.injekcije.app.ui.components.GlassCard
import com.injekcije.app.ui.components.StatusBadge
import com.injekcije.app.ui.components.DATE_TIME_FORMAT
import com.injekcije.app.ui.components.TIME_FORMAT
import com.injekcije.app.ui.components.DATE_TIME_SHORT_FORMAT
import java.util.Locale

@Composable
fun HeaderSection(cycle: Cycle?, onSettings: () -> Unit, onLearning: () -> Unit, strings: Strings) {
    Row(Modifier.fillMaxWidth().height(UI.headerHeight), Arrangement.SpaceBetween, Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text(
                cycle?.name ?: strings.injections,
                color = THEME.textPrimary,
                fontSize = UI.titleSize,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (cycle != null) Text("${strings.week} ${cycle.currentWeek()} / ${cycle.durationWeeks}", color = THEME.accentPrimary, fontSize = UI.captionSize)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(UI.smallSpacing)) {
            IconButton(onClick = onLearning) {
                Icon(Icons.AutoMirrored.Filled.MenuBook, contentDescription = "Otvori edukaciju", tint = THEME.textMuted)
            }
            IconButton(onClick = onSettings) {
                Icon(Icons.Default.Settings, contentDescription = "Otvori podešavanja", tint = THEME.textMuted)
            }
        }
    }
}

@Composable
fun NoCycleCard(strings: Strings, onSetup: () -> Unit) {
    GlassCard(Modifier.fillMaxWidth()) {
        Column(Modifier.fillMaxWidth().padding(UI.largeSpacing), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(strings.noActiveCycle, color = THEME.textPrimary, fontSize = UI.subtitleSize, fontWeight = FontWeight.Medium)
            Spacer(Modifier.height(UI.defaultSpacing))
            Text(strings.createCycleToStart, color = THEME.textMuted, fontSize = UI.bodySize, textAlign = TextAlign.Center)
            Spacer(Modifier.height(UI.mediumSpacing))
            Button(onClick = onSetup, colors = ButtonDefaults.buttonColors(THEME.accentPrimary, THEME.textDark)) {
                Icon(Icons.Default.Add, null); Spacer(Modifier.width(UI.smallSpacing)); Text(strings.newCycle)
            }
        }
    }
}

@Composable
fun NextInjectionCard(info: NextInjectionInfo?, modifier: Modifier, strings: Strings, onClick: (NextInjectionInfo?) -> Unit) {
    val borderColor by animateColorAsState(info?.status?.color ?: THEME.glassBorder, label = "border")
    GlassCard(modifier.heightIn(min = UI.statusCardHeight).clickable { onClick(info) }, borderColor) {
        Column(Modifier.fillMaxHeight().padding(UI.cardPadding)) {
            Text(strings.nextInjection, color = THEME.textMuted, fontSize = UI.captionSize)
            if (info != null) {
                Spacer(Modifier.height(UI.smallSpacing))
                Text(
                    info.substanceName,
                    color = THEME.textPrimary,
                    fontSize = UI.subtitleSize,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(DoseCalculator.formatDoseWithVolume(info.doseMg, info.volumeMl), color = THEME.accentPrimary, fontSize = UI.bodySize)
                Spacer(Modifier.weight(1f))
                StatusBadge(info.status, strings)
                Spacer(Modifier.height(4.dp))
                Text(info.getTimeDisplay(strings), color = THEME.textSecondary, fontSize = UI.captionSize, fontWeight = FontWeight.Medium)
                if (!info.isFirstInjection) {
                    Text(info.getScheduledDateTimeDisplay(), color = THEME.textMuted, fontSize = UI.smallSize)
                }
            } else {
                Spacer(Modifier.weight(1f))
                Text(strings.noScheduled, color = THEME.textMuted, fontSize = UI.bodySize)
            }
        }
    }
}

data class OralDoseInfo(
    val substanceName: String,
    val substanceId: String,
    val dailyDoseMg: Double,
    val schedule: OralDosingSchedule,
    val nextDoseTime: java.time.LocalDateTime?,
    val dosePerServing: Double,
    val isOverdue: Boolean,
    val intervalHours: Double,
    val isFirstDose: Boolean = false,
    val startWeek: Int = 1,
    val endWeek: Int? = null
)

fun getOralDoseInfo(cycle: Cycle, repo: InjectionRepository): List<OralDoseInfo> {
    val now = java.time.LocalDateTime.now()
    val currentWeek = cycle.currentWeek()

    return cycle.compounds
        .filter { OralDosingSchedule.isOralSteroid(it.substanceId) }
        // Prikaži samo oralne supstance aktivne u tekućoj nedelji
        // (kickstarter za nedelje 1-4 ne sme da "kasni" do kraja ciklusa)
        .filter { it.isActiveInWeek(currentWeek, cycle.durationWeeks) }
        .mapNotNull { compound ->
            val schedule = OralDosingSchedule.forSubstance(compound.substanceId) ?: return@mapNotNull null
            val substance = SampleData.getDefaultSubstances().find { it.id == compound.substanceId }
            val dailyDose = compound.getDoseForWeek(currentWeek) / 7.0
            val dosePerServing = dailyDose / schedule.dosesPerDay

            val intervalHours = 24.0 / schedule.dosesPerDay

            val lastDose = repo.getLastOralDoseForSubstance(compound.substanceId)

            val nextDoseTime: java.time.LocalDateTime?
            val isOverdue: Boolean
            val isFirstDose: Boolean

            if (lastDose == null) {
                nextDoseTime = null
                isOverdue = false
                isFirstDose = true
            } else {
                val intervalMinutes = (intervalHours * 60).toLong()
                nextDoseTime = lastDose.dateTime.plusMinutes(intervalMinutes)
                isOverdue = now.isAfter(nextDoseTime)
                isFirstDose = false
            }

            OralDoseInfo(
                substanceName = substance?.shortName ?: compound.substanceId,
                substanceId = compound.substanceId,
                dailyDoseMg = dailyDose,
                schedule = schedule,
                nextDoseTime = nextDoseTime,
                dosePerServing = dosePerServing,
                isOverdue = isOverdue,
                intervalHours = intervalHours,
                isFirstDose = isFirstDose,
                startWeek = compound.startWeek,
                endWeek = compound.endWeek
            )
        }
}

@Composable
fun NextOralDoseCard(info: OralDoseInfo?, modifier: Modifier, strings: Strings, isFullWidth: Boolean = false, onLogDose: () -> Unit = {}) {
    val now = java.time.LocalDateTime.now()
    val minutesUntil = info?.nextDoseTime?.let { java.time.Duration.between(now, it).toMinutes() }
    val isDueSoon = minutesUntil != null && minutesUntil in 0..30
    val borderColor = when {
        info?.isOverdue == true -> THEME.danger
        info?.isFirstDose == true -> THEME.success
        isDueSoon -> THEME.warning
        else -> THEME.success
    }
    val alignment = if (isFullWidth) Alignment.CenterHorizontally else Alignment.Start

    GlassCard(modifier.heightIn(min = UI.statusCardHeight).clickable { onLogDose() }, borderColor) {
        Column(
            Modifier.fillMaxWidth().heightIn(min = UI.statusCardHeight).padding(UI.cardPadding),
            horizontalAlignment = alignment
        ) {
            Text(strings.nextDose, color = THEME.textMuted, fontSize = UI.captionSize)
            if (info != null) {
                Spacer(Modifier.height(UI.smallSpacing))
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        info.substanceName,
                        modifier = Modifier.weight(1f, fill = false),
                        color = THEME.textPrimary,
                        fontSize = UI.subtitleSize,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (info.startWeek > 1 || info.endWeek != null) {
                        Spacer(Modifier.width(UI.smallSpacing))
                        val endText = info.endWeek?.toString() ?: "kraj"
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = THEME.accentSecondary.copy(0.2f)
                        ) {
                            Text(
                                "N${info.startWeek}-$endText",
                                color = THEME.accentSecondary,
                                fontSize = UI.captionSize,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
                Text("${info.dosePerServing.toInt()}mg", color = THEME.accentSecondary, fontSize = UI.bodySize)
                Spacer(Modifier.height(UI.smallSpacing))

                val intervalText = if (info.intervalHours == info.intervalHours.toLong().toDouble()) {
                    "svaka ${info.intervalHours.toInt()}h"
                } else {
                    "svaka ${String.format(Locale.getDefault(), "%.1f", info.intervalHours)}h"
                }
                Text(intervalText, color = THEME.textMuted, fontSize = UI.smallSize)

                Spacer(Modifier.height(UI.smallSpacing))

                if (info.isFirstDose) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.PlayArrow, null, tint = THEME.success, modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Čeka prvi unos", color = THEME.success, fontSize = UI.captionSize, fontWeight = FontWeight.Medium)
                    }
                } else if (info.nextDoseTime != null) {
                    @Suppress("NAME_SHADOWING")
                    val minutesUntil = java.time.Duration.between(now, info.nextDoseTime).toMinutes()
                    val absMinutes = kotlin.math.abs(minutesUntil)
                    val hoursUntil = absMinutes / 60
                    val minsUntil = absMinutes % 60

                    val timeText = when {
                        info.isOverdue && hoursUntil > 0 -> "kasni ${hoursUntil}h ${minsUntil}m"
                        info.isOverdue -> "kasni ${minsUntil}m"
                        hoursUntil > 0 -> "za ${hoursUntil}h ${minsUntil}m"
                        else -> "za ${minsUntil}m"
                    }
                    val timeColor = when {
                        info.isOverdue -> THEME.danger
                        minutesUntil <= 30 -> THEME.warning
                        else -> THEME.textSecondary
                    }

                    val nextTimeFormatted = if (info.nextDoseTime.toLocalDate() == now.toLocalDate()) {
                        info.nextDoseTime.toLocalTime().format(TIME_FORMAT)
                    } else if (info.nextDoseTime.toLocalDate() == now.toLocalDate().plusDays(1)) {
                        "sutra ${info.nextDoseTime.toLocalTime().format(TIME_FORMAT)}"
                    } else {
                        info.nextDoseTime.format(DATE_TIME_SHORT_FORMAT)
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Schedule, null, tint = timeColor, modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("$nextTimeFormatted ($timeText)", color = timeColor, fontSize = UI.captionSize, fontWeight = FontWeight.Medium)
                    }
                }

                Spacer(Modifier.height(4.dp))
                Text("${strings.halfLife}: ${info.schedule.halfLifeHours.toInt()}${strings.hours}", color = THEME.textMuted.copy(0.8f), fontSize = UI.captionSize)
            } else {
                Spacer(Modifier.weight(1f))
                Text(strings.noScheduled, color = THEME.textMuted, fontSize = UI.bodySize)
            }
        }
    }
}

@Composable
fun SiteCard(site: InjectionSite, daysSince: Int?, modifier: Modifier, strings: Strings, borderColor: Color? = null, onClick: () -> Unit) {
    GlassCard(modifier.heightIn(min = UI.statusCardHeight).clickable { onClick() }, borderColor ?: THEME.glassBorder) {
        Column(Modifier.fillMaxHeight().padding(UI.cardPadding)) {
            Text(strings.nextSite, color = THEME.textMuted, fontSize = UI.captionSize)
            Spacer(Modifier.height(UI.smallSpacing))
            Text(site.getDisplayName(strings), color = THEME.textPrimary, fontSize = UI.subtitleSize, fontWeight = FontWeight.Bold)
            Text("${strings.max} ${DoseCalculator.formatVolume(site.maxVolumeMl)}", color = THEME.accentSecondary, fontSize = UI.bodySize)
            Spacer(Modifier.weight(1f))
            Text(
                if (daysSince != null) "$daysSince ${strings.daysRest}" else strings.neverUsed,
                // Usaglašeno sa SiteHistory.MIN_REST_DAYS (14): zeleno tek posle punog
                // odmora, žuto za delimičan odmor (7-13 dana), crveno ispod 7 dana
                color = when {
                    (daysSince ?: Int.MAX_VALUE) >= SiteHistory.MIN_REST_DAYS -> THEME.success
                    (daysSince ?: 0) >= 7 -> THEME.warning
                    else -> THEME.danger
                },
                fontSize = UI.captionSize,
                fontWeight = FontWeight.Medium
            )
            Spacer(Modifier.height(4.dp))
            Text(strings.clickToAdjust, color = THEME.textMuted.copy(0.8f), fontSize = UI.captionSize)
        }
    }
}

@Composable
fun WeekScheduleSection(schedule: List<WeekSchedule>, @Suppress("UNUSED_PARAMETER") currentWeek: Int) {
    Column {
        Text("Nedeljni raspored", color = THEME.textSecondary, fontSize = UI.captionSize, modifier = Modifier.padding(bottom = UI.smallSpacing))
        Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(UI.smallSpacing)) {
            schedule.forEach { week -> WeekCard(week.week, week.isCurrent, week.compounds.all { it.isComplete }) }
        }
    }
}

@Composable
fun WeekCard(week: Int, isCurrent: Boolean, isComplete: Boolean) {
    val bg = when { isCurrent -> THEME.accentPrimary.copy(0.2f); isComplete -> THEME.success.copy(0.1f); else -> THEME.bgCard }
    val border = when { isCurrent -> THEME.accentPrimary; isComplete -> THEME.success.copy(0.5f); else -> THEME.glassBorder }
    Box(Modifier.width(UI.weekCardWidth).height(50.dp).background(bg, RoundedCornerShape(8.dp)).border(1.dp, border, RoundedCornerShape(8.dp)), Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("W$week", color = if (isCurrent) THEME.accentPrimary else THEME.textPrimary, fontSize = UI.bodySize, fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal)
            if (isComplete) Icon(Icons.Default.Check, null, tint = THEME.success, modifier = Modifier.size(12.dp))
        }
    }
}

@Composable
fun CompoundsSection(cycle: Cycle, upcoming: List<NextInjectionInfo>, repo: InjectionRepository, strings: Strings, onEdit: (CycleCompound) -> Unit, onDelete: (CycleCompound) -> Unit, onLog: (NextInjectionInfo?) -> Unit, onLogOral: (String, String, Double) -> Unit, onAddCompound: () -> Unit) {
    val currentWeek = cycle.currentWeek()
    Column {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(strings.substancesInCycle, color = THEME.textSecondary, fontSize = UI.captionSize)
            TextButton(
                onClick = onAddCompound,
                contentPadding = PaddingValues(horizontal = UI.defaultSpacing, vertical = UI.smallSpacing)
            ) {
                Icon(Icons.Default.Add, null, modifier = Modifier.size(16.dp), tint = THEME.accentPrimary)
                Spacer(Modifier.width(4.dp))
                Text(strings.addSubstance, color = THEME.accentPrimary, fontSize = UI.captionSize)
            }
        }
        Spacer(Modifier.height(UI.smallSpacing))
        cycle.compounds.forEach { compound ->
            val substance = repo.getSubstance(compound.substanceId)
            CompoundCard(compound, substance, upcoming.find { it.substanceId == compound.substanceId }, currentWeek, strings, onEdit, onDelete, onLog, onLogOral)
            Spacer(Modifier.height(UI.defaultSpacing))
        }
    }
}

@Composable
fun CompoundCard(compound: CycleCompound, substance: Substance?, upcoming: NextInjectionInfo?, currentWeek: Int, strings: Strings, onEdit: (CycleCompound) -> Unit, onDelete: (CycleCompound) -> Unit, onLog: (NextInjectionInfo?) -> Unit, onLogOral: (String, String, Double) -> Unit) {
    val weeklyDose = compound.getDoseForWeek(currentWeek)
    val dosePerInjection = compound.getDosePerInjectionForWeek(currentWeek)
    val volumePerInjection = compound.getVolumePerInjectionForWeek(currentWeek)
    val isOral = OralDosingSchedule.isOralSteroid(compound.substanceId)

    val displayDose = if (isOral) (weeklyDose / 7).toInt() else weeklyDose.toInt()
    val doseUnit = if (isOral) strings.perDay else strings.perWeek

    val onCardClick = {
        if (isOral) {
            onLogOral(compound.substanceId, substance?.shortName ?: compound.substanceId, dosePerInjection)
        } else {
            onLog(upcoming)
        }
    }

    val weekRangeText = if (compound.startWeek > 1 || compound.endWeek != null) {
        val endText = compound.endWeek?.toString() ?: "kraj"
        "N${compound.startWeek}-$endText"
    } else null

    GlassCard(Modifier.fillMaxWidth().clickable { onCardClick() }) {
        Column(Modifier.fillMaxWidth().padding(UI.cardPadding)) {
            Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Box(Modifier.size(8.dp).background(THEME.getCategoryColor(substance?.category ?: ""), CircleShape))
                    Spacer(Modifier.width(UI.defaultSpacing))
                    Text(
                        substance?.shortName ?: compound.substanceId,
                        modifier = Modifier.weight(1f, fill = false),
                        color = THEME.textPrimary,
                        fontSize = UI.subtitleSize,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (weekRangeText != null) {
                        Spacer(Modifier.width(UI.defaultSpacing))
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = THEME.accentSecondary.copy(0.2f)
                        ) {
                            Text(
                                weekRangeText,
                                color = THEME.accentSecondary,
                                fontSize = UI.smallSize,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
                Text("$displayDose$doseUnit", color = THEME.accentPrimary, fontSize = UI.bodySize, fontWeight = FontWeight.Medium)
            }
            Spacer(Modifier.height(UI.smallSpacing))
            Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                Text(compound.frequency.getDisplayName(strings), color = THEME.textMuted, fontSize = UI.captionSize)
                if (upcoming != null) Text(upcoming.getTimeDisplay(strings), color = upcoming.status.color, fontSize = UI.captionSize)
            }
            Spacer(Modifier.height(UI.smallSpacing))
            val doseLabel = if (isOral) strings.perDose else strings.perInjection
            val doseText = if (isOral) {
                "${dosePerInjection.toInt()}mg"
            } else {
                DoseCalculator.formatDoseWithVolume(dosePerInjection, volumePerInjection)
            }
            Text("$doseLabel: $doseText", color = THEME.accentPrimary, fontSize = UI.captionSize)

            Spacer(Modifier.height(UI.defaultSpacing))
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(UI.smallSpacing)
            ) {
                Surface(
                    onClick = { onEdit(compound) },
                    shape = RoundedCornerShape(8.dp),
                    color = THEME.accentPrimary.copy(0.1f),
                    modifier = Modifier.weight(1f).heightIn(min = UI.buttonHeight)
                ) {
                    Row(
                        Modifier.padding(horizontal = UI.mediumSpacing, vertical = UI.smallSpacing),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Edit, null, tint = THEME.accentPrimary, modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(UI.smallSpacing))
                        Text(strings.editDoses, color = THEME.accentPrimary, fontSize = UI.captionSize, fontWeight = FontWeight.Medium)
                    }
                }
                Surface(
                    onClick = { onDelete(compound) },
                    shape = RoundedCornerShape(8.dp),
                    color = THEME.danger.copy(0.1f),
                    // Fiksna veličina: fillMaxSize unutar rastegljivog Surface-a je
                    // grabio celu širinu reda pre merenja weight() suseda
                    modifier = Modifier.size(UI.buttonHeight)
                ) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Delete, contentDescription = "Ukloni supstancu", tint = THEME.danger, modifier = Modifier.size(18.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun RecentInjectionsSection(repo: InjectionRepository, repoTick: Int, strings: Strings, onEdit: (InjectionRecord) -> Unit, onDelete: (String) -> Unit) {
    val injections = remember(repoTick) { repo.getInjections().take(10) }

    if (injections.isEmpty()) return

    Column {
        Text(strings.recentInjections, color = THEME.textSecondary, fontSize = UI.captionSize, modifier = Modifier.padding(bottom = UI.smallSpacing))
        GlassCard(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(UI.cardPadding)) {
                injections.forEachIndexed { index, r ->
                    val s = repo.getSubstance(r.substanceId)
                    Row(
                        Modifier.fillMaxWidth().clickable { onEdit(r) }.padding(vertical = UI.defaultSpacing),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Column(Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(Modifier.size(8.dp).background(THEME.getCategoryColor(s?.category ?: ""), CircleShape))
                                Spacer(Modifier.width(UI.defaultSpacing))
                                Text(
                                    s?.shortName ?: r.substanceId,
                                    modifier = Modifier.weight(1f, fill = false),
                                    color = THEME.textPrimary,
                                    fontSize = UI.bodySize,
                                    fontWeight = FontWeight.Medium,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(Modifier.width(UI.defaultSpacing))
                                Text(r.dateTime.format(DATE_TIME_FORMAT), color = THEME.textMuted, fontSize = UI.smallSize, maxLines = 1)
                            }
                            Spacer(Modifier.height(4.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.LocationOn, null, tint = THEME.accentSecondary, modifier = Modifier.size(14.dp))
                                Spacer(Modifier.width(4.dp))
                                Text(r.site.getDisplayName(strings), color = THEME.accentSecondary, fontSize = UI.captionSize, fontWeight = FontWeight.Medium)
                                Spacer(Modifier.width(UI.mediumSpacing))
                                Text(DoseCalculator.formatDoseWithVolume(r.doseMg, r.volumeMl), color = THEME.textSecondary, fontSize = UI.captionSize)
                            }
                            if (r.notes.isNotBlank()) {
                                Spacer(Modifier.height(2.dp))
                                Text(r.notes, color = THEME.textMuted, fontSize = UI.smallSize, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            }
                        }
                        IconButton(onClick = { onDelete(r.id) }, modifier = Modifier.size(UI.buttonHeight)) {
                            Icon(Icons.Default.Delete, contentDescription = "Obriši injekciju", tint = THEME.danger, modifier = Modifier.size(18.dp))
                        }
                    }
                    if (index < injections.lastIndex) {
                        HorizontalDivider(color = THEME.divider.copy(0.5f))
                    }
                }
            }
        }
    }
}

@Composable
fun RecentOralDosesSection(repo: InjectionRepository, repoTick: Int, strings: Strings, onDelete: (String) -> Unit) {
    val oralDoses = remember(repoTick) {
        repo.getOralDoses()
            .sortedByDescending { it.dateTime }
            .take(10)
    }

    if (oralDoses.isEmpty()) return

    Column {
        Text("Poslednje oralne doze", color = THEME.textSecondary, fontSize = UI.captionSize, modifier = Modifier.padding(bottom = UI.smallSpacing))
        GlassCard(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(UI.cardPadding)) {
                oralDoses.forEachIndexed { index, dose ->
                    val s = repo.getSubstance(dose.substanceId)
                    Row(
                        Modifier.fillMaxWidth().padding(vertical = UI.defaultSpacing),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(Modifier.size(8.dp).background(THEME.getCategoryColor(s?.category ?: "Oralni"), CircleShape))
                                Spacer(Modifier.width(UI.defaultSpacing))
                                Text(
                                    s?.shortName ?: dose.substanceId,
                                    modifier = Modifier.weight(1f, fill = false),
                                    color = THEME.textPrimary,
                                    fontSize = UI.bodySize,
                                    fontWeight = FontWeight.Medium,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(Modifier.width(UI.defaultSpacing))
                                Text(dose.dateTime.format(DATE_TIME_FORMAT), color = THEME.textMuted, fontSize = UI.smallSize, maxLines = 1)
                            }
                            Spacer(Modifier.height(4.dp))
                            Text("${dose.doseMg.toInt()}mg", color = THEME.accentSecondary, fontSize = UI.captionSize)
                        }
                        IconButton(onClick = { onDelete(dose.id) }, modifier = Modifier.size(UI.buttonHeight)) {
                            Icon(Icons.Default.Delete, contentDescription = "Obriši oralnu dozu", tint = THEME.danger, modifier = Modifier.size(18.dp))
                        }
                    }
                    if (index < oralDoses.lastIndex) {
                        HorizontalDivider(color = THEME.divider.copy(0.5f))
                    }
                }
            }
        }
    }
}

@Composable
fun BottomBar(strings: Strings, onNew: () -> Unit) {
    Row(Modifier.fillMaxWidth().height(UI.bottomBarHeight).background(THEME.bgCard.copy(0.8f), RoundedCornerShape(16.dp)).padding(horizontal = UI.mediumSpacing), Arrangement.Center, Alignment.CenterVertically) {
        BottomBtn({ Icon(Icons.Default.Add, null, tint = THEME.textPrimary) }, strings.newCycle, false, onNew)
    }
}

@Composable
fun BottomBtn(icon: @Composable () -> Unit, label: String, primary: Boolean, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .sizeIn(minWidth = 64.dp, minHeight = 56.dp)
            .clickable { onClick() },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(modifier = Modifier.size(40.dp).background(if (primary) THEME.accentPrimary else Color.Transparent, CircleShape), contentAlignment = Alignment.Center) { icon() }
        Text(label, color = if (primary) THEME.accentPrimary else THEME.textMuted, fontSize = UI.smallSize)
    }
}

// PCTGuideCard je definisan u com.injekcije.app.ui.PCTGuideScreen.kt
