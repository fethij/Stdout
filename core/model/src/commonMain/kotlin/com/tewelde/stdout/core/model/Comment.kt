package com.tewelde.stdout.core.model


data class Comment(
    val id: Long,
    val text: String,
    val author: String,
    val time: Long,
    val parent: Long,
    val kids: List<Long> = emptyList()
)
