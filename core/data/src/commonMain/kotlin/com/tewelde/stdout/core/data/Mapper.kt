package com.tewelde.stdout.core.data

import com.tewelde.stdout.core.database.StoryEntity
import com.tewelde.stdout.core.model.Story
import com.tewelde.stdout.core.network.model.NetworkStory

object Mapper {
    fun StoryEntity.toDomain(): Story = Story(
            id = id,
            title = title,
            url = url,
            by = by,
            score = score,
            time = time,
            descendants = descendants,
            kids = kids?.split(",")?.map { it.toLong() }
        )

    fun NetworkStory.toEntity(): StoryEntity = StoryEntity(
            id = id,
            title = title,
            url = url,
            by = by,
            score = score ?: 0,
            time = time,
            descendants = descendants,
            kids = kids?.joinToString(",")
        )
}