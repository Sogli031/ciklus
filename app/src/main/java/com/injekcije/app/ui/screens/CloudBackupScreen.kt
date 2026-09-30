package com.injekcije.app.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.injekcije.app.InjekcijeApp
import com.injekcije.app.THEME
import com.injekcije.app.UI
import com.injekcije.app.Strings
import com.injekcije.app.data.BackupPayloadParser
import com.injekcije.app.data.ImportResult
import com.injekcije.app.data.InjectionRepository
import com.injekcije.app.data.backup.CloudCheck
import com.injekcije.app.data.backup.GoogleDriveBackup
import com.injekcije.app.data.backup.RestoreOutcome
import com.injekcije.app.data.backup.SignInState
import com.injekcije.app.data.backup.SyncState
import com.injekcije.app.ui.components.buttonShape
import com.injekcije.app.ui.components.formatSyncTime
import com.injekcije.app.ui.components.DATE_TIME_FORMAT
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

/** Pitanje pre backup-a koji bi zamenio postojeći backup u oblaku. */
private enum class BackupQuestion { CLOUD_NOT_ADOPTED, LOCAL_EMPTY }

private const val PRE_RESTORE_FILE = "ciklus_pre_restore_backup.json"

@Composable
fun CloudBackupDialog(
    backup: GoogleDriveBackup,
    repo: InjectionRepository,
    signInLauncher: androidx.activity.result.ActivityResultLauncher<android.content.Intent>,
    strings: Strings,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val app = context.applicationContext as InjekcijeApp
    val signInState by backup.signInState.collectAsState()
    val syncState by backup.syncState.collectAsState()
    val lastSyncTime by backup.lastSyncTime.collectAsState()
    var pendingRestoreJson by remember { mutableStateOf<String?>(null) }
    var pendingRestoreFromCloud by remember { mutableStateOf(true) }
    var backupQuestion by remember { mutableStateOf<BackupQuestion?>(null) }
    // (uspeh, tekst) - rezultat poslednje operacije, prikazuje se kao dijalog
    var resultMessage by remember { mutableStateOf<Pair<Boolean, String>?>(null) }
    var busy by remember { mutableStateOf(false) }
    val preRestoreFile = remember { File(context.filesDir, PRE_RESTORE_FILE) }
    var hasPreRestore by remember { mutableStateOf(preRestoreFile.exists()) }

    // Operacije rade u korutini aplikacije: zatvaranje dijaloga ih ne prekida.
    fun launchOperation(block: suspend () -> Unit) {
        app.appScope.launch(Dispatchers.Main) {
            busy = true
            try {
                block()
            } catch (e: kotlinx.coroutines.CancellationException) {
                throw e
            } catch (e: Exception) {
                resultMessage = false to (e.message ?: e.javaClass.simpleName)
            } finally {
                busy = false
            }
        }
    }

    suspend fun runBackup() {
        val success = backup.backup(repo.exportJson())
        resultMessage = if (success) {
            true to strings.backupSuccess
        } else {
            val reason = (backup.syncState.value as? SyncState.Error)?.message
            false to (strings.backupError + (reason?.let { ": $it" } ?: ""))
        }
    }

    // "Prazno ne gazi popunjeno": pre otpreme se proverava oblak. Neuspela provera
    // nikad ne znači "oblak je prazan" - tada se NIŠTA ne otprema.
    fun requestBackup() = launchOperation {
        when (val check = backup.checkCloud()) {
            is CloudCheck.Failed -> resultMessage = false to
                "Provera oblaka nije uspela (${check.message}). Backup NIJE napravljen, da postojeći backup ne bi bio pregažen."
            CloudCheck.Empty -> runBackup()
            CloudCheck.Exists -> when {
                !backup.isCloudAdopted() -> backupQuestion = BackupQuestion.CLOUD_NOT_ADOPTED
                !repo.hasUserData() -> backupQuestion = BackupQuestion.LOCAL_EMPTY
                else -> runBackup()
            }
        }
    }

    fun startRestore() = launchOperation {
        when (val outcome = backup.restore()) {
            is RestoreOutcome.Success -> {
                pendingRestoreFromCloud = true
                pendingRestoreJson = outcome.json
            }
            RestoreOutcome.NotFound -> resultMessage = false to "U oblaku nema backup-a za ovaj nalog."
            is RestoreOutcome.Error -> resultMessage = false to "${strings.restoreError}: ${outcome.message}"
        }
    }

    // Sve-ili-ništa: importJson prvo parsira i proverava CEO sadržaj; na grešku se stanje ne menja.
    fun applyRestore(json: String, fromCloud: Boolean) = launchOperation {
        // Lokalni snimak pre vraćanja, za slučaj da je backup pogrešan
        withContext(Dispatchers.IO) {
            runCatching { preRestoreFile.writeText(repo.exportJson()) }
        }
        when (val result = repo.importJson(json, strict = true)) {
            is ImportResult.Success -> {
                // Odmah na disk + ponovno zakazivanje svih notifikacija (korutina aplikacije)
                val saved = app.persistAndRescheduleAfterRestore().await()
                if (fromCloud) backup.recordSuccessfulRestore()
                hasPreRestore = preRestoreFile.exists()
                resultMessage = true to buildString {
                    append("${strings.restoreSuccess}. Vraćeno — ${result.summary}.")
                    if (!saved) append("\n\nUpozorenje: upis na disk nije uspeo. Ne zatvaraj aplikaciju i pokušaj ponovo.")
                }
            }
            is ImportResult.Error -> resultMessage = false to
                "Vraćanje nije uspelo — podaci na telefonu NISU promenjeni.\n\n${result.message}"
        }
    }

    // Prihvaćena ponuda "Vrati iz oblaka" odmah posle prijave
    val autoRestoreRequest by backup.autoRestoreRequest.collectAsState()
    LaunchedEffect(autoRestoreRequest) {
        if (autoRestoreRequest && backup.consumeAutoRestoreRequest()) startRestore()
    }

    pendingRestoreJson?.let { json ->
        val exportedAt = remember(json) { InjectionRepository.peekExportedAt(json) }
        val localModified = remember { File(context.filesDir, "ciklus_local_data.json").lastModified() }
        val backupIsEmpty = remember(json) {
            runCatching { BackupPayloadParser.parse(json, strict = false).isEmpty }.getOrDefault(false)
        }
        val localHasData = remember(json) { repo.hasUserData() }
        val emptyOverPopulated = backupIsEmpty && localHasData
        val backupDateText = exportedAt?.let {
            java.time.Instant.ofEpochMilli(it)
                .atZone(java.time.ZoneId.systemDefault())
                .toLocalDateTime()
                .format(DATE_TIME_FORMAT)
        }
        AlertDialog(
            onDismissRequest = { pendingRestoreJson = null },
            containerColor = THEME.bgCard,
            title = { Text(if (emptyOverPopulated) "Backup je prazan" else "Vraćanje iz backup-a", color = THEME.textPrimary) },
            text = {
                Text(
                    when {
                        emptyOverPopulated ->
                            "Ovaj backup NEMA nijedan ciklus ni unos, a na telefonu postoje podaci. Vraćanjem BRIŠEŠ lokalne podatke. Nastavi?"
                        exportedAt != null && localModified > 0 && exportedAt < localModified ->
                            "Backup (od $backupDateText) je stariji od lokalnih podataka. Vraćanjem gubiš novije unose. Nastavi?"
                        backupDateText != null ->
                            "Vraćanjem backup-a (od $backupDateText) zamenjuješ trenutne lokalne podatke. Nastavi?"
                        else ->
                            "Vraćanjem backup-a zamenjuješ trenutne lokalne podatke. Nastavi?"
                    },
                    color = THEME.textSecondary
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    val json2 = json
                    val fromCloud = pendingRestoreFromCloud
                    pendingRestoreJson = null
                    applyRestore(json2, fromCloud)
                }) { Text(if (emptyOverPopulated) "Ipak vrati" else "Nastavi", color = THEME.danger) }
            },
            dismissButton = {
                TextButton(onClick = { pendingRestoreJson = null }) {
                    Text("Otkaži", color = THEME.textMuted)
                }
            }
        )
    }

    backupQuestion?.let { question ->
        AlertDialog(
            onDismissRequest = { backupQuestion = null },
            containerColor = THEME.bgCard,
            title = {
                Text(
                    if (question == BackupQuestion.LOCAL_EMPTY) "Na telefonu nema podataka" else "U oblaku već postoji backup",
                    color = THEME.textPrimary
                )
            },
            text = {
                Text(
                    if (question == BackupQuestion.LOCAL_EMPTY) {
                        "U oblaku postoji backup, a na telefonu trenutno nema nijednog ciklusa ni unosa. " +
                            "Ako nastaviš, backup u oblaku biće zamenjen praznim."
                    } else {
                        "Ova instalacija još nije preuzela podatke iz oblaka. Ako sada napraviš backup, " +
                            "backup u oblaku biće zamenjen podacima sa ovog telefona" +
                            (if (!repo.hasUserData()) ", a na telefonu trenutno NEMA podataka" else "") +
                            ". Preporuka: prvo vrati podatke iz oblaka."
                    },
                    color = THEME.textSecondary
                )
            },
            confirmButton = {
                Column(Modifier.fillMaxWidth()) {
                    Button(
                        onClick = { backupQuestion = null; startRestore() },
                        modifier = Modifier.fillMaxWidth(),
                        shape = buttonShape,
                        colors = ButtonDefaults.buttonColors(THEME.accentPrimary, THEME.textDark)
                    ) { Text("Vrati iz oblaka") }
                    TextButton(
                        onClick = {
                            backupQuestion = null
                            // Izričita potvrda: korisnik usvaja trenutno lokalno stanje
                            backup.markCloudAdopted()
                            launchOperation { runBackup() }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("Ipak napravi backup", color = THEME.danger) }
                    TextButton(onClick = { backupQuestion = null }, modifier = Modifier.fillMaxWidth()) {
                        Text("Otkaži", color = THEME.textMuted)
                    }
                }
            }
        )
    }

    resultMessage?.let { (success, text) ->
        AlertDialog(
            onDismissRequest = { resultMessage = null },
            containerColor = THEME.bgCard,
            title = { Text(if (success) "Gotovo" else "Greška", color = THEME.textPrimary) },
            text = { Text(text, color = THEME.textSecondary) },
            confirmButton = {
                TextButton(onClick = { resultMessage = null }) {
                    Text("U redu", color = if (success) THEME.accentPrimary else THEME.danger)
                }
            }
        )
    }

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
                Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                    Text("Backup u cloudu", color = THEME.textPrimary, fontSize = UI.titleSize, fontWeight = FontWeight.Bold)
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Zatvori backup", tint = THEME.textMuted)
                    }
                }
                Spacer(Modifier.height(UI.largeSpacing))

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = THEME.bgCardElevated,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(Modifier.padding(UI.mediumSpacing)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                if (signInState is SignInState.SignedIn) Icons.Default.CloudDone else Icons.Default.CloudOff,
                                null,
                                tint = if (signInState is SignInState.SignedIn) THEME.success else THEME.textMuted,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(Modifier.width(UI.defaultSpacing))
                            Column {
                                Text(
                                    if (signInState is SignInState.SignedIn) "Google nalog povezan" else "Google nalog nije povezan",
                                    color = THEME.textMuted,
                                    fontSize = UI.captionSize
                                )
                                Text(
                                    when (signInState) {
                                        is SignInState.SignedIn -> (signInState as SignInState.SignedIn).email
                                        SignInState.SignedOut -> strings.notConnected
                                    },
                                    color = THEME.textPrimary,
                                    fontSize = UI.bodySize,
                                    fontWeight = FontWeight.Medium
                                )
                                if (lastSyncTime != null) {
                                    Text(
                                        "${strings.lastSync}: ${formatSyncTime(lastSyncTime!!, strings)}",
                                        color = THEME.textMuted,
                                        fontSize = UI.smallSize
                                    )
                                }
                            }
                        }

                        AnimatedVisibility(visible = busy || syncState == SyncState.Syncing) {
                            Row(
                                Modifier.fillMaxWidth().padding(top = UI.defaultSpacing),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    strokeWidth = 2.dp,
                                    color = THEME.accentPrimary
                                )
                                Spacer(Modifier.width(UI.smallSpacing))
                                Text("Sinhronizacija je u toku...", color = THEME.accentPrimary, fontSize = UI.smallSize)
                            }
                        }

                        AnimatedVisibility(visible = !busy && syncState is SyncState.Error) {
                            Text(
                                text = "Greška: ${(syncState as? SyncState.Error)?.message ?: ""}",
                                color = THEME.danger,
                                fontSize = UI.smallSize,
                                modifier = Modifier.fillMaxWidth().padding(top = UI.defaultSpacing)
                            )
                        }
                    }
                }

                Spacer(Modifier.height(UI.largeSpacing))

                when (signInState) {
                    SignInState.SignedOut -> {
                        Button(
                            onClick = { signInLauncher.launch(backup.getSignInIntent()) },
                            modifier = Modifier.fillMaxWidth().height(UI.buttonHeight),
                            shape = buttonShape,
                            colors = ButtonDefaults.buttonColors(THEME.accentPrimary, THEME.textDark)
                        ) {
                            Icon(Icons.AutoMirrored.Filled.Login, null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(UI.smallSpacing))
                            Text(strings.connectGoogle)
                        }
                    }
                    is SignInState.SignedIn -> {
                        Button(
                            onClick = { requestBackup() },
                            modifier = Modifier.fillMaxWidth().height(UI.buttonHeight),
                            shape = buttonShape,
                            enabled = !busy && syncState != SyncState.Syncing,
                            colors = ButtonDefaults.buttonColors(THEME.accentPrimary, THEME.textDark)
                        ) {
                            Icon(Icons.Default.CloudUpload, null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(UI.smallSpacing))
                            Text("Napravi backup")
                        }

                        Spacer(Modifier.height(UI.smallSpacing))

                        OutlinedButton(
                            onClick = { startRestore() },
                            modifier = Modifier.fillMaxWidth().height(UI.buttonHeight),
                            shape = buttonShape,
                            enabled = !busy && syncState != SyncState.Syncing
                        ) {
                            Icon(Icons.Default.CloudDownload, null, modifier = Modifier.size(18.dp), tint = THEME.accentSecondary)
                            Spacer(Modifier.width(UI.smallSpacing))
                            Text("Vrati iz oblaka", color = THEME.textPrimary)
                        }

                        Spacer(Modifier.height(UI.largeSpacing))

                        TextButton(
                            onClick = {
                                launchOperation { backup.signOut() }
                            },
                            modifier = Modifier.fillMaxWidth().heightIn(min = UI.buttonHeight)
                        ) {
                            Icon(Icons.AutoMirrored.Filled.Logout, null, modifier = Modifier.size(18.dp), tint = THEME.danger)
                            Spacer(Modifier.width(UI.smallSpacing))
                            Text("Odjavi Google nalog", color = THEME.danger)
                        }
                    }
                }

                // Snimak stanja pre poslednjeg vraćanja - omogućava da se pogrešno vraćanje poništi
                if (hasPreRestore) {
                    TextButton(
                        onClick = {
                            launchOperation {
                                val snapshot = withContext(Dispatchers.IO) { preRestoreFile.readText() }
                                pendingRestoreFromCloud = false
                                pendingRestoreJson = snapshot
                            }
                        },
                        enabled = !busy,
                        modifier = Modifier.fillMaxWidth().heightIn(min = UI.buttonHeight)
                    ) {
                        Text("Poništi poslednje vraćanje", color = THEME.textMuted)
                    }
                }
            }
        }
    }
}

@Composable
fun WelcomeDialog(
    strings: Strings,
    onConnectGoogle: () -> Unit,
    onSkip: () -> Unit
) {
    Dialog(onDismissRequest = {}) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = THEME.bgCard,
            tonalElevation = 8.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                Modifier
                    .padding(UI.xlSpacing)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    Icons.Default.Cloud,
                    null,
                    tint = THEME.accentPrimary,
                    modifier = Modifier.size(64.dp)
                )
                Spacer(Modifier.height(UI.largeSpacing))

                Text(
                    "Backup podataka",
                    color = THEME.textPrimary,
                    fontSize = UI.titleSize,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(UI.defaultSpacing))

                Text(
                    "Poveži Google nalog ako želiš ručno da čuvaš backup i po potrebi vratiš podatke iz cloud-a.",
                    color = THEME.textSecondary,
                    fontSize = UI.bodySize,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(UI.xlSpacing))

                Button(
                    onClick = onConnectGoogle,
                    modifier = Modifier.fillMaxWidth().height(UI.buttonHeight),
                    shape = buttonShape,
                    colors = ButtonDefaults.buttonColors(THEME.accentPrimary, THEME.textDark)
                ) {
                    Icon(Icons.AutoMirrored.Filled.Login, null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(UI.smallSpacing))
                    Text(strings.connectGoogle)
                }

                Spacer(Modifier.height(UI.smallSpacing))

                TextButton(
                    onClick = onSkip,
                    modifier = Modifier.fillMaxWidth().heightIn(min = UI.buttonHeight)
                ) {
                    Text("Nastavi bez backup-a", color = THEME.textMuted)
                }
            }
        }
    }
}
