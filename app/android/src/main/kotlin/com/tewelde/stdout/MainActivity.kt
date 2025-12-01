package com.tewelde.stdout

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import com.tewelde.com.tewelde.stdout.di.AndroidUiComponent
import com.tewelde.stdout.common.di.ComponentHolder

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val uiComponent = ComponentHolder
            .component<AndroidUiComponent.Factory>()
            .create(this)
            .also {
                ComponentHolder.components += it
            }

        setContent {
            uiComponent.appUi.Content(
                onRootPop = backDispatcherRootPop(),
            )

            EdgeToEdgeSideEffect(
                isStatusBarLight = !isSystemInDarkTheme(),
                isNavigationBarLight = !isSystemInDarkTheme()
            )
        }
    }
}

/**
 * https://github.com/slackhq/circuit/blob/158d07b703778816a69f3cb13b63ef456a8c42e9/circuit-foundation/src/androidMain/kotlin/com/slack/circuit/foundation/Navigator.android.kt#L34
 */
@Composable
private fun backDispatcherRootPop(): () -> Unit {
    val onBackPressedDispatcher =
        LocalOnBackPressedDispatcherOwner.current?.onBackPressedDispatcher
            ?: error("No OnBackPressedDispatcherOwner found, unable to handle root navigation pops.")
    return { onBackPressedDispatcher.onBackPressed() }
}

@Composable
fun EdgeToEdgeSideEffect(
    isStatusBarLight: Boolean,
    isNavigationBarLight: Boolean,
) {
    val activity = LocalActivity.current as ComponentActivity

    DisposableEffect(
        isStatusBarLight,
        isNavigationBarLight
    ) {
        activity.enableEdgeToEdge(
            statusBarStyle =
                if (isStatusBarLight) {
                    SystemBarStyle.light(
                        Color.TRANSPARENT,
                        Color.TRANSPARENT
                    )
                } else {
                    SystemBarStyle.dark(
                        Color.TRANSPARENT
                    )
                },
            navigationBarStyle =
                if (isNavigationBarLight) {
                    SystemBarStyle.light(
                        Color.TRANSPARENT,
                        Color.TRANSPARENT
                    )
                } else {
                    SystemBarStyle.dark(
                        Color.TRANSPARENT
                    )
                }
        )
        onDispose { }
    }
}