package com.tewelde.stdout.core.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "story_types")
data class StoryTypeEntity(
    @PrimaryKey val type: String,
    val storyIds: String // Comma separated IDs
)
