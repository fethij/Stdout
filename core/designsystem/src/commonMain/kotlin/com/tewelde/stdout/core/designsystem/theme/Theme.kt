package com.tewelde.stdout.core.designsystem.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val HackerNewsOrange = Color(0xFFFF6600)
val TerminalBackground = Color(0xFF121212)
val TerminalText = Color(0xFFE0E0E0)

private val DarkColorPalette: ColorScheme = darkColorScheme(
    primary = HackerNewsOrange,
    secondary = Color.Gray,
    background = TerminalBackground,
    surface = TerminalBackground,
    onPrimary = Color.Black,
    onSecondary = Color.Black,
    onBackground = TerminalText,
    onSurface = TerminalText,
)

@Composable
fun StdoutTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColorPalette,
        content = content
    )
}
