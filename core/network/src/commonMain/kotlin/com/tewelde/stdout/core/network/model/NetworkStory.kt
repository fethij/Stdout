package com.tewelde.stdout.core.network.model

import kotlinx.serialization.Serializable

@Serializable
data class NetworkStory(
    val id: Long,
    val title: String,
    val url: String? = null,
    val by: String,
    val score: Int? = null,
    val time: Long,
    val descendants: Int? = null,
    val kids: List<Long>? = null,
    val type: String? = null
)
