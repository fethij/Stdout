package com.tewelde.stdout.core.data.store

import com.tewelde.stdout.common.coroutines.DispatcherProvider
import com.tewelde.stdout.core.database.StoryDao
import com.tewelde.stdout.core.database.StoryEntity
import com.tewelde.stdout.core.database.StoryTypeDao
import com.tewelde.stdout.core.database.StoryTypeEntity
import com.tewelde.stdout.core.model.Story
import com.tewelde.stdout.core.model.StoryType
import com.tewelde.stdout.core.network.HackerNewsApi
import com.tewelde.stdout.core.network.model.NetworkStory
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import me.tatarka.inject.annotations.Inject
import org.mobilenativefoundation.store.store5.Fetcher
import org.mobilenativefoundation.store.store5.SourceOfTruth
import org.mobilenativefoundation.store.store5.Store
import org.mobilenativefoundation.store.store5.StoreBuilder
import software.amazon.lastmile.kotlin.inject.anvil.AppScope
import software.amazon.lastmile.kotlin.inject.anvil.SingleIn

object StoriesStore {
    sealed interface Operation {
        data class Single(val id: Long) : Operation
        data class ByType(val type: StoryType) : Operation
    }

    sealed interface Output {
        data class Single(val story: Story) : Output
        data class Collection(val stories: List<Long>) : Output {
            fun isEmpty(): Boolean = stories.isEmpty()
        }
    }

    @Inject
    @SingleIn(AppScope::class)
    class Factory(
        private val api: HackerNewsApi,
        private val storyDao: StoryDao,
        private val storyTypeDao: StoryTypeDao,
        private val dispatcherProvider: DispatcherProvider
    ) {
        fun createStoryStore(): Store<Long, Story> {
            return StoreBuilder.from(
                fetcher = Fetcher.of { id: Long ->
                    withContext(dispatcherProvider.io) {
                        api.getStory(id).toDomain()
                    }
                },
                sourceOfTruth = SourceOfTruth.of(
                    reader = { id: Long ->
                        storyDao.getStory(id)
                            .map { it?.toDomain() }
                            .flowOn(dispatcherProvider.databaseRead)
                    },
                    writer = { _: Long, story: Story ->
                        withContext(dispatcherProvider.databaseWrite) {
                            storyDao.insertStory(story.toEntity())
                        }
                    },
                    delete = { id: Long ->
                        // Optional: implement deletion if needed
                    }
                )
            ).build()
        }

        fun createStoryListStore(): Store<StoryType, List<Long>> {
            return StoreBuilder.from(
                fetcher = Fetcher.of { type: StoryType ->
                    withContext(dispatcherProvider.io) {
                        val endpoint = when (type) {
                            StoryType.TOP -> "topstories"
                            StoryType.NEW -> "newstories"
                            StoryType.BEST -> "beststories"
                            StoryType.ASK -> "askstories"
                            StoryType.SHOW -> "showstories"
                            StoryType.JOB -> "jobstories"
                        }
                        api.getStories(endpoint)
                    }
                },
                sourceOfTruth = SourceOfTruth.of(
                    reader = { type: StoryType ->
                        storyTypeDao.getStoryType(type.name)
                            .map { entity ->
                                entity?.storyIds?.split(",")?.map { it.toLong() } ?: emptyList()
                            }
                            .flowOn(dispatcherProvider.databaseRead)
                    },
                    writer = { type: StoryType, ids: List<Long> ->
                        withContext(dispatcherProvider.databaseWrite) {
                            storyTypeDao.insertStoryType(
                                StoryTypeEntity(
                                    type = type.name,
                                    storyIds = ids.joinToString(",")
                                )
                            )
                        }
                    }
                )
            ).build()
        }
    }
}

private fun NetworkStory.toDomain(): Story {
    return Story(
        id = id,
        title = title,
        url = url,
        by = by,
        score = score ?: 0,
        time = time,
        descendants = descendants,
        kids = kids
    )
}

private fun StoryEntity.toDomain(): Story {
    return Story(
        id = id,
        title = title,
        url = url,
        by = by,
        score = score,
        time = time,
        descendants = descendants,
        kids = kids?.split(",")?.map { it.toLong() }
    )
}

private fun Story.toEntity(): StoryEntity {
    return StoryEntity(
        id = id,
        title = title,
        url = url,
        by = by,
        score = score,
        time = time,
        descendants = descendants,
        kids = kids?.joinToString(",")
    )
}
