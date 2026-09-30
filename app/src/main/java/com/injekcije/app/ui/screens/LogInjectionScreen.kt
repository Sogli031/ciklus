package com.injekcije.app.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.injekcije.app.THEME
import com.injekcije.app.UI
import com.injekcije.app.Strings
import com.injekcije.app.data.*
import com.injekcije.app.domain.DoseCalculator
import com.injekcije.app.notifications.dismissOverdueNotification
import com.injekcije.app.notifications.scheduleInjectionNotifications
import com.injekcije.app.ui.components.buttonShape
import com.injekcije.app.ui.components.textFieldColors
import com.injekcije.app.ui.components.textFieldShape
import com.injekcije.app.ui.components.DATE_TIME_FORMAT
import com.injekcije.app.ui.components.TIME_FORMAT
import com.injekcije.app.ui.components.SHORT_DATE_FORMAT
import java.time.LocalDateTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LogInjectionDialog(info: NextInjectionInfo?, repo: InjectionRepository, strings: Strings, onDismiss: () -> Unit) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val cycle = repo.getActiveCycle()
    if (cycle == null) {
        // Promena stanja ne sme da se desi tokom kompozicije
        LaunchedEffect(Unit) { onDismiss() }
        return
    }
    val currentWeek = cycle.currentWeek()

    // Injekcija sme da se loguje samo za injektabilne supstance - oralne se
    // unose kroz LogOralDoseDialog i ne smeju da troše rotaciju mesta
    val injectableCompounds = cycle.compounds.filter { !OralDosingSchedule.isOralSteroid(it.substanceId) }

    if (info == null && injectableCompounds.isEmpty()) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text("Nema injektabilnih supstanci", color = THEME.textPrimary) },
            text = { Text("Nema injektabilnih supstanci u ciklusu.", color = THEME.textSecondary) },
            confirmButton = {
                TextButton(onClick = onDismiss) { Text(strings.close, color = THEME.accentPrimary) }
            },
            containerColor = THEME.bgCard
        )
        return
    }

    var selectedSubIds by remember {
        mutableStateOf(listOfNotNull(info?.substanceId ?: injectableCompounds.firstOrNull()?.substanceId))
    }
    var site by remember { mutableStateOf(info?.site ?: repo.getRecommendedSite()) }
    var notes by remember { mutableStateOf("") }
    var siteMenu by remember { mutableStateOf(false) }
    var addSubMenu by remember { mutableStateOf(false) }
    var selectedDateTime by remember { mutableStateOf(LocalDateTime.now()) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    var showEarlyConfirm by remember { mutableStateOf(false) }
    var earlyConfirmMessage by remember { mutableStateOf("") }

    val selectedCompounds = selectedSubIds.mapNotNull { subId ->
        cycle.compounds.find { it.substanceId == subId }?.let { compound ->
            compound to repo.getSubstance(subId)
        }
    }

    val saveInjection = {
        if (selectedCompounds.isNotEmpty()) {
            val recordWeek = cycle.currentWeek(selectedDateTime.toLocalDate())
            val records = selectedCompounds.map { (compound, _) ->
                InjectionRecord(
                    cycleId = cycle.id,
                    substanceId = compound.substanceId,
                    dateTime = selectedDateTime,
                    site = site,
                    doseMg = compound.getDosePerInjectionForWeek(recordWeek),
                    volumeMl = compound.getVolumePerInjectionForWeek(recordWeek),
                    week = recordWeek,
                    notes = if (selectedCompounds.size > 1) "[${strings.combined}] $notes" else notes
                )
            }
            repo.addCombinedInjections(records)
            dismissOverdueNotification(context)
            scheduleInjectionNotifications(context)
            onDismiss()
        }
    }

    val totalVolume = selectedCompounds.sumOf { it.first.getVolumePerInjectionForWeek(currentWeek) }
    val availableToAdd = injectableCompounds.filter { it.substanceId !in selectedSubIds }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = THEME.bgCard,
            tonalElevation = 8.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(Modifier.padding(UI.xlSpacing).verticalScroll(rememberScrollState())) {
                Text(strings.logInjection, color = THEME.textPrimary, fontSize = UI.titleSize, fontWeight = FontWeight.Bold)

                Spacer(Modifier.height(UI.largeSpacing))

                Text(strings.substances, color = THEME.textMuted, fontSize = UI.captionSize)
                Spacer(Modifier.height(UI.smallSpacing))

                selectedCompounds.forEachIndexed { _, (compound, substance) ->
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = THEME.bgCardElevated,
                        modifier = Modifier.fillMaxWidth().padding(vertical = UI.smallSpacing)
                    ) {
                        Row(
                            Modifier.fillMaxWidth().padding(UI.mediumSpacing),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                Box(Modifier.size(8.dp).background(THEME.getCategoryColor(substance?.category ?: ""), CircleShape))
                                Spacer(Modifier.width(UI.defaultSpacing))
                                Column {
                                    Text(substance?.shortName ?: compound.substanceId, color = THEME.textPrimary, fontSize = UI.bodySize, fontWeight = FontWeight.Medium)
                                    Text(DoseCalculator.formatDoseWithVolume(compound.getDosePerInjectionForWeek(currentWeek), compound.getVolumePerInjectionForWeek(currentWeek)), color = THEME.accentPrimary, fontSize = UI.captionSize)
                                }
                            }
                            if (selectedSubIds.size > 1) {
                                IconButton(
                                    onClick = { selectedSubIds = selectedSubIds.filter { it != compound.substanceId } },
                                    modifier = Modifier.size(UI.buttonHeight)
                                ) {
                                    Icon(
                                        Icons.Default.Close,
                                        contentDescription = "Ukloni ${substance?.shortName ?: compound.substanceId}",
                                        tint = THEME.textMuted,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                if (availableToAdd.isNotEmpty()) {
                    Spacer(Modifier.height(UI.smallSpacing))
                    Box {
                        OutlinedButton(
                            onClick = { addSubMenu = true },
                            modifier = Modifier.fillMaxWidth(),
                            shape = buttonShape,
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = THEME.accentSecondary)
                        ) {
                            Icon(Icons.Default.Add, null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(UI.smallSpacing))
                            Text(strings.addAnotherSubstance)
                        }
                        DropdownMenu(addSubMenu, { addSubMenu = false }) {
                            availableToAdd.forEach { c ->
                                val s = repo.getSubstance(c.substanceId)
                                DropdownMenuItem(
                                    text = {
                                        Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                                            Text(s?.shortName ?: c.substanceId)
                                            Text(DoseCalculator.formatVolume(c.getVolumePerInjectionForWeek(currentWeek)), color = THEME.textMuted, fontSize = UI.smallSize)
                                        }
                                    },
                                    onClick = {
                                        selectedSubIds = selectedSubIds + c.substanceId
                                        addSubMenu = false
                                    }
                                )
                            }
                        }
                    }
                }

                if (selectedCompounds.size > 1) {
                    Spacer(Modifier.height(UI.mediumSpacing))
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = THEME.accentPrimary.copy(0.1f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(Modifier.padding(UI.mediumSpacing), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Info, null, tint = THEME.accentPrimary, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(UI.defaultSpacing))
                            Text("${strings.totalVolume}: ${DoseCalculator.formatVolume(totalVolume)}", color = THEME.accentPrimary, fontSize = UI.captionSize, fontWeight = FontWeight.Medium)
                        }
                    }
                }

                Spacer(Modifier.height(UI.largeSpacing))

                Text(strings.site, color = THEME.textMuted, fontSize = UI.captionSize)
                Spacer(Modifier.height(UI.smallSpacing))
                val enabledSites = repo.getEnabledSites()
                // Istorija mesta se dohvata jednom, ne po stavci menija pri svakoj rekompoziciji
                val siteHistories = remember { repo.getSiteHistory() }
                Box {
                    OutlinedButton({ siteMenu = true }, Modifier.fillMaxWidth(), shape = buttonShape) {
                        Text(site.getDisplayName(strings), color = THEME.textPrimary)
                    }
                    DropdownMenu(siteMenu, { siteMenu = false }) {
                        InjectionSite.rotationOrder().filter { it in enabledSites }.forEach { s ->
                            val h = siteHistories.find { it.site == s }
                            DropdownMenuItem(
                                text = {
                                    Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                                        Text(s.getDisplayName(strings))
                                        Text(if (h?.daysSinceLastUse != null) "${h.daysSinceLastUse}d" else "-", color = if (h?.isAvailable == true) THEME.success else THEME.warning, fontSize = UI.smallSize)
                                    }
                                },
                                onClick = { site = s; siteMenu = false }
                            )
                        }
                    }
                }

                if (totalVolume > site.maxVolumeMl) {
                    Spacer(Modifier.height(UI.smallSpacing))
                    Text("${strings.warning}: ${DoseCalculator.formatVolume(totalVolume)} ${strings.exceedsMax} ${DoseCalculator.formatVolume(site.maxVolumeMl)} ${strings.forSite} ${site.getDisplayName(strings)}", color = THEME.warning, fontSize = UI.smallSize)
                }

                Spacer(Modifier.height(UI.mediumSpacing))
                OutlinedTextField(notes, { notes = it }, Modifier.fillMaxWidth(), label = { Text(strings.notesOptional) }, colors = textFieldColors(), maxLines = 2, shape = textFieldShape)

                Spacer(Modifier.height(UI.mediumSpacing))
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

                Spacer(Modifier.height(UI.xlSpacing))

                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(UI.mediumSpacing)) {
                    OutlinedButton(onClick = onDismiss, modifier = Modifier.weight(1f).height(UI.buttonHeight), shape = buttonShape) {
                        Text(strings.cancel, color = THEME.textMuted)
                    }
                    Button(
                        onClick = {
                            if (selectedCompounds.isNotEmpty()) {
                                val earlyEntries = selectedCompounds.mapNotNull { (compound, substance) ->
                                    val last = repo.getLastInjectionForSubstance(compound.substanceId)
                                    if (last != null && selectedDateTime.isAfter(last.dateTime)) {
                                        val intervalMinutes = (compound.frequency.days * 24 * 60).toLong()
                                        val expectedTime = last.dateTime.plusMinutes(intervalMinutes)
                                        if (selectedDateTime.isBefore(expectedTime)) {
                                            val name = substance?.shortName ?: compound.substanceId
                                            name to expectedTime
                                        } else null
                                    } else null
                                }

                                if (earlyEntries.isNotEmpty()) {
                                    earlyConfirmMessage = buildString {
                                        append("Unos je pre planiranog termina:")
                                        earlyEntries.forEach { (name, time) ->
                                        append("\n- ").append(name).append(": ")
                                            .append(time.format(DATE_TIME_FORMAT))
                                        }
                                        append("\nSačuvati ipak?")
                                    }
                                    showEarlyConfirm = true
                                } else {
                                    saveInjection()
                                }
                            }
                        },
                        modifier = Modifier.weight(1f).height(UI.buttonHeight),
                        shape = buttonShape,
                        enabled = selectedCompounds.isNotEmpty(),
                        colors = ButtonDefaults.buttonColors(THEME.accentPrimary, THEME.textDark)
                    ) {
                        Text(strings.save)
                    }
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
                    saveInjection()
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
                        selectedDateTime = java.time.LocalDateTime.of(newDate, selectedDateTime.toLocalTime())
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
                    selectedDateTime = selectedDateTime
                        .withHour(timePickerState.hour)
                        .withMinute(timePickerState.minute)
                    showTimePicker = false
                }) { Text("Primeni vreme", color = THEME.accentPrimary) }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) { Text(strings.cancel, color = THEME.textMuted) }
            },
            title = { Text("Vreme unosa", color = THEME.textPrimary) },
            text = { TimePicker(state = timePickerState) },
            containerColor = THEME.bgCard
        )
    }
}
