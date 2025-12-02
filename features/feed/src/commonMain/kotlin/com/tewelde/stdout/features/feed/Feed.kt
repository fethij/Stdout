package com.tewelde.stdout.features.feed

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.paging.LoadState
import androidx.paging.PagingData
import androidx.paging.compose.collectAsLazyPagingItems
import com.slack.circuit.codegen.annotations.CircuitInject
import com.slack.circuit.retained.rememberRetained
import com.slack.circuit.runtime.CircuitUiEvent
import com.slack.circuit.runtime.CircuitUiState
import com.slack.circuit.runtime.Navigator
import com.slack.circuit.runtime.presenter.Presenter
import com.tewelde.stdout.common.di.UiScope
import com.tewelde.stdout.core.data.StoryRepository
import com.tewelde.stdout.core.model.Story
import com.tewelde.stdout.core.model.StoryType
import com.tewelde.stdout.core.navigation.DetailsScreen
import com.tewelde.stdout.core.navigation.FeedScreen
import com.tewelde.stdout.core.navigation.UrlScreen
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import me.tatarka.inject.annotations.Assisted
import me.tatarka.inject.annotations.Inject
import kotlin.time.Clock
import kotlin.time.Duration.Companion.minutes
import kotlin.time.ExperimentalTime
import kotlin.time.Instant


data class FeedState(
    val stories: Flow<PagingData<Story>>,
    val selectedType: StoryType,
    val eventSink: (FeedEvent) -> Unit
) : CircuitUiState

sealed interface FeedEvent : CircuitUiEvent {
    data class OpenStory(val story: Story) : FeedEvent
    data class ChangeType(val type: StoryType) : FeedEvent
    data object Refresh : FeedEvent
}

@Inject
@CircuitInject(FeedScreen::class, UiScope::class)
class FeedPresenter(
    private val repository: StoryRepository,
    @Assisted private val navigator: Navigator
) : Presenter<FeedState> {
    @Composable
    override fun present(): FeedState {
        var selectedType by rememberRetained { mutableStateOf(StoryType.TOP) }

        val stories = remember(selectedType) {
            repository.getStoriesPaged(selectedType)
        }

        return FeedState(
            stories = stories,
            selectedType = selectedType
        ) { event ->
            when (event) {
                is FeedEvent.OpenStory -> {
                    event.story.url?.let {
                        navigator.goTo(UrlScreen(it))
                    } ?: {
                        navigator.goTo(
                            DetailsScreen(
                                event.story.id
                            )
                        )
                    }
                }

                is FeedEvent.ChangeType -> {
                    selectedType = event.type
                }

                FeedEvent.Refresh -> {

                }
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
@CircuitInject(FeedScreen::class, UiScope::class)
fun Feed(state: FeedState, modifier: Modifier = Modifier) {
    val lazyPagingItems = state.stories.collectAsLazyPagingItems()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF121212))
    ) {
        StoryTypeSelector(
            selectedType = state.selectedType,
            onTypeSelected = { state.eventSink(FeedEvent.ChangeType(it)) },
            modifier = Modifier.padding(16.dp)
        )

        PullToRefreshBox(
            isRefreshing = lazyPagingItems.loadState.refresh is LoadState.Loading,
            onRefresh = { lazyPagingItems.refresh() },
            modifier = Modifier.fillMaxSize()
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize()
            ) {
                items(lazyPagingItems.itemCount) { index ->
                    val story = lazyPagingItems[index]
                    if (story != null) {
                        StoryItem(story, onClick = { state.eventSink(FeedEvent.OpenStory(story)) })
                    }
                }
            }
        }
    }
}

@Composable
fun StoryItem(story: Story, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp)
    ) {
        Row {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = story.title,
                    color = Color.White,
                    fontFamily = FontFamily.SansSerif,
                    fontWeight = FontWeight.Medium,
                    fontSize = 16.sp
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.ArrowUpward,
                            contentDescription = null,
                            modifier = Modifier.size(12.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = story.score.toString(),
                            color = MaterialTheme.colorScheme.secondary,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 12.sp
                        )
                    }
                    FullStop()
                    Text(
                        text = "@${story.by}",
                        color = MaterialTheme.colorScheme.secondary,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp
                    )
                    FullStop()
                    RelativeTimeText(
                        timestamp = story.time,
                        color = MaterialTheme.colorScheme.secondary,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp
                    )
                }
            }
            Text(
                text = "[${story.descendants ?: 0}]",
                color = MaterialTheme.colorScheme.primary,
                fontFamily = FontFamily.Monospace,
                fontSize = 12.sp
            )
        }
    }
}

@Composable
fun FullStop() {
    Text(
        text = "::",
        color = MaterialTheme.colorScheme.secondary,
        fontFamily = FontFamily.Monospace,
        fontSize = 12.sp
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

@Composable
fun StoryTypeSelector(
    selectedType: StoryType,
    onTypeSelected: (StoryType) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("root@hn:~/")
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(StoryType.entries) { type ->
                val isSelected = type == selectedType
                Text(
                    text = type.name.lowercase(),
                    color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Gray,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    modifier = Modifier
                        .clickable { onTypeSelected(type) }
                        .padding(4.dp)
                )
            }
        }
    }
}
