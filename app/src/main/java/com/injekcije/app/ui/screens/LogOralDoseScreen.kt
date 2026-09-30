package com.injekcije.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.injekcije.app.THEME
import com.injekcije.app.UI
import com.injekcije.app.Strings
import com.injekcije.app.data.*
import com.injekcije.app.notifications.scheduleOralSteroidNotifications
import com.injekcije.app.ui.components.buttonShape
import com.injekcije.app.ui.components.DATE_TIME_FORMAT
import com.injekcije.app.ui.components.TIME_FORMAT
import com.injekcije.app.ui.components.SHORT_DATE_FORMAT
import java.time.LocalDateTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LogOralDoseDialog(
    substanceId: String,
    substanceName: String,
    dosePerServing: Double,
    repo: InjectionRepository,
    strings: Strings,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val cycle = repo.getActiveCycle()
    if (cycle == null) {
        // Promena stanja ne sme da se desi tokom kompozicije
        LaunchedEffect(Unit) { onDismiss() }
        return
    }
    val todayDoses = remember { repo.getTodayOralDosesForSubstance(substanceId) }
    var selectedDateTime by remember { mutableStateOf(LocalDateTime.now()) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    var showEarlyConfirm by remember { mutableStateOf(false) }
    var earlyConfirmMessage by remember { mutableStateOf("") }

    val saveDose = {
        val recordWeek = cycle.currentWeek(selectedDateTime.toLocalDate())
        val record = OralDoseRecord(
            cycleId = cycle.id,
            substanceId = substanceId,
            dateTime = selectedDateTime,
            doseMg = dosePerServing,
            week = recordWeek
        )
        repo.addOralDose(record)
        scheduleOralSteroidNotifications(context)
        onDismiss()
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = THEME.bgCard,
            tonalElevation = 8.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                Modifier.padding(UI.xlSpacing),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Unesi oralnu dozu", color = THEME.textPrimary, fontSize = UI.titleSize, fontWeight = FontWeight.Bold)

                Spacer(Modifier.height(UI.largeSpacing))

                Text(substanceName, color = THEME.accentSecondary, fontSize = UI.subtitleSize, fontWeight = FontWeight.Medium)
                Text("${dosePerServing.toInt()} mg po dozi", color = THEME.textMuted, fontSize = UI.bodySize)

                Spacer(Modifier.height(UI.mediumSpacing))

                if (todayDoses.isNotEmpty()) {
                    Text("Danas uzeto: ${todayDoses.size} x", color = THEME.textSecondary, fontSize = UI.captionSize)
                    val times = todayDoses.map { it.dateTime.toLocalTime().format(TIME_FORMAT) }
                    Text(times.joinToString(", "), color = THEME.textMuted, fontSize = UI.smallSize)
                    Spacer(Modifier.height(UI.mediumSpacing))
                }

                Text("Datum i vreme", color = THEME.textMuted, fontSize = UI.captionSize)
                Spacer(Modifier.height(UI.smallSpacing))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(UI.smallSpacing)) {
                    OutlinedButton(
                        onClick = { showDatePicker = true },
                        modifier = Modifier.weight(1f).heightIn(min = UI.buttonHeight),
                        shape = buttonShape,
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Icon(Icons.Default.CalendarToday, null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text(selectedDateTime.format(SHORT_DATE_FORMAT), color = THEME.textPrimary, maxLines = 1, softWrap = false)
                    }
                    OutlinedButton(
                        onClick = { showTimePicker = true },
                        modifier = Modifier.weight(1f).heightIn(min = UI.buttonHeight),
                        shape = buttonShape,
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Icon(Icons.Default.Schedule, null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text(selectedDateTime.format(TIME_FORMAT), color = THEME.textPrimary, maxLines = 1, softWrap = false)
                    }
                }
                Spacer(Modifier.height(UI.mediumSpacing))

                Button(
                    onClick = {
                        val schedule = OralDosingSchedule.forSubstance(substanceId)
                        val lastDose = repo.getLastOralDoseForSubstance(substanceId)
                        val expectedTime = if (schedule != null && lastDose != null &&
                            selectedDateTime.isAfter(lastDose.dateTime)
                        ) {
                            val intervalMinutes = (24 * 60) / schedule.dosesPerDay
                            lastDose.dateTime.plusMinutes(intervalMinutes.toLong())
                        } else null

                        if (expectedTime != null && selectedDateTime.isBefore(expectedTime)) {
                            earlyConfirmMessage =
                                "Doza je planirana za ${expectedTime.format(DATE_TIME_FORMAT)}. Sačuvati ipak?"
                            showEarlyConfirm = true
                        } else {
                            saveDose()
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(UI.buttonHeight),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(THEME.accentSecondary, THEME.textDark)
                ) {
                    Icon(Icons.Default.Check, null, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(UI.defaultSpacing))
                    Text("Sačuvaj dozu", fontSize = UI.bodySize, fontWeight = FontWeight.Bold)
                }

                Spacer(Modifier.height(UI.defaultSpacing))

                TextButton(onClick = onDismiss) {
                    Text(strings.cancel, color = THEME.textMuted)
                }
            }
        }
    }

    if (showEarlyConfirm) {
        AlertDialog(
            onDismissRequest = { showEarlyConfirm = false },
            title = { Text("Potvrdi raniji unos", color = THEME.textPrimary) },
            text = { Text(earlyConfirmMessage, color = THEME.textSecondary) },
            confirmButton = {
                TextButton(onClick = {
                    showEarlyConfirm = false
                    saveDose()
                }) { Text("Sačuvaj raniji unos", color = THEME.accentPrimary) }
            },
            dismissButton = {
                TextButton(onClick = { showEarlyConfirm = false }) {
                    Text(strings.cancel, color = THEME.textMuted)
                }
            },
            containerColor = THEME.bgCard
        )
    }

    if (showDatePicker) {
        // Material3 DatePicker radi u UTC - lokalna zona pravi pomeraj od jednog dana
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = selectedDateTime.toLocalDate()
                .atStartOfDay(java.time.ZoneOffset.UTC).toInstant().toEpochMilli()
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val newDate = java.time.Instant.ofEpochMilli(millis)
                            .atZone(java.time.ZoneOffset.UTC)
                            .toLocalDate()
                        selectedDateTime = LocalDateTime.of(newDate, selectedDateTime.toLocalTime())
                    }
                    showDatePicker = false
                }) { Text("Primeni datum", color = THEME.accentPrimary) }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text(strings.cancel, color = THEME.textMuted) }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

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
                    selectedDateTime = selectedDateTime.withHour(timePickerState.hour).withMinute(timePickerState.minute)
                    showTimePicker = false
                }) { Text("Primeni vreme", color = THEME.accentPrimary) }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) { Text(strings.cancel, color = THEME.textMuted) }
            },
            title = { Text("Vreme doze", color = THEME.textPrimary) },
            text = { TimePicker(state = timePickerState) },
            containerColor = THEME.bgCard
        )
    }
}
