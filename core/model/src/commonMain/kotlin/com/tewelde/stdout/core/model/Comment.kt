package com.tewelde.stdout.core.model

import kotlinx.serialization.Serializable

// TODO shouldn't be serializable the domain model. we should have NetworkComment model in network module which is serializable
@Serializable
data class Comment(
    val id: Long,
    val text: String,
    val author: String,
    val time: Long,
    val parent: Long,
    val kids: List<Long> = emptyList()
)
