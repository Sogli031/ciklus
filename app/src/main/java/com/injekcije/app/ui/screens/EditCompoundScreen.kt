package com.injekcije.app.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.injekcije.app.THEME
import com.injekcije.app.UI
import com.injekcije.app.Strings
import com.injekcije.app.data.*
import com.injekcije.app.notifications.scheduleInjectionNotifications
import com.injekcije.app.notifications.scheduleOralSteroidNotifications
import com.injekcije.app.ui.components.buttonShape
import com.injekcije.app.ui.components.textFieldColors
import com.injekcije.app.ui.components.textFieldShape
import com.injekcije.app.ui.formatDoseNumber
import com.injekcije.app.ui.normalizeDecimalInput
import java.util.Locale

@Composable
fun EditCompoundDialog(compound: CycleCompound, cycleDurationWeeks: Int, repo: InjectionRepository, strings: Strings, onDismiss: () -> Unit) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val substance = repo.getSubstance(compound.substanceId)
    var dose by remember { mutableStateOf(formatDoseNumber(compound.weeklyDoseMg)) }
    var freq by remember { mutableStateOf(compound.frequency) }
    var freqMenu by remember { mutableStateOf(false) }
    var phases by remember { mutableStateOf(compound.phases) }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    var selectedWeeks by remember { mutableStateOf<Set<Int>>(emptySet()) }
    var showDoseInput by remember { mutableStateOf(false) }
    var customDoseInput by remember { mutableStateOf("") }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Obriši supstancu?", color = THEME.textPrimary) },
            text = { Text("Da li si siguran da želiš da obrišeš ${substance?.shortName ?: compound.substanceId} iz ciklusa?", color = THEME.textSecondary) },
            confirmButton = {
                Button(
                    onClick = {
                        repo.removeCompoundFromCycle(compound.substanceId)
                        scheduleInjectionNotifications(context)
                        scheduleOralSteroidNotifications(context)
                        showDeleteConfirm = false
                        onDismiss()
                    },
                    colors = ButtonDefaults.buttonColors(THEME.danger, THEME.textPrimary)
                ) {
                    Text("Obriši")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("Odustani", color = THEME.textMuted)
                }
            },
            containerColor = THEME.bgCard
        )
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = THEME.bgCard,
            tonalElevation = 8.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(Modifier.padding(UI.xlSpacing).verticalScroll(rememberScrollState())) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = THEME.bgCardElevated,
                    border = BorderStroke(1.dp, THEME.glassBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(UI.mediumSpacing),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            Modifier.size(40.dp).background(THEME.getCategoryColor(substance?.category ?: "").copy(0.2f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Box(Modifier.size(12.dp).background(THEME.getCategoryColor(substance?.category ?: ""), CircleShape))
                        }
                        Spacer(Modifier.width(UI.mediumSpacing))
                        Column {
                            Text(substance?.shortName ?: compound.substanceId, color = THEME.textPrimary, fontSize = UI.titleSize, fontWeight = FontWeight.Bold)
                            Text(strings.editDosing, color = THEME.textMuted, fontSize = UI.captionSize)
                        }
                    }
                }

                Spacer(Modifier.height(UI.xlSpacing))

                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = THEME.bgCardElevated,
                    border = BorderStroke(1.dp, THEME.glassBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(Modifier.padding(UI.mediumSpacing)) {
                        Text(strings.baseDose, color = THEME.textSecondary, fontSize = UI.captionSize)
                        Spacer(Modifier.height(UI.smallSpacing))
                        OutlinedTextField(
                            value = dose,
                            onValueChange = { input -> normalizeDecimalInput(input)?.let { dose = it } },
                            modifier = Modifier.fillMaxWidth(),
                            colors = textFieldColors(),
                            shape = textFieldShape,
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )

                        Spacer(Modifier.height(UI.mediumSpacing))

                        Text(strings.frequency, color = THEME.textSecondary, fontSize = UI.captionSize)
                        Spacer(Modifier.height(UI.smallSpacing))
                        Box {
                            OutlinedButton({ freqMenu = true }, Modifier.fillMaxWidth().height(UI.buttonHeight), shape = buttonShape, colors = ButtonDefaults.outlinedButtonColors(contentColor = THEME.textPrimary)) {
                                Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                                    Text(freq.getDisplayName(strings))
                                    Icon(Icons.Default.KeyboardArrowDown, null, tint = THEME.textMuted)
                                }
                            }
                            DropdownMenu(freqMenu, { freqMenu = false }) {
                                InjectionFrequency.entries.forEach { f ->
                                    DropdownMenuItem(text = { Text(f.getDisplayName(strings)) }, onClick = { freq = f; freqMenu = false })
                                }
                            }
                        }
                    }
                }

                Spacer(Modifier.height(UI.largeSpacing))

                val baseDose = dose.toDoubleOrNull() ?: compound.weeklyDoseMg
                val maxDose = (maxOf(baseDose, phases.maxOfOrNull { it.weeklyDoseMg } ?: baseDose) * 1.2)
                    .coerceAtLeast(1.0)

                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = THEME.bgCardElevated,
                    border = BorderStroke(1.dp, THEME.glassBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(Modifier.padding(UI.mediumSpacing)) {
                        Text(strings.dosesPerWeeks, color = THEME.textSecondary, fontSize = UI.captionSize, fontWeight = FontWeight.Medium)
                        Spacer(Modifier.height(UI.smallSpacing))
                        Text(strings.clickWeekToEdit, color = THEME.textMuted, fontSize = UI.smallSize)
                        Spacer(Modifier.height(UI.mediumSpacing))

                        Row(
                            Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(UI.smallSpacing)
                        ) {
                            (1..cycleDurationWeeks).forEach { week ->
                                val weekDose = phases.find { week in it.startWeek..it.endWeek }?.weeklyDoseMg ?: baseDose
                                val isCustom = phases.any { week in it.startWeek..it.endWeek }
                                val isSelected = week in selectedWeeks
                                val barHeight = ((weekDose / maxDose) * 80).coerceIn(20.0, 80.0)
                                val weekShape = RoundedCornerShape(10.dp)

                                Column(
                                    modifier = Modifier
                                        .sizeIn(minWidth = UI.buttonHeight, minHeight = 118.dp)
                                        .then(
                                            if (isSelected) {
                                                Modifier
                                                    .background(THEME.accentPrimary.copy(alpha = 0.1f), weekShape)
                                                    .border(1.dp, THEME.accentPrimary.copy(alpha = 0.45f), weekShape)
                                            } else {
                                                Modifier
                                            }
                                        )
                                        .clickable {
                                            selectedWeeks = if (isSelected) {
                                                selectedWeeks - week
                                            } else {
                                                selectedWeeks + week
                                            }
                                        }
                                        .padding(horizontal = UI.smallSpacing, vertical = UI.smallSpacing),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        "${weekDose.toInt()}",
                                        color = when {
                                            isSelected -> THEME.accentPrimary
                                            isCustom -> THEME.accentSecondary
                                            else -> THEME.textSecondary
                                        },
                                        fontSize = UI.captionSize,
                                        fontWeight = if (isSelected || isCustom) FontWeight.Bold else FontWeight.Normal
                                    )
                                    Spacer(Modifier.height(UI.smallSpacing))

                                    Box(
                                        Modifier
                                            .width(30.dp)
                                            .height(barHeight.dp)
                                            .background(
                                                when {
                                                    isSelected -> THEME.accentPrimary
                                                    isCustom -> THEME.accentSecondary
                                                    else -> THEME.textMuted.copy(0.5f)
                                                },
                                                RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp)
                                            )
                                            .then(
                                                if (isSelected) Modifier.border(2.dp, THEME.accentPrimary, RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                                                else Modifier
                                            )
                                    )

                                    Text(
                                        "N$week",
                                        color = if (isSelected) THEME.accentPrimary else THEME.textMuted,
                                        fontSize = UI.captionSize,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    )
                                }
                            }
                        }

                        if (selectedWeeks.isNotEmpty()) {
                            Spacer(Modifier.height(UI.mediumSpacing))
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = THEME.bgCard,
                                border = BorderStroke(1.dp, THEME.accentPrimary.copy(alpha = 0.45f)),
                                tonalElevation = 4.dp,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(Modifier.padding(UI.mediumSpacing)) {
                                    val sortedWeeks = selectedWeeks.sorted()
                                    val rangeText = if (sortedWeeks.size == 1) {
                                        "${strings.week} ${sortedWeeks.first()}"
                                    } else {
                                        "${strings.weeks}: ${sortedWeeks.joinToString(", ")}"
                                    }
                                    Text(rangeText, color = THEME.accentPrimary, fontSize = UI.captionSize, fontWeight = FontWeight.Medium)
                                    Spacer(Modifier.height(UI.defaultSpacing))

                                    if (showDoseInput) {
                                        Row(
                                            Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(UI.defaultSpacing),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            OutlinedTextField(
                                                value = customDoseInput,
                                                onValueChange = { input -> normalizeDecimalInput(input)?.let { customDoseInput = it } },
                                                modifier = Modifier.weight(1f),
                                                placeholder = { Text("mg/ned", color = THEME.textMuted) },
                                                colors = textFieldColors(),
                                                shape = textFieldShape,
                                                singleLine = true,
                                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                                            )
                                            Button(
                                                onClick = {
                                                    val newDose = customDoseInput.toDoubleOrNull()
                                                    if (newDose != null && newDose > 0) {
                                                        val newPhases = phases.filter { phase ->
                                                            selectedWeeks.none { week -> week in phase.startWeek..phase.endWeek }
                                                        }.toMutableList()

                                                        if (newDose != baseDose) {
                                                            val sorted = selectedWeeks.sorted()
                                                            var rangeStart = sorted.first()
                                                            var prev = rangeStart

                                                            for (i in 1..sorted.size) {
                                                                val current = sorted.getOrNull(i)
                                                                if (current == null || current != prev + 1) {
                                                                    newPhases.add(DosePhase(rangeStart, prev, newDose))
                                                                    if (current != null) {
                                                                        rangeStart = current
                                                                    }
                                                                }
                                                                if (current != null) prev = current
                                                            }
                                                        }

                                                        phases = newPhases.sortedBy { it.startWeek }
                                                        selectedWeeks = emptySet()
                                                        showDoseInput = false
                                                        customDoseInput = ""
                                                    }
                                                },
                                                shape = buttonShape,
                                                colors = ButtonDefaults.buttonColors(THEME.accentPrimary, THEME.textDark),
                                                enabled = customDoseInput.toDoubleOrNull()?.let { it > 0 } == true,
                                                modifier = Modifier.height(UI.buttonHeight)
                                            ) {
                                                Text(strings.apply)
                                            }
                                        }
                                    } else {
                                        Row(
                                            Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(UI.defaultSpacing)
                                        ) {
                                            OutlinedButton(
                                                onClick = {
                                                    showDoseInput = true
                                                    customDoseInput = ""
                                                },
                                                modifier = Modifier.weight(1f).height(UI.buttonHeight),
                                                shape = buttonShape
                                            ) {
                                                Icon(Icons.Default.Edit, null, modifier = Modifier.size(16.dp))
                                                Spacer(Modifier.width(4.dp))
                                                Text(strings.editDoses, color = THEME.accentPrimary, fontSize = UI.captionSize)
                                            }
                                            OutlinedButton(
                                                onClick = {
                                                    phases = phases.filter { phase ->
                                                        selectedWeeks.none { week -> week in phase.startWeek..phase.endWeek }
                                                    }
                                                    selectedWeeks = emptySet()
                                                },
                                                modifier = Modifier.weight(1f).height(UI.buttonHeight),
                                                shape = buttonShape
                                            ) {
                                                Icon(Icons.Default.Refresh, null, modifier = Modifier.size(16.dp), tint = THEME.textMuted)
                                                Spacer(Modifier.width(4.dp))
                                                Text(strings.resetAll, color = THEME.textMuted, fontSize = UI.captionSize)
                                            }
                                        }
                                    }

                                    TextButton(
                                        onClick = { selectedWeeks = emptySet(); showDoseInput = false },
                                        modifier = Modifier.align(Alignment.End)
                                    ) {
                                        Text(strings.cancelSelection, color = THEME.textMuted, fontSize = UI.smallSize)
                                    }
                                }
                            }
                        }

                        Spacer(Modifier.height(UI.mediumSpacing))
                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(UI.mediumSpacing)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(Modifier.size(10.dp).background(THEME.textMuted.copy(0.5f), RoundedCornerShape(2.dp)))
                                Spacer(Modifier.width(4.dp))
                                Text(strings.baseDoseLabel, color = THEME.textMuted, fontSize = UI.smallSize)
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(Modifier.size(10.dp).background(THEME.accentSecondary, RoundedCornerShape(2.dp)))
                                Spacer(Modifier.width(4.dp))
                                Text(strings.modified, color = THEME.textMuted, fontSize = UI.smallSize)
                            }
                        }
                    }
                }

                if (selectedWeeks.isEmpty() && cycleDurationWeeks >= 4) {
                    Spacer(Modifier.height(UI.mediumSpacing))
                    Text(strings.quickOptions, color = THEME.textMuted, fontSize = UI.smallSize)
                    Spacer(Modifier.height(UI.smallSpacing))
                    Row(
                        Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(UI.smallSpacing)
                    ) {
                        SuggestionChip(
                            onClick = {
                                val frontLoadDose = baseDose * 1.5
                                phases = listOf(DosePhase(1, 2, frontLoadDose))
                            },
                            modifier = Modifier.heightIn(min = 36.dp),
                            label = { Text("${strings.frontLoad} (1-2)", fontSize = UI.smallSize) },
                            colors = SuggestionChipDefaults.suggestionChipColors(
                                containerColor = THEME.bgCardElevated,
                                labelColor = THEME.textSecondary
                            )
                        )
                        SuggestionChip(
                            onClick = {
                                val lastWeeks = (cycleDurationWeeks - 1)..cycleDurationWeeks
                                phases = listOf(DosePhase(lastWeeks.first, lastWeeks.last, baseDose * 0.5))
                            },
                            modifier = Modifier.heightIn(min = 36.dp),
                            label = { Text("${strings.taper} (${strings.last} 2)", fontSize = UI.smallSize) },
                            colors = SuggestionChipDefaults.suggestionChipColors(
                                containerColor = THEME.bgCardElevated,
                                labelColor = THEME.textSecondary
                            )
                        )
                        if (phases.isNotEmpty()) {
                            SuggestionChip(
                                onClick = { phases = emptyList() },
                                modifier = Modifier.heightIn(min = 36.dp),
                                label = { Text(strings.resetAll, fontSize = UI.smallSize) },
                                colors = SuggestionChipDefaults.suggestionChipColors(
                                    containerColor = THEME.danger.copy(0.2f),
                                    labelColor = THEME.danger
                                )
                            )
                        }
                    }
                }

                Spacer(Modifier.height(UI.xlSpacing))

                OutlinedButton(
                    onClick = { showDeleteConfirm = true },
                    modifier = Modifier.fillMaxWidth().height(UI.buttonHeight),
                    shape = buttonShape,
                    colors = ButtonDefaults.outlinedButtonColors(containerColor = THEME.danger.copy(alpha = 0.08f), contentColor = THEME.danger),
                    border = BorderStroke(1.dp, THEME.danger.copy(alpha = 0.5f))
                ) {
                    Icon(Icons.Default.Delete, null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(UI.smallSpacing))
                    Text("Ukloni iz ciklusa")
                }

                Spacer(Modifier.height(UI.mediumSpacing))

                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(UI.mediumSpacing)) {
                    OutlinedButton(onClick = onDismiss, modifier = Modifier.weight(1f).height(UI.buttonHeight), shape = buttonShape) {
                        Text(strings.cancel, color = THEME.textMuted)
                    }
                    Button(
                        onClick = {
                            val d = dose.toDoubleOrNull()
                            if (d != null && d > 0) {
                                repo.updateCompoundDose(compound.substanceId, d, freq)
                                repo.updateCompoundPhases(compound.substanceId, phases)
                                // Promena doze/frekvencije menja raspored - preraspodeli alarme
                                scheduleInjectionNotifications(context)
                                scheduleOralSteroidNotifications(context)
                                onDismiss()
                            }
                        },
                        modifier = Modifier.weight(1f).height(UI.buttonHeight),
                        shape = buttonShape,
                        enabled = dose.toDoubleOrNull()?.let { it > 0 } == true,
                        colors = ButtonDefaults.buttonColors(THEME.accentPrimary, THEME.textDark)
                    ) {
                        Text(strings.save)
                    }
                }
            }
        }
    }
}

@Composable
fun AddCompoundDialog(
    cycleDurationWeeks: Int,
    repo: InjectionRepository,
    strings: Strings,
    onDismiss: () -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val activeCycle = repo.getActiveCycle()
    val existingSubstanceIds = activeCycle?.compounds?.map { it.substanceId }?.toSet() ?: emptySet()
    val availableSubstances = remember(existingSubstanceIds) {
        SampleData.getDefaultSubstances().filter { it.id !in existingSubstanceIds }
    }

    var sub by remember { mutableStateOf<Substance?>(null) }
    var dose by remember { mutableStateOf("500") } // interno: uvek nedeljna doza
    var doseInput by remember { mutableStateOf("500") } // sirovi unos (dnevni za oralne)
    var freq by remember { mutableStateOf(InjectionFrequency.E3_5D) }
    var conc by remember { mutableStateOf("") }
    var subMenu by remember { mutableStateOf(false) }
    var freqMenu by remember { mutableStateOf(false) }
    var startWeek by remember { mutableIntStateOf(1) }
    var endWeek by remember { mutableIntStateOf(cycleDurationWeeks) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = THEME.bgCard,
            tonalElevation = 8.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(Modifier.padding(UI.xlSpacing).verticalScroll(rememberScrollState())) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = THEME.bgCardElevated,
                    border = BorderStroke(1.dp, THEME.glassBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(UI.mediumSpacing),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, tint = THEME.accentPrimary, modifier = Modifier.size(28.dp))
                        Spacer(Modifier.width(UI.mediumSpacing))
                        Column {
                            Text(strings.addSubstance, color = THEME.textPrimary, fontSize = UI.titleSize, fontWeight = FontWeight.Bold)
                            Text("${strings.week} ${activeCycle?.currentWeek() ?: 1} / $cycleDurationWeeks", color = THEME.textMuted, fontSize = UI.captionSize)
                        }
                    }
                }

                Spacer(Modifier.height(UI.xlSpacing))

                if (availableSubstances.isEmpty()) {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = THEME.warning.copy(0.1f),
                        border = BorderStroke(1.dp, THEME.warning.copy(alpha = 0.35f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(Modifier.padding(UI.mediumSpacing), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Info, null, tint = THEME.warning)
                            Spacer(Modifier.width(UI.defaultSpacing))
                            Text("Sve supstance su već dodate u ciklus", color = THEME.warning, fontSize = UI.bodySize)
                        }
                    }
                    Spacer(Modifier.height(UI.xlSpacing))
                    Button(
                        onClick = onDismiss,
                        modifier = Modifier.fillMaxWidth().height(UI.buttonHeight),
                        shape = buttonShape,
                        colors = ButtonDefaults.buttonColors(THEME.accentPrimary, THEME.textDark)
                    ) {
                        Text(strings.close)
                    }
                } else {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = THEME.bgCardElevated,
                        border = BorderStroke(1.dp, THEME.glassBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(Modifier.padding(UI.mediumSpacing)) {
                            Text(strings.substance, color = THEME.textSecondary, fontSize = UI.captionSize, fontWeight = FontWeight.Medium)
                            Spacer(Modifier.height(UI.smallSpacing))
                            Box {
                                OutlinedButton(
                                    onClick = { subMenu = true },
                                    modifier = Modifier.fillMaxWidth().height(UI.buttonHeight),
                                    shape = buttonShape,
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = if (sub != null) THEME.textPrimary else THEME.textMuted)
                                ) {
                                    Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            if (sub != null) {
                                                Box(Modifier.size(8.dp).background(THEME.getCategoryColor(sub!!.category), CircleShape))
                                                Spacer(Modifier.width(UI.defaultSpacing))
                                            }
                                            Text(sub?.shortName ?: strings.selectSubstance)
                                        }
                                        Icon(Icons.Default.KeyboardArrowDown, null, tint = THEME.textMuted)
                                    }
                                }
                                DropdownMenu(subMenu, { subMenu = false }, modifier = Modifier.heightIn(max = 300.dp)) {
                                    val categories = availableSubstances.map { it.category }.distinct()
                                    categories.forEach { category ->
                                        Text(
                                            category,
                                            color = THEME.getCategoryColor(category),
                                            fontSize = UI.captionSize,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = UI.mediumSpacing, vertical = UI.smallSpacing)
                                        )
                                        availableSubstances.filter { it.category == category }.forEach { s ->
                                            DropdownMenuItem(
                                                text = {
                                                    Column {
                                                        Text(s.shortName)
                                                        Text(
                                                            "${s.defaultConcentration.toInt()}mg/mL • ${s.recommendedFrequency.displayName}",
                                                            fontSize = UI.smallSize,
                                                            color = THEME.textMuted
                                                        )
                                                    }
                                                },
                                                onClick = {
                                                    sub = s
                                                    conc = s.defaultConcentration.toInt().toString()
                                                    dose = if (s.recommendedWeeklyDose > 0) formatDoseNumber(s.recommendedWeeklyDose) else "500"
                                                    doseInput = if (OralDosingSchedule.isOralSteroid(s.id)) {
                                                        val weekly = dose.toDoubleOrNull() ?: 0.0
                                                        if (weekly > 0) formatDoseNumber(weekly / 7.0) else ""
                                                    } else {
                                                        dose
                                                    }
                                                    val allowed = OralDosingSchedule.getAllowedFrequencies(s.id)
                                                    freq = if (allowed.contains(s.recommendedFrequency)) {
                                                        s.recommendedFrequency
                                                    } else {
                                                        allowed.first()
                                                    }
                                                    if (OralDosingSchedule.isOralSteroid(s.id) && s.recommendedMaxWeeks > 0) {
                                                        endWeek = minOf(s.recommendedMaxWeeks, cycleDurationWeeks)
                                                    } else {
                                                        endWeek = cycleDurationWeeks
                                                    }
                                                    subMenu = false
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    if (sub != null) {
                        Spacer(Modifier.height(UI.mediumSpacing))
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = THEME.accentPrimary.copy(0.1f),
                            border = BorderStroke(1.dp, THEME.accentPrimary.copy(alpha = 0.35f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(Modifier.padding(UI.mediumSpacing), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Info, contentDescription = null, tint = THEME.accentPrimary, modifier = Modifier.size(20.dp))
                                Spacer(Modifier.width(UI.defaultSpacing))
                                Column {
                                    Text("Preporučeno trajanje: ${sub!!.recommendedMaxWeeks} nedelja", color = THEME.accentPrimary, fontSize = UI.bodySize, fontWeight = FontWeight.Medium)
                                    if (sub!!.recommendedMaxWeeks <= 6) {
                                        Text("Ograniči upotrebu zbog hepatotoksičnosti", color = THEME.warning, fontSize = UI.smallSize)
                                    }
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(UI.mediumSpacing))

                    val allowedFrequencies = sub?.let { OralDosingSchedule.getAllowedFrequencies(it.id) }
                        ?: InjectionFrequency.entries.toList()
                    val isOral = sub?.let { OralDosingSchedule.isOralSteroid(it.id) } ?: false

                    val doseLabel = if (isOral) "Dnevna doza" else strings.weeklyDose
                    val recommendedDose = sub?.let {
                        if (isOral) it.recommendedDose else it.recommendedWeeklyDose
                    } ?: 0.0

                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = THEME.bgCardElevated,
                        border = BorderStroke(1.dp, THEME.glassBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(Modifier.padding(UI.mediumSpacing)) {
                            Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                                Text(doseLabel, color = THEME.textSecondary, fontSize = UI.captionSize, fontWeight = FontWeight.Medium)
                                if (recommendedDose > 0) {
                                    Text("Preporuka: ${recommendedDose.toInt()}mg", color = THEME.accentPrimary, fontSize = UI.smallSize)
                                }
                            }
                            Spacer(Modifier.height(UI.smallSpacing))
                            OutlinedTextField(
                                value = doseInput,
                                onValueChange = { input ->
                                    normalizeDecimalInput(input)?.let { normalized ->
                                        doseInput = normalized
                                        // Za oralne: dnevni unos -> nedeljna doza (Double matematika čuva decimale)
                                        dose = if (isOral) {
                                            val daily = normalized.toDoubleOrNull() ?: 0.0
                                            if (daily > 0) (daily * 7.0).toString() else ""
                                        } else {
                                            normalized
                                        }
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors = textFieldColors(),
                                shape = textFieldShape,
                                singleLine = true,
                                suffix = { Text(if (isOral) "mg/dan" else "mg/ned", color = THEME.textMuted) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                            )
                        }
                    }

                    Spacer(Modifier.height(UI.mediumSpacing))

                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = THEME.bgCardElevated,
                        border = BorderStroke(1.dp, THEME.glassBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(Modifier.padding(UI.mediumSpacing)) {
                            Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                                Text(strings.frequency, color = THEME.textSecondary, fontSize = UI.captionSize, fontWeight = FontWeight.Medium)
                                if (sub != null) {
                                    Text("${strings.recommendation}: ${sub!!.recommendedFrequency.getDisplayName(strings)}", color = THEME.accentPrimary, fontSize = UI.smallSize)
                                }
                            }
                            Spacer(Modifier.height(UI.smallSpacing))

                            if (allowedFrequencies.size == 1) {
                                Surface(
                                    modifier = Modifier.fillMaxWidth().heightIn(min = UI.buttonHeight),
                                    shape = buttonShape,
                                    color = THEME.bgCard,
                                    border = BorderStroke(1.dp, THEME.glassBorder)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth().padding(UI.mediumSpacing),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Default.Lock, contentDescription = null, tint = THEME.textMuted, modifier = Modifier.size(18.dp))
                                            Spacer(Modifier.width(UI.defaultSpacing))
                                            Text(text = allowedFrequencies.first().getDisplayName(strings), color = THEME.textPrimary, fontSize = UI.bodySize)
                                        }
                                        Text(text = if (isOral) "Obavezno za oralne" else "Fiksno", color = THEME.textMuted, fontSize = UI.smallSize)
                                    }
                                }
                            } else {
                                Box {
                                    OutlinedButton(
                                        onClick = { freqMenu = true },
                                        modifier = Modifier.fillMaxWidth().height(UI.buttonHeight),
                                        shape = buttonShape,
                                        colors = ButtonDefaults.outlinedButtonColors(contentColor = THEME.textPrimary)
                                    ) {
                                        Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                                            Text(freq.getDisplayName(strings))
                                            Icon(Icons.Default.KeyboardArrowDown, null, tint = THEME.textMuted)
                                        }
                                    }
                                    DropdownMenu(freqMenu, { freqMenu = false }) {
                                        allowedFrequencies.forEach { f ->
                                            DropdownMenuItem(
                                                text = {
                                                    Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                                                        Text(f.getDisplayName(strings))
                                                        Text("${String.format(Locale.getDefault(), "%.1f", f.injectionsPerWeek)}x/ned", color = THEME.textMuted, fontSize = UI.smallSize)
                                                    }
                                                },
                                                onClick = { freq = f; freqMenu = false }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(UI.mediumSpacing))

                    val concLabel = if (isOral) "Jačina tablete" else strings.concentration
                    val concSuffix = if (isOral) "mg/tab" else "mg/mL"

                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = THEME.bgCardElevated,
                        border = BorderStroke(1.dp, THEME.glassBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(Modifier.padding(UI.mediumSpacing)) {
                            Text(concLabel, color = THEME.textSecondary, fontSize = UI.captionSize, fontWeight = FontWeight.Medium)
                            Spacer(Modifier.height(UI.smallSpacing))
                            OutlinedTextField(
                                value = conc,
                                onValueChange = { input -> normalizeDecimalInput(input)?.let { conc = it } },
                                modifier = Modifier.fillMaxWidth(),
                                colors = textFieldColors(),
                                shape = textFieldShape,
                                singleLine = true,
                                suffix = { Text(concSuffix, color = THEME.textMuted) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                            )
                        }
                    }

                    Spacer(Modifier.height(UI.mediumSpacing))

                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = THEME.bgCardElevated,
                        border = BorderStroke(1.dp, THEME.glassBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(Modifier.padding(UI.mediumSpacing)) {
                            Text("Trajanje u ciklusu", color = THEME.textSecondary, fontSize = UI.captionSize, fontWeight = FontWeight.Medium)
                            Spacer(Modifier.height(UI.defaultSpacing))

                            Column(verticalArrangement = Arrangement.spacedBy(UI.mediumSpacing)) {
                                Row(
                                    Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Od nedelje", color = THEME.textMuted, fontSize = UI.smallSize)
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(UI.smallSpacing)
                                    ) {
                                        IconButton(onClick = { if (startWeek > 1) startWeek-- }, enabled = startWeek > 1, modifier = Modifier.size(UI.buttonHeight)) {
                                            Icon(Icons.Default.Remove, contentDescription = "Smanji početnu nedelju", tint = if (startWeek > 1) THEME.accentPrimary else THEME.textMuted)
                                        }
                                        Text("$startWeek", color = THEME.textPrimary, fontSize = UI.subtitleSize, fontWeight = FontWeight.Bold, modifier = Modifier.width(40.dp), textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                                        IconButton(onClick = { if (startWeek < endWeek) startWeek++ }, enabled = startWeek < endWeek, modifier = Modifier.size(UI.buttonHeight)) {
                                            Icon(Icons.Default.Add, contentDescription = "Povećaj početnu nedelju", tint = if (startWeek < endWeek) THEME.accentPrimary else THEME.textMuted)
                                        }
                                    }
                                }

                                Row(
                                    Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Do nedelje", color = THEME.textMuted, fontSize = UI.smallSize)
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(UI.smallSpacing)
                                    ) {
                                        IconButton(onClick = { if (endWeek > startWeek) endWeek-- }, enabled = endWeek > startWeek, modifier = Modifier.size(UI.buttonHeight)) {
                                            Icon(Icons.Default.Remove, contentDescription = "Smanji završnu nedelju", tint = if (endWeek > startWeek) THEME.accentPrimary else THEME.textMuted)
                                        }
                                        Text("$endWeek", color = THEME.textPrimary, fontSize = UI.subtitleSize, fontWeight = FontWeight.Bold, modifier = Modifier.width(40.dp), textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                                        IconButton(onClick = { if (endWeek < cycleDurationWeeks) endWeek++ }, enabled = endWeek < cycleDurationWeeks, modifier = Modifier.size(UI.buttonHeight)) {
                                            Icon(Icons.Default.Add, contentDescription = "Povećaj završnu nedelju", tint = if (endWeek < cycleDurationWeeks) THEME.accentPrimary else THEME.textMuted)
                                        }
                                    }
                                }
                            }

                            val duration = endWeek - startWeek + 1
                            Text(
                                "Ukupno: $duration ${if (duration == 1) "nedelja" else if (duration in 2..4) "nedelje" else "nedelja"}",
                                color = THEME.textMuted,
                                fontSize = UI.smallSize,
                                modifier = Modifier.padding(top = UI.smallSpacing)
                            )
                        }
                    }

                    Spacer(Modifier.height(UI.xlSpacing))

                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(UI.mediumSpacing)) {
                        OutlinedButton(onClick = onDismiss, modifier = Modifier.weight(1f).height(UI.buttonHeight), shape = buttonShape) {
                            Text(strings.cancel, color = THEME.textMuted)
                        }
                        Button(
                            onClick = {
                                val d = dose.toDoubleOrNull()
                                val c = conc.toDoubleOrNull()
                                if (sub != null && d != null && c != null && d > 0 && c > 0) {
                                    val compound = CycleCompound(
                                        substanceId = sub!!.id,
                                        weeklyDoseMg = d,
                                        frequency = freq,
                                        concentrationMgMl = c,
                                        startWeek = startWeek,
                                        endWeek = if (endWeek == cycleDurationWeeks) null else endWeek
                                    )
                                    repo.addCompoundToCycle(compound)
                                    scheduleInjectionNotifications(context)
                                    scheduleOralSteroidNotifications(context)
                                    onDismiss()
                                }
                            },
                            modifier = Modifier.weight(1f).height(UI.buttonHeight),
                            shape = buttonShape,
                            enabled = sub != null && dose.toDoubleOrNull()?.let { it > 0 } == true && conc.toDoubleOrNull()?.let { it > 0 } == true,
                            colors = ButtonDefaults.buttonColors(THEME.accentPrimary, THEME.textDark)
                        ) {
                            Icon(Icons.Default.Add, null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(UI.smallSpacing))
                            Text(strings.add)
                        }
                    }
                }
            }
        }
    }
}
