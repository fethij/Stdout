package com.tewelde.stdout.core.model

import kotlinx.serialization.Serializable

// TODO shouldn't be serializable the domain model. we should have NetworkStory model in network module which is serializable
@Serializable
data class Story(
    val id: Long,
    val title: String,
    val url: String? = null,
    val by: String? = null,
    val score: Int? = null,
    val time: Long,
    val descendants: Int? = null,
    val kids: List<Long>? = null
)

