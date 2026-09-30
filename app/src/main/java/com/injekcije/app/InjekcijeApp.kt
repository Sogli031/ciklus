package com.injekcije.app

import android.app.Application
import android.util.Log
import com.injekcije.app.data.InjectionRepository
import com.injekcije.app.data.backup.BackupScheduler
import com.injekcije.app.data.backup.GoogleDriveBackup
import com.injekcije.app.data.backup.GoogleDriveBackupProvider
import com.injekcije.app.notifications.cancelAllAlarms
import com.injekcije.app.notifications.scheduleInjectionNotifications
import com.injekcije.app.notifications.scheduleOralSteroidNotifications
import com.injekcije.app.notifications.scheduleSafetyNetWork
import com.injekcije.app.notifications.schedulePCTNotifications
import com.programi.crashlog.CrashLogStorage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import java.io.File

class InjekcijeApp : Application() {

    lateinit var repository: InjectionRepository
        private set

    lateinit var localeManager: LocaleManager
        private set

    lateinit var googleDriveBackup: GoogleDriveBackup
        private set

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    /** Korutine koje moraju da prežive zatvaranje ekrana (backup / vraćanje). */
    val appScope: CoroutineScope get() = applicationScope

    @Volatile
    var isInitialized = false
        private set

    override fun onCreate() {
        super.onCreate()
        CrashLogStorage.install(this, getString(R.string.app_name), ::redactCrashLog)
    }

    /**
     * Inicijalizacija repozitorijuma i servisa.
     * Poziva se iz MainActivity NAKON provere crash loga.
     */
    fun initialize() {
        if (isInitialized) return

        repository = InjectionRepository()
        localeManager = LocaleManager(this)
        googleDriveBackup = GoogleDriveBackupProvider.get(this)

        // Učitaj iz lokalne memorije SINHRONO, PRE registrovanja auto-save callback-a.
        // U suprotnom, korisnička akcija pre završetka učitavanja bi pregazila
        // fajl na disku skoro praznim podacima. Fajl je mali - sinhrono učitavanje
        // na startu je prihvatljivo.
        repository.loadFromLocal(this)

        // Auto-save lokalno - debounce i serijalizovan atomski upis su u Repository-ju
        repository.onChange {
            repository.scheduleSave(this)
            // Svaka izmena zakazuje i backup u oblak; REPLACE politika spoji niz
            // brzih izmena u jedan posao.
            BackupScheduler.scheduleAfterChange(this)
        }

        // isInitialized se postavlja POSLEDNJE, tek kada su repository i podaci spremni,
        // da BroadcastReceiver-i preko getInstance() ne bi dobili prazan repozitorijum.
        isInitialized = true

        // Dnevni backup ako korisnik dugo ne menja ništa (KEEP: ne resetuje se na svakom startu)
        BackupScheduler.schedulePeriodic(this)

        // Zakazivanje notifikacija u pozadini (podaci su već učitani)
        applicationScope.launch {
            try {
                scheduleInjectionNotifications(this@InjekcijeApp)
                scheduleOralSteroidNotifications(this@InjekcijeApp)
                schedulePCTNotifications(this@InjekcijeApp, repository)
                scheduleSafetyNetWork(this@InjekcijeApp)
            } catch (e: Exception) {
                Log.e("InjekcijeApp", "Greška pri inicijalnom zakazivanju notifikacija", e)
            }
        }
    }

    /**
     * Posle vraćanja backup-a: vraćeno stanje se ODMAH upisuje na disk (bez čekanja na
     * debounce auto-save), a svi alarmi se otkazuju i zakazuju ponovo prema vraćenim podacima.
     * Radi u korutini aplikacije, pa zatvaranje ekrana za backup ne prekida posao.
     * Rezultat je true ako je upis na disk uspeo.
     */
    fun persistAndRescheduleAfterRestore(): Deferred<Boolean> = applicationScope.async {
        val saved = repository.saveToLocal(this@InjekcijeApp)
        try {
            cancelAllAlarms(this@InjekcijeApp)
            scheduleInjectionNotifications(this@InjekcijeApp)
            scheduleOralSteroidNotifications(this@InjekcijeApp)
            schedulePCTNotifications(this@InjekcijeApp, repository)
        } catch (e: Exception) {
            Log.e("InjekcijeApp", "Greška pri zakazivanju notifikacija posle vraćanja", e)
        }
        saved
    }

    /**
     * Redaktuje osetljive podatke iz crash loga.
     * Zamenjuje datume, ID-jeve ciklusa, dozaže i email adrese sa [REDACTED].
     */
    private fun redactCrashLog(crashText: String): String {
        var redacted = crashText
        // Redaktuj datume (formati: 2024-01-15, 15.01.2024, 15/01/2024)
        redacted = redacted.replace(Regex("""\d{4}-\d{2}-\d{2}"""), "[REDACTED_DATE]")
        redacted = redacted.replace(Regex("""\d{2}[./]\d{2}[./]\d{4}"""), "[REDACTED_DATE]")
        // Redaktuj UUID-ove (ID-jevi ciklusa)
        redacted = redacted.replace(
            Regex("""[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}""", RegexOption.IGNORE_CASE),
            "[REDACTED_ID]"
        )
        // Redaktuj dozaže (brojevi praćeni mg, ml, IU)
        redacted = redacted.replace(Regex("""\d+\.?\d*\s*(mg|ml|IU|iu)""", RegexOption.IGNORE_CASE), "[REDACTED_DOSE]")
        // Redaktuj email adrese (podržava više formata uključujući subdomene i duže TLD-ove)
        redacted = redacted.replace(Regex("""[a-zA-Z0-9._%+\-]+@[a-zA-Z0-9.\-]+\.[a-zA-Z]{2,}"""), "[REDACTED_EMAIL]")
        // Redaktuj Unix timestamp-ove (10+ cifara - epoch milisekunde ili sekunde)
        redacted = redacted.replace(Regex("""\b\d{10,13}\b"""), "[REDACTED_TIMESTAMP]")
        // Redaktuj IPv4 adrese
        redacted = redacted.replace(Regex("""\b\d{1,3}\.\d{1,3}\.\d{1,3}\.\d{1,3}\b"""), "[REDACTED_IP]")
        // Redaktuj IPv6 adrese (skraćeni i puni format)
        redacted = redacted.replace(Regex("""(?i)\b[0-9a-f]{1,4}(:[0-9a-f]{1,4}){7}\b"""), "[REDACTED_IP]")
        redacted = redacted.replace(Regex("""(?i)\b[0-9a-f]{1,4}(:[0-9a-f]{1,4})*::[0-9a-f]{0,4}(:[0-9a-f]{1,4})*\b"""), "[REDACTED_IP]")
        // Redaktuj telefonske brojeve (međunarodni i lokalni formati)
        redacted = redacted.replace(Regex("""\+?\d[\d\s\-().]{7,}\d"""), "[REDACTED_PHONE]")
        return redacted
    }

    /**
     * Briše crash log fajl nakon uspešnog prikazivanja.
     */
    fun deleteCrashLog() {
        try {
            val crashFile = File(filesDir, "crash_log.txt")
            if (crashFile.exists()) {
                crashFile.delete()
            }
        } catch (_: Exception) {
            // Ignorišemo greške pri brisanju
        }
    }
}
