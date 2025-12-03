package com.tewelde.stdout.features.details

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.paging.LoadState
import androidx.paging.PagingData
import androidx.paging.compose.collectAsLazyPagingItems
import com.slack.circuit.codegen.annotations.CircuitInject
import com.slack.circuit.runtime.CircuitUiEvent
import com.slack.circuit.runtime.CircuitUiState
import com.slack.circuit.runtime.Navigator
import com.slack.circuit.runtime.presenter.Presenter
import com.tewelde.stdout.common.di.UiScope
import com.tewelde.stdout.core.data.StoryRepository
import com.tewelde.stdout.core.designsystem.theme.component.StoryItem
import com.tewelde.stdout.core.model.Comment
import com.tewelde.stdout.core.model.Story
import com.tewelde.stdout.core.navigation.DetailsScreen
import com.tewelde.stdout.core.navigation.UrlScreen
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import me.tatarka.inject.annotations.Assisted
import me.tatarka.inject.annotations.Inject

data class DetailsState(
    val story: Story?,
    val comments: Flow<PagingData<Comment>>?,
    val replies: Map<Long, List<Comment>> = emptyMap(),
    val loadingReplies: Set<Long> = emptySet(),
    val isLoading: Boolean,
    val eventSink: (DetailsEvent) -> Unit
) : CircuitUiState

sealed interface DetailsEvent : CircuitUiEvent {
    data object NavigateUp : DetailsEvent
    data class LoadReplies(val commentId: Long, val kidIds: List<Long>) : DetailsEvent

    data class OpenStory(val story: Story) : DetailsEvent
}

@Inject
@CircuitInject(DetailsScreen::class, UiScope::class)
class DetailsPresenter(
    @Assisted private val screen: DetailsScreen,
    private val repository: StoryRepository,
    @Assisted private val navigator: Navigator
) : Presenter<DetailsState> {
    @Composable
    override fun present(): DetailsState {
        var story by remember { mutableStateOf<Story?>(null) }
        var isLoading by remember { mutableStateOf(false) }
        var replies by remember { mutableStateOf<Map<Long, List<Comment>>>(emptyMap()) }
        var loadingReplies by remember { mutableStateOf<Set<Long>>(emptySet()) }
        val scope = androidx.compose.runtime.rememberCoroutineScope()

        val comments = remember(story) {
            story?.kids?.let { kids ->
                repository.getCommentsPaged(kids)
            }
        }

        LaunchedEffect(screen.storyId) {
            isLoading = true
            // In a real app, we'd have a method to get a single story, potentially cached
            // For now, we assume it might be in the store or we fetch it
            story = repository.getStory(screen.storyId)
            isLoading = false
        }

        return DetailsState(
            story = story,
            comments = comments,
            replies = replies,
            loadingReplies = loadingReplies,
            isLoading = isLoading
        ) { event ->
            when (event) {
                DetailsEvent.NavigateUp -> navigator.pop()
                is DetailsEvent.LoadReplies -> {
                    scope.launch {
                        loadingReplies = loadingReplies + event.commentId
                        val fetchedReplies = repository.getComments(event.kidIds)
                        replies = replies + (event.commentId to fetchedReplies)
                        loadingReplies = loadingReplies - event.commentId
                    }
                }

                is DetailsEvent.OpenStory -> {
                    event.story.url?.let {
                        navigator.goTo(UrlScreen(it))
                    }
                }
            }
        }
    }
}

@Composable
@CircuitInject(DetailsScreen::class, UiScope::class)
fun Details(state: DetailsState, modifier: Modifier = Modifier) {
    val lazyPagingItems = state.comments?.collectAsLazyPagingItems()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF121212))
            .padding(16.dp)
    ) {
        // Top Bar
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 16.dp)
        ) {
            IconButton(onClick = { state.eventSink(DetailsEvent.NavigateUp) }) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "story",
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
        }

        Text(
            text = state.story?.title ?: "Loading...",
            color = Color.White,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            modifier = Modifier
                .padding(bottom = 8.dp)
                .clickable {
                    state.story?.let {
                        state.eventSink(DetailsEvent.OpenStory(it))
                    }
                }
        )

        if (state.story != null) {
            StoryItem(
                state.story,
                onClick = { state.eventSink(DetailsEvent.OpenStory(state.story)) },
                showTitle = false,
                showComments = false
            )
        }

        if (lazyPagingItems != null) {
            LazyColumn(
                modifier = Modifier.fillMaxSize()
            ) {
                if (lazyPagingItems.loadState.refresh == LoadState.Loading) {
                    item {
                        Box(
                            modifier = Modifier.fillParentMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }

                items(lazyPagingItems.itemCount) { index ->
                    val comment = lazyPagingItems[index]
                    if (comment != null) {
                        CommentItem(
                            comment = comment,
                            replies = state.replies,
                            loadingReplies = state.loadingReplies,
                            storyAuthor = state.story?.by,
                            onExpand = { id, kids ->
                                state.eventSink(
                                    DetailsEvent.LoadReplies(
                                        id,
                                        kids
                                    )
                                )
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CommentItem(
    comment: Comment,
    replies: Map<Long, List<Comment>>,
    loadingReplies: Set<Long>,
    storyAuthor: String?,
    onExpand: (Long, List<Long>) -> Unit,
    depth: Int = 0
) {
    var expanded by remember { mutableStateOf(true) }
    val indent = minOf(depth, 8) * 8

    Column(
        modifier = Modifier.animateContentSize()
    ) {
        Row(
            modifier = Modifier
                .padding(start = indent.dp)
                .clickable {
                    expanded = !expanded
                    if (expanded && comment.kids.isNotEmpty() && replies[comment.id] == null) {
                        onExpand(comment.id, comment.kids)
                    }
                }
                .padding(vertical = 4.dp)
        ) {
            // Vertical line for indentation visual (optional, maybe just space is enough for now to fix the issue)
            // If we want the line, it needs to be careful about height.
            // For now, let's stick to simple padding indentation as per plan to fix the "too close to right" issue.
            // The previous implementation had a line. Let's try to keep it but inside the row?
            // Actually, the previous implementation added padding recursively.
            // Here we calculate absolute padding.

            if (depth > 0) {
                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(20.dp)
                        .background(Color.Gray)
                )
                Spacer(modifier = Modifier.width(8.dp))
            }

            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    val isOp = comment.author == storyAuthor
                    Text(
                        text = if (isOp) "[${comment.author} [OP]]" else "[${comment.author}]",
                        color = if (isOp) Color(0xFFFFA500) else Color.Gray,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 14.sp
                    )
                    if (!expanded) {
                        Text(
                            text = " [+${comment.kids.size}]",
                            color = Color.Gray,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 14.sp
                        )
                    }
                }

                if (expanded) {
                    Text(
                        text = comment.text,
                        color = Color.LightGray,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }
            }
        }

        if (expanded) {
            val commentReplies = replies[comment.id]
            if (commentReplies != null) {
                commentReplies.forEach { reply ->
                    CommentItem(
                        comment = reply,
                        replies = replies,
                        loadingReplies = loadingReplies,
                        storyAuthor = storyAuthor,
                        onExpand = onExpand,
                        depth = depth + 1
                    )
                }
            } else if (comment.kids.isNotEmpty()) {
                if (loadingReplies.contains(comment.id)) {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .padding(start = (indent + 8).dp) // Indent spinner too
                            .height(20.dp)
                            .width(20.dp),
                        color = MaterialTheme.colorScheme.secondary,
                        strokeWidth = 2.dp
                    )
                } else {
                    // Trigger load if expanded and not loaded
                    LaunchedEffect(Unit) {
                        onExpand(comment.id, comment.kids)
                    }
                    Text(
                        text = "Loading replies...",
                        color = Color.DarkGray,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(start = (indent + 8).dp)
                    )
                }
            }
        }
    }
}
