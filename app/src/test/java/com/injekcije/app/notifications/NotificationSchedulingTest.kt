package com.injekcije.app.notifications

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class NotificationSchedulingTest {
    @Test
    fun `notification scheduler prefers exact alarms with bounded fallback`() {
        val source = listOf(
            File("src/main/java/com/injekcije/app/notifications/Notifications.kt"),
            File("app/src/main/java/com/injekcije/app/notifications/Notifications.kt")
        ).first { it.exists() }.readText()

        assertTrue(source.contains("scheduleNotificationAlarm("))
        assertTrue(source.contains("setExactAndAllowWhileIdle"))
        assertTrue(source.contains("NOTIFICATION_FALLBACK_WINDOW_MILLIS = 10 * 60 * 1000L"))
        assertFalse(source.contains("setAndAllowWhileIdle"))
    }

    @Test
    fun `boot receiver reschedules after time changes`() {
        val receiver = listOf(
            File("src/main/java/com/injekcije/app/notifications/BootReceiver.kt"),
            File("app/src/main/java/com/injekcije/app/notifications/BootReceiver.kt")
        ).first { it.exists() }.readText()
        val manifest = listOf(
            File("src/main/AndroidManifest.xml"),
            File("app/src/main/AndroidManifest.xml")
        ).first { it.exists() }.readText()

        assertTrue(receiver.contains("Intent.ACTION_TIME_CHANGED"))
        assertTrue(receiver.contains("Intent.ACTION_TIMEZONE_CHANGED"))
        assertTrue(manifest.contains("android.intent.action.TIME_SET"))
        assertTrue(manifest.contains("android.intent.action.TIMEZONE_CHANGED"))
    }

    @Test
    fun `main activity warns about exact alarm permission on every startup`() {
        val source = listOf(
            File("src/main/java/com/injekcije/app/MainActivity.kt"),
            File("app/src/main/java/com/injekcije/app/MainActivity.kt")
        ).first { it.exists() }.readText()

        assertTrue(source.contains("override fun onResume()"))
        assertTrue(source.contains("requestExactAlarmPermissionIfNeeded()"))
        assertTrue(source.contains("ExactAlarmPermissionDialog("))
        assertTrue(source.contains("mutableStateOf(false)"))
        assertTrue(source.contains("Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM"))
        assertFalse(source.contains("AlertDialog.Builder(this)"))
        assertFalse(source.contains("exact_alarm_permission_asked"))
    }
}
