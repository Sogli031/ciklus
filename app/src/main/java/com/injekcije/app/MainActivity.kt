package com.injekcije.app

import android.Manifest
import android.app.AlarmManager
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import com.injekcije.app.data.InjectionRepository
import com.injekcije.app.data.backup.GoogleDriveBackup
import com.injekcije.app.notifications.scheduleInjectionNotifications
import com.injekcije.app.notifications.scheduleOralSteroidNotifications
import com.injekcije.app.ui.CrashLogScreen
import com.injekcije.app.ui.MainScreen
import java.io.File
import java.io.PrintWriter
import java.io.StringWriter

class MainActivity : ComponentActivity() {

    private lateinit var repo: InjectionRepository
    private lateinit var localeManager: LocaleManager
    private lateinit var backup: GoogleDriveBackup
    private var lastKnownNotificationPermission: Boolean? = null
    private var exactAlarmWarningShownThisForeground = false
    private var notificationPermissionRequestInFlight = false
    private var showExactAlarmPermissionDialog by mutableStateOf(false)

    private val notificationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            notificationPermissionRequestInFlight = false
            lastKnownNotificationPermission = granted
            if (granted) {
                rescheduleNotificationsIfReady()
            }
            requestExactAlarmPermissionIfNeeded()
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        WindowCompat.setDecorFitsSystemWindows(window, false)

        // Proveri crash log pre svega ostalog
        val crashFile = File(filesDir, "crash_log.txt")
        if (crashFile.exists()) {
            val crashLog = crashFile.readText()
            crashFile.delete()
            setContent {
                InjekcijeTheme {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = THEME.bgDark
                    ) {
                        CrashLogScreen(crashLog)
                    }
                }
            }
            return
        }

        // Nema crash loga - inicijalizuj normalno, ali wrappuj u try-catch
        try {
            val app = application as InjekcijeApp
            app.initialize()

            repo = app.repository
            localeManager = app.localeManager
            backup = app.googleDriveBackup

            lastKnownNotificationPermission = hasNotificationPermission()
            requestNotificationPermissionIfNeeded()

            setContent {
                ProvideStrings(localeManager) {
                    InjekcijeTheme {
                        Surface(
                            modifier = Modifier.fillMaxSize(),
                            color = THEME.bgDark
                        ) {
                            MainScreen(repo, localeManager, backup)
                            if (showExactAlarmPermissionDialog) {
                                ExactAlarmPermissionDialog(
                                    onOpenSettings = ::openExactAlarmSettings,
                                    onDismiss = { showExactAlarmPermissionDialog = false }
                                )
                            }
                        }
                    }
                }
            }
        } catch (e: Exception) {
            val sw = StringWriter()
            e.printStackTrace(PrintWriter(sw))
            val crashLog = sw.toString()

            setContent {
                InjekcijeTheme {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = THEME.bgDark
                    ) {
                        CrashLogScreen(crashLog)
                    }
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        exactAlarmWarningShownThisForeground = false
    }

    override fun onResume() {
        super.onResume()
        if (!::repo.isInitialized) return

        // Re-zakazivanje samo kada dozvola pređe iz odbijene u dozvoljenu.
        val hasPermission = hasNotificationPermission()
        val permissionBecameGranted = lastKnownNotificationPermission == false && hasPermission
        lastKnownNotificationPermission = hasPermission

        if (permissionBecameGranted) {
            rescheduleNotificationsIfReady()
        }
        requestExactAlarmPermissionIfNeeded()
    }

    private fun rescheduleNotificationsIfReady() {
        if (!::repo.isInitialized) return
        try {
            scheduleInjectionNotifications(this)
            scheduleOralSteroidNotifications(this)
        } catch (e: Exception) {
            Log.e("MainActivity", "Greška pri ponovnom zakazivanju notifikacija", e)
        }
    }

    private fun requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            return
        }

        if (hasNotificationPermission()) return

        val prefs = getSharedPreferences("injekcije_prefs", MODE_PRIVATE)
        val alreadyAsked = prefs.getBoolean("notification_permission_asked", false)
        if (!alreadyAsked) {
            prefs.edit().putBoolean("notification_permission_asked", true).apply()
            notificationPermissionRequestInFlight = true
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    private fun hasNotificationPermission(): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            return true
        }
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestExactAlarmPermissionIfNeeded() {
        if (notificationPermissionRequestInFlight || exactAlarmWarningShownThisForeground) return
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return

        val alarmManager = getSystemService(AlarmManager::class.java)
        if (alarmManager.canScheduleExactAlarms()) return

        exactAlarmWarningShownThisForeground = true
        showExactAlarmPermissionDialog = true
    }

    private fun openExactAlarmSettings() {
        showExactAlarmPermissionDialog = false
        runCatching { startActivity(exactAlarmSettingsIntent()) }
    }

    private fun exactAlarmSettingsIntent(): Intent {
        return Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
            data = Uri.parse("package:$packageName")
        }
    }
}

@Composable
fun InjekcijeTheme(content: @Composable () -> Unit) {
    val colorScheme = darkColorScheme(
        primary = THEME.accentPrimary,
        secondary = THEME.accentSecondary,
        background = THEME.bgDark,
        surface = THEME.bgCard,
        onPrimary = THEME.textDark,
        onSecondary = THEME.textDark,
        onBackground = THEME.textPrimary,
        onSurface = THEME.textPrimary
    )

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}

@Composable
private fun ExactAlarmPermissionDialog(
    onOpenSettings: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.88f)
                .wrapContentHeight(),
            shape = RoundedCornerShape(24.dp),
            color = THEME.bgCard,
            border = BorderStroke(1.dp, THEME.warning.copy(alpha = 0.45f))
        ) {
            Column(
                modifier = Modifier
                    .padding(UI.xlSpacing)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(UI.mediumSpacing),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Tačni alarmi nisu uključeni",
                    color = THEME.warning,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "Da bi podsetnici stizali u tačno vreme, uključi dozvolu Alarmi i podsetnici za ovu aplikaciju.",
                    color = THEME.textSecondary,
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center
                )
                Button(
                    onClick = onOpenSettings,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = UI.buttonHeight),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = THEME.warning,
                        contentColor = THEME.textDark
                    )
                ) {
                    Text("Otvori podešavanje", fontWeight = FontWeight.Bold)
                }
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = UI.buttonHeight),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, THEME.glassBorder),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = THEME.textSecondary)
                ) {
                    Text("Kasnije")
                }
            }
        }
    }
}
