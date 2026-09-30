package com.injekcije.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.injekcije.app.THEME
import com.injekcije.app.UI
import com.injekcije.app.Strings
import com.injekcije.app.data.InjectionRecord
import com.injekcije.app.data.InjectionRepository
import com.injekcije.app.data.InjectionSite
import com.injekcije.app.data.getDisplayName
import com.injekcije.app.domain.DoseCalculator
import com.injekcije.app.ui.components.DATE_TIME_FORMAT
import com.injekcije.app.ui.components.SHORT_DATE_FORMAT
import com.injekcije.app.ui.components.TIME_FORMAT
import com.injekcije.app.ui.components.buttonShape
import com.injekcije.app.ui.components.textFieldColors
import com.injekcije.app.ui.components.textFieldShape
import com.injekcije.app.notifications.scheduleInjectionNotifications
import java.util.Locale

@Composable
fun HistoryDialog(
    repo: InjectionRepository,
    strings: Strings,
    onDismiss: () -> Unit,
    onEdit: (InjectionRecord) -> Unit,
    onDelete: (String) -> Unit
) {
    val injections = remember { repo.getInjections() }
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onDismiss) { Text(strings.close, color = THEME.accentPrimary) }
        },
        text = {
            if (injections.isEmpty()) {
                Text(
                    strings.noInjections,
                    color = THEME.textMuted,
                    modifier = Modifier.padding(UI.largeSpacing)
                )
            } else {
                LazyColumn(Modifier.height(400.dp)) {
                    items(injections, key = { it.id }) { r ->
                        val s = repo.getSubstance(r.substanceId)
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .clickable { onEdit(r) }
                                .padding(vertical = UI.defaultSpacing),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        Modifier
                                            .size(8.dp)
                                            .background(
                                                THEME.getCategoryColor(s?.category ?: ""),
                                                CircleShape
                                            )
                                    )
                                    Spacer(Modifier.width(UI.defaultSpacing))
                                    Text(
                                        s?.shortName ?: r.substanceId,
                                        color = THEME.textPrimary,
                                        fontSize = UI.bodySize,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                                Spacer(Modifier.height(2.dp))
                                Text(
                                    r.dateTime.format(DATE_TIME_FORMAT),
                                    color = THEME.textMuted,
                                    fontSize = UI.captionSize
                                )
                                Spacer(Modifier.height(2.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Default.LocationOn,
                                        null,
                                        tint = THEME.accentSecondary,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Spacer(Modifier.width(4.dp))
                                    Text(
                                        r.site.getDisplayName(strings),
                                        color = THEME.accentSecondary,
                                        fontSize = UI.captionSize,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                                Text(
                                    DoseCalculator.formatDoseWithVolume(r.doseMg, r.volumeMl),
                                    color = THEME.textSecondary,
                                    fontSize = UI.smallSize
                                )
                                if (r.notes.isNotBlank()) {
                                    Text(
                                        r.notes,
                                        color = THEME.textMuted,
                                        fontSize = UI.smallSize,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                            IconButton(onClick = { onDelete(r.id) }) {
                                Icon(Icons.Default.Delete, null, tint = THEME.danger)
                            }
                        }
                        HorizontalDivider(
                            color = THEME.divider,
                            modifier = Modifier.padding(vertical = UI.smallSpacing)
                        )
                    }
                }
            }
        },
        title = { Text(strings.injectionHistory, color = THEME.textPrimary) },
        containerColor = THEME.bgCard
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditInjectionDialog(
    record: InjectionRecord,
    repo: InjectionRepository,
    strings: Strings,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var notes by remember { mutableStateOf(record.notes) }
    var site by remember { mutableStateOf(record.site) }
    var doseInput by remember { mutableStateOf(formatEditableNumber(record.doseMg)) }
    var volumeInput by remember { mutableStateOf(formatEditableNumber(record.volumeMl)) }
    var siteMenu by remember { mutableStateOf(false) }
    var selectedDateTime by remember { mutableStateOf(record.dateTime) }
    var showTimePicker by remember { mutableStateOf(false) }

    val latestDateTime = remember(record.id) {
        repo.getInjections().maxOfOrNull { it.dateTime }
    }
    val canEditTime = latestDateTime != null && record.dateTime == latestDateTime
    val concentrationMgMl = remember(record.id) {
        when {
            record.doseMg > 0.0 && record.volumeMl > 0.0 -> record.doseMg / record.volumeMl
            else -> {
                val fromCycle = repo.getActiveCycle()
                    ?.compounds
                    ?.find { it.substanceId == record.substanceId }
                    ?.concentrationMgMl
                val fromSubstance = repo.getSubstance(record.substanceId)?.defaultConcentration
                (fromCycle ?: fromSubstance ?: 1.0).coerceAtLeast(1.0)
            }
        }
    }

    val doseMg = parsePositiveDecimal(doseInput)
    val volumeMl = parsePositiveDecimal(volumeInput)
    val hasValidInput = doseMg != null && volumeMl != null

    val s = repo.getSubstance(record.substanceId)

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(
                onClick = {
                    if (doseMg == null || volumeMl == null) return@Button

                    val updatedRecord = record.copy(
                        dateTime = if (canEditTime) selectedDateTime else record.dateTime,
                        site = site,
                        doseMg = doseMg,
                        volumeMl = volumeMl,
                        notes = notes
                    )
                    repo.updateInjection(updatedRecord)
                    scheduleInjectionNotifications(context)
                    onDismiss()
                },
                enabled = hasValidInput,
                colors = ButtonDefaults.buttonColors(THEME.accentPrimary, THEME.textDark)
            ) {
                Text(strings.save)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(strings.cancel, color = THEME.textMuted) }
        },
        title = { Text(strings.editInjection, color = THEME.textPrimary) },
        text = {
            Column {
                Text(
                    s?.shortName ?: record.substanceId,
                    color = THEME.textPrimary,
                    fontSize = UI.subtitleSize
                )
                Spacer(Modifier.height(UI.smallSpacing))
                Text(
                    "Datum: ${record.dateTime.toLocalDate().format(SHORT_DATE_FORMAT)}",
                    color = THEME.textMuted,
                    fontSize = UI.smallSize
                )
                Spacer(Modifier.height(UI.mediumSpacing))

                Text(strings.site, color = THEME.textMuted, fontSize = UI.captionSize)
                Spacer(Modifier.height(UI.smallSpacing))
                Box {
                    OutlinedButton(
                        onClick = { siteMenu = true },
                        modifier = Modifier.fillMaxWidth(),
                        shape = buttonShape
                    ) {
                        Text(site.getDisplayName(strings), color = THEME.textPrimary)
                    }
                    DropdownMenu(expanded = siteMenu, onDismissRequest = { siteMenu = false }) {
                        InjectionSite.rotationOrder().forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option.getDisplayName(strings)) },
                                onClick = {
                                    site = option
                                    siteMenu = false
                                }
                            )
                        }
                    }
                }

                Spacer(Modifier.height(UI.mediumSpacing))
                OutlinedTextField(
                    value = doseInput,
                    onValueChange = { input ->
                        val sanitized = sanitizeDecimalInput(input)
                        doseInput = sanitized
                        val doseValue = parsePositiveDecimal(sanitized)
                        if (doseValue != null) {
                            val derivedVolume = DoseCalculator.mgToMl(doseValue, concentrationMgMl).getOrNull()
                            if (derivedVolume != null) {
                                volumeInput = formatEditableNumber(derivedVolume)
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Doza (mg)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    colors = textFieldColors(),
                    shape = textFieldShape
                )

                Spacer(Modifier.height(UI.smallSpacing))
                OutlinedTextField(
                    value = volumeInput,
                    onValueChange = { input ->
                        val sanitized = sanitizeDecimalInput(input)
                        volumeInput = sanitized
                        val volumeValue = parsePositiveDecimal(sanitized)
                        if (volumeValue != null) {
                            val derivedDose = DoseCalculator.mlToMg(volumeValue, concentrationMgMl).getOrNull()
                            if (derivedDose != null) {
                                doseInput = formatEditableNumber(derivedDose)
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Količina (mL)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    colors = textFieldColors(),
                    shape = textFieldShape
                )

                if (volumeMl != null && volumeMl > site.maxVolumeMl) {
                    Spacer(Modifier.height(UI.smallSpacing))
                    Text(
                        "Upozorenje: ${DoseCalculator.formatVolume(volumeMl)} prelazi preporuku za ${site.getDisplayName(strings)} (${DoseCalculator.formatVolume(site.maxVolumeMl)}).",
                        color = THEME.warning,
                        fontSize = UI.smallSize
                    )
                }

                if (!hasValidInput) {
                    Spacer(Modifier.height(UI.smallSpacing))
                    Text(
                        "Unesi validnu dozu i količinu (broj veći od 0).",
                        color = THEME.danger,
                        fontSize = UI.smallSize
                    )
                }

                Spacer(Modifier.height(UI.mediumSpacing))
                Text("Vreme injekcije", color = THEME.textMuted, fontSize = UI.captionSize)
                Spacer(Modifier.height(UI.smallSpacing))
                if (canEditTime) {
                    OutlinedButton(
                        onClick = { showTimePicker = true },
                        modifier = Modifier.fillMaxWidth(),
                        shape = buttonShape
                    ) {
                        Icon(Icons.Default.Schedule, null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(UI.smallSpacing))
                        Text(
                            selectedDateTime.format(TIME_FORMAT),
                            color = THEME.textPrimary
                        )
                    }
                } else {
                    Text(
                        "Vreme možeš da menjaš samo za poslednju injekciju.",
                        color = THEME.textMuted,
                        fontSize = UI.smallSize
                    )
                }

                Spacer(Modifier.height(UI.mediumSpacing))
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(strings.notes) },
                    colors = textFieldColors(),
                    maxLines = 3,
                    shape = textFieldShape
                )
            }
        },
        containerColor = THEME.bgCard
    )

    if (showTimePicker) {
        val timePickerState = rememberTimePickerState(
            initialHour = selectedDateTime.hour,
            initialMinute = selectedDateTime.minute,
            is24Hour = true
        )
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    selectedDateTime = selectedDateTime
                        .withHour(timePickerState.hour)
                        .withMinute(timePickerState.minute)
                    showTimePicker = false
                }) { Text("OK", color = THEME.accentPrimary) }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) {
                    Text(strings.cancel, color = THEME.textMuted)
                }
            },
            text = { TimePicker(state = timePickerState) },
            containerColor = THEME.bgCard
        )
    }
}

@Composable
fun ConfirmDeleteDialog(
    strings: Strings,
    message: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) = AlertDialog(
    onDismissRequest = onDismiss,
    confirmButton = {
        Button(
            onClick = onConfirm,
            colors = ButtonDefaults.buttonColors(THEME.danger, THEME.textPrimary)
        ) {
            Text(strings.delete)
        }
    },
    dismissButton = {
        TextButton(onClick = onDismiss) { Text(strings.cancel, color = THEME.textMuted) }
    },
    title = { Text(strings.confirmDelete, color = THEME.textPrimary) },
    text = { Text(message, color = THEME.textSecondary) },
    containerColor = THEME.bgCard
)

private fun sanitizeDecimalInput(raw: String): String {
    val normalized = raw
        .replace(',', '.')
        .filter { it.isDigit() || it == '.' }

    val firstDot = normalized.indexOf('.')
    return if (firstDot < 0) {
        normalized
    } else {
        normalized.substring(0, firstDot + 1) +
            normalized.substring(firstDot + 1).replace(".", "")
    }
}

private fun parsePositiveDecimal(input: String): Double? {
    val normalized = sanitizeDecimalInput(input)
    val value = normalized.toDoubleOrNull()
    return if (value != null && value.isFinite() && value > 0.0) {
        value
    } else {
        null
    }
}

private fun formatEditableNumber(value: Double): String {
    if (!value.isFinite()) return ""
    return if (value == value.toLong().toDouble()) {
        value.toLong().toString()
    } else {
        String.format(Locale.US, "%.3f", value)
            .trimEnd('0')
            .trimEnd('.')
    }
}
