package com.tewelde.stdout.core.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "comments")
data class CommentEntity(
    @PrimaryKey val id: Long,
    val text: String,
    val author: String,
    val time: Long,
    val parent: Long,
    val kids: String // Comma separated IDs
)
