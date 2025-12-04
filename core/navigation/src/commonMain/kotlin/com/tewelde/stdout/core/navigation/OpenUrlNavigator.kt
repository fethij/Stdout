package com.tewelde.stdout.core.navigation

import com.slack.circuit.runtime.Navigator
import com.slack.circuit.runtime.screen.Screen

class OpenUrlNavigator(
    val navigator: Navigator,
    val openUrl: (String) -> Unit
) : Navigator by navigator {
    override fun goTo(screen: Screen): Boolean {
        return when (screen) {
            is UrlScreen -> {
                openUrl(screen.url)
                true
            }

            else -> navigator.goTo(screen)
        }
    }
}