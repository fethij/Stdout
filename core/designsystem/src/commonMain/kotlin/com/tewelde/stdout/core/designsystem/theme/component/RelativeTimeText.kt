package com.tewelde.stdout.core.designsystem.theme.component

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.TextUnit
import kotlinx.coroutines.delay
import kotlin.time.Clock
import kotlin.time.Duration.Companion.minutes
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@Composable
fun RelativeTimeText(
    timestamp: Long,
    modifier: Modifier = Modifier,
    color: Color = Color.Unspecified,
    fontFamily: FontFamily? = null,
    fontSize: TextUnit = TextUnit.Unspecified
) {
    var timeText by remember(timestamp) { mutableStateOf(formatTime(timestamp)) }

    LaunchedEffect(timestamp) {
        while (true) {
            timeText = formatTime(timestamp)
            delay(1.minutes)
        }
    }

    Text(
        text = timeText,
        modifier = modifier,
        color = color,
        fontFamily = fontFamily,
        fontSize = fontSize
    )
}

@OptIn(ExperimentalTime::class)
fun formatTime(timestamp: Long): String {
    val now = Clock.System.now()
    val storyTime = Instant.fromEpochSeconds(timestamp)
    val duration = now - storyTime

    return when {
        duration.inWholeMinutes < 60 -> "${duration.inWholeMinutes}m ago"
        duration.inWholeHours < 24 -> "${duration.inWholeHours}h ago"
        else -> "${duration.inWholeDays}d ago"
    }
}