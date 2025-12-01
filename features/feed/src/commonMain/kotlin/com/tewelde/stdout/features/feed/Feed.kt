package com.tewelde.stdout.features.feed

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.slack.circuit.codegen.annotations.CircuitInject
import com.slack.circuit.runtime.CircuitUiEvent
import com.slack.circuit.runtime.CircuitUiState
import com.slack.circuit.runtime.Navigator
import com.slack.circuit.runtime.presenter.Presenter
import com.tewelde.stdout.common.di.UiScope
import com.tewelde.stdout.core.data.StoryRepository
import com.tewelde.stdout.core.model.Story
import com.tewelde.stdout.core.navigation.FeedScreen
import kotlinx.coroutines.launch
import me.tatarka.inject.annotations.Inject
import me.tatarka.inject.annotations.Assisted


data class FeedState(
    val stories: List<Story>,
    val isLoading: Boolean,
    val eventSink: (FeedEvent) -> Unit
) : CircuitUiState

sealed interface FeedEvent : CircuitUiEvent {
    data class OpenStory(val id: Long) : FeedEvent
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
        var stories by remember { mutableStateOf(emptyList<Story>()) }
        var isLoading by remember { mutableStateOf(false) }
        val scope = rememberCoroutineScope()

        LaunchedEffect(Unit) {
            isLoading = true
            // stories = repository.getTopStories()
            isLoading = false
        }

        return FeedState(
            stories = stories,
            isLoading = isLoading
        ) { event ->
            when (event) {
                is FeedEvent.OpenStory -> {
                    // Navigate to details
                    // navigator.goTo(DetailsScreen(event.id))
                }

                FeedEvent.Refresh -> {
                    scope.launch {
                        isLoading = true
                        // stories = repository.getTopStories(refresh = true)
                        isLoading = false
                    }
                }
            }
        }
    }
}

@Composable
@CircuitInject(FeedScreen::class, UiScope::class)
fun Feed(state: FeedState, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF121212))
    ) {
        Text(
            text = "root@hn:~/top $",
            color = Color(0xFFE0E0E0),
            fontFamily = FontFamily.Monospace,
            modifier = Modifier.padding(16.dp)
        )

        if (state.isLoading) {
            Text(
                text = "fetching data...",
                color = Color.Gray,
                fontFamily = FontFamily.Monospace,
                modifier = Modifier.padding(16.dp)
            )
        } else {
            LazyColumn {
                items(state.stories) { story ->
                    StoryItem(story, onClick = { state.eventSink(FeedEvent.OpenStory(story.id)) })
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
            Text(
                text = "01", // Rank placeholder
                color = Color(0xFFFF6600),
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = story.title,
                    color = Color.White,
                    fontFamily = FontFamily.SansSerif,
                    fontWeight = FontWeight.Medium,
                    fontSize = 16.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${story.score} points :: ${story.author} :: 3h ago",
                    color = Color.Gray,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 12.sp
                )
            }
        }
    }
}
