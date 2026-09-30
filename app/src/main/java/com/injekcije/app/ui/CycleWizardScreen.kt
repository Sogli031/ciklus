package com.injekcije.app.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.platform.LocalContext
import com.injekcije.app.THEME
import com.injekcije.app.UI
import com.injekcije.app.Strings
import com.injekcije.app.data.*
import com.injekcije.app.ui.components.buttonShape
import com.injekcije.app.ui.components.textFieldColors
import com.injekcije.app.ui.components.textFieldShape
import com.injekcije.app.notifications.scheduleInjectionNotifications
import com.injekcije.app.notifications.scheduleOralSteroidNotifications
import android.widget.Toast
import java.time.LocalDate
import java.util.Locale

// =============================================================================
// WIZARD STEP DEFINITION
// =============================================================================

sealed class WizardStep {
    data object Name : WizardStep()
    data object Duration : WizardStep()
    data object SelectSubstances : WizardStep()
    data class ConfigureSubstance(val index: Int) : WizardStep()
    data object Review : WizardStep()
}

// =============================================================================
// SUBSTANCE CONFIG DATA CLASS
// =============================================================================

data class SubstanceConfig(
    val substance: Substance,
    var weeklyDoseMg: String = if (substance.recommendedWeeklyDose > 0)
        substance.recommendedWeeklyDose.toInt().toString() else "500",
    var frequency: InjectionFrequency = substance.recommendedFrequency,
    var concentrationMgMl: String = substance.defaultConcentration.toString(),
    var weeklyDoses: MutableMap<Int, String> = mutableMapOf(),
    var startWeek: Int = 1,
    var endWeek: Int? = null  // null = cycle duration
)

// =============================================================================
// MAIN WIZARD COMPOSABLE
// =============================================================================

@Composable
fun CycleWizardScreen(
    repo: InjectionRepository,
    onDismiss: () -> Unit,
    onCycleCreated: () -> Unit,
    @Suppress("UNUSED_PARAMETER") strings: Strings
) {
    val context = LocalContext.current
    var currentStep by remember { mutableStateOf<WizardStep>(WizardStep.Name) }

    // Wizard state
    var cycleName by remember { mutableStateOf("Ciklus ${repo.getAllCycles().size + 1}") }
    var durationWeeks by remember { mutableStateOf("12") }
    var selectedSubstances by remember { mutableStateOf<Set<String>>(emptySet()) }
    var substanceConfigs by remember { mutableStateOf<Map<String, SubstanceConfig>>(emptyMap()) }
    var showDiscardConfirm by remember { mutableStateOf(false) }

    // Sistemsko "nazad" vodi korak unazad; na prvom koraku traži potvrdu odbacivanja
    BackHandler {
        when (val step = currentStep) {
            is WizardStep.Name -> showDiscardConfirm = true
            is WizardStep.Duration -> currentStep = WizardStep.Name
            is WizardStep.SelectSubstances -> currentStep = WizardStep.Duration
            is WizardStep.ConfigureSubstance -> currentStep = if (step.index > 0) {
                WizardStep.ConfigureSubstance(step.index - 1)
            } else {
                WizardStep.SelectSubstances
            }
            is WizardStep.Review -> currentStep = if (selectedSubstances.isNotEmpty()) {
                WizardStep.ConfigureSubstance(selectedSubstances.size - 1)
            } else {
                WizardStep.SelectSubstances
            }
        }
    }

    if (showDiscardConfirm) {
        AlertDialog(
            onDismissRequest = { showDiscardConfirm = false },
            title = { Text("Odbaci podešavanje ciklusa?", color = THEME.textPrimary) },
            text = { Text("Uneti podaci neće biti sačuvani.", color = THEME.textSecondary) },
            confirmButton = {
                Button(
                    onClick = {
                        showDiscardConfirm = false
                        onDismiss()
                    },
                    colors = ButtonDefaults.buttonColors(THEME.danger, THEME.textPrimary)
                ) { Text("Odbaci") }
            },
            dismissButton = {
                TextButton(onClick = { showDiscardConfirm = false }) {
                    Text("Ostani", color = THEME.textMuted)
                }
            },
            containerColor = THEME.bgCard
        )
    }

    val allSubstances = remember { SampleData.getDefaultSubstances() }
    val categories = remember { SampleData.getCategories() }

    // Calculate progress
    val totalSteps = 3 + selectedSubstances.size + 1 // name + duration + select + configs + review
    val currentStepIndex = when (currentStep) {
        is WizardStep.Name -> 0
        is WizardStep.Duration -> 1
        is WizardStep.SelectSubstances -> 2
        is WizardStep.ConfigureSubstance -> 3 + (currentStep as WizardStep.ConfigureSubstance).index
        is WizardStep.Review -> totalSteps - 1
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(THEME.bgDark)
            .statusBarsPadding()
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header with progress
            WizardHeader(
                currentStepIndex = currentStepIndex,
                totalSteps = totalSteps,
                onClose = onDismiss
            )

            // Content
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                when (val step = currentStep) {
                    is WizardStep.Name -> NameStep(
                        name = cycleName,
                        onNameChange = { cycleName = it },
                        onNext = { currentStep = WizardStep.Duration }
                    )

                    is WizardStep.Duration -> DurationStep(
                        duration = durationWeeks,
                        onDurationChange = { durationWeeks = it },
                        onBack = { currentStep = WizardStep.Name },
                        onNext = { currentStep = WizardStep.SelectSubstances }
                    )

                    is WizardStep.SelectSubstances -> SelectSubstancesStep(
                        allSubstances = allSubstances,
                        categories = categories,
                        selectedIds = selectedSubstances,
                        onSelectionChange = { id, selected ->
                            selectedSubstances = if (selected) {
                                selectedSubstances + id
                            } else {
                                selectedSubstances - id
                            }
                        },
                        onBack = { currentStep = WizardStep.Duration },
                        onNext = {
                            // Zadrži postojeće konfiguracije; kreiraj nove samo za
                            // novododate supstance (povratak nazad ne briše unos)
                            val newConfigs = selectedSubstances.mapNotNull { id ->
                                val substance = allSubstances.find { it.id == id }
                                substance?.let { id to (substanceConfigs[id] ?: SubstanceConfig(it)) }
                            }.toMap()
                            substanceConfigs = newConfigs

                            if (selectedSubstances.isNotEmpty()) {
                                currentStep = WizardStep.ConfigureSubstance(0)
                            }
                        },
                        canProceed = selectedSubstances.isNotEmpty()
                    )

                    is WizardStep.ConfigureSubstance -> {
                        val substanceIds = selectedSubstances.toList()
                        val substanceId = substanceIds.getOrNull(step.index)
                        val config = substanceId?.let { substanceConfigs[it] }

                        if (config != null) {
                            ConfigureSubstanceStep(
                                config = config,
                                substanceIndex = step.index + 1,
                                totalSubstances = substanceIds.size,
                                durationWeeks = durationWeeks.toIntOrNull() ?: 12,
                                onConfigChange = { newConfig ->
                                    substanceConfigs = substanceConfigs + (substanceId to newConfig)
                                },
                                onBack = {
                                    if (step.index > 0) {
                                        currentStep = WizardStep.ConfigureSubstance(step.index - 1)
                                    } else {
                                        currentStep = WizardStep.SelectSubstances
                                    }
                                },
                                onNext = {
                                    if (step.index < substanceIds.size - 1) {
                                        currentStep = WizardStep.ConfigureSubstance(step.index + 1)
                                    } else {
                                        currentStep = WizardStep.Review
                                    }
                                }
                            )
                        }
                    }

                    is WizardStep.Review -> ReviewStep(
                        cycleName = cycleName,
                        durationWeeks = durationWeeks,
                        configs = substanceConfigs.values.toList(),
                        onBack = {
                            val count = selectedSubstances.size
                            if (count > 0) {
                                currentStep = WizardStep.ConfigureSubstance(count - 1)
                            } else {
                                currentStep = WizardStep.SelectSubstances
                            }
                        },
                        onCreate = {
                            // Validacija pre kreiranja ciklusa
                            val cycleDuration = durationWeeks.toIntOrNull()
                            if (cycleDuration == null || cycleDuration <= 0) {
                                Toast.makeText(context, "Trajanje ciklusa mora biti pozitivno", Toast.LENGTH_SHORT).show()
                                return@ReviewStep
                            }
                            if (cycleDuration > 104) {
                                Toast.makeText(context, "Maksimalno trajanje je 104 nedelje (2 godine)", Toast.LENGTH_SHORT).show()
                                return@ReviewStep
                            }
                            if (substanceConfigs.isEmpty()) {
                                Toast.makeText(context, "Dodajte bar jedno jedinjenje", Toast.LENGTH_SHORT).show()
                                return@ReviewStep
                            }
                            val invalidCompound = substanceConfigs.values.firstOrNull { config ->
                                (config.weeklyDoseMg.toDoubleOrNull() ?: 0.0) <= 0.0 ||
                                (config.concentrationMgMl.toDoubleOrNull() ?: 0.0) <= 0.0
                            }
                            if (invalidCompound != null) {
                                Toast.makeText(context, "Doza i koncentracija moraju biti veći od 0 za ${invalidCompound.substance.shortName}", Toast.LENGTH_LONG).show()
                                return@ReviewStep
                            }

                            val compounds = substanceConfigs.values.map { config ->
                                val phases = config.weeklyDoses
                                    .filter { it.value.isNotBlank() }
                                    .map { (week, dose) ->
                                        DosePhase(week, week, dose.toDoubleOrNull() ?: 0.0)
                                    }
                                    .filter { it.weeklyDoseMg > 0 }

                                CycleCompound(
                                    substanceId = config.substance.id,
                                    weeklyDoseMg = config.weeklyDoseMg.toDoubleOrNull() ?: 500.0,
                                    frequency = config.frequency,
                                    concentrationMgMl = config.concentrationMgMl.toDoubleOrNull() ?: 250.0,
                                    phases = phases,
                                    startWeek = config.startWeek,
                                    endWeek = if (config.endWeek == cycleDuration) null else config.endWeek
                                )
                            }

                            val cycle = Cycle(
                                name = cycleName,
                                startDate = LocalDate.now(),
                                durationWeeks = cycleDuration,
                                compounds = compounds
                            )

                            repo.addCycle(cycle)
                            scheduleInjectionNotifications(context)
                            scheduleOralSteroidNotifications(context)
                            onCycleCreated()
                        }
                    )
                }
            }
        }
    }
}

// =============================================================================
// POMOĆNE FUNKCIJE ZA DECIMALNI UNOS
// =============================================================================

/**
 * Normalizuje decimalni unos: zarez (srpski standard) pretvara u tačku,
 * dozvoljava samo cifre i najviše jednu decimalnu tačku.
 * Vraća null ako unos nije validan (unos se tada ignoriše).
 */
internal fun normalizeDecimalInput(input: String): String? {
    val normalized = input.replace(',', '.')
    return if (normalized.all { it.isDigit() || it == '.' } && normalized.count { it == '.' } <= 1) {
        normalized
    } else {
        null
    }
}

/**
 * Formatira dozu bez suvišnog ".0" za cele brojeve (500 umesto 500.0, ali 2.5 ostaje 2.5).
 */
internal fun formatDoseNumber(value: Double): String =
    if (value == value.toLong().toDouble()) value.toLong().toString() else value.toString()

// =============================================================================
// WIZARD HEADER
// =============================================================================

@Composable
private fun WizardHeader(
    currentStepIndex: Int,
    totalSteps: Int,
    onClose: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(THEME.bgCard)
            .padding(UI.largeSpacing)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Novi ciklus",
                    color = THEME.textPrimary,
                    fontSize = UI.titleSize,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(UI.smallSpacing))
                Text(
                    text = "Korak ${currentStepIndex + 1} od $totalSteps",
                    color = THEME.textMuted,
                    fontSize = UI.captionSize,
                    fontWeight = FontWeight.Medium
                )
            }
            IconButton(onClick = onClose) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Zatvori",
                    tint = THEME.textMuted
                )
            }
        }

        Spacer(Modifier.height(UI.largeSpacing))

        // Progress bar
        LinearProgressIndicator(
            progress = { (currentStepIndex + 1).toFloat() / totalSteps },
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp)),
            color = THEME.accentPrimary,
            trackColor = THEME.bgCardElevated
        )
    }
}

// =============================================================================
// STEP 1: NAME
// =============================================================================

@Composable
private fun NameStep(
    name: String,
    onNameChange: (String) -> Unit,
    onNext: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(UI.xlSpacing),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Kako želite da nazovete ciklus?",
                color = THEME.textPrimary,
                fontSize = UI.titleSize,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(UI.xlSpacing))

            OutlinedTextField(
                value = name,
                onValueChange = onNameChange,
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Naziv ciklusa", color = THEME.textMuted) },
                colors = textFieldColors(),
                shape = textFieldShape,
                singleLine = true
            )

            Spacer(Modifier.height(UI.largeSpacing))

            Text(
                text = "Možete ostaviti predloženi naziv ili upisati svoj.",
                color = THEME.textMuted,
                fontSize = UI.captionSize,
                textAlign = TextAlign.Center
            )
        }

        Spacer(Modifier.height(UI.xlSpacing))

        Button(
            onClick = onNext,
            modifier = Modifier
                .fillMaxWidth()
                .height(UI.buttonHeight),
            enabled = name.isNotBlank(),
            shape = buttonShape,
            colors = ButtonDefaults.buttonColors(
                containerColor = THEME.accentPrimary,
                contentColor = THEME.textDark
            )
        ) {
            Text("Dalje", fontWeight = FontWeight.Bold)
            Spacer(Modifier.width(UI.smallSpacing))
            Icon(Icons.AutoMirrored.Filled.ArrowForward, null, Modifier.size(18.dp))
        }

        Spacer(Modifier.height(UI.largeSpacing))
    }
}

// =============================================================================
// STEP 2: DURATION
// =============================================================================

@Composable
private fun DurationStep(
    duration: String,
    onDurationChange: (String) -> Unit,
    onBack: () -> Unit,
    onNext: () -> Unit
) {
    val durationInt = duration.toIntOrNull() ?: 0
    val hasDurationError = duration.isNotBlank() && durationInt > 104

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(UI.xlSpacing),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Koliko nedelja traje ciklus?",
                color = THEME.textPrimary,
                fontSize = UI.titleSize,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(UI.xlSpacing))

            OutlinedTextField(
                value = duration,
                onValueChange = { if (it.all { c -> c.isDigit() }) onDurationChange(it) },
                modifier = Modifier.width(128.dp),
                placeholder = { Text("12", color = THEME.textMuted) },
                colors = textFieldColors(),
                shape = textFieldShape,
                singleLine = true,
                textStyle = LocalTextStyle.current.copy(
                    textAlign = TextAlign.Center,
                    fontSize = UI.titleSize,
                    fontWeight = FontWeight.Bold
                ),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )

            Spacer(Modifier.height(UI.defaultSpacing))

            Text(
                text = "nedelja",
                color = THEME.textMuted,
                fontSize = UI.bodySize
            )

            Spacer(Modifier.height(UI.largeSpacing))

            Text(
                text = if (hasDurationError) {
                    "Maksimalno trajanje je 104 nedelje (2 godine)"
                } else {
                    "Dozvoljen opseg je 1-104 nedelje."
                },
                color = if (hasDurationError) THEME.danger else THEME.textMuted,
                fontSize = UI.captionSize,
                textAlign = TextAlign.Center
            )
        }

        Spacer(Modifier.height(UI.xlSpacing))

        // Navigation buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(UI.mediumSpacing)
        ) {
            OutlinedButton(
                onClick = onBack,
                modifier = Modifier
                    .weight(1f)
                    .height(UI.buttonHeight),
                shape = buttonShape,
                colors = ButtonDefaults.outlinedButtonColors(contentColor = THEME.textMuted)
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, null, Modifier.size(18.dp))
                Spacer(Modifier.width(UI.smallSpacing))
                Text("Nazad")
            }

            Button(
                onClick = onNext,
                modifier = Modifier
                    .weight(1f)
                    .height(UI.buttonHeight),
                enabled = duration.isNotBlank() && (duration.toIntOrNull() ?: 0) in 1..104,
                shape = buttonShape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = THEME.accentPrimary,
                    contentColor = THEME.textDark
                )
            ) {
                Text("Dalje", fontWeight = FontWeight.Bold)
                Spacer(Modifier.width(UI.smallSpacing))
                Icon(Icons.AutoMirrored.Filled.ArrowForward, null, Modifier.size(18.dp))
            }
        }

        Spacer(Modifier.height(UI.largeSpacing))
    }
}

// =============================================================================
// STEP 3: SELECT SUBSTANCES
// =============================================================================

@Composable
private fun SelectSubstancesStep(
    allSubstances: List<Substance>,
    categories: List<String>,
    selectedIds: Set<String>,
    onSelectionChange: (String, Boolean) -> Unit,
    onBack: () -> Unit,
    onNext: () -> Unit,
    canProceed: Boolean
) {
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        Text(
            text = "Izaberite supstance",
            color = THEME.textPrimary,
            fontSize = UI.titleSize,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(
                start = UI.xlSpacing,
                top = UI.xlSpacing,
                end = UI.xlSpacing,
                bottom = UI.largeSpacing
            )
        )

        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(
                start = UI.largeSpacing,
                end = UI.largeSpacing,
                bottom = UI.mediumSpacing
            )
        ) {
            categories.forEach { category ->
                val substancesInCategory = allSubstances.filter { it.category == category }

                item {
                    Text(
                        text = category,
                        color = THEME.getCategoryColor(category),
                        fontSize = UI.captionSize,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(
                            top = UI.defaultSpacing,
                            bottom = UI.smallSpacing
                        )
                    )
                }

                items(substancesInCategory) { substance ->
                    val isSelected = substance.id in selectedIds

                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = UI.smallSpacing)
                            .semantics(mergeDescendants = true) {}
                            .toggleable(
                                value = isSelected,
                                role = Role.Checkbox,
                                onValueChange = { onSelectionChange(substance.id, it) }
                            ),
                        shape = textFieldShape,
                        color = if (isSelected) {
                            THEME.accentPrimary.copy(alpha = 0.16f)
                        } else {
                            THEME.bgCardElevated
                        },
                        border = if (isSelected) {
                            BorderStroke(1.dp, THEME.accentPrimary)
                        } else {
                            BorderStroke(1.dp, THEME.glassBorder)
                        }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = UI.buttonHeight)
                                .padding(horizontal = UI.mediumSpacing, vertical = UI.smallSpacing),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = isSelected,
                                onCheckedChange = null,
                                colors = CheckboxDefaults.colors(
                                    checkedColor = THEME.accentPrimary,
                                    uncheckedColor = THEME.textMuted,
                                    checkmarkColor = THEME.textDark
                                )
                            )

                            Spacer(Modifier.width(UI.defaultSpacing))

                            Column(modifier = Modifier.weight(1f)) {
                                val concentrationUnit = if (OralDosingSchedule.isOralSteroid(substance.id)) "mg/tab" else "mg/mL"
                                Text(
                                    text = substance.shortName,
                                    color = THEME.textPrimary,
                                    fontSize = UI.bodySize,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = "${substance.defaultConcentration.toInt()}$concentrationUnit",
                                    color = THEME.textMuted,
                                    fontSize = UI.captionSize
                                )
                            }

                            if (isSelected) {
                                Spacer(Modifier.width(UI.defaultSpacing))
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = THEME.accentPrimary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }

                item {
                    Spacer(Modifier.height(UI.mediumSpacing))
                }
            }
        }

        // Navigation buttons
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(UI.xlSpacing),
            horizontalArrangement = Arrangement.spacedBy(UI.mediumSpacing)
        ) {
            OutlinedButton(
                onClick = onBack,
                modifier = Modifier
                    .weight(1f)
                    .height(UI.buttonHeight),
                shape = buttonShape,
                colors = ButtonDefaults.outlinedButtonColors(contentColor = THEME.textMuted)
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, null, Modifier.size(18.dp))
                Spacer(Modifier.width(UI.smallSpacing))
                Text("Nazad")
            }

            Button(
                onClick = onNext,
                modifier = Modifier
                    .weight(1f)
                    .height(UI.buttonHeight),
                enabled = canProceed,
                shape = buttonShape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = THEME.accentPrimary,
                    contentColor = THEME.textDark
                )
            ) {
                Text("Dalje (${selectedIds.size})", fontWeight = FontWeight.Bold)
                Spacer(Modifier.width(UI.smallSpacing))
                Icon(Icons.AutoMirrored.Filled.ArrowForward, null, Modifier.size(18.dp))
            }
        }
    }
}

// =============================================================================
// STEP 4: CONFIGURE SUBSTANCE
// =============================================================================

@Composable
private fun ConfigureSubstanceStep(
    config: SubstanceConfig,
    substanceIndex: Int,
    totalSubstances: Int,
    durationWeeks: Int,
    onConfigChange: (SubstanceConfig) -> Unit,
    onBack: () -> Unit,
    onNext: () -> Unit
) {
    var showAdvanced by remember(config.substance.id) { mutableStateOf(config.weeklyDoses.isNotEmpty()) }
    var expanded by remember(config.substance.id) { mutableStateOf(false) }
    var startWeekExpanded by remember(config.substance.id) { mutableStateOf(false) }
    var endWeekExpanded by remember(config.substance.id) { mutableStateOf(false) }

    // Get oral dosing schedule and allowed frequencies
    val oralSchedule = OralDosingSchedule.forSubstance(config.substance.id)
    val allowedFrequencies = OralDosingSchedule.getAllowedFrequencies(config.substance.id)
    val isOral = OralDosingSchedule.isOralSteroid(config.substance.id)

    // Auto-set frequency to ED if oral and not already ED
    LaunchedEffect(config.substance.id) {
        if (isOral && config.frequency != InjectionFrequency.ED) {
            onConfigChange(config.copy(frequency = InjectionFrequency.ED))
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(UI.xlSpacing)
    ) {
        // Substance header
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = textFieldShape,
            color = THEME.bgCardElevated,
            border = BorderStroke(1.dp, THEME.glassBorder)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(UI.largeSpacing),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .background(THEME.getCategoryColor(config.substance.category), CircleShape)
                )
                Spacer(Modifier.width(UI.defaultSpacing))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = config.substance.shortName,
                        color = THEME.textPrimary,
                        fontSize = UI.titleSize,
                        fontWeight = FontWeight.Bold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = config.substance.category,
                        color = THEME.textMuted,
                        fontSize = UI.captionSize,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Spacer(Modifier.width(UI.mediumSpacing))
                Surface(
                    shape = RoundedCornerShape(999.dp),
                    color = THEME.accentPrimary.copy(alpha = 0.12f),
                    border = BorderStroke(1.dp, THEME.accentPrimary.copy(alpha = 0.35f))
                ) {
                    Text(
                        text = "Supstanca $substanceIndex od $totalSubstances",
                        color = THEME.accentPrimary,
                        fontSize = UI.captionSize,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(horizontal = UI.defaultSpacing, vertical = UI.smallSpacing)
                    )
                }
            }
        }

        // Oral steroid notification info
        oralSchedule?.let { schedule ->
            Spacer(Modifier.height(UI.mediumSpacing))
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = textFieldShape,
                color = THEME.bgCard,
                border = BorderStroke(1.dp, THEME.accentPrimary.copy(alpha = 0.35f))
            ) {
                Row(
                    modifier = Modifier.padding(UI.largeSpacing),
                    verticalAlignment = Alignment.Top
                ) {
                    Surface(
                        shape = CircleShape,
                        color = THEME.accentPrimary.copy(alpha = 0.14f)
                    ) {
                        Icon(
                            Icons.Default.Notifications,
                            contentDescription = null,
                            tint = THEME.accentPrimary,
                            modifier = Modifier.padding(UI.defaultSpacing).size(18.dp)
                        )
                    }
                    Spacer(Modifier.width(UI.mediumSpacing))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Podsetnici za tablete",
                            color = THEME.accentPrimary,
                            fontSize = UI.bodySize,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(Modifier.height(UI.smallSpacing))
                        Text(
                            text = "Dobićeš ${schedule.dosesPerDay} podsetnika dnevno:",
                            color = THEME.textPrimary,
                            fontSize = UI.captionSize
                        )
                        Text(
                            text = schedule.dosingTimes.joinToString(", ") { "${it}:00" },
                            color = THEME.accentPrimary,
                            fontSize = UI.bodySize,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "(Poluživot: ${schedule.halfLifeHours}h)",
                            color = THEME.textMuted,
                            fontSize = UI.captionSize
                        )
                    }
                }
            }
        }

        // Dosing tip for oral steroids and other special substances
        config.substance.dosingTip?.let { tip ->
            Spacer(Modifier.height(UI.mediumSpacing))
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = textFieldShape,
                color = THEME.bgCard,
                border = BorderStroke(1.dp, THEME.accentSecondary.copy(alpha = 0.35f))
            ) {
                Row(
                    modifier = Modifier.padding(UI.largeSpacing),
                    verticalAlignment = Alignment.Top
                ) {
                    Surface(
                        shape = CircleShape,
                        color = THEME.accentSecondary.copy(alpha = 0.14f)
                    ) {
                        Icon(
                            Icons.Default.Info,
                            contentDescription = null,
                            tint = THEME.accentSecondary,
                            modifier = Modifier.padding(UI.defaultSpacing).size(18.dp)
                        )
                    }
                    Spacer(Modifier.width(UI.mediumSpacing))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Preporuka za doziranje",
                            color = THEME.accentSecondary,
                            fontSize = UI.bodySize,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(Modifier.height(UI.smallSpacing))
                        Text(
                            text = tip,
                            color = THEME.textPrimary,
                            fontSize = UI.captionSize,
                            lineHeight = 18.sp
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(UI.xlSpacing))

        // Week range selection (for oral steroids like kickstarter or finisher)
        WizardConfigSection(label = "Period korišćenja") {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(UI.mediumSpacing)
            ) {
                // Start week
                Column(modifier = Modifier.weight(1f)) {
                    Text("Od nedelje", color = THEME.textMuted, fontSize = UI.captionSize)
                    Spacer(Modifier.height(UI.smallSpacing))
                    Box {
                        OutlinedButton(
                            onClick = { startWeekExpanded = true },
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = UI.buttonHeight),
                            shape = buttonShape,
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = THEME.textPrimary)
                        ) {
                            Text("${config.startWeek}")
                            Spacer(Modifier.weight(1f))
                            Icon(Icons.Default.ArrowDropDown, null, Modifier.size(18.dp))
                        }
                        DropdownMenu(expanded = startWeekExpanded, onDismissRequest = { startWeekExpanded = false }) {
                            (1..durationWeeks).forEach { week ->
                                DropdownMenuItem(
                                    text = { Text("Nedelja $week") },
                                    onClick = {
                                        val newEnd = if (config.endWeek != null && week > config.endWeek!!) week else config.endWeek
                                        onConfigChange(config.copy(startWeek = week, endWeek = newEnd))
                                        startWeekExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                // End week
                Column(modifier = Modifier.weight(1f)) {
                    Text("Do nedelje", color = THEME.textMuted, fontSize = UI.captionSize)
                    Spacer(Modifier.height(UI.smallSpacing))
                    Box {
                        OutlinedButton(
                            onClick = { endWeekExpanded = true },
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = UI.buttonHeight),
                            shape = buttonShape,
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = THEME.textPrimary)
                        ) {
                            Text("${config.endWeek ?: durationWeeks}")
                            Spacer(Modifier.weight(1f))
                            Icon(Icons.Default.ArrowDropDown, null, Modifier.size(18.dp))
                        }
                        DropdownMenu(expanded = endWeekExpanded, onDismissRequest = { endWeekExpanded = false }) {
                            (config.startWeek..durationWeeks).forEach { week ->
                                DropdownMenuItem(
                                    text = { Text("Nedelja $week") },
                                    onClick = {
                                        onConfigChange(config.copy(endWeek = if (week == durationWeeks) null else week))
                                        endWeekExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(UI.largeSpacing))

        // Dose input - different label for orals (daily) vs injectables (weekly)
        val doseLabel = if (isOral) "Dnevna doza (mg/dan)" else "Nedeljna doza (mg/ned)"
        val recommendedDose = if (isOral) config.substance.recommendedDose else config.substance.recommendedWeeklyDose
        val dosePlaceholder = if (recommendedDose > 0) recommendedDose.toInt().toString() else "500"

        // Za oralne se interno čuva nedeljna doza, a prikazuje/unosi dnevna.
        // Sirovi string je izvor istine za polje - decimale (npr. 2.5mg) ne smeju
        // da se gube kroz konverziju niti da se polje prepisuje usred kucanja.
        var doseInput by remember(config.substance.id) {
            mutableStateOf(
                if (isOral) {
                    val weekly = config.weeklyDoseMg.toDoubleOrNull() ?: 0.0
                    if (weekly > 0) formatDoseNumber(weekly / 7.0) else ""
                } else {
                    config.weeklyDoseMg
                }
            )
        }

        WizardConfigSection(label = doseLabel, accentColor = THEME.accentPrimary) {
            if (recommendedDose > 0) {
                Text(
                    "Preporuka: ${recommendedDose.toInt()}mg",
                    color = THEME.accentPrimary,
                    fontSize = UI.captionSize,
                    fontWeight = FontWeight.Medium
                )
                Spacer(Modifier.height(UI.smallSpacing))
            }
            OutlinedTextField(
                value = doseInput,
                onValueChange = { input ->
                    normalizeDecimalInput(input)?.let { normalized ->
                        doseInput = normalized
                        // Za oralne: dnevni unos se čuva kao nedeljna doza (Double matematika)
                        val newValue = if (isOral) {
                            val daily = normalized.toDoubleOrNull() ?: 0.0
                            if (daily > 0) (daily * 7.0).toString() else ""
                        } else {
                            normalized
                        }
                        onConfigChange(config.copy(weeklyDoseMg = newValue))
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text(dosePlaceholder, color = THEME.textMuted) },
                colors = textFieldColors(),
                shape = textFieldShape,
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
        }

        Spacer(Modifier.height(UI.largeSpacing))

        // Frequency - filtered based on substance type
        WizardConfigSection(label = "Frekvencija") {
            if (allowedFrequencies.size == 1) {
                // Locked frequency (e.g., oral steroids are always ED)
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = textFieldShape,
                    color = THEME.bgCardElevated,
                    border = BorderStroke(1.dp, THEME.glassBorder)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = UI.buttonHeight)
                            .padding(horizontal = UI.mediumSpacing, vertical = UI.smallSpacing),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Lock,
                            contentDescription = null,
                            tint = THEME.textMuted,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(UI.defaultSpacing))
                        Text(
                            text = allowedFrequencies.first().displayName,
                            color = THEME.textPrimary,
                            fontSize = UI.bodySize,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(Modifier.weight(1f))
                        Text(
                            text = if (isOral) "Obavezno za oralne" else "Fiksno",
                            color = THEME.textMuted,
                            fontSize = UI.captionSize
                        )
                    }
                }
            } else {
                // Dropdown with filtered frequencies
                Box {
                    OutlinedButton(
                        onClick = { expanded = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = UI.buttonHeight),
                        shape = buttonShape,
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = THEME.textPrimary)
                    ) {
                        Text(config.frequency.displayName)
                        Spacer(Modifier.weight(1f))
                        Icon(Icons.Default.ArrowDropDown, null)
                    }
                    DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        allowedFrequencies.forEach { freq ->
                            DropdownMenuItem(
                                text = { Text("${freq.displayName} (${String.format(Locale.getDefault(), "%.1f", freq.injectionsPerWeek)}x/ned)") },
                                onClick = {
                                    onConfigChange(config.copy(frequency = freq))
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(UI.largeSpacing))

        // Concentration - different label for orals vs injectables
        val concLabel = if (isOral) "Jačina tablete (mg/tab)" else "Koncentracija (mg/mL)"
        val concPlaceholder = if (isOral) "10" else "250"

        WizardConfigSection(label = concLabel) {
            OutlinedTextField(
                value = config.concentrationMgMl,
                onValueChange = { input ->
                    normalizeDecimalInput(input)?.let { onConfigChange(config.copy(concentrationMgMl = it)) }
                },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text(concPlaceholder, color = THEME.textMuted) },
                colors = textFieldColors(),
                shape = textFieldShape,
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
        }

        Spacer(Modifier.height(UI.xlSpacing))

        // Advanced: per-week dosing
        Surface(
            modifier = Modifier
                .fillMaxWidth(),
            shape = textFieldShape,
            color = THEME.bgCard,
            border = BorderStroke(
                1.dp,
                if (showAdvanced) THEME.accentSecondary.copy(alpha = 0.45f) else THEME.glassBorder
            )
        ) {
            Column(modifier = Modifier.padding(UI.largeSpacing)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .semantics(mergeDescendants = true) {}
                        .toggleable(
                            value = showAdvanced,
                            role = Role.Checkbox,
                            onValueChange = { showAdvanced = it }
                        ),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = showAdvanced,
                        onCheckedChange = null,
                        colors = CheckboxDefaults.colors(
                            checkedColor = THEME.accentSecondary,
                            uncheckedColor = THEME.textMuted
                        )
                    )
                    Spacer(Modifier.width(UI.defaultSpacing))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "Različite doze po nedeljama",
                            color = THEME.textPrimary,
                            fontSize = UI.bodySize,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            "Prazno polje koristi osnovnu dozu; popunite samo nedelje koje se razlikuju.",
                            color = THEME.textMuted,
                            fontSize = UI.captionSize,
                            lineHeight = 16.sp
                        )
                    }
                }

                if (showAdvanced) {
                    Spacer(Modifier.height(UI.largeSpacing))

                    Text(
                        "Prilagođena vrednost se primenjuje samo na tu nedelju.",
                        color = THEME.textMuted,
                        fontSize = UI.captionSize
                    )

                    Spacer(Modifier.height(UI.defaultSpacing))

                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(UI.defaultSpacing)
                    ) {
                        for (week in 1..durationWeeks) {
                            val weekDose = config.weeklyDoses[week] ?: ""
                            val hasCustomDose = weekDose.isNotBlank()

                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.width(76.dp)
                            ) {
                                Text(
                                    text = "N$week",
                                    color = if (hasCustomDose) THEME.accentSecondary else THEME.textMuted,
                                    fontSize = UI.captionSize,
                                    fontWeight = if (hasCustomDose) FontWeight.Bold else FontWeight.Normal
                                )
                                Spacer(Modifier.height(UI.smallSpacing))
                                OutlinedTextField(
                                    value = weekDose,
                                    onValueChange = { newVal ->
                                        val newDoses = config.weeklyDoses.toMutableMap()
                                        if (newVal.isBlank()) {
                                            newDoses.remove(week)
                                        } else {
                                            normalizeDecimalInput(newVal)?.let { newDoses[week] = it }
                                        }
                                        onConfigChange(config.copy(weeklyDoses = newDoses))
                                    },
                                    modifier = Modifier.width(76.dp),
                                    placeholder = {
                                        Text(
                                            config.weeklyDoseMg.ifBlank { "0" },
                                            color = THEME.textMuted.copy(alpha = 0.55f),
                                            fontSize = UI.captionSize,
                                            textAlign = TextAlign.Center
                                        )
                                    },
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedTextColor = THEME.textPrimary,
                                        unfocusedTextColor = THEME.textPrimary,
                                        focusedBorderColor = if (hasCustomDose) THEME.accentSecondary else THEME.accentPrimary,
                                        unfocusedBorderColor = if (hasCustomDose) THEME.accentSecondary else THEME.glassBorder,
                                        focusedContainerColor = THEME.bgInput,
                                        unfocusedContainerColor = THEME.bgInput,
                                        cursorColor = THEME.accentPrimary
                                    ),
                                    shape = RoundedCornerShape(8.dp),
                                    singleLine = true,
                                    textStyle = LocalTextStyle.current.copy(
                                        textAlign = TextAlign.Center,
                                        fontSize = UI.captionSize
                                    ),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(Modifier.weight(1f))

        // Navigation buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(UI.mediumSpacing)
        ) {
            OutlinedButton(
                onClick = onBack,
                modifier = Modifier
                    .weight(1f)
                    .height(UI.buttonHeight),
                shape = buttonShape,
                colors = ButtonDefaults.outlinedButtonColors(contentColor = THEME.textMuted)
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, null, Modifier.size(18.dp))
                Spacer(Modifier.width(UI.smallSpacing))
                Text("Nazad")
            }

            Button(
                onClick = onNext,
                modifier = Modifier
                    .weight(1f)
                    .height(UI.buttonHeight),
                enabled = (config.weeklyDoseMg.toDoubleOrNull() ?: 0.0) > 0.0 &&
                        (config.concentrationMgMl.toDoubleOrNull() ?: 0.0) > 0.0,
                shape = buttonShape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = THEME.accentPrimary,
                    contentColor = THEME.textDark
                )
            ) {
                Text("Dalje", fontWeight = FontWeight.Bold)
                Spacer(Modifier.width(UI.smallSpacing))
                Icon(Icons.AutoMirrored.Filled.ArrowForward, null, Modifier.size(18.dp))
            }
        }
    }
}

@Composable
private fun WizardConfigSection(
    label: String,
    accentColor: Color? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = textFieldShape,
        color = THEME.bgCard,
        border = BorderStroke(1.dp, accentColor?.copy(alpha = 0.35f) ?: THEME.glassBorder)
    ) {
        Column(modifier = Modifier.padding(UI.largeSpacing)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                accentColor?.let { color ->
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .background(color, CircleShape)
                    )
                    Spacer(Modifier.width(UI.defaultSpacing))
                }
                Text(
                    text = label,
                    color = accentColor ?: THEME.textSecondary,
                    fontSize = UI.captionSize,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(Modifier.height(UI.mediumSpacing))
            content()
        }
    }
}

// =============================================================================
// STEP 5: REVIEW
// =============================================================================

@Composable
private fun ReviewStep(
    cycleName: String,
    durationWeeks: String,
    configs: List<SubstanceConfig>,
    onBack: () -> Unit,
    onCreate: () -> Unit
) {
    val durationInt = durationWeeks.toIntOrNull() ?: 0
    val canCreate = cycleName.isNotBlank() &&
            durationInt in 1..104 &&
            configs.isNotEmpty() &&
            configs.all { (it.weeklyDoseMg.toDoubleOrNull() ?: 0.0) > 0.0 &&
                         (it.concentrationMgMl.toDoubleOrNull() ?: 0.0) > 0.0 }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(UI.xlSpacing)
    ) {
        Text(
            text = "Pregled ciklusa",
            color = THEME.textPrimary,
            fontSize = UI.titleSize,
            fontWeight = FontWeight.Bold
        )

        Spacer(Modifier.height(UI.xlSpacing))

        // Cycle info card
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = textFieldShape,
            color = THEME.bgCard
        ) {
            Column(modifier = Modifier.padding(UI.largeSpacing)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(UI.mediumSpacing),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Naziv:", color = THEME.textMuted, fontSize = UI.bodySize)
                    Text(
                        text = cycleName,
                        color = THEME.textPrimary,
                        fontSize = UI.bodySize,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.End,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Spacer(Modifier.height(UI.defaultSpacing))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Trajanje:", color = THEME.textMuted, fontSize = UI.bodySize)
                    Text("$durationWeeks nedelja", color = THEME.textPrimary, fontSize = UI.bodySize, fontWeight = FontWeight.Medium)
                }
            }
        }

        Spacer(Modifier.height(UI.largeSpacing))

        Text(
            text = "Supstance (${configs.size})",
            color = THEME.textSecondary,
            fontSize = UI.captionSize,
            fontWeight = FontWeight.Bold
        )

        Spacer(Modifier.height(UI.defaultSpacing))

        // Substances list
        val cycleDuration = durationWeeks.toIntOrNull() ?: 12
        configs.forEach { config ->
            val effectiveEndWeek = config.endWeek ?: cycleDuration
            val weekRangeText = if (config.startWeek == 1 && effectiveEndWeek == cycleDuration) {
                "Ceo ciklus"
            } else {
                "N${config.startWeek}-N${effectiveEndWeek}"
            }

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = UI.smallSpacing),
                shape = textFieldShape,
                color = THEME.bgCard
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(UI.mediumSpacing),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(THEME.getCategoryColor(config.substance.category), CircleShape)
                    )
                    Spacer(Modifier.width(UI.defaultSpacing))
                    Column(modifier = Modifier.weight(1f)) {
                        val isOralSubstance = OralDosingSchedule.isOralSteroid(config.substance.id)
                        val doseDisplay = if (isOralSubstance) {
                            val daily = (config.weeklyDoseMg.toDoubleOrNull() ?: 0.0) / 7.0
                            "${formatDoseNumber(daily)}mg/dan"
                        } else {
                            "${config.weeklyDoseMg}mg/ned"
                        }

                        Text(
                            text = config.substance.shortName,
                            color = THEME.textPrimary,
                            fontSize = UI.bodySize,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "$doseDisplay | ${config.frequency.displayName}",
                            color = THEME.textMuted,
                            fontSize = UI.captionSize
                        )
                        Text(
                            text = weekRangeText,
                            color = if (weekRangeText == "Ceo ciklus") THEME.textMuted else THEME.accentSecondary,
                            fontSize = UI.smallSize
                        )
                        if (config.weeklyDoses.isNotEmpty()) {
                            Text(
                                text = "Prilagođene doze: ${config.weeklyDoses.size} nedelja",
                                color = THEME.accentSecondary,
                                fontSize = UI.smallSize
                            )
                        }
                    }
                    val concSuffix = if (OralDosingSchedule.isOralSteroid(config.substance.id)) "mg/tab" else "mg/mL"
                    Text(
                        text = "${config.concentrationMgMl}$concSuffix",
                        color = THEME.textMuted,
                        fontSize = UI.captionSize
                    )
                }
            }
        }

        Spacer(Modifier.weight(1f))

        // Navigation buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(UI.mediumSpacing)
        ) {
            OutlinedButton(
                onClick = onBack,
                modifier = Modifier
                    .weight(1f)
                    .height(UI.buttonHeight),
                shape = buttonShape,
                colors = ButtonDefaults.outlinedButtonColors(contentColor = THEME.textMuted)
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, null, Modifier.size(18.dp))
                Spacer(Modifier.width(UI.smallSpacing))
                Text("Nazad")
            }

            Button(
                onClick = onCreate,
                modifier = Modifier
                    .weight(1f)
                    .height(UI.buttonHeight),
                enabled = canCreate,
                shape = buttonShape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = THEME.accentPrimary,
                    contentColor = THEME.textDark
                )
            ) {
                Icon(Icons.Default.Check, null, Modifier.size(18.dp))
                Spacer(Modifier.width(UI.smallSpacing))
                Text("Kreiraj ciklus", fontWeight = FontWeight.Bold)
            }
        }
    }
}
