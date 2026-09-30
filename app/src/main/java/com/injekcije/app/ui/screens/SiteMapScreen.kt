package com.injekcije.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.injekcije.app.THEME
import com.injekcije.app.UI
import com.injekcije.app.Strings
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import com.injekcije.app.data.InjectionRepository
import com.injekcije.app.data.InjectionSite
import com.injekcije.app.data.getDisplayName
import com.injekcije.app.data.getTip
import com.injekcije.app.ui.components.buttonShape

@Composable
fun SiteMapDialog(repo: InjectionRepository, strings: Strings, onDismiss: () -> Unit) {
    val history = repo.getSiteHistory()
    var enabledSites by remember { mutableStateOf(repo.getEnabledSites()) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = THEME.bgCard,
            tonalElevation = 8.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(Modifier.padding(UI.xlSpacing)) {
                Text(strings.injectionSites, color = THEME.textPrimary, fontSize = UI.titleSize, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(UI.smallSpacing))
                Text(strings.selectSitesForRotation, color = THEME.textMuted, fontSize = UI.captionSize)

                Spacer(Modifier.height(UI.largeSpacing))

                val sortedHistory = remember(history) { history.sortedByDescending { it.daysSinceLastUse ?: Int.MAX_VALUE } }
                LazyColumn(Modifier.heightIn(max = 400.dp)) {
                    items(sortedHistory, key = { it.site.name }) { h ->
                        val isEnabled = h.site in enabledSites
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isEnabled) THEME.accentPrimary.copy(0.1f) else THEME.bgCardElevated,
                            onClick = {
                                enabledSites = if (isEnabled) {
                                    enabledSites - h.site
                                } else {
                                    enabledSites + h.site
                                }
                            },
                            modifier = Modifier.fillMaxWidth().padding(vertical = UI.smallSpacing)
                        ) {
                            Row(
                                Modifier.fillMaxWidth().padding(UI.mediumSpacing),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                    Checkbox(
                                        checked = isEnabled,
                                        onCheckedChange = { checked ->
                                            enabledSites = if (checked) enabledSites + h.site else enabledSites - h.site
                                        },
                                        colors = CheckboxDefaults.colors(
                                            checkedColor = THEME.accentPrimary,
                                            uncheckedColor = THEME.textMuted
                                        )
                                    )
                                    Spacer(Modifier.width(UI.smallSpacing))
                                    Column(Modifier.weight(1f)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Box(Modifier.size(8.dp).background(if (h.isAvailable) THEME.success else THEME.warning, CircleShape))
                                            Spacer(Modifier.width(UI.defaultSpacing))
                                            Text(h.site.getDisplayName(strings), color = THEME.textPrimary, fontSize = UI.bodySize, fontWeight = FontWeight.Medium)
                                        }
                                        Spacer(Modifier.height(2.dp))
                                        Text(h.site.getTip(strings), color = THEME.textMuted, fontSize = UI.smallSize)
                                    }
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        if (h.daysSinceLastUse != null) "${h.daysSinceLastUse}d" else "-",
                                        color = if (h.isAvailable) THEME.success else THEME.warning,
                                        fontSize = UI.bodySize,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Text("${h.totalInjections} ${strings.total}", color = THEME.textMuted, fontSize = UI.smallSize)
                                }
                            }
                        }
                    }
                }

                Spacer(Modifier.height(UI.largeSpacing))

                val enabledCount = enabledSites.size
                if (enabledCount == 0) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = THEME.danger.copy(0.2f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(Modifier.padding(UI.mediumSpacing), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Warning, null, tint = THEME.danger, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(UI.defaultSpacing))
                            Text(strings.selectAtLeastOne, color = THEME.danger, fontSize = UI.captionSize)
                        }
                    }
                } else {
                    Text(
                        "${strings.rotation}: $enabledCount ${if (enabledCount == 1) strings.site_s else strings.sites}",
                        color = THEME.textMuted,
                        fontSize = UI.captionSize,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(Modifier.height(UI.largeSpacing))

                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(UI.mediumSpacing)) {
                    OutlinedButton(onClick = onDismiss, modifier = Modifier.weight(1f), shape = buttonShape) {
                        Text(strings.cancel, color = THEME.textMuted)
                    }
                    Button(
                        onClick = {
                            if (enabledSites.isNotEmpty()) {
                                repo.setEnabledSites(enabledSites)
                                onDismiss()
                            }
                        },
                        modifier = Modifier.weight(1f),
                        shape = buttonShape,
                        enabled = enabledSites.isNotEmpty(),
                        colors = ButtonDefaults.buttonColors(THEME.accentPrimary, THEME.textDark)
                    ) {
                        Text(strings.save)
                    }
                }
            }
        }
    }
}
