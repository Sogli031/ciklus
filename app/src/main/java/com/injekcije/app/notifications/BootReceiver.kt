package com.injekcije.app.notifications

import android.app.AlarmManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.injekcije.app.data.InjectionRepository

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            Intent.ACTION_BOOT_COMPLETED,
            Intent.ACTION_MY_PACKAGE_REPLACED,
            Intent.ACTION_TIME_CHANGED,
            Intent.ACTION_TIMEZONE_CHANGED,
            // Korisnik je naknadno odobrio "Alarms & reminders" - alarme zakazane
            // preko inexact fallback-a treba ponovo zakazati kao tačne (API 31+)
            AlarmManager.ACTION_SCHEDULE_EXACT_ALARM_PERMISSION_STATE_CHANGED -> {
                Log.d("BootReceiver", "Ponovno zakazivanje alarma (${intent.action})...")

                val appContext = context.applicationContext

                // Učitavanje JSON podataka + zakazivanje ne sme da blokira main thread
                val pendingResult = goAsync()
                receiverExecutor.execute {
                    try {
                        rescheduleAll(appContext)
                    } catch (e: Exception) {
                        Log.e("BootReceiver", "Greška pri ponovnom zakazivanju alarma: ${e.message}")
                    } finally {
                        pendingResult.finish()
                    }
                }
            }
            else -> return
        }
    }

    private fun rescheduleAll(context: Context) {
        // Učitaj podatke sa diska pre zakazivanja
        val repository = InjectionRepository.getInstance(context)

        val activeCycle = repository.getActiveCycle()
        if (activeCycle == null) {
            Log.d("BootReceiver", "Nema aktivnog ciklusa")
        } else {
            Log.d("BootReceiver", "Aktivan ciklus pronađen: ${activeCycle.name}, zakazujem sve alarme")
        }

        // Ponovo zakaži notifikacije nakon restarta uređaja ili ažuriranja aplikacije
        try {
            scheduleInjectionNotifications(context)
            Log.d("BootReceiver", "Alarmi za injekcije zakazani")
        } catch (e: Exception) {
            Log.e("BootReceiver", "Greška pri zakazivanju alarma za injekcije: ${e.message}")
        }

        try {
            scheduleOralSteroidNotifications(context, suppressImmediate = true)
            Log.d("BootReceiver", "Alarmi za oralne steroide zakazani")
        } catch (e: Exception) {
            Log.e("BootReceiver", "Greška pri zakazivanju alarma za oralne steroide: ${e.message}")
        }

        // Zakaži PCT notifikacije
        try {
            schedulePCTNotifications(context, repository)
            Log.d("BootReceiver", "PCT alarmi zakazani")
        } catch (e: Exception) {
            Log.e("BootReceiver", "Greška pri zakazivanju PCT alarma: ${e.message}")
        }

        // Pokreni periodični WorkManager safety-net
        try {
            scheduleSafetyNetWork(context)
            Log.d("BootReceiver", "Safety-net WorkManager pokrenut")
        } catch (e: Exception) {
            Log.e("BootReceiver", "Greška pri pokretanju WorkManager-a: ${e.message}")
        }

        Log.d("BootReceiver", "Sve notifikacije uspešno ponovo zakazane")
    }
}
