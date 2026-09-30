package com.injekcije.app.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.injekcije.app.data.InjectionRepository
import java.time.LocalDateTime
import java.util.concurrent.Executors

private const val ORAL_DOSE_LOG_GRACE_MINUTES = 30

// Zajednički background executor za receiver-e (izbegava rad na main thread-u)
internal val receiverExecutor = Executors.newSingleThreadExecutor()

class NotificationReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val type = intent.getStringExtra(EXTRA_NOTIFICATION_TYPE) ?: return
        val appContext = context.applicationContext

        // Učitavanje JSON podataka + zakazivanje alarma ne sme da blokira main thread
        val pendingResult = goAsync()
        receiverExecutor.execute {
            try {
                handle(appContext, intent, type)
            } catch (e: Exception) {
                Log.e("NotificationReceiver", "Greška pri obradi notifikacije ($type): ${e.message}")
            } finally {
                pendingResult.finish()
            }
        }
    }

    private fun handle(context: Context, intent: Intent, type: String) {
        val repository = InjectionRepository.getInstance(context)

        when (type) {
            TYPE_REMINDER -> {
                val settings = repository.getNotificationSettings()
                if (!settings.injectionEnabled) return

                val minutesBefore = intent.getIntExtra(EXTRA_MINUTES_BEFORE, 0)
                val isAllowed = when (minutesBefore) {
                    300 -> settings.injectionReminder5h
                    180 -> settings.injectionReminder3h
                    30 -> settings.injectionReminder30min
                    0 -> settings.injectionReminderAtTime
                    else -> true
                }
                if (!isAllowed) return

                val substanceName = intent.getStringExtra(EXTRA_SUBSTANCE_NAME) ?: "Injekcija"
                showReminderNotification(context, minutesBefore, substanceName)
            }
            TYPE_OVERDUE_CHECK -> {
                val settings = repository.getNotificationSettings()
                if (!settings.injectionEnabled || !settings.injectionOverdueEnabled) {
                    dismissOverdueNotification(context)
                    return
                }

                val mostUrgent = repository.getMostUrgentInjection()
                // AWAITING_FIRST ima prednost nad OVERDUE u modelu, pa propuštenu
                // PRVU injekciju takođe tretiramo kao overdue (hoursRemaining < 0).
                val isOverdue = mostUrgent != null && (
                    mostUrgent.status == com.injekcije.app.data.InjectionStatus.OVERDUE ||
                        (mostUrgent.status == com.injekcije.app.data.InjectionStatus.AWAITING_FIRST &&
                            mostUrgent.hoursRemaining < 0)
                    )
                if (mostUrgent != null && isOverdue) {
                    showOverdueNotification(context, mostUrgent.substanceName, mostUrgent.scheduledDateTime.toString())
                } else {
                    dismissOverdueNotification(context)
                    scheduleInjectionNotifications(context)
                }
            }
            TYPE_ORAL_DOSE -> {
                val substanceName = intent.getStringExtra(EXTRA_SUBSTANCE_NAME) ?: "Tableta"
                val substanceId = intent.getStringExtra(EXTRA_SUBSTANCE_ID)
                val doseNumber = intent.getIntExtra(EXTRA_DOSE_NUMBER, 1)
                val totalDoses = intent.getIntExtra(EXTRA_TOTAL_DOSES, 1)
                val doseMg = intent.getDoubleExtra(EXTRA_DOSE_MG, 0.0)
                val minutesBefore = intent.getIntExtra(EXTRA_MINUTES_BEFORE, 0)
                val scheduledTime = intent.getStringExtra(EXTRA_SCHEDULED_TIME)
                    ?.let { runCatching { LocalDateTime.parse(it) }.getOrNull() }

                val settings = repository.getNotificationSettings()
                if (!settings.oralEnabled) return
                val isAllowed = when (minutesBefore) {
                    30 -> settings.oralReminder30min
                    0 -> settings.oralReminderAtTime
                    else -> true
                }
                if (!isAllowed) return

                // Guard: supstanca mora biti aktivna komponenta aktivnog ciklusa
                // (ciklus je možda obrisan/izmenjen posle zakazivanja alarma).
                if (substanceId != null) {
                    val cycle = repository.getActiveCycle()
                    val currentWeek = cycle?.currentWeek()
                    val isActiveCompound = cycle != null && currentWeek != null &&
                        cycle.compounds.any {
                            it.substanceId == substanceId &&
                                it.isActiveInWeek(currentWeek, cycle.durationWeeks)
                        }
                    if (!isActiveCompound) {
                        // Ne prikazuj i ne zakazuj ponovo
                        return
                    }
                }

                val shouldNotify = if (substanceId != null && scheduledTime != null) {
                    val lastDose = repository.getLastOralDoseForSubstance(substanceId)
                    val cutoff = scheduledTime.minusMinutes(ORAL_DOSE_LOG_GRACE_MINUTES.toLong())
                    val alreadyTaken = lastDose != null && !lastDose.dateTime.isBefore(cutoff)
                    !alreadyTaken
                } else {
                    true
                }

                if (shouldNotify) {
                    showOralDoseNotification(context, substanceName, doseNumber, totalDoses, doseMg, minutesBefore)
                    if (minutesBefore == 0 && substanceId != null && scheduledTime != null && settings.oralOverdueEnabled) {
                        scheduleOralOverdueCheck(
                            context,
                            LocalDateTime.now().plusMinutes(settings.oralOverdueIntervalMinutes.toLong()),
                            substanceId,
                            substanceName,
                            scheduledTime,
                            doseNumber,
                            totalDoses,
                            doseMg
                        )
                    }
                }

                if (minutesBefore == 0) {
                    scheduleOralSteroidNotifications(context, suppressImmediate = true)
                }
            }
            TYPE_ORAL_OVERDUE_CHECK -> {
                val substanceId = intent.getStringExtra(EXTRA_SUBSTANCE_ID) ?: return
                val substanceName = intent.getStringExtra(EXTRA_SUBSTANCE_NAME) ?: "Tableta"
                val doseNumber = intent.getIntExtra(EXTRA_DOSE_NUMBER, 1)
                val totalDoses = intent.getIntExtra(EXTRA_TOTAL_DOSES, 1)
                val doseMg = intent.getDoubleExtra(EXTRA_DOSE_MG, 0.0)
                val scheduledTime = intent.getStringExtra(EXTRA_SCHEDULED_TIME)
                    ?.let { runCatching { LocalDateTime.parse(it) }.getOrNull() }
                    ?: return

                val settings = repository.getNotificationSettings()
                if (!settings.oralEnabled || !settings.oralOverdueEnabled) {
                    return
                }

                val cycle = repository.getActiveCycle()
                val currentWeek = cycle?.currentWeek()
                val isActiveCompound = if (cycle != null && currentWeek != null) {
                    cycle.compounds.any {
                        it.substanceId == substanceId &&
                            it.isActiveInWeek(currentWeek, cycle.durationWeeks)
                    }
                } else {
                    false
                }
                if (!isActiveCompound || !com.injekcije.app.data.OralDosingSchedule.isOralSteroid(substanceId)) {
                    return
                }

                val lastDose = repository.getLastOralDoseForSubstance(substanceId)

                val schedule = com.injekcije.app.data.OralDosingSchedule.forSubstance(substanceId)
                if (schedule != null && lastDose != null) {
                    val intervalMinutes = (24 * 60) / schedule.dosesPerDay
                    val nextDoseTime = lastDose.dateTime.plusMinutes(intervalMinutes.toLong())
                    if (nextDoseTime.isAfter(LocalDateTime.now())) {
                        return
                    }
                }

                val cutoff = scheduledTime.minusMinutes(ORAL_DOSE_LOG_GRACE_MINUTES.toLong())
                val alreadyTaken = lastDose != null && !lastDose.dateTime.isBefore(cutoff)
                if (!alreadyTaken) {
                    showOralDoseNotification(context, substanceName, doseNumber, totalDoses, doseMg, 0)
                    scheduleOralOverdueCheck(
                        context,
                        LocalDateTime.now().plusMinutes(settings.oralOverdueIntervalMinutes.toLong()),
                        substanceId,
                        substanceName,
                        scheduledTime,
                        doseNumber,
                        totalDoses,
                        doseMg
                    )
                }
            }
            TYPE_PCT_START -> {
                val settings = repository.getNotificationSettings()
                if (!settings.pctEnabled) return

                val isAdvance = intent.getBooleanExtra(EXTRA_PCT_ADVANCE, false)
                if (isAdvance && !settings.pctAdvanceWarning) return
                if (!isAdvance && !settings.pctStartNotification) return

                val pctInfo = repository.getActivePCTGuideInfo()
                // Ne prikazuj ako je korisnik u međuvremenu već započeo PCT
                if (pctInfo != null && pctInfo.pctData.pctStartDate == null) {
                    showPCTStartNotification(
                        context,
                        pctInfo.pctData.protocol.displayName,
                        pctInfo.daysUntilStart
                    )
                }
            }
            TYPE_PCT_BLOODWORK -> {
                val settings = repository.getNotificationSettings()
                if (!settings.pctEnabled || !settings.pctBloodworkReminder) return
                showPCTBloodworkNotification(context)
            }
        }
    }
}
