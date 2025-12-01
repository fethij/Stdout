package com.tewelde.stdout.core.designsystem.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val HackerNewsOrange = Color(0xFFFF6600)
val TerminalBackground = Color(0xFF121212)
val TerminalText = Color(0xFFE0E0E0)

private val DarkColorPalette = darkColors(
    primary = HackerNewsOrange,
    primaryVariant = HackerNewsOrange,
    secondary = HackerNewsOrange,
    background = TerminalBackground,
    surface = TerminalBackground,
    onPrimary = Color.Black,
    onSecondary = Color.Black,
    onBackground = TerminalText,
    onSurface = TerminalText,
)

@Composable
fun TerminalTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colors = DarkColorPalette,
        content = content
    )
}
