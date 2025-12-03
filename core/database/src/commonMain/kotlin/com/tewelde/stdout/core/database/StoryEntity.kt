package com.tewelde.stdout.core.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "stories")
data class StoryEntity(
    @PrimaryKey val id: Long,
    val title: String,
    val url: String?,
    val by: String,
    val score: Int,
    val time: Long,
    val descendants: Int?,
    val kids: String?, // Comma separated IDs
    val imageUrl: String? = null,
    val siteName: String? = null,
    val description: String? = null
)
