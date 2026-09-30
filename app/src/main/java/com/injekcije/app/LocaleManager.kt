package com.injekcije.app

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

enum class AppLanguage(val code: String, val displayName: String) {
    SERBIAN("sr", "Srpski")
}

val LocalStrings = compositionLocalOf<Strings> { SerbianStrings }

class LocaleManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("injekcije_prefs", Context.MODE_PRIVATE)

    var currentLanguage by mutableStateOf(loadLanguage())
        private set

    private fun loadLanguage(): AppLanguage {
        val code = prefs.getString("language", AppLanguage.SERBIAN.code)
        return AppLanguage.entries.find { it.code == code } ?: AppLanguage.SERBIAN
    }

    fun getStrings(): Strings = SerbianStrings
}

@Composable
fun ProvideStrings(localeManager: LocaleManager, content: @Composable () -> Unit) {
    val strings = remember(localeManager.currentLanguage) { localeManager.getStrings() }
    CompositionLocalProvider(LocalStrings provides strings) {
        content()
    }
}

// String resources interface
interface Strings {
    // App
    val appName: String
    val version: String

    // Main screen
    val injections: String
    val week: String
    val noActiveCycle: String
    val createCycleToStart: String
    val newCycle: String
    val nextInjection: String
    val noScheduled: String
    val nextSite: String
    val max: String
    val daysRest: String
    val neverUsed: String
    val clickToAdjust: String
    val logInjection: String
    val substancesInCycle: String
    val substances: String
    val recentInjections: String
    val perWeek: String
    val perDay: String
    val perInjection: String
    val perDose: String
    val nextDose: String
    val dosingSchedule: String
    val halfLife: String
    val hours: String
    val editDoses: String

    // Cycle setup
    val cycleName: String
    val duration: String
    val weeks: String
    val durationWeeks: String
    val addSubstance: String
    val added: String
    val substance: String
    val selectSubstance: String
    val weeklyDose: String
    val frequency: String
    val recommendation: String
    val concentration: String
    val differentDosesPerWeek: String
    val forFrontloadTapering: String
    val dosesPerWeek: String
    val enterDoseAutoFill: String
    val entered: String
    val auto: String
    val create: String
    val cancel: String
    val add: String

    // Log injection dialog
    val site: String
    val addAnotherSubstance: String
    val totalVolume: String
    val warning: String
    val exceedsMax: String
    val forSite: String
    val notesOptional: String
    val save: String
    val combined: String

    // History
    val injectionHistory: String
    val noInjections: String
    val close: String
    val total: String

    // Edit injection
    val editInjection: String
    val notes: String

    // Confirm delete
    val confirmDelete: String
    val deleteInjectionConfirm: String
    val deleteOralDoseConfirm: String
    val delete: String

    // Site map
    val injectionSites: String
    val selectSitesForRotation: String
    val rotation: String
    val site_s: String
    val sites: String
    val selectAtLeastOne: String

    // Settings
    val settings: String
    val activeCycle: String
    val deleteCycle: String
    val deleteCycleConfirm: String

    // Cloud backup
    val cloudBackup: String
    val notConnected: String
    val connectGoogle: String
    val signOut: String
    val backup: String
    val restore: String
    val syncing: String
    val lastSync: String
    val justNow: String
    val minutesAgo: String
    val yesterday: String
    val daysAgo: String
    val welcomeTitle: String
    val welcomeMessage: String
    val skip: String
    val dataLoaded: String
    val backupSuccess: String
    val backupError: String
    val restoreSuccess: String
    val restoreError: String

    // Tips
    val injectionTips: String
    val generalTips: String

    // Learning
    val learning: String
    val learningSubtitle: String

    // Edit compound
    val editDosing: String
    val baseDose: String
    val dosesPerWeeks: String
    val clickWeekToEdit: String
    val apply: String
    val quickOptions: String
    val frontLoad: String
    val taper: String
    val last: String
    val resetAll: String
    val baseDoseLabel: String
    val modified: String
    val cancelSelection: String

    // Injection status
    val onTrack: String
    val dueSoon: String
    val dueNow: String
    val overdue: String
    val awaitingFirst: String

    // Time display
    val inHours: String
    val hoursAgo: String
    val now: String
    val firstInjectionPending: String

    // Frequencies
    val freqDaily: String
    val freqEod: String
    val freqE3d: String
    val freqE35d: String
    val freqE5d: String
    val freqWeekly: String
    val freqE10d: String
    val freqBiweekly: String
    val timesPerWeek: String

    // Sites
    val gluteLeft: String
    val gluteRight: String
    val vgLeft: String
    val vgRight: String
    val deltLeft: String
    val deltRight: String
    val quadLeft: String
    val quadRight: String
    val pecLeft: String
    val pecRight: String

    // Site tips
    val gluteTip: String
    val vgTip: String
    val deltTip: String
    val quadTip: String
    val pecTip: String

    // General tips
    val tip1: String
    val tip2: String
    val tip3: String
    val tip4: String
    val tip5: String

    // Site specific tips
    val gluteTips: List<String>
    val vgTips: List<String>
    val deltTips: List<String>
    val quadTips: List<String>
    val pecTips: List<String>

    // Notification settings
    val notifications: String
    val injectionNotifications: String
    val oralNotifications: String
    val pctNotifications: String
    val fiveHoursBefore: String
    val threeHoursBefore: String
    val thirtyMinutesBefore: String
    val atTime: String
    val overdueNotification: String
    val repeatInterval: String
    val reminderTime: String
    val threeDaysBefore: String
    val startDay: String
    val bloodworkReminder: String
    val min30: String
    val hour1: String
    val hour2: String
    val hour3: String
    val hour4: String
}

object SerbianStrings : Strings {
    override val appName = "Ciklus"
    override val version = "Verzija"

    override val injections = "Injekcije"
    override val week = "Nedelja"
    override val noActiveCycle = "Nema aktivnog ciklusa"
    override val createCycleToStart = "Kreiraj novi ciklus da počneš sa praćenjem"
    override val newCycle = "Nov ciklus"
    override val nextInjection = "Sledeća injekcija"
    override val noScheduled = "Nema zakazanih"
    override val nextSite = "Sledeće mesto"
    override val max = "Max"
    override val daysRest = "dana odmora"
    override val neverUsed = "Nikad korišćeno"
    override val clickToAdjust = "Klikni za podešavanje"
    override val logInjection = "Unesi injekciju"
    override val substancesInCycle = "Supstance u ciklusu"
    override val substances = "supstanci"
    override val recentInjections = "Poslednje injekcije"
    override val perWeek = "mg/ned"
    override val perDay = "mg/dan"
    override val perInjection = "Po injekciji"
    override val perDose = "Po dozi"
    override val nextDose = "Sledeća doza"
    override val dosingSchedule = "Raspored"
    override val halfLife = "Poluvreme"
    override val hours = "h"
    override val editDoses = "Izmeni doze"

    override val cycleName = "Ime ciklusa"
    override val duration = "Trajanje"
    override val weeks = "nedelje"
    override val durationWeeks = "Trajanje (nedelje)"
    override val addSubstance = "Dodaj supstancu"
    override val added = "dodato"
    override val substance = "Supstanca"
    override val selectSubstance = "Izaberi supstancu"
    override val weeklyDose = "Nedeljna doza (mg)"
    override val frequency = "Frekvencija"
    override val recommendation = "Preporuka"
    override val concentration = "Koncentracija (mg/mL)"
    override val differentDosesPerWeek = "Različite doze po nedeljama"
    override val forFrontloadTapering = "Za front-load, tapering, itd."
    override val dosesPerWeek = "Doze po nedeljama"
    override val enterDoseAutoFill = "Unesi dozu za nedelju - automatski se popunjava do sledeće izmene"
    override val entered = "Uneto"
    override val auto = "Auto"
    override val create = "Kreiraj"
    override val cancel = "Otkaži"
    override val add = "Dodaj"

    override val site = "Mesto"
    override val addAnotherSubstance = "Dodaj još supstancu (isti špric)"
    override val totalVolume = "Ukupan volumen"
    override val warning = "Upozorenje"
    override val exceedsMax = "prelazi max"
    override val forSite = "za"
    override val notesOptional = "Beleške (opciono)"
    override val save = "Sačuvaj"
    override val combined = "Kombinovano"

    override val injectionHistory = "Istorija injekcija"
    override val noInjections = "Nema unetih injekcija"
    override val close = "Zatvori"
    override val total = "ukupno"

    override val editInjection = "Izmeni injekciju"
    override val notes = "Beleške"

    override val confirmDelete = "Potvrdi brisanje"
    override val deleteInjectionConfirm = "Da li ste sigurni da želite da obrišete ovu injekciju?"
    override val deleteOralDoseConfirm = "Da li ste sigurni da želite da obrišete ovu oralnu dozu?"
    override val delete = "Obriši"

    override val injectionSites = "Mesta za injekcije"
    override val selectSitesForRotation = "Označi mesta koja želiš u rotaciji"
    override val rotation = "Rotacija"
    override val site_s = "mesto"
    override val sites = "mesta"
    override val selectAtLeastOne = "Izaberi bar jedno mesto"

    override val settings = "Podešavanja"
    override val activeCycle = "Aktivni ciklus"
    override val deleteCycle = "Obriši ciklus?"
    override val deleteCycleConfirm = "Brisanjem ciklusa obrisaćete i sve injekcije iz tog ciklusa. Ova akcija se ne može poništiti."

    // Cloud backup
    override val cloudBackup = "Cloud Backup"
    override val notConnected = "Nije povezano"
    override val connectGoogle = "Povezi Google nalog"
    override val signOut = "Odjavi se"
    override val backup = "Backup"
    override val restore = "Vrati podatke"
    override val syncing = "Sinhronizacija..."
    override val lastSync = "Poslednji backup"
    override val justNow = "Upravo sada"
    override val minutesAgo = "min pre"
    override val yesterday = "Juče"
    override val daysAgo = "dana pre"
    override val welcomeTitle = "Dobro došli!"
    override val welcomeMessage = "Poveži svoj Google nalog da ručno praviš backup i po potrebi vratiš podatke iz cloud-a."
    override val skip = "Preskoči"
    override val dataLoaded = "Podaci učitani"
    override val backupSuccess = "Backup uspešno sačuvan"
    override val backupError = "Greška pri backup-u"
    override val restoreSuccess = "Podaci uspešno vraćeni"
    override val restoreError = "Greška pri vraćanju podataka"

    override val injectionTips = "Saveti za injekcije"
    override val generalTips = "Opšti saveti"

    override val learning = "Učenje"
    override val learningSubtitle = "Edukativni sadržaj o steroidima"

    override val editDosing = "Izmeni doziranje"
    override val baseDose = "Osnovna doza (mg/nedelja)"
    override val dosesPerWeeks = "Doze po nedeljama"
    override val clickWeekToEdit = "Klikni na nedelju da izmeniš dozu. Drži za više nedelja."
    override val apply = "Primeni"
    override val quickOptions = "Brze opcije"
    override val frontLoad = "Front-load"
    override val taper = "Taper"
    override val last = "zadnje"
    override val resetAll = "Resetuj sve"
    override val baseDoseLabel = "Osnovna"
    override val modified = "Izmenjena"
    override val cancelSelection = "Poništi izbor"

    override val onTrack = "Na vreme"
    override val dueSoon = "Uskoro"
    override val dueNow = "Sada"
    override val overdue = "Kasni"
    override val awaitingFirst = "Čeka se"

    override val inHours = "za %s"
    override val hoursAgo = "pre %s"
    override val now = "sada"
    override val firstInjectionPending = "Prva injekcija na čekanju"

    override val freqDaily = "Dnevno"
    override val freqEod = "Svaki drugi dan"
    override val freqE3d = "Svaka 3 dana"
    override val freqE35d = "2x nedeljno"
    override val freqE5d = "Svaki 5 dana"
    override val freqWeekly = "Nedeljno"
    override val freqE10d = "Svakih 10 dana"
    override val freqBiweekly = "Svake 2 nedelje"
    override val timesPerWeek = "x/ned"

    override val gluteLeft = "Gluteus L"
    override val gluteRight = "Gluteus D"
    override val vgLeft = "VG Levi"
    override val vgRight = "VG Desni"
    override val deltLeft = "Delt L"
    override val deltRight = "Delt D"
    override val quadLeft = "Quad L"
    override val quadRight = "Quad D"
    override val pecLeft = "Pec L"
    override val pecRight = "Pec D"

    override val gluteTip = "Gornji spolj. kvadrant, 3-4mL"
    override val vgTip = "Najbezbednije, 2-3mL"
    override val deltTip = "Spoljasnji deo, 1-1.5mL"
    override val quadTip = "Spoljna sredina, 2-3mL"
    override val pecTip = "Sredina grudi, 1-1.5mL"

    override val tip1 = "Uvek aspiriraj pre injekcije"
    override val tip2 = "Koristi 90 ugao za IM injekcije"
    override val tip3 = "Rotiraj mesta - min 14 dana odmora"
    override val tip4 = "Čistite mesto alkoholom"
    override val tip5 = "Igla 1-1.5\" (25-38mm) za IM"

    override val gluteTips = listOf(
        "Gornji spoljašnji kvadrant",
        "Može da primi 3-4 mL",
        "Izbegavaj išijatički nerv"
    )
    override val vgTips = listOf(
        "Najbezbednije mesto",
        "Malo nerava i krvnih sudova",
        "Može da primi 2-3 mL"
    )
    override val deltTips = listOf(
        "Spoljašnja strana ramena",
        "Max 1-1.5 mL",
        "Dobro za kratke estere"
    )
    override val quadTips = listOf(
        "Spoljna sredina butine",
        "Lako za samoadministraciju",
        "Može da bude bolnije"
    )
    override val pecTips = listOf(
        "Sredina grudi",
        "Max 1-1.5 mL",
        "Oprez sa nervima"
    )

    // Notification settings
    override val notifications = "Notifikacije"
    override val injectionNotifications = "Injekcije"
    override val oralNotifications = "Oralni steroidi"
    override val pctNotifications = "PCT"
    override val fiveHoursBefore = "5 sati pre"
    override val threeHoursBefore = "3 sata pre"
    override val thirtyMinutesBefore = "30 minuta pre"
    override val atTime = "Na vreme"
    override val overdueNotification = "Propuštena doza"
    override val repeatInterval = "Ponavljanje"
    override val reminderTime = "Vreme podsetnika"
    override val threeDaysBefore = "3 dana pre"
    override val startDay = "Dan početka"
    override val bloodworkReminder = "Krvna slika"
    override val min30 = "30min"
    override val hour1 = "1h"
    override val hour2 = "2h"
    override val hour3 = "3h"
    override val hour4 = "4h"
}
