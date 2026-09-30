package com.injekcije.app.notifications

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.injekcije.app.data.InjectionRepository

/**
 * Periodični safety-net worker: ponovo pokreće sve schedulere notifikacija
 * (injekcije, oralni steroidi, PCT) za slučaj da su alarmi izgubljeni
 * (force-stop aplikacije, battery optimizacija, čišćenje alarma od strane sistema).
 *
 * Zakazuje se preko [scheduleSafetyNetWork] kao unique periodic work.
 */
class RescheduleWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        return try {
            val repository = InjectionRepository.getInstance(applicationContext)
            scheduleInjectionNotifications(applicationContext)
            scheduleOralSteroidNotifications(applicationContext, suppressImmediate = true)
            schedulePCTNotifications(applicationContext, repository)
            Result.success()
        } catch (e: Exception) {
            Log.e("RescheduleWorker", "Greška pri ponovnom zakazivanju notifikacija: ${e.message}")
            Result.retry()
        }
    }
}
