package com.tewelde.stdout

import androidx.compose.ui.window.ComposeUIViewController
import com.tewelde.stdout.common.di.ComponentHolder
import com.tewelde.stdout.di.IosUiComponent
import me.tatarka.inject.annotations.Inject
import platform.UIKit.UIViewController

typealias StdoutUiViewController = () -> UIViewController

@Inject
fun StdoutUiViewController(): UIViewController =
    ComposeUIViewController {
        val uiComponent = ComponentHolder.component<IosUiComponent>()
        uiComponent.appUi.Content(
            onRootPop = { /* no-op */ }
        )
    }