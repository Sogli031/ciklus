package com.injekcije.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.injekcije.app.THEME
import com.injekcije.app.UI

@Composable
fun CrashLogScreen(crashLog: String) {
    val clipboardManager = LocalClipboardManager.current
    var copied by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(THEME.bgDark)
            .padding(UI.largeSpacing)
    ) {
        Spacer(modifier = Modifier.height(48.dp))

        Text(
            text = "Aplikacija je pukla",
            color = THEME.danger,
            fontSize = 24.sp,
            modifier = Modifier.padding(bottom = UI.largeSpacing)
        )

        Button(
            onClick = {
                clipboardManager.setText(AnnotatedString(crashLog))
                copied = true
            },
            colors = ButtonDefaults.buttonColors(
                containerColor = if (copied) THEME.success else THEME.danger
            ),
            shape = RoundedCornerShape(UI.cardRadius),
            modifier = Modifier
                .fillMaxWidth()
                .height(UI.buttonHeight)
        ) {
            Text(
                text = if (copied) "Kopirano!" else "Kopiraj crash log",
                color = THEME.textPrimary,
                fontSize = UI.subtitleSize
            )
        }

        Spacer(modifier = Modifier.height(UI.largeSpacing))

        Text(
            text = crashLog,
            color = THEME.textSecondary,
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace,
            modifier = Modifier
                .fillMaxSize()
                .background(
                    color = THEME.bgCard,
                    shape = RoundedCornerShape(UI.cardRadius)
                )
                .padding(UI.cardPadding)
                .verticalScroll(rememberScrollState())
                .horizontalScroll(rememberScrollState())
        )
    }
}
