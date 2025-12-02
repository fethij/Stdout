package com.tewelde.stdout.features.details

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import com.slack.circuit.codegen.annotations.CircuitInject
import com.slack.circuit.runtime.CircuitUiEvent
import com.slack.circuit.runtime.CircuitUiState
import com.slack.circuit.runtime.Navigator
import com.slack.circuit.runtime.presenter.Presenter
import com.tewelde.stdout.common.di.UiScope
import com.tewelde.stdout.core.data.StoryRepository
import com.tewelde.stdout.core.model.Story
import com.tewelde.stdout.core.navigation.DetailsScreen
import me.tatarka.inject.annotations.Inject
import me.tatarka.inject.annotations.Assisted


data class DetailsState(
    val story: Story?,
    val isLoading: Boolean,
    val eventSink: (DetailsEvent) -> Unit
) : CircuitUiState

sealed interface DetailsEvent : CircuitUiEvent {
    data object NavigateUp : DetailsEvent
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

        LaunchedEffect(screen.storyId) {
            isLoading = true
            // In a real app, we'd have a method to get a single story, potentially cached
            // For now, we assume it might be in the store or we fetch it
            story = repository.getStory(screen.storyId)
            isLoading = false
        }

        return DetailsState(
            story = story,
            isLoading = isLoading
        ) { event ->
            when (event) {
                DetailsEvent.NavigateUp -> navigator.pop()
            }
        }
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
        Text(
            text = "Story Details: ${state.story?.title ?: "Loading..."}",
            color = Color.White,
            fontFamily = FontFamily.Monospace
        )
    }
}
