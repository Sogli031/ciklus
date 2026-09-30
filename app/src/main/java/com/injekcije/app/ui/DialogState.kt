package com.injekcije.app.ui

import com.injekcije.app.data.InjectionRecord
import com.injekcije.app.data.NextInjectionInfo
import com.injekcije.app.data.CycleCompound
import com.injekcije.app.data.PCTGuideInfo

sealed class DialogState {
    data object None : DialogState()
    data object CycleSetup : DialogState()
    data object AddSubstance : DialogState()
    data object Settings : DialogState()
    data object CloudBackup : DialogState()
    data object History : DialogState()
    data object SiteMap : DialogState()
    data object Learning : DialogState()
    data class LearningCategory(val categoryId: String) : DialogState()
    data class LearningArticle(val categoryId: String, val articleId: String) : DialogState()
    data class LogInjection(val info: NextInjectionInfo? = null) : DialogState()
    data class LogOralDose(val substanceId: String, val substanceName: String, val dosePerServing: Double) : DialogState()
    data class EditInjection(val record: InjectionRecord) : DialogState()
    data class EditCompound(val compound: CycleCompound, val cycleDurationWeeks: Int) : DialogState()
    data class ConfirmDelete(val recordId: String) : DialogState()
    data class ConfirmDeleteOralDose(val recordId: String) : DialogState()
    data class ConfirmDeleteCompound(val compound: CycleCompound) : DialogState()
    data class AddCompound(val cycleDurationWeeks: Int) : DialogState()

    // PCT-related states
    data object PCTGuide : DialogState()
    data class PCTSetup(val cycleId: String) : DialogState()
    data class PCTDetails(val info: PCTGuideInfo) : DialogState()

    // Notification settings
    data object NotificationSettings : DialogState()
}
