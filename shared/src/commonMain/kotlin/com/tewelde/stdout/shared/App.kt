package com.tewelde.stdout.shared

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.displayCutoutPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.slack.circuit.backstack.rememberSaveableBackStack
import com.slack.circuit.foundation.Circuit
import com.slack.circuit.foundation.CircuitCompositionLocals
import com.slack.circuit.foundation.NavigableCircuitContent
import com.slack.circuit.foundation.rememberCircuitNavigator
import com.slack.circuit.overlay.ContentWithOverlays
import com.slack.circuitx.gesturenavigation.GestureNavigationDecorationFactory
import com.tewelde.stdout.common.di.UiScope
import com.tewelde.stdout.core.designsystem.theme.StdoutTheme
import com.tewelde.stdout.core.navigation.FeedScreen
import me.tatarka.inject.annotations.Inject
import software.amazon.lastmile.kotlin.inject.anvil.ContributesBinding
import software.amazon.lastmile.kotlin.inject.anvil.SingleIn


interface AppUi {
    @Composable
    fun Content(
        onRootPop: () -> Unit,
        modifier: Modifier = Modifier,
    )
}

@Inject
@SingleIn(UiScope::class)
@ContributesBinding(UiScope::class)
class StdoutApp(
    private val circuit: Circuit,
) : AppUi {

    @Composable
    override fun Content(onRootPop: () -> Unit, modifier: Modifier) {
        val backStack = rememberSaveableBackStack(root = FeedScreen)
        val navigator = rememberCircuitNavigator(backStack) { onRootPop() }
        CircuitCompositionLocals(circuit) {
            StdoutTheme {
                Surface(color = MaterialTheme.colorScheme.background) {
                    ContentWithOverlays {
                        NavigableCircuitContent(
                            modifier = modifier.displayCutoutPadding(),
                            navigator = navigator,
                            backStack = backStack,
                            decoratorFactory = remember(navigator) {
                                GestureNavigationDecorationFactory(onBackInvoked = navigator::pop)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun App() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text("APP")
    }
}