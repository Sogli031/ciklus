# Project Map
_Generated: 2026-05-12 22:44 | Staleness: timestamps_

## Directory Structure
app/ — Android aplikacija za praćenje ciklusa, injekcija, oralnih doza, PCT-a, notifikacija i backup-a.
app/src/main/java/com/injekcije/app/data/ — modeli ciklusa, repozitorijum, naučni/pomoćni podaci i Google Drive backup.
app/src/main/java/com/injekcije/app/domain/ — čista kalkulacija doza i koncentracija.
app/src/main/java/com/injekcije/app/ui/ — Compose ekrani, dijalozi i zajedničke UI komponente.
app/src/main/java/com/injekcije/app/notifications/ — alarmi, boot receiver i prikaz reminder notifikacija.
app/src/test/ — unit testovi za doze, import/export, PCT i rotaciju mesta.
.planning/ — lokalni planerski artefakti; ne tretirati kao runtime source.

## Key Files
settings.gradle.kts — definiše Gradle root `Injekcije` i modul `:app`.
app/build.gradle.kts — Compose Android konfiguracija, AGP/Kotlin verzije i zavisnosti.
app/src/main/AndroidManifest.xml — permisije za internet, storage, tačne alarme, boot i receiver-e.
app/src/main/java/com/injekcije/app/MainActivity.kt — ulaz u Compose UI i temu.
app/src/main/java/com/injekcije/app/InjekcijeApp.kt — Application klasa i inicijalizacija crash log-a.
app/src/main/java/com/injekcije/app/data/Repository.kt — glavni persistence/import/export sloj za ciklus i evidenciju.
app/src/main/java/com/injekcije/app/data/Models.kt — centralni modeli za supstance, injekcije, oralne doze i statuse.
app/src/main/java/com/injekcije/app/domain/DoseCalculator.kt — poslovna logika za mg/ml i raspored doziranja.
app/src/main/java/com/injekcije/app/ui/MainScreen.kt — glavni Compose ekran aplikacije.
app/src/main/java/com/injekcije/app/notifications/Notifications.kt — zakazivanje, otkazivanje i prikaz notifikacija.
app/src/test/java/com/injekcije/app/domain/DoseCalculatorTest.kt — osnovna regresiona zaštita za doziranje.

## Critical Constraints
- Poštovati parent `AGENTS.md`: sve je lokalno, bez Git komandi i bez PR/backup Git toka.
- Korisnički tekst treba da ostane na srpskoj latinici sa dijakritikom gde se dodaje ili menja.
- Reminder logika zavisi od tačnih alarma i boot receiver-a; promene proveravati uz testove za raspored i notification path.
- Google Drive backup je deo korisničkog toka, pa import/export izmene moraju čuvati kompatibilnost JSON-a.

## Hot Files
app/src/main/java/com/injekcije/app/data/Repository.kt, app/src/main/java/com/injekcije/app/data/Models.kt, app/src/main/java/com/injekcije/app/domain/DoseCalculator.kt, app/src/main/java/com/injekcije/app/ui/MainScreen.kt, app/src/main/java/com/injekcije/app/notifications/Notifications.kt
