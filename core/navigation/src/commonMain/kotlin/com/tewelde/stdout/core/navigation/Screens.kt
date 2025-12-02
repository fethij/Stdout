package com.tewelde.stdout.core.navigation

import com.slack.circuit.runtime.screen.Screen

@Parcelize
data object FeedScreen : Screen

@Parcelize
data class DetailsScreen(val storyId: Long) : Screen

@Parcelize
data class UrlScreen(val url: String): Screen
