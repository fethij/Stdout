package com.tewelde.stdout.core.model

data class Story(
    val id: Long,
    val title: String,
    val url: String? = null,
    val author: String,
    val score: Int,
    val time: Long,
    val descendants: Int = 0,
    val kids: List<Long> = listOf()
)
