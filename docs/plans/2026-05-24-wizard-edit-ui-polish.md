# Wizard/Edit UI Polish Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers-optimized:subagent-driven-development (recommended) or superpowers-optimized:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Polish the cycle wizard and compound edit/add flows so they look clearer and more professional while keeping dosing behavior unchanged.

**Architecture:** This is a UI-only Compose polish pass limited to wizard/edit surfaces. Existing state, repository calls, validation, dose calculations, generated data, scheduling calls, and model semantics remain unchanged. Reuse the existing dark Material 3 style, `THEME`, `UI`, `GlassCard`, cyan/orange accents, `buttonShape`, `textFieldShape`, `textFieldColors`, and the existing 8dp spacing rhythm.

**Tech Stack:** Kotlin, Jetpack Compose, Material 3, Android Gradle.

**Assumptions:**
- Assumes the requested polish can be completed inside `CycleWizardScreen.kt` and `EditCompoundScreen.kt` — will NOT proceed to shared components unless an existing component is absolutely necessary for consistency and the change is limited to presentation.
- Assumes no screenshot/Compose UI test suite exists in this project — verification relies on Gradle unit tests and debug assembly.
- Assumes UI text can be adjusted for clarity without changing the meaning of existing values — will NOT rename domain concepts or alter dose semantics.

---

## Scope

**Modify only:**
- `app/src/main/java/com/injekcije/app/ui/CycleWizardScreen.kt`
- `app/src/main/java/com/injekcije/app/ui/screens/EditCompoundScreen.kt`

**Do not modify:**
- Repository, data models, JSON, backup/import/export, notifications, PCT, Settings, Dashboard, generated data, `SubstanceConfig`, `CycleCompound`, `DosePhase`, `OralDosingSchedule`, dose calculations, intervals, frequencies, weeks, or validation semantics.

**Verification commands after implementation:**
- `.\gradlew.bat testDebugUnitTest`
- `.\gradlew.bat assembleDebug`

**Stop conditions:**
- Stop if a visual change requires changing dose conversion, phase generation, validation rules, or repository behavior.
- Stop if a shared component change would affect Dashboard, Settings, Backup, Notifications, or PCT.
- Stop if a UI change needs a new library or a new design system.

---

## Task 1: Wizard shell i osnovni koraci

**Cilj:** Make the wizard shell and first three steps feel like a polished guided flow instead of a loose collection of controls.

**Fajlovi:**
- Modify: `app/src/main/java/com/injekcije/app/ui/CycleWizardScreen.kt`

**Konkretne UI promene:**
- [x] Polish `WizardHeader`: keep the same progress calculation, but improve title/progress hierarchy, keep the progress bar, and ensure the close icon has `contentDescription = "Zatvori"`.
- [x] In `NameStep` and `DurationStep`, keep the same state callbacks and validation gates, but group the title, field, helper/error text, and footer actions with consistent `UI.largeSpacing` / `UI.xlSpacing`.
- [x] In `SelectSubstancesStep`, keep the same category and selection data, but make each selectable row visually clearer with stable spacing, visible selected state, and at least 48dp effective touch height.
- [x] Keep footer buttons consistent: `OutlinedButton` / `Button`, `Modifier.height(UI.buttonHeight)`, existing `buttonShape`, cyan primary action, muted secondary action.
- [x] Keep icons inside labeled buttons decorative (`contentDescription = null`), because the button text already names the action.

**Šta ne sme da se promeni:**
- `WizardStep` order, `totalSteps`, `currentStepIndex`, selected substance set behavior, default cycle name, duration validation range `1..104`, and all `onNext` / `onBack` transitions.

**Test/provera:**
- Run `.\gradlew.bat assembleDebug`.
- Expected: build succeeds with no Compose compile errors.

**Očekivani rezultat:** The wizard opening, name, duration, and substance selection steps look structured, readable, and consistent with the existing dark Material style.

---

## Task 2: ConfigureSubstanceStep polish

**Cilj:** Make substance configuration easier to scan by separating input, recommendation, and locked/fixed information.

**Fajlovi:**
- Modify: `app/src/main/java/com/injekcije/app/ui/CycleWizardScreen.kt`

**Konkretne UI promene:**
- [x] Polish the substance header so long names do not crowd the step count; keep the category color dot and `Supstanca X od Y`.
- [x] Group oral reminder info and dosing tip into clearer informational surfaces using existing cyan/orange accents; keep their text meaning and visibility rules unchanged.
- [x] Group `Period korišćenja`, dose, frequency, and concentration into visually distinct sections with short section labels and existing `OutlinedTextField` / `OutlinedButton` patterns.
- [x] Ensure period dropdown buttons use `Modifier.heightIn(min = UI.buttonHeight)` and keep the existing dropdown ranges and `onConfigChange` calls unchanged.
- [x] Add `contentDescription` only to icon-only controls if any are introduced; decorative icons paired with visible labels remain `null`.
- [x] Keep footer buttons at `UI.buttonHeight` and keep enabled conditions exactly as currently defined.

**Šta ne sme da se promeni:**
- Daily-to-weekly oral dose conversion, `LaunchedEffect` ED frequency auto-set for oral substances, allowed frequency filtering, start/end week persistence, concentration persistence, and enabled state logic.

**Test/provera:**
- Run `.\gradlew.bat assembleDebug`.
- Expected: build succeeds and `ConfigureSubstanceStep` compiles without signature or state errors.

**Očekivani rezultat:** Configure step reads as a professional form with clear sections, while all existing data semantics and conversions remain untouched.

---

## Task 3: Weekly dose UI polish

**Cilj:** Make weekly dose customization and preview graphs readable and touch-friendly without changing phase behavior.

**Fajlovi:**
- Modify: `app/src/main/java/com/injekcije/app/ui/CycleWizardScreen.kt`
- Modify: `app/src/main/java/com/injekcije/app/ui/screens/EditCompoundScreen.kt`

**Konkretne UI promene:**
- [x] In `CycleWizardScreen.kt`, polish the advanced weekly dose row: clearer explanatory text, wider per-week input cells where needed, and readable labels using `UI.captionSize` instead of tiny text where readability suffers.
- [x] In `EditCompoundScreen.kt`, polish the weekly dose bar preview: increase week hit areas to at least 48dp effective width/height, increase 9sp dose/week labels to a readable existing size, and preserve selected/custom/base color meanings.
- [x] Keep the same horizontal scroll behavior, same week order, same selected week toggling, and same custom dose input flow.
- [x] Visually separate selected-week editing from chart preview using the existing elevated dark surface and cyan selected color.
- [x] Keep quick option chips visually secondary and preserve their current actions.
- [x] Add `contentDescription` for icon-only edit/reset/delete controls where the visible text does not already name the action.

**Šta ne sme da se promeni:**
- `weeklyDoses` map behavior, `selectedWeeks` toggling semantics, `DosePhase` creation, base dose fallback, front-load/taper calculations, custom phase merge behavior, and reset behavior.

**Test/provera:**
- Run `.\gradlew.bat testDebugUnitTest`.
- Run `.\gradlew.bat assembleDebug`.
- Expected: unit tests pass and debug build succeeds.

**Očekivani rezultat:** Weekly customization stops looking like a debug graph and becomes a readable editing surface with clear selected/custom/base states.

---

## Task 4: Edit/AddCompound dialogs polish

**Cilj:** Make edit and add dialogs consistent with the polished wizard while preserving all existing save/add/remove behavior.

**Fajlovi:**
- Modify: `app/src/main/java/com/injekcije/app/ui/screens/EditCompoundScreen.kt`

**Konkretne UI promene:**
- [x] Polish `EditCompoundDialog` header, input grouping, weekly preview section, destructive action, and footer buttons using existing `buttonShape`, `textFieldShape`, `textFieldColors`, and `UI.buttonHeight`.
- [x] Polish `AddCompoundDialog` header, substance picker, recommendation/info surfaces, dose/frequency/concentration sections, duration stepper, and footer buttons.
- [x] Increase duration stepper icon buttons from 36dp to at least 48dp and add content descriptions such as `Smanji početnu nedelju`, `Povećaj početnu nedelju`, `Smanji završnu nedelju`, and `Povećaj završnu nedelju`.
- [x] Keep empty state for unavailable substances, but make its icon decorative or described according to whether text already explains the state.
- [x] Keep destructive button visually distinct with existing danger color and keep confirmation dialog behavior unchanged.
- [x] Ensure all icon-only actions have `contentDescription`; icons inside labeled buttons can remain decorative.

**Šta ne sme da se promeni:**
- `repo.updateCompoundDose`, `repo.updateCompoundPhases`, `repo.removeCompoundFromCycle`, `repo.addCompoundToCycle`, notification rescheduling calls, allowed frequencies, recommended defaults, oral daily/weekly conversion, start/end week constraints, and all enabled conditions.

**Test/provera:**
- Run `.\gradlew.bat testDebugUnitTest`.
- Run `.\gradlew.bat assembleDebug`.
- Expected: unit tests pass and debug build succeeds.

**Očekivani rezultat:** Edit and add compound flows feel consistent, serious, and touch-friendly while preserving every existing data and scheduling behavior.

---

## Final Verification

- [x] Run `.\gradlew.bat testDebugUnitTest`
  - Expected: `BUILD SUCCESSFUL`
- [x] Run `.\gradlew.bat assembleDebug`
  - Expected: `BUILD SUCCESSFUL`
- [x] Scan changed files for accidental scope expansion:
  - Only `CycleWizardScreen.kt` and `EditCompoundScreen.kt` should change.
  - No repository, data, backup/import/export, notification, PCT, Settings, Dashboard, model, or generated data files should change.
- [x] Scan changed files for unfinished markers:
  - No unfinished marker comments or stub implementations introduced.

**Ready state:** The second UI polish wave is complete only when both Gradle commands succeed and the changed files remain inside the approved UI-only scope.
