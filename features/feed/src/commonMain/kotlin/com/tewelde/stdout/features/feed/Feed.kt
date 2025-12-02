package com.tewelde.stdout.features.feed

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults.Indicator
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
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
import com.tewelde.stdout.core.designsystem.theme.component.StoryItem
import com.tewelde.stdout.core.model.Story
import com.tewelde.stdout.core.model.StoryType
import com.tewelde.stdout.core.navigation.DetailsScreen
import com.tewelde.stdout.core.navigation.FeedScreen
import com.tewelde.stdout.core.navigation.UrlScreen
import kotlinx.coroutines.flow.Flow
import me.tatarka.inject.annotations.Assisted
import me.tatarka.inject.annotations.Inject


data class FeedState(
    val stories: Flow<PagingData<Story>>,
    val selectedType: StoryType,
    val eventSink: (FeedEvent) -> Unit
) : CircuitUiState

sealed interface FeedEvent : CircuitUiEvent {
    data class OpenStory(val story: Story) : FeedEvent
    data class OpenComments(val story: Story) : FeedEvent
    data class ChangeType(val type: StoryType) : FeedEvent
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

                is FeedEvent.OpenComments -> {
                    navigator.goTo(DetailsScreen(event.story.id))
                }

                is FeedEvent.ChangeType -> {
                    selectedType = event.type
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

        val pullToRefreshState = rememberPullToRefreshState()
        PullToRefreshBox(
            isRefreshing = lazyPagingItems.loadState.refresh is LoadState.Loading,
            onRefresh = { lazyPagingItems.refresh() },
            modifier = Modifier.fillMaxSize(),
            state = pullToRefreshState,
            indicator = {
                Indicator(
                    modifier = Modifier.align(Alignment.TopCenter),
                    isRefreshing = lazyPagingItems.loadState.refresh is LoadState.Loading,
                    containerColor = MaterialTheme.colorScheme.surface,
                    color = MaterialTheme.colorScheme.primary,
                    state = pullToRefreshState
                )
            }
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize()
            ) {
                items(lazyPagingItems.itemCount) { index ->
                    val story = lazyPagingItems[index]
                    if (story != null) {
                        StoryItem(
                            story,
                            onClick = { state.eventSink(FeedEvent.OpenStory(story)) },
                            onCommentClick = { state.eventSink(FeedEvent.OpenComments(story)) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        )
                    }
                }

                if (lazyPagingItems.loadState.append is LoadState.Loading) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }
    }
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
