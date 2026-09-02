package com.artemis.pushup1v1.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DuelColorScheme = darkColorScheme(
    primary = Color(0xFFFF6B35),
    secondary = Color(0xFFF7C548),
    background = Color(0xFF1B1F3B),
    surface = Color(0xFF262B4D)
)

@Composable
fun PushUp1v1Theme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DuelColorScheme,
        content = content
    )
}
