package com.tewelde.stdout.core.network.model

import kotlinx.serialization.Serializable

@Serializable
data class NetworkComment(
    val id: Long,
    val text: String? = null,
    val by: String? = null,
    val time: Long? = null,
    val parent: Long? = null,
    val kids: List<Long>? = null,
    val type: String? = null
)
