package com.injekcije.app.data.backup

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.injekcije.app.InjekcijeApp

/**
 * Pozadinski posao koji šalje backup na Google Drive. Pokreće ga [BackupScheduler]
 * (posle izmene + dnevno), uz uslov da ima mreže.
 *
 * Primenjuje ista pravila kao ručni backup sa ekrana, samo bez pitanja korisniku:
 * ako bi backup pregazio nešto u oblaku što ova instalacija nije preuzela, ili ako
 * lokalno nema korisničkih podataka, posao se tiho preskače.
 */
class BackupWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val app = applicationContext as? InjekcijeApp ?: return Result.success()

        // U pozadinskom procesu MainActivity možda nije pokrenut; initialize() je
        // idempotentan i sinhrono učita lokalno stanje.
        app.initialize()
        val repo = app.repository
        val backup = app.googleDriveBackup

        if (!backup.isSignedIn()) {
            Log.d(TAG, "Preskačem auto-backup: korisnik nije prijavljen")
            return Result.success()
        }

        // "Prazno ne gazi popunjeno": bez korisničkih unosa nema šta da se sačuva,
        // a u oblaku bi samo zaklonilo poslednji dobar backup.
        if (!repo.hasUserData()) {
            Log.d(TAG, "Preskačem auto-backup: nema korisničkih podataka")
            return Result.success()
        }

        when (val check = backup.checkCloud()) {
            // Neuspela provera nikad ne znači „oblak je prazan" — bolje pokušati kasnije.
            is CloudCheck.Failed -> {
                Log.w(TAG, "Provera oblaka nije uspela: ${check.message}")
                return Result.retry()
            }
            CloudCheck.Empty -> Unit
            CloudCheck.Exists -> if (!backup.isCloudAdopted()) {
                // Sveža instalacija ne sme tiho da pregazi zatečeni backup —
                // prvo korisnik na ekranu bira Vrati ili „ipak napravi backup".
                Log.d(TAG, "Preskačem auto-backup: oblak nije preuzet na ovoj instalaciji")
                return Result.success()
            }
        }

        val success = backup.backup(repo.exportJson())
        return if (success) {
            Log.d(TAG, "Auto-backup uspešan")
            Result.success()
        } else {
            val reason = (backup.syncState.value as? SyncState.Error)?.message
            Log.w(TAG, "Auto-backup neuspešan: $reason")
            if (runAttemptCount >= MAX_RETRY_ATTEMPTS) Result.failure() else Result.retry()
        }
    }

    companion object {
        private const val TAG = "BackupWorker"
        private const val MAX_RETRY_ATTEMPTS = 7
    }
}
