package com.tewelde.stdout.core.model


data class Story(
    val id: Long,
    val title: String,
    val url: String? = null,
    val by: String,
    val score: Int,
    val time: Long,
    val descendants: Int? = null,
    val kids: List<Long>? = null,
    val imageUrl: String? = null,
    val siteName: String? = null,
    val description: String? = null
)
