package com.injekcije.app.ui

import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.injekcije.app.THEME
import com.injekcije.app.UI
import com.injekcije.app.LocaleManager
import com.injekcije.app.LocalStrings
import com.injekcije.app.data.*
import com.injekcije.app.data.backup.GoogleDriveBackup
import com.injekcije.app.notifications.areNotificationsEnabled
import com.injekcije.app.notifications.scheduleInjectionNotifications
import com.injekcije.app.notifications.scheduleOralSteroidNotifications
import com.google.android.gms.auth.api.signin.GoogleSignIn
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import kotlinx.coroutines.delay
import android.content.Intent
import android.provider.Settings

// Screen composables
import com.injekcije.app.ui.screens.*
// Component composables
import com.injekcije.app.ui.components.GlassCard

@Composable
fun MainScreen(repo: InjectionRepository, localeManager: LocaleManager, backup: GoogleDriveBackup) {
    val strings = LocalStrings.current
    val context = LocalContext.current
    val repoTick = remember { mutableIntStateOf(0) }

    DisposableEffect(repo) {
        val token = repo.onChange { repoTick.intValue++ }
        onDispose { repo.removeOnChange(token) }
    }

    // Minutni tik + osveženje pri povratku aplikacije u prvi plan - vremenski
    // zavisne vrednosti (hoursRemaining, status, "kasni Xh") moraju da se
    // preračunaju i kada se podaci nisu menjali.
    var timeTick by remember { mutableIntStateOf(0) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(60_000)
            timeTick++
        }
    }
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) timeTick++
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    var dialogState by remember { mutableStateOf<DialogState>(DialogState.None) }
    val dismissDialog = { dialogState = DialogState.None }

    // CycleSetup ima sopstveni BackHandler (korak nazad + potvrda odbacivanja)
    BackHandler(enabled = dialogState != DialogState.None && dialogState != DialogState.CycleSetup) {
        dialogState = when (val state = dialogState) {
            is DialogState.LearningArticle -> DialogState.LearningCategory(state.categoryId)
            is DialogState.LearningCategory -> DialogState.Learning
            is DialogState.PCTDetails -> DialogState.None
            is DialogState.PCTSetup -> DialogState.None
            else -> DialogState.None
        }
    }

    var showWelcomeDialog by remember { mutableStateOf(backup.isFirstLaunch()) }

    val signInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        val account = try { task.result } catch (e: Exception) { null }
        backup.handleSignInResult(account)

        if (account != null && showWelcomeDialog) {
            showWelcomeDialog = false
            backup.markFirstLaunchDone()
        }
    }

    val activeCycle = remember(repoTick.intValue, timeTick) { repo.getActiveCycle() }
    val upcomingInjections = remember(repoTick.intValue, timeTick) { repo.getUpcomingInjections() }
    val recommendedSite = remember(repoTick.intValue, timeTick) { repo.getRecommendedSite() }
    val siteHistory = remember(repoTick.intValue, timeTick) { repo.getSiteHistory() }

    val activePCTInfo = remember(repoTick.intValue, timeTick) { repo.getActivePCTGuideInfo() }
    val finishedCycleNeedingPCT = remember(repoTick.intValue, timeTick) { repo.getFinishedCycleNeedingPCT() }

    var notificationCheckTick by remember { mutableIntStateOf(0) }
    LaunchedEffect(dialogState) { notificationCheckTick++ }
    val notificationsEnabled = remember(notificationCheckTick) { areNotificationsEnabled(context) }

    // Show full-screen wizard when CycleSetup is active
    if (dialogState == DialogState.CycleSetup) {
        CycleWizardScreen(
            repo = repo,
            onDismiss = dismissDialog,
            onCycleCreated = dismissDialog,
            strings = strings
        )
        return
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(THEME.bgDark, THEME.bgPrimary)))
    ) {
        Column(modifier = Modifier.fillMaxSize().statusBarsPadding().padding(UI.mediumSpacing)) {
            HeaderSection(activeCycle, { dialogState = DialogState.Settings }, { dialogState = DialogState.Learning }, strings)
            Spacer(Modifier.height(UI.mediumSpacing))

            // Notification warning banner
            if (!notificationsEnabled) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 64.dp)
                        .clickable {
                            val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                                putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                            }
                            context.startActivity(intent)
                        },
                    shape = RoundedCornerShape(12.dp),
                    color = THEME.danger.copy(alpha = 0.15f),
                    border = BorderStroke(1.dp, THEME.danger.copy(alpha = 0.5f))
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.NotificationsOff,
                            contentDescription = null,
                            tint = THEME.danger,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "Notifikacije su isključene!",
                                color = THEME.danger,
                                fontWeight = FontWeight.Bold,
                                fontSize = UI.bodySize
                            )
                            Text(
                                "Nećeš dobiti podsetnik za injekcije. Dodirni da uključiš.",
                                color = THEME.textMuted,
                                fontSize = UI.smallSize
                            )
                        }
                        Icon(
                            Icons.Default.ChevronRight,
                            contentDescription = null,
                            tint = THEME.danger
                        )
                    }
                }
                Spacer(Modifier.height(UI.mediumSpacing))
            }

            Column(modifier = Modifier.weight(1f).verticalScroll(rememberScrollState())) {
                // PCT Guide Card
                if (activePCTInfo != null || finishedCycleNeedingPCT != null) {
                    PCTGuideCard(
                        pctInfo = activePCTInfo,
                        finishedCycle = finishedCycleNeedingPCT,
                        onSetupPCT = { cycleId -> dialogState = DialogState.PCTSetup(cycleId) },
                        onViewPCT = { info -> dialogState = DialogState.PCTDetails(info) }
                    )
                    Spacer(Modifier.height(UI.mediumSpacing))
                }

                if (activeCycle == null) {
                    NoCycleCard(strings) { dialogState = DialogState.CycleSetup }
                    Spacer(Modifier.height(UI.mediumSpacing))
                } else {
                    val oralDoseInfos = remember(repoTick.intValue, timeTick) { getOralDoseInfo(activeCycle, repo) }
                    val hasInjectables = upcomingInjections.isNotEmpty()
                    val hasOrals = oralDoseInfos.isNotEmpty()
                    val primaryInjectableId = activeCycle.compounds
                        .firstOrNull { !OralDosingSchedule.isOralSteroid(it.substanceId) }
                        ?.substanceId
                    val nextInjectionInfo = primaryInjectableId
                        ?.let { id -> upcomingInjections.find { it.substanceId == id } }
                        ?: upcomingInjections.firstOrNull()

                    // Injectable tracking row
                    if (hasInjectables) {
                        Row(Modifier.fillMaxWidth().height(IntrinsicSize.Max), horizontalArrangement = Arrangement.spacedBy(UI.defaultSpacing)) {
                            NextInjectionCard(nextInjectionInfo, Modifier.weight(1f).fillMaxHeight(), strings) { info ->
                                dialogState = DialogState.LogInjection(info)
                            }
                            SiteCard(
                                recommendedSite,
                                siteHistory.find { it.site == recommendedSite }?.daysSinceLastUse,
                                Modifier.weight(1f).fillMaxHeight(),
                                strings,
                                nextInjectionInfo?.status?.color
                            ) {
                                dialogState = DialogState.SiteMap
                            }
                        }
                        Spacer(Modifier.height(UI.mediumSpacing))
                    }

                    // Oral tracking row
                    if (hasOrals) {
                        if (oralDoseInfos.size == 1) {
                            NextOralDoseCard(
                                info = oralDoseInfos.first(),
                                modifier = Modifier.fillMaxWidth(),
                                strings = strings,
                                isFullWidth = true,
                                onLogDose = {
                                    val info = oralDoseInfos.first()
                                    dialogState = DialogState.LogOralDose(info.substanceId, info.substanceName, info.dosePerServing)
                                }
                            )
                        } else {
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(UI.defaultSpacing)) {
                                oralDoseInfos.forEach { oralInfo ->
                                    NextOralDoseCard(
                                        info = oralInfo,
                                        modifier = Modifier.weight(1f),
                                        strings = strings,
                                        onLogDose = { dialogState = DialogState.LogOralDose(oralInfo.substanceId, oralInfo.substanceName, oralInfo.dosePerServing) }
                                    )
                                }
                            }
                        }
                        Spacer(Modifier.height(UI.mediumSpacing))
                    }

                    // Big injection button - samo ako ciklus ima injektabilne supstance
                    if (primaryInjectableId != null) {
                        Button(
                            onClick = { dialogState = DialogState.LogInjection(nextInjectionInfo) },
                            modifier = Modifier.fillMaxWidth().height(56.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(THEME.accentPrimary, THEME.textDark)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(24.dp))
                            Spacer(Modifier.width(UI.defaultSpacing))
                            Text(strings.logInjection, fontSize = UI.subtitleSize, fontWeight = FontWeight.Bold)
                        }

                        Spacer(Modifier.height(UI.mediumSpacing))
                    }
                    CompoundsSection(
                        cycle = activeCycle,
                        upcoming = upcomingInjections,
                        repo = repo,
                        strings = strings,
                        onEdit = { compound -> dialogState = DialogState.EditCompound(compound, activeCycle.durationWeeks) },
                        onDelete = { compound -> dialogState = DialogState.ConfirmDeleteCompound(compound) },
                        onLog = { info -> dialogState = DialogState.LogInjection(info) },
                        onLogOral = { substanceId, substanceName, dosePerServing ->
                            dialogState = DialogState.LogOralDose(substanceId, substanceName, dosePerServing)
                        },
                        onAddCompound = { dialogState = DialogState.AddCompound(activeCycle.durationWeeks) }
                    )
                    Spacer(Modifier.height(UI.mediumSpacing))
                    RecentInjectionsSection(repo, repoTick.intValue, strings, { r -> dialogState = DialogState.EditInjection(r) }, { id -> dialogState = DialogState.ConfirmDelete(id) })
                    Spacer(Modifier.height(UI.mediumSpacing))
                    RecentOralDosesSection(repo, repoTick.intValue, strings) { id -> dialogState = DialogState.ConfirmDeleteOralDose(id) }
                }
            }

            // Bottom bar only when no active cycle
            if (activeCycle == null) {
                Spacer(Modifier.height(UI.defaultSpacing))
                BottomBar(strings) { dialogState = DialogState.CycleSetup }
            }
        }

        // Dialog state routing
        when (val state = dialogState) {
            DialogState.None -> {}
            DialogState.CycleSetup -> {} // Handled above as full-screen
            is DialogState.LogInjection -> LogInjectionDialog(state.info, repo, strings, dismissDialog)
            is DialogState.LogOralDose -> LogOralDoseDialog(state.substanceId, state.substanceName, state.dosePerServing, repo, strings, dismissDialog)
            DialogState.History -> HistoryDialog(repo, strings, dismissDialog, { r -> dialogState = DialogState.EditInjection(r) }, { id -> dialogState = DialogState.ConfirmDelete(id) })
            is DialogState.EditInjection -> EditInjectionDialog(state.record, repo, strings, dismissDialog)
            is DialogState.EditCompound -> EditCompoundDialog(state.compound, state.cycleDurationWeeks, repo, strings, dismissDialog)
            is DialogState.AddCompound -> AddCompoundDialog(state.cycleDurationWeeks, repo, strings, dismissDialog)
            is DialogState.ConfirmDelete -> ConfirmDeleteDialog(strings, strings.deleteInjectionConfirm, { repo.deleteInjection(state.recordId); dismissDialog() }, dismissDialog)
            is DialogState.ConfirmDeleteOralDose -> ConfirmDeleteDialog(strings, strings.deleteOralDoseConfirm, { repo.deleteOralDose(state.recordId); dismissDialog() }, dismissDialog)
            is DialogState.ConfirmDeleteCompound -> {
                val substanceName = repo.getSubstance(state.compound.substanceId)?.shortName ?: state.compound.substanceId
                AlertDialog(
                    onDismissRequest = dismissDialog,
                    title = { Text("Ukloni supstancu?", color = THEME.textPrimary) },
                    text = { Text("Da li želiš da ukloniš $substanceName iz ciklusa?", color = THEME.textSecondary) },
                    confirmButton = {
                        Button(
                            onClick = {
                                repo.removeCompoundFromCycle(state.compound.substanceId)
                                scheduleInjectionNotifications(context)
                                scheduleOralSteroidNotifications(context)
                                dismissDialog()
                            },
                            colors = ButtonDefaults.buttonColors(THEME.danger, THEME.textPrimary)
                        ) { Text("Ukloni") }
                    },
                    dismissButton = { TextButton(onClick = dismissDialog) { Text(strings.cancel, color = THEME.textMuted) } },
                    containerColor = THEME.bgCard
                )
            }
            DialogState.SiteMap -> SiteMapDialog(repo, strings, dismissDialog)
            DialogState.Settings -> SettingsDialog(repo, localeManager, backup, strings, dismissDialog, { dialogState = DialogState.CloudBackup }, { dialogState = DialogState.NotificationSettings })
            DialogState.CloudBackup -> CloudBackupDialog(backup, repo, signInLauncher, strings, dismissDialog)
            DialogState.AddSubstance -> {
                // Promena stanja ne sme da se desi tokom kompozicije
                LaunchedEffect(Unit) { dismissDialog() }
            }
            DialogState.Learning -> LearningCategoriesScreen(
                strings = strings,
                onDismiss = dismissDialog,
                onCategoryClick = { categoryId -> dialogState = DialogState.LearningCategory(categoryId) }
            )
            is DialogState.LearningCategory -> LearningCategoryScreen(
                categoryId = state.categoryId,
                strings = strings,
                onDismiss = { dialogState = DialogState.Learning },
                onArticleClick = { articleId -> dialogState = DialogState.LearningArticle(state.categoryId, articleId) }
            )
            is DialogState.LearningArticle -> LearningArticleScreen(
                categoryId = state.categoryId,
                articleId = state.articleId,
                strings = strings,
                onDismiss = { dialogState = DialogState.LearningCategory(state.categoryId) }
            )

            // PCT dialogs
            DialogState.PCTGuide -> {
                val info = activePCTInfo
                if (info != null) {
                    PCTDetailsDialog(info, repo, dismissDialog)
                } else {
                    // Promena stanja ne sme da se desi tokom kompozicije
                    LaunchedEffect(Unit) { dismissDialog() }
                }
            }
            is DialogState.PCTSetup -> PCTSetupDialog(
                cycleId = state.cycleId,
                repo = repo,
                onDismiss = dismissDialog,
                onPCTCreated = { dismissDialog() }
            )
            is DialogState.PCTDetails -> PCTDetailsDialog(
                info = state.info,
                repo = repo,
                onDismiss = dismissDialog
            )
            // Notification settings
            DialogState.NotificationSettings -> NotificationSettingsDialog(
                repo = repo,
                strings = strings,
                onDismiss = dismissDialog
            )
        }

        // Odmah posle prijave: u oblaku postoji backup koji ova instalacija još nije preuzela.
        // Proveru radi GoogleDriveBackup.handleSignInResult; neuspela provera ne nudi ništa,
        // ali zaštita pri ručnom backup-u ostaje.
        val restoreOffer by backup.restoreOffer.collectAsState()
        if (restoreOffer && !showWelcomeDialog) {
            AlertDialog(
                onDismissRequest = { backup.dismissRestoreOffer() },
                containerColor = THEME.bgCard,
                title = { Text("Nađen backup u oblaku", color = THEME.textPrimary) },
                text = {
                    Text(
                        "Na ovom Google nalogu već postoji backup. Hoćeš li da vratiš te podatke? " +
                            "Pre zamene videćeš datum backup-a i moći ćeš da odustaneš.",
                        color = THEME.textSecondary
                    )
                },
                confirmButton = {
                    TextButton(onClick = {
                        backup.requestAutoRestore()
                        dialogState = DialogState.CloudBackup
                    }) { Text("Vrati iz oblaka", color = THEME.accentPrimary) }
                },
                dismissButton = {
                    TextButton(onClick = { backup.dismissRestoreOffer() }) {
                        Text("Ne sada", color = THEME.textMuted)
                    }
                }
            )
        }

        // Welcome dialog for first launch
        if (showWelcomeDialog) {
            WelcomeDialog(
                strings = strings,
                onConnectGoogle = {
                    signInLauncher.launch(backup.getSignInIntent())
                },
                onSkip = {
                    showWelcomeDialog = false
                    backup.markFirstLaunchDone()
                }
            )
        }
    }
}
