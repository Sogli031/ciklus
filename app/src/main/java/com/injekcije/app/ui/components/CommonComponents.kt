package com.injekcije.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.injekcije.app.THEME
import com.injekcije.app.UI
import com.injekcije.app.Strings
import com.injekcije.app.data.InjectionStatus
import com.injekcije.app.data.getDisplayName
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.text.font.FontWeight
import java.time.format.DateTimeFormatter
import java.util.Locale

val DATE_TIME_FORMAT: DateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")
val TIME_FORMAT: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")
val SHORT_DATE_FORMAT: DateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yy")
val DATE_TIME_SHORT_FORMAT: DateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM HH:mm")

val textFieldShape = RoundedCornerShape(12.dp)
val buttonShape = RoundedCornerShape(12.dp)

@Composable
fun GlassCard(modifier: Modifier = Modifier, borderColor: Color = THEME.glassBorder, content: @Composable () -> Unit) {
    Card(modifier.border(1.dp, borderColor, RoundedCornerShape(UI.cardRadius)), RoundedCornerShape(UI.cardRadius), CardDefaults.cardColors(THEME.bgCard.copy(0.7f))) { content() }
}

@Composable
fun textFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedTextColor = THEME.textPrimary,
    unfocusedTextColor = THEME.textPrimary,
    focusedContainerColor = THEME.bgCardElevated,
    unfocusedContainerColor = THEME.bgCardElevated,
    cursorColor = THEME.accentPrimary,
    focusedBorderColor = THEME.accentPrimary,
    unfocusedBorderColor = THEME.glassBorder,
    focusedLabelColor = THEME.accentPrimary,
    unfocusedLabelColor = THEME.textMuted,
    selectionColors = TextSelectionColors(
        handleColor = THEME.accentPrimary,
        backgroundColor = THEME.accentPrimary.copy(alpha = 0.3f)
    )
)

@Composable
fun StatusBadge(status: InjectionStatus, strings: Strings) {
    Box(Modifier.background(status.color.copy(0.2f), RoundedCornerShape(4.dp)).padding(horizontal = 6.dp, vertical = 2.dp)) {
        Text(status.getDisplayName(strings), color = status.color, fontSize = UI.smallSize, fontWeight = FontWeight.Medium)
    }
}

fun formatSyncTime(timestamp: Long, strings: Strings): String {
    val now = System.currentTimeMillis()
    val diff = now - timestamp
    val seconds = diff / 1000
    val minutes = seconds / 60
    val hours = minutes / 60
    val days = hours / 24

    return when {
        seconds < 60 -> strings.justNow
        minutes < 60 -> "$minutes ${strings.minutesAgo}"
        hours < 24 -> "$hours ${strings.hoursAgo}"
        days == 1L -> strings.yesterday
        days < 7 -> "$days ${strings.daysAgo}"
        else -> {
            val date = java.text.SimpleDateFormat("dd.MM. HH:mm", Locale.getDefault())
            date.format(java.util.Date(timestamp))
        }
    }
}
