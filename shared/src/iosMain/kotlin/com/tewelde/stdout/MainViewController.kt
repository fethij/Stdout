package com.tewelde.stdout

import androidx.compose.ui.uikit.LocalUIViewController
import androidx.compose.ui.window.ComposeUIViewController
import com.tewelde.stdout.common.di.ComponentHolder
import com.tewelde.stdout.di.IosUiComponent
import me.tatarka.inject.annotations.Inject
import platform.Foundation.NSURL
import platform.SafariServices.SFSafariViewController
import platform.UIKit.UIViewController

typealias StdoutUiViewController = () -> UIViewController

@Inject
fun StdoutUiViewController(): UIViewController =
    ComposeUIViewController {
        val uiComponent = ComponentHolder.component<IosUiComponent>()
        val uiViewController = LocalUIViewController.current

        uiComponent.appUi.Content(
            onRootPop = { /* no-op */ },
            launchUrl = { url ->
                val safari = SFSafariViewController(NSURL(string = url))
                uiViewController.presentViewController(
                    safari,
                    animated = true,
                    completion = null
                )
            }
        )
    }