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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.UriHandler
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fleeksoft.ksoup.Ksoup
import com.mohamedrejeb.richeditor.model.RichTextState
import com.mohamedrejeb.richeditor.model.rememberRichTextState
import com.mohamedrejeb.richeditor.ui.material3.RichText
import com.slack.circuit.codegen.annotations.CircuitInject
import com.slack.circuit.runtime.CircuitUiEvent
import com.slack.circuit.runtime.CircuitUiState
import com.slack.circuit.runtime.Navigator
import com.slack.circuit.runtime.presenter.Presenter
import com.tewelde.stdout.common.LoadState
import com.tewelde.stdout.common.di.UiScope
import com.tewelde.stdout.core.designsystem.theme.component.StoryItem
import com.tewelde.stdout.core.domain.ObserveCommentsUseCase
import com.tewelde.stdout.core.domain.ObserveStoryUseCase
import com.tewelde.stdout.core.model.Comment
import com.tewelde.stdout.core.model.Story
import com.tewelde.stdout.core.navigation.DetailsScreen
import com.tewelde.stdout.core.navigation.UrlScreen
import kotlinx.coroutines.flow.map
import me.tatarka.inject.annotations.Assisted
import me.tatarka.inject.annotations.Inject


data class DetailsState(
    val storyLoadState: StoryLoadState,
    val commentsLoadState: CommentsLoadState,
    val eventSink: (DetailsEvent) -> Unit,
) : CircuitUiState


sealed interface CommentsLoadState : CircuitUiState {
    data object Empty : CommentsLoadState
    data object Loading : CommentsLoadState
    data class Error(
        val message: String,
        val eventSink: (DetailsEvent) -> Unit,
    ) : CommentsLoadState

    data class Success(
        val comments: List<Comment>,
        val uriHandler: UriHandler,
        val richTextState: RichTextState,
        val eventSink: (DetailsEvent) -> Unit,
    ) : CommentsLoadState
}

sealed interface StoryLoadState : CircuitUiState {
    data object Loading : StoryLoadState
    data class Error(
        val message: String,
        val eventSink: (DetailsEvent) -> Unit,
    ) : StoryLoadState

    data class Success(
        val story: Story,
        val eventSink: (DetailsEvent) -> Unit,
    ) : StoryLoadState
}

sealed interface DetailsEvent : CircuitUiEvent {
    data object NavigateUp : DetailsEvent
    data class OpenStory(val story: Story) : DetailsEvent
}

@Inject
@CircuitInject(DetailsScreen::class, UiScope::class)
class DetailsPresenter(
    @Assisted private val screen: DetailsScreen,
    private val observeComments: ObserveCommentsUseCase,
    private val observeStory: ObserveStoryUseCase,
    @Assisted private val navigator: Navigator
) : Presenter<DetailsState> {
    @Composable
    override fun present(): DetailsState {
        val eventSink: (DetailsEvent) -> Unit = { event ->
            when (event) {
                DetailsEvent.NavigateUp -> navigator.pop()
                is DetailsEvent.OpenStory -> event.story.url?.let { navigator.goTo(UrlScreen(it)) }
            }
        }
        val uriHandler: UriHandler by remember(screen.storyId) {
            mutableStateOf(object : UriHandler {
                override fun openUri(uri: String) {
                    val decodedUri = Ksoup.parse(uri).text()
                    navigator.goTo(UrlScreen(decodedUri))
                }
            })
        }
        val richTextState = rememberRichTextState().apply {
            config.linkColor = MaterialTheme.colorScheme.primary
            toggleSpanStyle(SpanStyle(fontFamily = FontFamily.Monospace))
        }
        val commentsState by observeComments(screen.storyId)
            .map { loadState ->
                when (loadState) {
                    is LoadState.Loading -> CommentsLoadState.Loading
                    is LoadState.Loaded<*> -> {
                        val comments = loadState.data as List<Comment>
                        if (comments.isEmpty()) {
                            CommentsLoadState.Empty
                        } else {
                            CommentsLoadState.Success(
                                comments = comments,
                                uriHandler = uriHandler,
                                richTextState = richTextState,
                                eventSink = eventSink
                            )
                        }
                    }

                    is LoadState.Error -> {
                        CommentsLoadState.Error(
                            message = loadState.error.message ?: "Unknown error",
                            eventSink = eventSink
                        )
                    }
                }
            }
            .collectAsState(initial = CommentsLoadState.Loading)

        val storyState by observeStory(screen.storyId)
            .map { loadState ->
                when (loadState) {
                    is LoadState.Loading -> StoryLoadState.Loading
                    is LoadState.Loaded<*> -> {
                        StoryLoadState.Success(
                            story = loadState.data as Story,
                            eventSink = eventSink
                        )
                    }

                    is LoadState.Error -> {
                        StoryLoadState.Error(
                            message = loadState.error.message ?: "Unknown error",
                            eventSink = eventSink
                        )
                    }
                }
            }
            .collectAsState(initial = StoryLoadState.Loading)

        return DetailsState(
            storyLoadState = storyState,
            commentsLoadState = commentsState,
            eventSink = eventSink
        )
    }
}

@Composable
@CircuitInject(DetailsScreen::class, UiScope::class)
fun Details(state: DetailsState, modifier: Modifier = Modifier) {
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
                text = "Story",
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
        }

        // Story Section
        when (val storyState = state.storyLoadState) {
            is StoryLoadState.Loading -> {
                Text(
                    text = "Loading Story...",
                    color = MaterialTheme.colorScheme.secondary,
                    fontFamily = FontFamily.Monospace
                )
            }

            is StoryLoadState.Error -> {
                Text(
                    text = "Error: ${storyState.message}",
                    color = MaterialTheme.colorScheme.error,
                    fontFamily = FontFamily.Monospace
                )
            }

            is StoryLoadState.Success -> {
                val story = storyState.story
                Text(
                    text = story.title,
                    color = MaterialTheme.colorScheme.secondary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier
                        .padding(bottom = 8.dp)
                        .clickable {
                            state.eventSink(DetailsEvent.OpenStory(story))
                        }
                )

                StoryItem(
                    story,
                    onClick = { state.eventSink(DetailsEvent.OpenStory(story)) },
                    showTitle = false,
                    showComments = false
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Comments Section
        when (val commentsState = state.commentsLoadState) {
            is CommentsLoadState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            }

            is CommentsLoadState.Error -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = "Error loading comments: ${commentsState.message}",
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }

            is CommentsLoadState.Empty -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = "No comments yet.",
                        color = Color.Gray
                    )
                }
            }

            is CommentsLoadState.Success -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize()
                ) {
                    val commentMap = commentsState.comments.associateBy { it.id }
                    val topLevelComments = commentsState.comments.filter { comment ->
                        !commentMap.containsKey(comment.parent)
                    }

                    fun buildFlatList(
                        comments: List<Comment>,
                        depth: Int
                    ): List<Pair<Comment, Int>> {
                        return comments.flatMap { comment ->
                            listOf(comment to depth) + buildFlatList(
                                comment.kids.mapNotNull { commentMap[it] },
                                depth + 1
                            )
                        }
                    }

                    val flatComments = buildFlatList(topLevelComments, 0)

                    items(flatComments.size) { index ->
                        val (comment, depth) = flatComments[index]
                        CommentItem(
                            comment = comment,
                            depth = depth,
                            uriHandler = commentsState.uriHandler,
                            richTextState = commentsState.richTextState
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommentItem(
    comment: Comment,
    depth: Int,
    uriHandler: UriHandler,
    richTextState: RichTextState
) {
    var expanded by remember { mutableStateOf(true) }
    val indent = minOf(depth, 8) * 8

    Column(
        modifier = Modifier.animateContentSize()
    ) {
        Row(
            modifier = Modifier
                .padding(start = indent.dp)
                .clickable { expanded = !expanded }
                .padding(vertical = 4.dp)
        ) {
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
                    Text(
                        text = "[${comment.author}]",
                        color = Color.Gray,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 14.sp
                    )
                }

                if (expanded) {
                    CompositionLocalProvider(LocalUriHandler provides uriHandler) {
                        RichText(
                            state = richTextState.setHtml(comment.text),
                            color = Color.LightGray,
                            fontFamily = FontFamily.Monospace,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }
                }
            }
        }
    }
}
