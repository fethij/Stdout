package com.tewelde.stdout.core.database

import androidx.room.Embedded
import androidx.room.Relation

data class StoryWithComments(
    @Embedded
    val story: StoryEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "storyId"
    )
    val comments: List<CommentEntity>
)
