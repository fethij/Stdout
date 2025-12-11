package com.tewelde.stdout.shared

import androidx.compose.foundation.layout.displayCutoutPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.slack.circuit.backstack.rememberSaveableBackStack
import com.slack.circuit.foundation.Circuit
import com.slack.circuit.foundation.CircuitCompositionLocals
import com.slack.circuit.foundation.NavigableCircuitContent
import com.slack.circuit.foundation.rememberCircuitNavigator
import com.slack.circuit.overlay.ContentWithOverlays
import com.slack.circuit.retained.LocalRetainedStateRegistry
import com.slack.circuit.retained.lifecycleRetainedStateRegistry
import com.slack.circuit.runtime.Navigator
import com.slack.circuitx.gesturenavigation.GestureNavigationDecorationFactory
import com.tewelde.stdout.common.di.UiScope
import com.tewelde.stdout.core.designsystem.theme.StdoutTheme
import com.tewelde.stdout.core.navigation.FeedScreen
import com.tewelde.stdout.core.navigation.OpenUrlNavigator
import me.tatarka.inject.annotations.Inject
import software.amazon.lastmile.kotlin.inject.anvil.ContributesBinding
import software.amazon.lastmile.kotlin.inject.anvil.SingleIn


interface AppUi {
    @Composable
    fun Content(
        modifier: Modifier = Modifier,
        launchUrl: (String) -> Unit,
        onRootPop: () -> Unit,
    )
}

@Inject
@SingleIn(UiScope::class)
@ContributesBinding(UiScope::class)
class StdoutApp(
    private val circuit: Circuit,
) : AppUi {

    @Composable
    override fun Content(
        modifier: Modifier,
        launchUrl: (String) -> Unit,
        onRootPop: () -> Unit,
    ) {
        val backStack = rememberSaveableBackStack(root = FeedScreen)
        val navigator = rememberCircuitNavigator(backStack) { onRootPop() }
        val urlNavigator: Navigator = remember(navigator) {
            OpenUrlNavigator(navigator, launchUrl)
        }
        CompositionLocalProvider(
            LocalRetainedStateRegistry provides lifecycleRetainedStateRegistry(),
        ) {
            CircuitCompositionLocals(circuit) {
                StdoutTheme {
                    Surface(color = MaterialTheme.colorScheme.background) {
                        ContentWithOverlays {
                            NavigableCircuitContent(
                                modifier = modifier.displayCutoutPadding(),
                                navigator = urlNavigator,
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
}