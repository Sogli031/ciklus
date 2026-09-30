# 3A Settings Polish Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers-optimized:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Polish the Settings dialog so it feels consistent, native, accessible, and serious while keeping all app behavior unchanged.

**Architecture:** This is a UI-only Compose polish pass limited to `SettingsScreen.kt` and already-used Serbian copy in `LocaleManager.kt`. It must preserve the current dark Material 3 theme, `THEME`, `UI`, `buttonShape`, 8dp spacing rhythm, and existing repository/backup/notification calls. No new design system, library, screen, state path, business rule, scheduling behavior, backup behavior, JSON format, PCT model, Dashboard, Wizard, or Edit flow changes are allowed.

**Tech Stack:** Kotlin, Jetpack Compose, Material 3, Android Gradle.

**Assumptions:**
- Assumes Settings polish can be completed in `SettingsScreen.kt` with small copy updates in `LocaleManager.kt` only -- will NOT work if a shared dialog framework or new component library is required.
- Assumes copy changes are presentation-only -- will NOT rename domain data, settings keys, backup/import/export semantics, or notification scheduling behavior.
- Assumes there are no Compose UI tests for this dialog -- verification relies on Gradle unit tests, debug assembly, and source scan.

---

## Files

- Modify: `app/src/main/java/com/injekcije/app/ui/screens/SettingsScreen.kt`
  - Responsibility: Settings dialog layout, active-cycle card, delete-cycle confirmation, navigation buttons to notification and cloud backup dialogs.
- Modify: `app/src/main/java/com/injekcije/app/LocaleManager.kt`
  - Responsibility: user-visible Serbian copy already consumed by the Settings dialog or its immediate settings rows.

## Non-Goals And Stop Conditions

Do not modify repository/data files, backup providers, JSON import/export, notification scheduling, notification channels, notification permission flow, alarm logic, PCT calculations/data/models/validation, Dashboard, Wizard/Edit screens, Gradle dependencies, or app architecture.

Stop if the requested visual polish requires changing `repo.deleteCycle(...)`, `cancelAllAlarms(...)`, `backup.signInState`, `onCloudBackup`, `onNotificationSettings`, dialog routing, persistence keys, or any condition that decides when behavior fires.

## Task 1: Settings Dialog Structure And Serbian Copy

**Goal:** Make `SettingsDialog` scan like a finished native settings surface with clear sections and Serbian user-visible labels.

**Files:**
- Modify: `app/src/main/java/com/injekcije/app/ui/screens/SettingsScreen.kt`
- Modify: `app/src/main/java/com/injekcije/app/LocaleManager.kt`

**Concrete UI/copy/accessibility changes:**
- Keep the same `Dialog`, `Surface`, dark colors, `buttonShape`, and existing navigation callbacks.
- Tighten Settings title, active-cycle section, notification row, cloud-backup row, version text, and close button spacing using existing `UI.*Spacing`.
- Keep row icons inside labeled rows decorative with `contentDescription = null`, because visible row text already names the action.
- Change user-visible copy only where Serbian/English is mixed in Settings context:
  - `cloudBackup`: `Cloud Backup` -> `Backup u cloudu`
  - `backup`: `Backup` -> `Napravi backup` only if this shared string is still acceptable for the later Backup/Cloud wave; otherwise leave it for 3B.
  - `backupSuccess`: `Backup uspešno sačuvan` -> `Backup je uspešno sačuvan` only if this string is touched in 3A; otherwise leave it for 3B.
- Do not change Settings row destinations or sign-in status display semantics.

**What must not change:**
- Do not change `onDismiss`, `onCloudBackup`, `onNotificationSettings`, `backup.signInState.collectAsState()`, active cycle lookup, or version value.
- Do not alter backup, import/export, notification scheduling, or repository behavior.

**Test/check:**
- After implementation, run `.\gradlew.bat assembleDebug`.
- Expected: debug build succeeds with no Compose compile errors.

**Expected result:** Settings dialog has clearer hierarchy and Serbian copy without changing any setting behavior.

## Task 2: Active Cycle Delete Action And Confirmation Dialog

**Goal:** Make deleting the active cycle visually clear, accessible, and touch-friendly while preserving the exact delete behavior.

**Files:**
- Modify: `app/src/main/java/com/injekcije/app/ui/screens/SettingsScreen.kt`

**Concrete UI/copy/accessibility changes:**
- Increase active-cycle delete `IconButton` target from `36.dp` to at least `UI.buttonHeight` / 48dp.
- Add icon-only `contentDescription = "Obriši aktivni ciklus"`.
- Keep the delete icon color as `THEME.danger`.
- Keep delete confirm dialog title/body/action semantics, but align button styling with existing Material pattern:
  - destructive primary: `strings.delete`
  - secondary/cancel: `strings.cancel`
  - clear title: `strings.deleteCycle`
- If copy is adjusted, keep the same meaning: deleting the cycle also deletes injections from that cycle and cannot be undone.

**What must not change:**
- Do not change the confirm action body: `repo.deleteCycle(activeCycle.id); cancelAllAlarms(context); showDeleteConfirm = false; onDismiss()`.
- Do not change when the confirm dialog appears or when the Settings dialog dismisses.
- Do not add extra confirmation steps or recovery behavior.

**Test/check:**
- Run `.\gradlew.bat assembleDebug`.
- Source-check that delete behavior calls are unchanged except formatting.
- Expected: build succeeds and delete path still compiles with the same repository/alarm calls.

**Expected result:** Delete is accessible to TalkBack, has a 48dp target, and still performs the exact same confirmed delete flow.

## Task 3: Final Verification And Scope Scan

**Goal:** Prove 3A stayed UI-only and did not touch unrelated app behavior.

**Files:**
- Check: `app/src/main/java/com/injekcije/app/ui/screens/SettingsScreen.kt`
- Check: `app/src/main/java/com/injekcije/app/LocaleManager.kt`

**Concrete verification steps:**
- Run `.\gradlew.bat testDebugUnitTest`
  - Expected: `BUILD SUCCESSFUL`.
- Run `.\gradlew.bat assembleDebug`
  - Expected: `BUILD SUCCESSFUL`.
- Run a manual source scan:
  - Only `SettingsScreen.kt` and, if copy is changed, `LocaleManager.kt` should be modified for this wave.
  - No changes in `app/src/main/java/com/injekcije/app/data/`.
  - No changes in `app/src/main/java/com/injekcije/app/data/backup/`.
  - No changes in `app/src/main/java/com/injekcije/app/notifications/`.
  - No changes in PCT models/calculation files.
  - No changes in Dashboard, Wizard, or Edit screens.
- Scan `SettingsScreen.kt` for icon-only actions in this wave:
  - Expected: active-cycle delete icon has a non-empty content description.
  - Expected: labeled row icons remain decorative.

**What must not change:**
- Do not add new tests for behavior unless implementation unexpectedly changes behavior; if behavior changes become necessary, stop and ask for a new approved scope.
- Do not commit, push, or modify Git workflow files.

**Expected result:** 3A is complete only when tests/build pass and the source scan confirms the change stayed inside Settings UI/copy/accessibility scope.

---

## Ready State

Use **Inline Execution** for this compact 3-task plan. Reply to start, or say `subagent` to switch execution mode.
