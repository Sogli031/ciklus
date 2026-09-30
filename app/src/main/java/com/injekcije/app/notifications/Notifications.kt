package com.injekcije.app.notifications

import android.Manifest
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.injekcije.app.MainActivity
import com.injekcije.app.data.InjectionRepository
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.Locale
import java.util.concurrent.TimeUnit
import kotlin.math.abs

private const val CHANNEL_ID_REMINDER = "injection_reminder"
private const val CHANNEL_ID_OVERDUE = "injection_overdue"
private const val CHANNEL_ID_PCT = "pct_reminder"
private const val CHANNEL_ID_ORAL = "oral_steroid_reminder"
const val NOTIFICATION_ID_REMINDER = 3001
const val NOTIFICATION_ID_OVERDUE = 3002
const val NOTIFICATION_ID_PCT_START = 3003
const val NOTIFICATION_ID_PCT_BLOODWORK = 3004
const val NOTIFICATION_ID_ORAL_BASE = 5000
private const val NOTIFICATION_FALLBACK_WINDOW_MILLIS = 10 * 60 * 1000L
// Reminder times before injection (in minutes)
private val REMINDER_MINUTES = listOf(300, 180, 30, 0) // 5h, 3h, 30min, 0min

// PCT alarm request codes i podešavanja
private const val PCT_START_REQUEST_CODE = 4001
private const val PCT_ADVANCE_REQUEST_CODE = 4002
private const val PCT_BLOODWORK_REQUEST_CODE = 4003
private const val PCT_ADVANCE_WARNING_DAYS = 3L
private const val PCT_NOTIFICATION_HOUR = 9

private const val ACTION_PCT_START = "com.injekcije.PCT_START"
private const val ACTION_PCT_ADVANCE = "com.injekcije.PCT_ADVANCE"
private const val ACTION_PCT_BLOODWORK = "com.injekcije.PCT_BLOODWORK"

// SharedPreferences za praćenje zakazanih oral-overdue alarma
private const val NOTIFICATION_PREFS_NAME = "injekcije_prefs"
private const val PREF_KEY_ORAL_OVERDUE_CODES = "oral_overdue_request_codes"

const val EXTRA_NOTIFICATION_TYPE = "notification_type"
const val EXTRA_MINUTES_BEFORE = "minutes_before"
const val EXTRA_SUBSTANCE_ID = "substance_id"
const val EXTRA_SUBSTANCE_NAME = "substance_name"
const val EXTRA_SCHEDULED_TIME = "scheduled_time"
const val EXTRA_DOSE_NUMBER = "dose_number"
const val EXTRA_TOTAL_DOSES = "total_doses"
const val EXTRA_DOSE_MG = "dose_mg"
const val EXTRA_PCT_ADVANCE = "pct_advance"

const val TYPE_REMINDER = "reminder"
const val TYPE_OVERDUE_CHECK = "overdue_check"
const val TYPE_ORAL_DOSE = "oral_dose"
const val TYPE_ORAL_OVERDUE_CHECK = "oral_overdue_check"
fun scheduleInjectionNotifications(context: Context) {
    ensureNotificationChannels(context)

    val repository = InjectionRepository.getInstance(context)

    val settings = repository.getNotificationSettings()

    // Ako su injekcije isključene, otkaži alarme za injekcije i ukloni prikazana obaveštenja
    if (!settings.injectionEnabled) {
        cancelInjectionNotifications(context)
        return
    }

    val nextInjection = repository.getMostUrgentInjection()
    if (nextInjection == null) {
        cancelInjectionNotifications(context)
        return
    }

    val scheduledTime = nextInjection.scheduledDateTime
    val substanceName = nextInjection.substanceName

    // Cancel existing injection alarms first.
    // Namerno se NE uklanja prikazana overdue notifikacija - nju uklanja
    // overdue-check receiver kada injekcija bude evidentirana.
    try {
        cancelInjectionAlarms(context)
    } catch (e: Exception) {
        Log.e("Notifications", "Greška pri otkazivanju postojećih alarma: ${e.message}")
    }

    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    val now = LocalDateTime.now()

    // Provera dozvole za tačne alarme na Android 12+
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        if (!alarmManager.canScheduleExactAlarms()) {
            Log.w("Notifications", "Nema dozvole za tačne alarme, koristi se inexact fallback")
        }
    }

    // Filtriraj reminder minute na osnovu settings
    val enabledReminders = REMINDER_MINUTES.filter { minutes ->
        when (minutes) {
            300 -> settings.injectionReminder5h
            180 -> settings.injectionReminder3h
            30 -> settings.injectionReminder30min
            0 -> settings.injectionReminderAtTime
            else -> true
        }
    }

    var scheduledCount = 0
    var failedCount = 0

    // Schedule reminder alarms
    enabledReminders.forEachIndexed { index, minutesBefore ->
        val alarmTime = scheduledTime.minusMinutes(minutesBefore.toLong())

        // Only schedule if alarm time is in the future
        if (alarmTime.isAfter(now)) {
            try {
                val intent = Intent(context, NotificationReceiver::class.java).apply {
                    action = "com.injekcije.INJECTION_REMINDER_$index"
                    putExtra(EXTRA_NOTIFICATION_TYPE, TYPE_REMINDER)
                    putExtra(EXTRA_MINUTES_BEFORE, minutesBefore)
                    putExtra(EXTRA_SUBSTANCE_NAME, substanceName)
                    putExtra(EXTRA_SCHEDULED_TIME, scheduledTime.toString())
                }

                val pendingIntent = PendingIntent.getBroadcast(
                    context,
                    1000 + index,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )

                val triggerTime = alarmTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

                scheduleNotificationAlarm(alarmManager, triggerTime, pendingIntent)

                scheduledCount++
                Log.d("Notifications", "Zakazan alarm za injekciju: $substanceName, $minutesBefore min pre, vreme: $alarmTime")
            } catch (e: SecurityException) {
                failedCount++
                Log.e("Notifications", "SecurityException pri zakazivanju alarma za injekciju: ${e.message}")
            } catch (e: Exception) {
                failedCount++
                Log.e("Notifications", "Greška pri zakazivanju alarma za injekciju: ${e.message}")
            }
        }
    }

    if (failedCount > 0) {
        Log.w("Notifications", "Zakazivanje injekcija završeno: $scheduledCount uspešno, $failedCount neuspešno od ${enabledReminders.size}")
    }

    // Schedule overdue check samo ako je overdue omogućen.
    // Anti-spam: ovde se nikada ne prikazuje overdue notifikacija direktno;
    // scheduler samo računa i zakazuje sledeći check alarm.
    if (settings.injectionOverdueEnabled) {
        val firstOverdueCheck = scheduledTime.plusMinutes(15)
        val overdueIntervalMinutes = settings.injectionOverdueIntervalMinutes.coerceAtLeast(1)
        val nextOverdueCheck = calculateNextInjectionOverdueCheck(
            now = now,
            firstCheckTime = firstOverdueCheck,
            intervalMinutes = overdueIntervalMinutes
        )

        try {
            scheduleOverdueCheck(context, alarmManager, nextOverdueCheck, substanceName, scheduledTime.toString())
            Log.d("Notifications", "Zakazan overdue check za: $substanceName, vreme: $nextOverdueCheck")
        } catch (e: Exception) {
            Log.e("Notifications", "Greška pri zakazivanju overdue check-a: ${e.message}")
        }
    }
}

private fun calculateNextInjectionOverdueCheck(
    now: LocalDateTime,
    firstCheckTime: LocalDateTime,
    intervalMinutes: Int
): LocalDateTime {
    var nextCheck = firstCheckTime
    while (!nextCheck.isAfter(now)) {
        nextCheck = nextCheck.plusMinutes(intervalMinutes.toLong())
    }
    return nextCheck
}

private fun scheduleOverdueCheck(
    context: Context,
    alarmManager: AlarmManager,
    checkTime: LocalDateTime,
    substanceName: String,
    scheduledTime: String
) {
    val intent = Intent(context, NotificationReceiver::class.java).apply {
        action = "com.injekcije.OVERDUE_CHECK"
        putExtra(EXTRA_NOTIFICATION_TYPE, TYPE_OVERDUE_CHECK)
        putExtra(EXTRA_SUBSTANCE_NAME, substanceName)
        putExtra(EXTRA_SCHEDULED_TIME, scheduledTime)
    }

    val pendingIntent = PendingIntent.getBroadcast(
        context,
        2000,
        intent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    val triggerTime = checkTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

    scheduleNotificationAlarm(alarmManager, triggerTime, pendingIntent)
}

private fun scheduleNotificationAlarm(
    alarmManager: AlarmManager,
    triggerAtMillis: Long,
    pendingIntent: PendingIntent
) {
    if (canScheduleNotificationExactAlarms(alarmManager)) {
        val exactScheduled = runCatching {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
            } else {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
            }
        }.isSuccess
        if (exactScheduled) return
    }
    alarmManager.setWindow(
        AlarmManager.RTC_WAKEUP,
        triggerAtMillis,
        NOTIFICATION_FALLBACK_WINDOW_MILLIS,
        pendingIntent
    )
}

private fun canScheduleNotificationExactAlarms(alarmManager: AlarmManager): Boolean {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return true
    return alarmManager.canScheduleExactAlarms()
}

/**
 * Otkazuje SVE alarme aplikacije (injekcije + oralni + oral-overdue + PCT)
 * i uklanja sva prikazana obaveštenja. Koristi se npr. pri brisanju ciklusa.
 */
fun cancelAllAlarms(context: Context) {
    cancelInjectionAlarms(context)
    cancelOralNotifications(context)
    cancelPCTNotifications(context)

    // Cancel any shown notifications
    val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    notificationManager.cancel(NOTIFICATION_ID_REMINDER)
    notificationManager.cancel(NOTIFICATION_ID_OVERDUE)
}

/**
 * Otkazuje samo alarme za injekcije (podsetnici + overdue check),
 * bez uklanjanja prikazanih obaveštenja. Koristi se u reschedule putanji.
 */
private fun cancelInjectionAlarms(context: Context) {
    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    // Cancel reminder alarms
    REMINDER_MINUTES.forEachIndexed { index, _ ->
        val intent = Intent(context, NotificationReceiver::class.java).apply {
            action = "com.injekcije.INJECTION_REMINDER_$index"
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            1000 + index,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
    }

    // Cancel overdue check
    val overdueIntent = Intent(context, NotificationReceiver::class.java).apply {
        action = "com.injekcije.OVERDUE_CHECK"
    }
    val overduePendingIntent = PendingIntent.getBroadcast(
        context,
        2000,
        overdueIntent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )
    alarmManager.cancel(overduePendingIntent)
}

/**
 * Otkazuje alarme za injekcije i uklanja prikazana obaveštenja za injekcije.
 */
fun cancelInjectionNotifications(context: Context) {
    cancelInjectionAlarms(context)

    val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    notificationManager.cancel(NOTIFICATION_ID_REMINDER)
    notificationManager.cancel(NOTIFICATION_ID_OVERDUE)
}

private fun ensureNotificationChannels(context: Context) {
    val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    // Reminder channel
    manager.createNotificationChannel(
        NotificationChannel(
            CHANNEL_ID_REMINDER,
            "Podsetnici za injekcije",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Obaveštenja pre zakazane injekcije"
            enableVibration(true)
        }
    )

    // Overdue channel (highest priority, persistent)
    manager.createNotificationChannel(
        NotificationChannel(
            CHANNEL_ID_OVERDUE,
            "Propuštene injekcije",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Obaveštenja za propuštene injekcije koje se ne mogu odbaciti"
            enableVibration(true)
        }
    )

    // PCT channel
    manager.createNotificationChannel(
        NotificationChannel(
            CHANNEL_ID_PCT,
            "PCT podsetnici",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Podsetnici za Post-Cycle Therapy (PCT) - početak, krvne slike"
            enableVibration(true)
        }
    )

    // Oral steroid channel
    manager.createNotificationChannel(
        NotificationChannel(
            CHANNEL_ID_ORAL,
            "Podsetnici za tablete",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Podsetnici za uzimanje oralnih steroida (višestruke dnevne doze)"
            enableVibration(true)
        }
    )}

private fun notificationsAllowed(context: Context): Boolean {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
        ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
        != PackageManager.PERMISSION_GRANTED
    ) {
        return false
    }
    return NotificationManagerCompat.from(context).areNotificationsEnabled()
}

fun showReminderNotification(context: Context, minutesBefore: Int, substanceName: String) {
    if (!notificationsAllowed(context)) return

    ensureNotificationChannels(context)

    val (title, text) = when (minutesBefore) {
        300 -> "Injekcija za 5 sati" to "$substanceName - pripremi se"
        180 -> "Injekcija za 3 sata" to "$substanceName - ne zaboravi"
        30 -> "Injekcija za 30 minuta!" to "$substanceName - uskoro je vreme"
        0 -> "Vreme je za injekciju!" to "$substanceName - uradi sada"
        else -> "Podsetnik za injekciju" to substanceName
    }

    val intent = Intent(context, MainActivity::class.java).apply {
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
    }
    val pendingIntent = PendingIntent.getActivity(
        context, 0, intent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    val notification = NotificationCompat.Builder(context, CHANNEL_ID_REMINDER)
        .setSmallIcon(android.R.drawable.ic_menu_today)
        .setContentTitle(title)
        .setContentText(text)
        .setContentIntent(pendingIntent)
        .setAutoCancel(true)
        .setPriority(NotificationCompat.PRIORITY_HIGH)
        .setCategory(NotificationCompat.CATEGORY_REMINDER)
        .build()

    try {
        NotificationManagerCompat.from(context).notify(NOTIFICATION_ID_REMINDER, notification)
    } catch (e: SecurityException) {
        // Permission not granted
    }
}

fun showOverdueNotification(context: Context, substanceName: String, scheduledTime: String) {
    // Calculate how late
    val scheduled = LocalDateTime.parse(scheduledTime)
    val now = LocalDateTime.now()

    if (notificationsAllowed(context)) {
        ensureNotificationChannels(context)

        val minutesLate = java.time.Duration.between(scheduled, now).toMinutes()

        val lateText = when {
            minutesLate < 60 -> "$minutesLate min"
            minutesLate < 1440 -> "${minutesLate / 60}h ${minutesLate % 60}min"
            else -> "${minutesLate / 1440} dana"
        }

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Persistent notification - cannot be dismissed
        val notification = NotificationCompat.Builder(context, CHANNEL_ID_OVERDUE)
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentTitle("PROPUŠTENA INJEKCIJA!")
            .setContentText("$substanceName - kasniš $lateText")
            .setStyle(NotificationCompat.BigTextStyle().bigText("$substanceName - kasniš $lateText\n\nOtvori aplikaciju i unesi injekciju da ukloniš ovo obaveštenje."))
            .setContentIntent(pendingIntent)
            .setOngoing(true) // Cannot be dismissed!
            .setAutoCancel(false)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setVisibility(NotificationCompat.VISIBILITY_PRIVATE)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(NOTIFICATION_ID_OVERDUE, notification)
        } catch (e: SecurityException) {
            // Permission not granted
        }
    }

    // Schedule next overdue check to update the "late" time.
    // Zakazuje se i kada su notifikacije trenutno zabranjene, da bi se lanac provera
    // nastavio ako korisnik naknadno omogući notifikacije u sistemskim podešavanjima.
    val repository = InjectionRepository.getInstance(context)
    val overdueInterval = repository.getNotificationSettings().injectionOverdueIntervalMinutes.toLong()
    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    scheduleOverdueCheck(context, alarmManager, now.plusMinutes(overdueInterval), substanceName, scheduledTime)
}

fun dismissOverdueNotification(context: Context) {
    val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    notificationManager.cancel(NOTIFICATION_ID_OVERDUE)
}

fun sendTestNotification(context: Context): Boolean {
    if (!notificationsAllowed(context)) return false

    ensureNotificationChannels(context)
    showReminderNotification(context, 0, "Test supstanca")
    return true
}

fun areNotificationsEnabled(context: Context): Boolean = notificationsAllowed(context)

// =============================================================================
// ORAL STEROID NOTIFICATIONS
// =============================================================================

// Reminder minutes before oral dose (30min advance warning + exact time)
private val ORAL_REMINDER_MINUTES = listOf(30, 0)
private const val ORAL_OVERDUE_REQUEST_BASE = 700000
private const val ORAL_OVERDUE_REQUEST_MOD = 100000

/**
 * Schedule notifications for oral steroids based on DYNAMIC timing.
 * Instead of fixed times (8:00, 20:00, etc.), notifications are based on
 * when the user ACTUALLY took the last dose + the dosing interval.
 *
 * For example:
 * - If dosesPerDay = 2, interval = 12 hours
 * - If last dose was at 21:30, next dose notification at 09:30 (21:30 + 12h)
 * - 30-minute warning at 09:00
 */
fun scheduleOralSteroidNotifications(context: Context, suppressImmediate: Boolean = true) {
    ensureNotificationChannels(context)

    val repository = InjectionRepository.getInstance(context)

    val settings = repository.getNotificationSettings()

    // Ako su oralne notifikacije isključene, otkaži sve
    if (!settings.oralEnabled) {
        cancelOralNotifications(context)
        return
    }

    val cycle = repository.getActiveCycle()
    if (cycle == null) {
        cancelOralNotifications(context)
        return
    }

        val currentWeek = cycle.currentWeek()
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val now = LocalDateTime.now()

        // Cancel existing oral alarms first
        try {
            cancelOralNotifications(context)
        } catch (e: Exception) {
            Log.e("Notifications", "Greška pri otkazivanju postojećih oralnih alarma: ${e.message}")
        }

        // Filtriraj oral reminder minute na osnovu settings
        val enabledOralReminders = ORAL_REMINDER_MINUTES.filter { minutes ->
            when (minutes) {
                30 -> settings.oralReminder30min
                0 -> settings.oralReminderAtTime
                else -> true
            }
        }

        val oralOverdueInterval = settings.oralOverdueIntervalMinutes

        var alarmIndex = 0

        for (compound in cycle.compounds) {
            // Skip if not active this week
            if (!compound.isActiveInWeek(currentWeek, cycle.durationWeeks)) continue

            // Skip non-oral steroids
            if (!com.injekcije.app.data.OralDosingSchedule.isOralSteroid(compound.substanceId)) continue

            val schedule = com.injekcije.app.data.OralDosingSchedule.forSubstance(compound.substanceId) ?: continue
            val substance = repository.getSubstance(compound.substanceId) ?: continue

            // Calculate dose per administration
            val weeklyDose = compound.getDoseForWeek(currentWeek)
            val dailyDose = weeklyDose / 7.0
            val dosePerAdmin = dailyDose / schedule.dosesPerDay

            // Calculate interval between doses (in minutes)
            val intervalMinutes = (24 * 60) / schedule.dosesPerDay

            // Get last dose for this substance
            val lastDose = repository.getLastOralDoseForSubstance(compound.substanceId)

            // Calculate next dose time based on last dose
            val nextDoseTime = if (lastDose != null) {
                lastDose.dateTime.plusMinutes(intervalMinutes.toLong())
            } else {
                // Nema prethodne doze - zakaži prvi podsetnik za sledeći
                // konfigurisani termin doziranja, da bi alarm bio u budućnosti
                nextConfiguredDoseTime(now, schedule.dosingTimes)
            }

            // If dose time is now or in the past, treat as overdue until user logs it.
            if (!nextDoseTime.isAfter(now)) {
                if (!suppressImmediate) {
                    // Dose is overdue - show notification now
                    showOralDoseNotification(
                        context,
                        substance.shortName,
                        1, // dose number not critical for overdue
                        schedule.dosesPerDay,
                        dosePerAdmin,
                        0 // at dose time
                    )
                }
                if (settings.oralOverdueEnabled) {
                    scheduleOralOverdueCheck(
                        context,
                        now.plusMinutes(oralOverdueInterval.toLong()),
                        compound.substanceId,
                        substance.shortName,
                        nextDoseTime,
                        1,
                        schedule.dosesPerDay,
                        dosePerAdmin
                    )
                }
            }

            // Determine which dose number this is (1, 2, or 3 out of dosesPerDay)
            val todayDosesCount = repository.getTodayOralDosesForSubstance(compound.substanceId).size
            val nextDoseNumber = (todayDosesCount % schedule.dosesPerDay) + 1

            // Schedule reminders based on enabled settings
            enabledOralReminders.forEach { minutesBefore ->
                val alarmTime = nextDoseTime.minusMinutes(minutesBefore.toLong())

                // Only schedule if alarm time is in the future
                if (alarmTime.isAfter(now)) {
                    val intent = Intent(context, NotificationReceiver::class.java).apply {
                        action = "com.injekcije.ORAL_DOSE_${alarmIndex}"
                        putExtra(EXTRA_NOTIFICATION_TYPE, TYPE_ORAL_DOSE)
                        putExtra(EXTRA_SUBSTANCE_ID, compound.substanceId)
                        putExtra(EXTRA_SUBSTANCE_NAME, substance.shortName)
                        putExtra(EXTRA_SCHEDULED_TIME, nextDoseTime.toString())
                        putExtra(EXTRA_DOSE_NUMBER, nextDoseNumber)
                        putExtra(EXTRA_TOTAL_DOSES, schedule.dosesPerDay)
                        putExtra(EXTRA_DOSE_MG, dosePerAdmin)
                        putExtra(EXTRA_MINUTES_BEFORE, minutesBefore)
                    }

                    val pendingIntent = PendingIntent.getBroadcast(
                        context,
                        NOTIFICATION_ID_ORAL_BASE + alarmIndex,
                        intent,
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                    )

                    val triggerTime = alarmTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

                    scheduleNotificationAlarm(alarmManager, triggerTime, pendingIntent)

                    alarmIndex++
                }
            }
        }
}

/**
 * Vraća prvi sledeći konfigurisani termin doziranja (sati iz rasporeda, npr. 8, 14, 20)
 * koji je posle [now] - danas ili sutra.
 */
private fun nextConfiguredDoseTime(now: LocalDateTime, dosingHours: List<Int>): LocalDateTime {
    val hours = dosingHours.sorted().ifEmpty { listOf(8) }
    val today = now.toLocalDate()
    val candidates = hours.map { today.atTime(it, 0) } + hours.map { today.plusDays(1).atTime(it, 0) }
    return candidates.first { it.isAfter(now) }
}

/**
 * Show notification for oral steroid dose.
 * @param minutesBefore 0 = time to take, 30 = 30min warning
 */
fun showOralDoseNotification(
    context: Context,
    substanceName: String,
    doseNumber: Int,
    totalDoses: Int,
    doseMg: Double,
    minutesBefore: Int = 0
) {
    if (!notificationsAllowed(context)) return

    ensureNotificationChannels(context)

    val doseText = "${String.format(Locale.getDefault(), "%.1f", doseMg)}mg"
    val (title, text) = when (minutesBefore) {
        30 -> "⏰ $substanceName za 30 min" to "Doza $doseNumber od $totalDoses ($doseText) - pripremi se"
        0 -> "💊 $substanceName - SADA" to "Uzmi dozu $doseNumber od $totalDoses ($doseText)"
        else -> "$substanceName - doza $doseNumber" to "Uzmi $doseText"
    }

    val intent = Intent(context, MainActivity::class.java).apply {
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
    }
    val pendingIntent = PendingIntent.getActivity(
        context, 0, intent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    // Use unique notification ID based on substance, dose number, and minutesBefore to avoid overwriting.
    // Supstanca koristi korak 10000, broj doze 1000, a indeks podsetnika (0 ili 1)
    // poslednju cifru - tako parovi (doseNumber, minutesBefore) ne mogu da kolidiraju.
    val minutesBeforeIndex = if (minutesBefore > 0) 1 else 0
    val notificationId = NOTIFICATION_ID_ORAL_BASE +
        abs(substanceName.hashCode() % 1000) * 10000 +
        doseNumber * 1000 +
        minutesBeforeIndex

    val notification = NotificationCompat.Builder(context, CHANNEL_ID_ORAL)
        .setSmallIcon(android.R.drawable.ic_menu_today)
        .setContentTitle(title)
        .setContentText(text)
        .setContentIntent(pendingIntent)
        .setAutoCancel(true)
        .setPriority(NotificationCompat.PRIORITY_HIGH)
        .setCategory(NotificationCompat.CATEGORY_REMINDER)
        .build()

    try {
        NotificationManagerCompat.from(context).notify(notificationId, notification)
    } catch (e: SecurityException) {
        // Permission not granted
    }
}

private fun oralOverdueRequestCode(substanceId: String, scheduledTime: LocalDateTime): Int {
    val raw = 31 * substanceId.hashCode() + scheduledTime.hashCode()
    val normalized = abs(raw % ORAL_OVERDUE_REQUEST_MOD)
    return ORAL_OVERDUE_REQUEST_BASE + normalized
}

fun scheduleOralOverdueCheck(
    context: Context,
    checkTime: LocalDateTime,
    substanceId: String,
    substanceName: String,
    scheduledTime: LocalDateTime,
    doseNumber: Int,
    totalDoses: Int,
    doseMg: Double
) {
    val now = LocalDateTime.now()
    if (checkTime.isBefore(now)) return

    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    val requestCode = oralOverdueRequestCode(substanceId, scheduledTime)

    val intent = Intent(context, NotificationReceiver::class.java).apply {
        action = "com.injekcije.ORAL_OVERDUE_$requestCode"
        putExtra(EXTRA_NOTIFICATION_TYPE, TYPE_ORAL_OVERDUE_CHECK)
        putExtra(EXTRA_SUBSTANCE_ID, substanceId)
        putExtra(EXTRA_SUBSTANCE_NAME, substanceName)
        putExtra(EXTRA_SCHEDULED_TIME, scheduledTime.toString())
        putExtra(EXTRA_DOSE_NUMBER, doseNumber)
        putExtra(EXTRA_TOTAL_DOSES, totalDoses)
        putExtra(EXTRA_DOSE_MG, doseMg)
    }

    val pendingIntent = PendingIntent.getBroadcast(
        context,
        requestCode,
        intent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    val triggerTime = checkTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

    scheduleNotificationAlarm(alarmManager, triggerTime, pendingIntent)

    // Zapamti request code da bi alarm mogao da se otkaže (npr. pri brisanju ciklusa)
    rememberOralOverdueRequestCode(context, requestCode)
}

private fun notificationPrefs(context: Context) =
    context.getSharedPreferences(NOTIFICATION_PREFS_NAME, Context.MODE_PRIVATE)

private fun rememberOralOverdueRequestCode(context: Context, requestCode: Int) {
    val prefs = notificationPrefs(context)
    val codes = prefs.getStringSet(PREF_KEY_ORAL_OVERDUE_CODES, emptySet())
        ?.toMutableSet() ?: mutableSetOf()
    if (codes.add(requestCode.toString())) {
        prefs.edit().putStringSet(PREF_KEY_ORAL_OVERDUE_CODES, codes).apply()
    }
}

/**
 * Otkazuje sve zakazane oral-overdue check alarme (praćene kroz SharedPreferences)
 * i briše evidenciju. Bez ovoga bi se overdue provere same obnavljale u nedogled.
 */
fun cancelOralOverdueChecks(context: Context) {
    val prefs = notificationPrefs(context)
    val codes = prefs.getStringSet(PREF_KEY_ORAL_OVERDUE_CODES, emptySet()) ?: emptySet()
    if (codes.isEmpty()) return

    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    codes.mapNotNull { it.toIntOrNull() }.forEach { requestCode ->
        val intent = Intent(context, NotificationReceiver::class.java).apply {
            action = "com.injekcije.ORAL_OVERDUE_$requestCode"
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
        pendingIntent.cancel()
    }
    prefs.edit().remove(PREF_KEY_ORAL_OVERDUE_CODES).apply()
}

/**
 * Cancel all oral steroid notification alarms (dose reminders + overdue checks).
 */
fun cancelOralNotifications(context: Context) {
    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    // Cancel up to 50 potential oral alarms (covers multiple substances with multiple daily doses)
    for (i in 0 until 50) {
        val intent = Intent(context, NotificationReceiver::class.java).apply {
            action = "com.injekcije.ORAL_DOSE_$i"
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            NOTIFICATION_ID_ORAL_BASE + i,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
    }

    // Cancel self-perpetuating overdue checks as well
    cancelOralOverdueChecks(context)
}

// =============================================================================
// PCT NOTIFICATIONS
// =============================================================================

/**
 * Zakazuje PCT alarme na osnovu aktivnog PCT-a:
 * - najavu početka PCT-a (podrazumevano 3 dana ranije, ako je uključeno pctAdvanceWarning)
 * - podsetnik na dan preporučenog početka PCT-a (pctStartNotification)
 * - podsetnik za sledeću krvnu sliku tokom aktivnog PCT-a (pctBloodworkReminder)
 *
 * Prethodno zakazani PCT alarmi se uvek prvo otkazuju (konzistentno schedule/cancel).
 */
fun schedulePCTNotifications(context: Context, repository: InjectionRepository) {
    ensureNotificationChannels(context)

    // Otkaži prethodno zakazane PCT alarme pre novog zakazivanja
    cancelPCTAlarms(context)

    val settings = repository.getNotificationSettings()
    if (!settings.pctEnabled) return

    val pctInfo = repository.getActivePCTGuideInfo() ?: return
    val pct = pctInfo.pctData

    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    val now = LocalDateTime.now()

    // Podsetnici za početak PCT-a - samo dok PCT još nije započet
    if (pct.pctStartDate == null && pct.pctEndDate == null) {
        val startDateTime = pct.recommendedStartDate.atTime(PCT_NOTIFICATION_HOUR, 0)

        if (settings.pctStartNotification && startDateTime.isAfter(now)) {
            try {
                schedulePCTAlarm(
                    context, alarmManager, startDateTime,
                    ACTION_PCT_START, PCT_START_REQUEST_CODE, TYPE_PCT_START, isAdvance = false
                )
                Log.d("Notifications", "Zakazan PCT start alarm za: $startDateTime")
            } catch (e: Exception) {
                Log.e("Notifications", "Greška pri zakazivanju PCT start alarma: ${e.message}")
            }
        }

        if (settings.pctAdvanceWarning) {
            val advanceDateTime = startDateTime.minusDays(PCT_ADVANCE_WARNING_DAYS)
            if (advanceDateTime.isAfter(now)) {
                try {
                    schedulePCTAlarm(
                        context, alarmManager, advanceDateTime,
                        ACTION_PCT_ADVANCE, PCT_ADVANCE_REQUEST_CODE, TYPE_PCT_START, isAdvance = true
                    )
                    Log.d("Notifications", "Zakazana PCT najava za: $advanceDateTime")
                } catch (e: Exception) {
                    Log.e("Notifications", "Greška pri zakazivanju PCT najave: ${e.message}")
                }
            }
        }
    }

    // Podsetnik za krvnu sliku - tokom aktivnog PCT-a
    if (settings.pctBloodworkReminder) {
        val bloodworkDate = pctInfo.nextBloodworkDate
        if (bloodworkDate != null) {
            val bloodworkDateTime = bloodworkDate.atTime(PCT_NOTIFICATION_HOUR, 0)
            if (bloodworkDateTime.isAfter(now)) {
                try {
                    schedulePCTAlarm(
                        context, alarmManager, bloodworkDateTime,
                        ACTION_PCT_BLOODWORK, PCT_BLOODWORK_REQUEST_CODE, TYPE_PCT_BLOODWORK, isAdvance = false
                    )
                    Log.d("Notifications", "Zakazan PCT bloodwork alarm za: $bloodworkDateTime")
                } catch (e: Exception) {
                    Log.e("Notifications", "Greška pri zakazivanju PCT bloodwork alarma: ${e.message}")
                }
            }
        }
    }
}

private fun schedulePCTAlarm(
    context: Context,
    alarmManager: AlarmManager,
    alarmTime: LocalDateTime,
    action: String,
    requestCode: Int,
    notificationType: String,
    isAdvance: Boolean
) {
    val intent = Intent(context, NotificationReceiver::class.java).apply {
        this.action = action
        putExtra(EXTRA_NOTIFICATION_TYPE, notificationType)
        putExtra(EXTRA_PCT_ADVANCE, isAdvance)
    }

    val pendingIntent = PendingIntent.getBroadcast(
        context,
        requestCode,
        intent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    val triggerTime = alarmTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

    scheduleNotificationAlarm(alarmManager, triggerTime, pendingIntent)
}

/**
 * Otkazuje samo PCT alarme (bez uklanjanja prikazanih obaveštenja).
 */
private fun cancelPCTAlarms(context: Context) {
    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    listOf(
        ACTION_PCT_START to PCT_START_REQUEST_CODE,
        ACTION_PCT_ADVANCE to PCT_ADVANCE_REQUEST_CODE,
        ACTION_PCT_BLOODWORK to PCT_BLOODWORK_REQUEST_CODE
    ).forEach { (action, requestCode) ->
        val intent = Intent(context, NotificationReceiver::class.java).apply {
            this.action = action
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
    }
}

/**
 * Otkazuje PCT alarme i uklanja prikazana PCT obaveštenja.
 */
fun cancelPCTNotifications(context: Context) {
    cancelPCTAlarms(context)

    val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    notificationManager.cancel(NOTIFICATION_ID_PCT_START)
    notificationManager.cancel(NOTIFICATION_ID_PCT_BLOODWORK)
}

fun showPCTStartNotification(context: Context, protocolName: String, daysUntil: Long) {
    if (!notificationsAllowed(context)) return

    ensureNotificationChannels(context)

    val title = if (daysUntil <= 0) "PCT počinje danas!" else "PCT počinje uskoro"
    val dayWord = if (daysUntil == 1L) "dan" else "dana"
    val text = if (daysUntil <= 0) {
        "Vaš $protocolName protokol počinje danas!"
    } else {
        "Vaš $protocolName protokol počinje za $daysUntil $dayWord."
    }

    val intent = Intent(context, MainActivity::class.java).apply {
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
    }
    val pendingIntent = PendingIntent.getActivity(
        context, 0, intent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    val notification = NotificationCompat.Builder(context, CHANNEL_ID_PCT)
        .setSmallIcon(android.R.drawable.ic_dialog_info)
        .setContentTitle(title)
        .setContentText(text)
        .setContentIntent(pendingIntent)
        .setAutoCancel(true)
        .setPriority(NotificationCompat.PRIORITY_HIGH)
        .setCategory(NotificationCompat.CATEGORY_REMINDER)
        .build()

    try {
        NotificationManagerCompat.from(context).notify(NOTIFICATION_ID_PCT_START, notification)
    } catch (e: SecurityException) {
        // Permission not granted
    }
}

fun showPCTBloodworkNotification(context: Context) {
    if (!notificationsAllowed(context)) return

    ensureNotificationChannels(context)

    val intent = Intent(context, MainActivity::class.java).apply {
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
    }
    val pendingIntent = PendingIntent.getActivity(
        context, 0, intent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    val notification = NotificationCompat.Builder(context, CHANNEL_ID_PCT)
        .setSmallIcon(android.R.drawable.ic_dialog_info)
        .setContentTitle("Podsetnik za krvnu sliku")
        .setContentText("Vreme je da uradiš krvne nalaze za praćenje PCT-a.")
        .setContentIntent(pendingIntent)
        .setAutoCancel(true)
        .setPriority(NotificationCompat.PRIORITY_HIGH)
        .setCategory(NotificationCompat.CATEGORY_REMINDER)
        .build()

    try {
        NotificationManagerCompat.from(context).notify(NOTIFICATION_ID_PCT_BLOODWORK, notification)
    } catch (e: SecurityException) {
        // Permission not granted
    }
}

// =============================================================================
// SAFETY-NET PERIODIC WORK
// =============================================================================

private const val SAFETY_NET_WORK_NAME = "notification_safety_net"
private const val SAFETY_NET_INTERVAL_HOURS = 6L

/**
 * Periodični WorkManager safety-net: na svakih ~6 sati ponovo pokreće sve
 * schedulere (injekcije, oralni steroidi, PCT) za slučaj da su alarmi
 * izgubljeni (force-stop, battery optimizacija, itd.).
 */
fun scheduleSafetyNetWork(context: Context) {
    val request = PeriodicWorkRequestBuilder<RescheduleWorker>(
        SAFETY_NET_INTERVAL_HOURS, TimeUnit.HOURS
    ).build()

    WorkManager.getInstance(context).enqueueUniquePeriodicWork(
        SAFETY_NET_WORK_NAME,
        ExistingPeriodicWorkPolicy.UPDATE,
        request
    )
}

fun cancelSafetyNetWork(context: Context) {
    WorkManager.getInstance(context).cancelUniqueWork(SAFETY_NET_WORK_NAME)
}

const val TYPE_PCT_START = "pct_start"
const val TYPE_PCT_BLOODWORK = "pct_bloodwork"
