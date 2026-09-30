package com.injekcije.app.data.backup

import android.content.Context
import android.content.Intent
import android.util.Log
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.Scope
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.googleapis.json.GoogleJsonResponseException
import com.google.api.client.http.ByteArrayContent
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.drive.Drive
import com.google.api.services.drive.DriveScopes
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.IOException

private const val TAG = "GoogleDriveBackup"
private const val BACKUP_FILE_NAME = "ciklus_backup.json"
/** Prethodne kopije: ciklus_backup_prev_<epochMillis>.json */
private const val PREV_PREFIX = "ciklus_backup_prev_"
private const val PREV_SUFFIX = ".json"
private const val PREV_COPIES_TO_KEEP = 5
private const val APP_DATA_FOLDER = "appDataFolder"
private const val PREFS_NAME = "backup_prefs"
private const val KEY_FIRST_LAUNCH_DONE = "first_launch_done"
private const val KEY_LAST_SYNC_TIME = "last_sync_time"
private const val KEY_CLOUD_ADOPTED = "cloud_adopted"

/** Ishod provere oblaka. Greška NIKAD ne znači "oblak je prazan". */
sealed class CloudCheck {
    data object Exists : CloudCheck()
    data object Empty : CloudCheck()
    data class Failed(val message: String) : CloudCheck()
}

/** Ishod preuzimanja backup-a: razlikuje "nema backup-a" od neuspelog preuzimanja. */
sealed class RestoreOutcome {
    data class Success(val json: String) : RestoreOutcome()
    data object NotFound : RestoreOutcome()
    data class Error(val message: String) : RestoreOutcome()
}

class GoogleDriveBackup(private val context: Context) {

    private val _signInState = MutableStateFlow<SignInState>(SignInState.SignedOut)
    val signInState: StateFlow<SignInState> = _signInState

    private val _syncState = MutableStateFlow<SyncState>(SyncState.Idle)
    val syncState: StateFlow<SyncState> = _syncState

    private val _lastSyncTime = MutableStateFlow<Long?>(null)
    val lastSyncTime: StateFlow<Long?> = _lastSyncTime

    /** Posle prijave: u oblaku postoji backup koji ova instalacija još nije preuzela. */
    private val _restoreOffer = MutableStateFlow(false)
    val restoreOffer: StateFlow<Boolean> = _restoreOffer

    /** Korisnik je prihvatio ponudu - ekran za backup treba odmah da pokrene vraćanje. */
    private val _autoRestoreRequest = MutableStateFlow(false)
    val autoRestoreRequest: StateFlow<Boolean> = _autoRestoreRequest

    @Volatile
    private var driveService: Drive? = null

    // Provera oblaka posle prijave ne sme da zavisi od otvorenog ekrana
    private val backgroundScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    // Mutex za token refresh - sprečava istovremene silentSignIn() pozive
    private val tokenRefreshMutex = Mutex()
    // Mutex za Drive operacije - sprečava istovremeni backup i restore
    private val driveOperationMutex = Mutex()

    private val prefs by lazy { context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE) }

    private val googleSignInClient: GoogleSignInClient by lazy {
        val signInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestScopes(Scope(DriveScopes.DRIVE_APPDATA))
            .build()
        GoogleSignIn.getClient(context, signInOptions)
    }

    init {
        val savedTime = prefs.getLong(KEY_LAST_SYNC_TIME, 0L)
        if (savedTime > 0) {
            _lastSyncTime.value = savedTime
        }

        val account = GoogleSignIn.getLastSignedInAccount(context)
        if (account != null && GoogleSignIn.hasPermissions(account, Scope(DriveScopes.DRIVE_APPDATA))) {
            _signInState.value = SignInState.SignedIn(account.email ?: "Unknown")
            initDriveService(account)
        }
    }

    fun getSignInIntent(): Intent = googleSignInClient.signInIntent

    fun handleSignInResult(account: GoogleSignInAccount?) {
        if (account != null) {
            _signInState.value = SignInState.SignedIn(account.email ?: "Unknown")
            initDriveService(account)
            offerRestoreIfCloudHasBackup()
        } else {
            _signInState.value = SignInState.SignedOut
        }
    }

    suspend fun signOut() {
        withContext(Dispatchers.IO) {
            try {
                googleSignInClient.signOut()
                _signInState.value = SignInState.SignedOut
                _restoreOffer.value = false
                driveService = null
            } catch (e: Exception) {
                Log.e(TAG, "Sign out failed", e)
            }
        }
    }

    private fun initDriveService(account: GoogleSignInAccount) {
        val credential = GoogleAccountCredential.usingOAuth2(
            context,
            listOf(DriveScopes.DRIVE_APPDATA)
        )
        credential.selectedAccount = account.account

        driveService = Drive.Builder(
            NetHttpTransport(),
            GsonFactory.getDefaultInstance(),
            credential
        )
            .setApplicationName("Ciklus")
            .build()
    }

    // =========================================================================
    // "OBLAK USVOJEN"
    // =========================================================================

    /**
     * Da li je OVA instalacija već usklađena sa oblakom (vratila backup, napravila prvi
     * backup u prazan oblak, ili je korisnik izričito potvrdio). Dok nije, backup preko
     * postojećeg backup-a traži potvrdu - sveža instalacija ne sme tiho da ga pregazi.
     */
    fun isCloudAdopted(): Boolean {
        if (!prefs.contains(KEY_CLOUD_ADOPTED)) {
            // Migracija postojećih instalacija: usvojeno ako je ikad uspešno sinhronizovano
            val adopted = prefs.getLong(KEY_LAST_SYNC_TIME, 0L) > 0L
            prefs.edit().putBoolean(KEY_CLOUD_ADOPTED, adopted).apply()
            return adopted
        }
        return prefs.getBoolean(KEY_CLOUD_ADOPTED, false)
    }

    /** Izričita potvrda korisnika ili uspešno vraćanje iz oblaka. */
    fun markCloudAdopted() {
        prefs.edit().putBoolean(KEY_CLOUD_ADOPTED, true).apply()
        _restoreOffer.value = false
    }

    fun dismissRestoreOffer() {
        _restoreOffer.value = false
    }

    /** Prihvaćena ponuda vraćanja: ekran za backup je preuzima tačno jednom. */
    fun requestAutoRestore() {
        _restoreOffer.value = false
        _autoRestoreRequest.value = true
    }

    fun consumeAutoRestoreRequest(): Boolean = _autoRestoreRequest.compareAndSet(expect = true, update = false)

    private fun offerRestoreIfCloudHasBackup() {
        if (isCloudAdopted()) return
        backgroundScope.launch {
            when (val check = checkCloud()) {
                CloudCheck.Exists -> _restoreOffer.value = true
                CloudCheck.Empty -> {}
                // Neuspela provera ne skida zaštitu: ista provera se ponavlja pre svakog backup-a
                is CloudCheck.Failed -> Log.w(TAG, "Provera oblaka posle prijave nije uspela: ${check.message}")
            }
        }
    }

    // =========================================================================
    // DRIVE OPERACIJE
    // =========================================================================

    /**
     * Validira strukturu JSON podataka pre importa.
     * Proverava da JSON ima ispravan format i da obavezna polja postoje.
     * Vraća null ako je validan, ili opis greške.
     */
    private fun validateBackupJson(jsonData: String): String? {
        return try {
            val json = org.json.JSONObject(jsonData)

            // Proveri da je JSON objekat (ne prazan string ili niz)
            if (json.length() == 0) return "Prazan JSON objekat"

            // Proveri da ključna polja, ako postoje, imaju ispravan tip (JSONArray)
            val arrayKeys = listOf("cycles", "injections", "oral_doses", "pct_data",
                "substances", "enabled_sites")
            for (key in arrayKeys) {
                if (json.has(key) && json.optJSONArray(key) == null) {
                    return "Polje '$key' mora biti niz"
                }
            }

            // Proveri notification_settings ako postoji - mora biti objekat
            if (json.has("notification_settings") && json.optJSONObject("notification_settings") == null) {
                return "Polje 'notification_settings' mora biti objekat"
            }

            null // Validacija uspešna
        } catch (e: org.json.JSONException) {
            "Neispravan JSON format: ${e.message}"
        }
    }

    /**
     * Izvršava Drive poziv; na 401/403 jednom osvežava token i ponavlja.
     * CancellationException se ne hvata - otkazivanje mora da se propagira.
     */
    private suspend fun <T> withDrive(block: (Drive) -> T): T = withContext(Dispatchers.IO) {
        val drive = driveService ?: throw IOException("Nisi prijavljen na Google nalog")
        try {
            block(drive)
        } catch (e: GoogleJsonResponseException) {
            if (e.statusCode == 401 || e.statusCode == 403) {
                Log.w(TAG, "Token istekao (${e.statusCode}), pokušavam refresh...")
                val refreshed = (if (refreshToken()) driveService else null)
                    ?: throw IOException("Autentifikacija neuspešna - prijavite se ponovo")
                block(refreshed)
            } else {
                throw e
            }
        }
    }

    /** Da li u oblaku postoji backup. Mrežna/auth greška je [CloudCheck.Failed], nikad "prazno". */
    suspend fun checkCloud(): CloudCheck {
        return try {
            val exists = driveOperationMutex.withLock { withDrive { findBackupFile(it) != null } }
            if (exists) CloudCheck.Exists else CloudCheck.Empty
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Provera oblaka nije uspela", e)
            CloudCheck.Failed(e.message ?: e.javaClass.simpleName)
        }
    }

    /**
     * Otprema backup. Pravilo "prazno ne gazi popunjeno" i potvrdu korisnika rešava
     * pozivalac (ekran) PRE ovog poziva - ovde više nema `force` prečice.
     */
    suspend fun backup(jsonData: String): Boolean {
        if (driveService == null) {
            _syncState.value = SyncState.Error("Nisi prijavljen na Google nalog")
            return false
        }
        return driveOperationMutex.withLock {
            try {
                _syncState.value = SyncState.Syncing
                withDrive { executeDriveBackup(it, jsonData) }

                val now = System.currentTimeMillis()
                _lastSyncTime.value = now
                prefs.edit()
                    .putLong(KEY_LAST_SYNC_TIME, now)
                    .putBoolean(KEY_CLOUD_ADOPTED, true)
                    .apply()
                _restoreOffer.value = false
                _syncState.value = SyncState.Success
                true
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "Backup neuspešan", e)
                _syncState.value = SyncState.Error(e.message ?: "Backup neuspešan")
                false
            }
        }
    }

    /**
     * Ako glavni fajl postoji, PRVO se pravi njegova prethodna kopija; ako to ne uspe,
     * glavni fajl se NE prepisuje (izuzetak prekida backup).
     */
    private fun executeDriveBackup(drive: Drive, jsonData: String) {
        val existingFileId = findBackupFile(drive)
        val contentStream = ByteArrayContent.fromString("application/json", jsonData)

        if (existingFileId != null) {
            try {
                savePreviousCopy(drive, existingFileId)
            } catch (e: IOException) {
                throw IOException(
                    "Prethodna kopija backup-a nije mogla da se sačuva, pa postojeći backup NIJE prepisan (${e.message})",
                    e
                )
            }
            drive.files().update(existingFileId, null, contentStream).execute()
            prunePreviousCopies(drive)
        } else {
            val fileMetadata = com.google.api.services.drive.model.File().apply {
                name = BACKUP_FILE_NAME
                parents = listOf(APP_DATA_FOLDER)
            }
            drive.files().create(fileMetadata, contentStream).execute()
        }
    }

    private fun savePreviousCopy(drive: Drive, fileId: String) {
        val metadata = com.google.api.services.drive.model.File().apply {
            name = "$PREV_PREFIX${System.currentTimeMillis()}$PREV_SUFFIX"
            parents = listOf(APP_DATA_FOLDER)
        }
        try {
            drive.files().copy(fileId, metadata).setFields("id").execute()
        } catch (e: GoogleJsonResponseException) {
            if (e.statusCode == 401 || e.statusCode == 403) throw e
            // Ako Drive odbije copy u appDataFolder-u, kopija se pravi ručno: preuzmi pa kreiraj
            Log.w(TAG, "files.copy odbijen (${e.statusCode}), pravim kopiju ručno", e)
            val previous = downloadContent(drive, fileId)
            drive.files()
                .create(metadata, ByteArrayContent.fromString("application/json", previous))
                .setFields("id")
                .execute()
        }
    }

    private fun prevTimestamp(name: String?): Long? =
        name?.takeIf { it.startsWith(PREV_PREFIX) && it.endsWith(PREV_SUFFIX) }
            ?.removePrefix(PREV_PREFIX)?.removeSuffix(PREV_SUFFIX)?.toLongOrNull()

    /** Čuva najnovijih [PREV_COPIES_TO_KEEP] prethodnih kopija. Neuspelo brisanje nije greška backup-a. */
    private fun prunePreviousCopies(drive: Drive) {
        try {
            val result = drive.files().list()
                .setSpaces(APP_DATA_FOLDER)
                .setFields("files(id, name)")
                .setPageSize(1000)
                .execute()
            result.files.orEmpty()
                .mapNotNull { file -> prevTimestamp(file.name)?.let { it to file } }
                .sortedByDescending { it.first }
                .drop(PREV_COPIES_TO_KEEP)
                .forEach { (_, file) ->
                    try {
                        drive.files().delete(file.id).execute()
                        Log.d(TAG, "Obrisana stara prethodna kopija: ${file.name}")
                    } catch (e: IOException) {
                        Log.w(TAG, "Neuspešno brisanje stare kopije: ${file.name}", e)
                    }
                }
        } catch (e: IOException) {
            Log.w(TAG, "Čišćenje starih kopija nije uspelo", e)
        }
    }

    /**
     * Pokušava da osveži Google token tihim ponovnim prijavljivanjem.
     * Koristi Mutex tako da istovremeni pozivi (backup + restore) ne rade dupli silentSignIn().
     * Vraća true ako je uspešno.
     */
    private suspend fun refreshToken(): Boolean {
        return tokenRefreshMutex.withLock {
            try {
                val account = GoogleSignIn.getLastSignedInAccount(context)
                if (account != null) {
                    googleSignInClient.silentSignIn().let { task ->
                        com.google.android.gms.tasks.Tasks.await(task, 15, java.util.concurrent.TimeUnit.SECONDS)
                        val refreshedAccount = task.result
                        if (refreshedAccount != null) {
                            initDriveService(refreshedAccount)
                            Log.d(TAG, "Token uspešno osvežen")
                            true
                        } else {
                            Log.w(TAG, "Token refresh vratio null nalog")
                            false
                        }
                    }
                } else {
                    Log.w(TAG, "Nema prethodnog naloga za token refresh")
                    false
                }
            } catch (e: java.util.concurrent.TimeoutException) {
                Log.e(TAG, "Token refresh istekao (timeout 15s)", e)
                false
            } catch (e: Exception) {
                Log.e(TAG, "Token refresh neuspešan", e)
                false
            }
        }
    }

    /** Preuzima backup. Na ovom putu se u oblaku NIŠTA ne briše i ne menja. */
    suspend fun restore(): RestoreOutcome {
        if (driveService == null) return RestoreOutcome.Error("Nisi prijavljen na Google nalog")

        return driveOperationMutex.withLock {
            try {
                _syncState.value = SyncState.Syncing
                val data = withDrive { drive ->
                    findBackupFile(drive)?.let { downloadContent(drive, it) }
                }
                if (data == null) {
                    _syncState.value = SyncState.Idle
                    return@withLock RestoreOutcome.NotFound
                }

                // Validiraj JSON pre vraćanja - ne primenjuj oštećene podatke
                val validationError = validateBackupJson(data)
                if (validationError != null) {
                    Log.e(TAG, "Restore validacija neuspešna: $validationError")
                    _syncState.value = SyncState.Error("Oštećen backup: $validationError")
                    return@withLock RestoreOutcome.Error("Oštećen backup: $validationError")
                }

                _syncState.value = SyncState.Idle
                RestoreOutcome.Success(data)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "Restore neuspešan", e)
                val message = e.message ?: "Restore neuspešan"
                _syncState.value = SyncState.Error(message)
                RestoreOutcome.Error(message)
            }
        }
    }

    /** Backup je stvarno primenjen na ovom telefonu - instalacija je usklađena sa oblakom. */
    fun recordSuccessfulRestore() {
        val now = System.currentTimeMillis()
        _lastSyncTime.value = now
        prefs.edit()
            .putLong(KEY_LAST_SYNC_TIME, now)
            .putBoolean(KEY_CLOUD_ADOPTED, true)
            .apply()
        _restoreOffer.value = false
        _syncState.value = SyncState.Success
    }

    private fun downloadContent(drive: Drive, fileId: String): String {
        val outputStream = ByteArrayOutputStream()
        drive.files().get(fileId).executeMediaAndDownloadTo(outputStream)
        return outputStream.toString("UTF-8")
    }

    /** Traži glavni backup fajl po TAČNOM imenu. Ništa ne briše (ni duplikate). */
    private fun findBackupFile(drive: Drive): String? {
        val result = drive.files().list()
            .setSpaces(APP_DATA_FOLDER)
            .setQ("name = '$BACKUP_FILE_NAME' and trashed = false")
            .setOrderBy("modifiedTime desc")
            .setFields("files(id, name, modifiedTime)")
            .execute()

        return result.files.orEmpty().firstOrNull { it.name == BACKUP_FILE_NAME }?.id
    }

    fun isSignedIn(): Boolean = _signInState.value is SignInState.SignedIn

    fun isFirstLaunch(): Boolean {
        return !prefs.getBoolean(KEY_FIRST_LAUNCH_DONE, false)
    }

    fun markFirstLaunchDone() {
        prefs.edit().putBoolean(KEY_FIRST_LAUNCH_DONE, true).apply()
    }

    fun resetSyncState() {
        _syncState.value = SyncState.Idle
    }
}

sealed class SignInState {
    data object SignedOut : SignInState()
    data class SignedIn(val email: String) : SignInState()
}

sealed class SyncState {
    data object Idle : SyncState()
    data object Syncing : SyncState()
    data object Success : SyncState()
    data class Error(val message: String) : SyncState()
}
