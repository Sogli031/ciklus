package com.injekcije.app.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.injekcije.app.THEME
import com.injekcije.app.UI
import com.injekcije.app.LocaleManager
import com.injekcije.app.Strings
import com.injekcije.app.data.InjectionRepository
import com.injekcije.app.data.backup.GoogleDriveBackup
import com.injekcije.app.data.backup.SignInState
import com.injekcije.app.notifications.cancelAllAlarms
import com.injekcije.app.notifications.scheduleInjectionNotifications
import com.injekcije.app.notifications.scheduleOralSteroidNotifications
import com.injekcije.app.notifications.schedulePCTNotifications
import com.injekcije.app.ui.components.buttonShape

@Composable
fun SettingsDialog(
    repo: InjectionRepository,
    localeManager: LocaleManager,
    backup: GoogleDriveBackup,
    strings: Strings,
    onDismiss: () -> Unit,
    onCloudBackup: () -> Unit,
    onNotificationSettings: () -> Unit
) {
    val context = LocalContext.current
    var showDeleteConfirm by remember { mutableStateOf(false) }
    val activeCycle = repo.getActiveCycle()
    val signInState by backup.signInState.collectAsState()

    if (showDeleteConfirm && activeCycle != null) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            confirmButton = {
                Button(
                    onClick = {
                        repo.deleteCycle(activeCycle.id); cancelAllAlarms(context); showDeleteConfirm = false; onDismiss()
                    },
                    colors = ButtonDefaults.buttonColors(THEME.danger, THEME.textPrimary),
                    shape = buttonShape
                ) { Text(strings.delete) }
            },
            dismissButton = { TextButton(onClick = { showDeleteConfirm = false }) { Text(strings.cancel, color = THEME.textMuted) } },
            title = { Text(strings.deleteCycle, color = THEME.textPrimary) },
            text = { Text(strings.deleteCycleConfirm, color = THEME.textSecondary) },
            containerColor = THEME.bgCard
        )
    } else {
        Dialog(onDismissRequest = onDismiss) {
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = THEME.bgCard,
                tonalElevation = 8.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    Modifier
                        .padding(UI.xlSpacing)
                        .verticalScroll(rememberScrollState())
                ) {
                    Text(
                        strings.settings,
                        color = THEME.textPrimary,
                        fontSize = UI.titleSize,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(UI.largeSpacing))

                    if (activeCycle != null) {
                        Text(
                            strings.activeCycle,
                            color = THEME.textSecondary,
                            fontSize = UI.captionSize,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(Modifier.height(UI.smallSpacing))
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = THEME.bgCardElevated,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                Modifier
                                    .fillMaxWidth()
                                    .heightIn(min = 64.dp)
                                    .padding(start = UI.mediumSpacing, top = UI.defaultSpacing, bottom = UI.defaultSpacing),
                                Arrangement.SpaceBetween,
                                Alignment.CenterVertically
                            ) {
                                Column(Modifier.weight(1f)) {
                                    Text(activeCycle.name, color = THEME.textPrimary, fontSize = UI.bodySize, fontWeight = FontWeight.Medium)
                                    Text("${strings.week} ${activeCycle.currentWeek()}/${activeCycle.durationWeeks}", color = THEME.textMuted, fontSize = UI.captionSize)
                                }
                                IconButton(
                                    onClick = { showDeleteConfirm = true },
                                    modifier = Modifier.size(UI.buttonHeight)
                                ) {
                                    Icon(
                                        Icons.Default.Delete,
                                        contentDescription = "Obriši aktivni ciklus",
                                        tint = THEME.danger,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                        Spacer(Modifier.height(UI.largeSpacing))
                    }

                    OutlinedButton(
                        onClick = { onDismiss(); onNotificationSettings() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = UI.buttonHeight),
                        shape = buttonShape,
                        contentPadding = PaddingValues(horizontal = UI.mediumSpacing, vertical = UI.defaultSpacing)
                    ) {
                        Icon(Icons.Default.Notifications, null, modifier = Modifier.size(18.dp), tint = THEME.accentPrimary)
                        Spacer(Modifier.width(UI.smallSpacing))
                        Column(Modifier.weight(1f)) {
                            Text(strings.notifications, color = THEME.textPrimary)
                            Text("Podsetnici za doze i PCT", color = THEME.textMuted, fontSize = UI.smallSize)
                        }
                        Icon(Icons.Default.ChevronRight, null, tint = THEME.textMuted)
                    }

                    Spacer(Modifier.height(UI.defaultSpacing))

                    OutlinedButton(
                        onClick = { onDismiss(); onCloudBackup() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = UI.buttonHeight),
                        shape = buttonShape,
                        contentPadding = PaddingValues(horizontal = UI.mediumSpacing, vertical = UI.defaultSpacing)
                    ) {
                        Icon(Icons.Default.Cloud, null, modifier = Modifier.size(18.dp), tint = THEME.accentPrimary)
                        Spacer(Modifier.width(UI.smallSpacing))
                        Column(Modifier.weight(1f)) {
                            Text("Backup u cloudu", color = THEME.textPrimary)
                            val statusText = when (signInState) {
                                is SignInState.SignedIn -> (signInState as SignInState.SignedIn).email
                                SignInState.SignedOut -> strings.notConnected
                            }
                            Text(statusText, color = THEME.textMuted, fontSize = UI.smallSize)
                        }
                        Icon(Icons.Default.ChevronRight, null, tint = THEME.textMuted)
                    }

                    Spacer(Modifier.height(UI.largeSpacing))

                    Text("${strings.version}: 1.0", color = THEME.textMuted, fontSize = UI.smallSize, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)

                    Spacer(Modifier.height(UI.mediumSpacing))

                    Button(
                        onClick = onDismiss,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(UI.buttonHeight),
                        shape = buttonShape,
                        colors = ButtonDefaults.buttonColors(THEME.accentPrimary, THEME.textDark)
                    ) {
                        Text(strings.close)
                    }
                }
            }
        }
    }
}

@Composable
fun NotificationSettingsDialog(
    repo: InjectionRepository,
    strings: Strings,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val currentSettings = remember { repo.getNotificationSettings() }

    var injectionEnabled by remember { mutableStateOf(currentSettings.injectionEnabled) }
    var injectionReminder5h by remember { mutableStateOf(currentSettings.injectionReminder5h) }
    var injectionReminder3h by remember { mutableStateOf(currentSettings.injectionReminder3h) }
    var injectionReminder30min by remember { mutableStateOf(currentSettings.injectionReminder30min) }
    var injectionReminderAtTime by remember { mutableStateOf(currentSettings.injectionReminderAtTime) }
    var injectionOverdueEnabled by remember { mutableStateOf(currentSettings.injectionOverdueEnabled) }
    var injectionOverdueInterval by remember { mutableIntStateOf(currentSettings.injectionOverdueIntervalMinutes) }

    var oralEnabled by remember { mutableStateOf(currentSettings.oralEnabled) }
    var oralReminder30min by remember { mutableStateOf(currentSettings.oralReminder30min) }
    var oralReminderAtTime by remember { mutableStateOf(currentSettings.oralReminderAtTime) }
    var oralOverdueEnabled by remember { mutableStateOf(currentSettings.oralOverdueEnabled) }
    var oralOverdueInterval by remember { mutableIntStateOf(currentSettings.oralOverdueIntervalMinutes) }
    var pctEnabled by remember { mutableStateOf(currentSettings.pctEnabled) }
    var pctAdvanceWarning by remember { mutableStateOf(currentSettings.pctAdvanceWarning) }
    var pctStartNotification by remember { mutableStateOf(currentSettings.pctStartNotification) }
    var pctBloodworkReminder by remember { mutableStateOf(currentSettings.pctBloodworkReminder) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = THEME.bgCard,
            tonalElevation = 8.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                Modifier
                    .padding(UI.xlSpacing)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    strings.notifications,
                    color = THEME.textPrimary,
                    fontSize = UI.titleSize,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(UI.xlSpacing))

                NotificationSectionHeader(
                    title = strings.injectionNotifications,
                    icon = Icons.Default.Notifications,
                    enabled = injectionEnabled,
                    onToggle = { injectionEnabled = it }
                )
                if (injectionEnabled) {
                    NotificationSwitch(strings.fiveHoursBefore, injectionReminder5h) { injectionReminder5h = it }
                    NotificationSwitch(strings.threeHoursBefore, injectionReminder3h) { injectionReminder3h = it }
                    NotificationSwitch(strings.thirtyMinutesBefore, injectionReminder30min) { injectionReminder30min = it }
                    NotificationSwitch(strings.atTime, injectionReminderAtTime) { injectionReminderAtTime = it }
                    NotificationSwitch(strings.overdueNotification, injectionOverdueEnabled) { injectionOverdueEnabled = it }
                    if (injectionOverdueEnabled) {
                        IntervalSelector(
                            label = strings.repeatInterval,
                            options = listOf(30 to "30 min", 60 to "1 sat", 120 to "2 sata"),
                            selected = injectionOverdueInterval,
                            onSelect = { injectionOverdueInterval = it }
                        )
                    }
                }
                Spacer(Modifier.height(UI.largeSpacing))

                NotificationSectionHeader(
                    title = strings.oralNotifications,
                    icon = Icons.Default.Medication,
                    enabled = oralEnabled,
                    onToggle = { oralEnabled = it }
                )
                if (oralEnabled) {
                    NotificationSwitch(strings.thirtyMinutesBefore, oralReminder30min) { oralReminder30min = it }
                    NotificationSwitch(strings.atTime, oralReminderAtTime) { oralReminderAtTime = it }
                    NotificationSwitch(strings.overdueNotification, oralOverdueEnabled) { oralOverdueEnabled = it }
                    if (oralOverdueEnabled) {
                        IntervalSelector(
                            label = strings.repeatInterval,
                            options = listOf(30 to "30 min", 60 to "1 sat", 120 to "2 sata"),
                            selected = oralOverdueInterval,
                            onSelect = { oralOverdueInterval = it }
                        )
                    }
                }
                Spacer(Modifier.height(UI.largeSpacing))


                NotificationSectionHeader(
                    title = strings.pctNotifications,
                    icon = Icons.Default.Science,
                    enabled = pctEnabled,
                    onToggle = { pctEnabled = it }
                )
                if (pctEnabled) {
                    NotificationSwitch(strings.threeDaysBefore, pctAdvanceWarning) { pctAdvanceWarning = it }
                    NotificationSwitch(strings.startDay, pctStartNotification) { pctStartNotification = it }
                    NotificationSwitch(strings.bloodworkReminder, pctBloodworkReminder) { pctBloodworkReminder = it }
                }

                Spacer(Modifier.height(UI.xlSpacing))

                Button(
                    onClick = {
                        val newSettings = com.injekcije.app.data.NotificationSettings(
                            injectionEnabled = injectionEnabled,
                            injectionReminder5h = injectionReminder5h,
                            injectionReminder3h = injectionReminder3h,
                            injectionReminder30min = injectionReminder30min,
                            injectionReminderAtTime = injectionReminderAtTime,
                            injectionOverdueEnabled = injectionOverdueEnabled,
                            injectionOverdueIntervalMinutes = injectionOverdueInterval,
                            oralEnabled = oralEnabled,
                            oralReminder30min = oralReminder30min,
                            oralReminderAtTime = oralReminderAtTime,
                            oralOverdueEnabled = oralOverdueEnabled,
                            oralOverdueIntervalMinutes = oralOverdueInterval,
                            pctEnabled = pctEnabled,
                            pctAdvanceWarning = pctAdvanceWarning,
                            pctStartNotification = pctStartNotification,
                            pctBloodworkReminder = pctBloodworkReminder
                        )
                        repo.updateNotificationSettings(newSettings)

                        scheduleInjectionNotifications(context)
                        scheduleOralSteroidNotifications(context)
                        schedulePCTNotifications(context, repo)

                        onDismiss()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = buttonShape,
                    colors = ButtonDefaults.buttonColors(THEME.accentPrimary, THEME.textDark)
                ) {
                    Text(strings.save)
                }
            }
        }
    }

}

@Composable
private fun NotificationSectionHeader(
    title: String,
    icon: ImageVector,
    enabled: Boolean,
    onToggle: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = UI.buttonHeight),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
            Icon(
                icon,
                contentDescription = null,
                tint = if (enabled) THEME.accentPrimary else THEME.textMuted,
                modifier = Modifier.size(20.dp)
            )
            Spacer(Modifier.width(UI.defaultSpacing))
            Column {
                Text(
                    title,
                    color = if (enabled) THEME.textPrimary else THEME.textMuted,
                    fontSize = UI.subtitleSize,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    if (enabled) "Uključeno" else "Isključeno",
                    color = THEME.textMuted,
                    fontSize = UI.smallSize
                )
            }
        }
        Switch(
            checked = enabled,
            onCheckedChange = onToggle,
            modifier = Modifier.semantics {
                contentDescription = "$title notifikacije"
                stateDescription = if (enabled) "Uključeno" else "Isključeno"
            },
            colors = SwitchDefaults.colors(
                checkedThumbColor = THEME.accentPrimary,
                checkedTrackColor = THEME.accentPrimary.copy(alpha = 0.3f),
                uncheckedThumbColor = THEME.textMuted,
                uncheckedTrackColor = THEME.bgCardElevated
            )
        )
    }
}

@Composable
private fun NotificationSwitch(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = UI.buttonHeight)
            .padding(start = UI.largeSpacing, end = UI.smallSpacing, top = UI.smallSpacing, bottom = UI.smallSpacing),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = THEME.textSecondary, fontSize = UI.bodySize, modifier = Modifier.weight(1f))
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            modifier = Modifier.semantics {
                contentDescription = label
                stateDescription = if (checked) "Uključeno" else "Isključeno"
            },
            colors = SwitchDefaults.colors(
                checkedThumbColor = THEME.accentPrimary,
                checkedTrackColor = THEME.accentPrimary.copy(alpha = 0.3f),
                uncheckedThumbColor = THEME.textMuted,
                uncheckedTrackColor = THEME.bgCardElevated
            )
        )
    }
}

@Composable
private fun IntervalSelector(
    label: String,
    options: List<Pair<Int, String>>,
    selected: Int,
    onSelect: (Int) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = UI.largeSpacing, end = UI.smallSpacing, top = UI.smallSpacing, bottom = UI.smallSpacing)
    ) {
        Text(label, color = THEME.textSecondary, fontSize = UI.captionSize)
        Spacer(Modifier.height(UI.smallSpacing))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(UI.smallSpacing)
        ) {
            options.forEach { (value, text) ->
                val isSelected = selected == value
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (isSelected) THEME.accentPrimary else THEME.bgCardElevated,
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = UI.buttonHeight)
                        .semantics {
                            contentDescription = "$label: $text"
                            stateDescription = if (isSelected) "Izabrano" else "Nije izabrano"
                        }
                        .clickable { onSelect(value) }
                ) {
                    Text(
                        text,
                        color = if (isSelected) THEME.textDark else THEME.textSecondary,
                        fontSize = UI.captionSize,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(vertical = UI.defaultSpacing)
                    )
                }
            }
        }
    }
}

