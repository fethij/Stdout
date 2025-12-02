package com.tewelde.stdout.core.data

import com.tewelde.stdout.common.coroutines.DispatcherProvider
import com.tewelde.stdout.core.database.CommentDao
import com.tewelde.stdout.core.database.StoryDao
import com.tewelde.stdout.core.database.StoryEntity
import com.tewelde.stdout.core.database.StoryTypeDao
import com.tewelde.stdout.core.database.StoryTypeEntity
import com.tewelde.stdout.core.model.Story
import com.tewelde.stdout.core.model.StoryType
import com.tewelde.stdout.core.network.HackerNewsApi
import com.tewelde.stdout.core.network.model.NetworkStory
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import me.tatarka.inject.annotations.Inject
import org.mobilenativefoundation.store.store5.Fetcher
import org.mobilenativefoundation.store.store5.SourceOfTruth
import org.mobilenativefoundation.store.store5.StoreBuilder
import org.mobilenativefoundation.store.store5.impl.extensions.fresh
import org.mobilenativefoundation.store.store5.impl.extensions.get
import software.amazon.lastmile.kotlin.inject.anvil.AppScope
import software.amazon.lastmile.kotlin.inject.anvil.SingleIn

@Inject
@SingleIn(AppScope::class)
class StoryRepository(
    private val api: HackerNewsApi,
    private val storyDao: StoryDao,
    private val storyTypeDao: StoryTypeDao,
    private val commentDao: CommentDao,
    private val dispatcherProvider: DispatcherProvider
) {
    private val storyStore = StoreBuilder.from(
        fetcher = Fetcher.of { id: Long ->
            withContext(dispatcherProvider.io) {
                api.getStory(id)
            }
        },
        sourceOfTruth = SourceOfTruth.of(
            reader = { id: Long ->
                storyDao.getStory(id)
                    .map { it?.toDomain() }
                    .flowOn(dispatcherProvider.databaseRead)
            },
            writer = { id: Long, story: NetworkStory ->
                withContext(dispatcherProvider.databaseWrite) {
                    storyDao.insertStory(story.toEntity())
                }
            },
            delete = { id: Long ->
//                withContext(dispatcherProvider.databaseWrite) {
//                    storyDao.deleteStory(id)
//                }
            }
        )
    ).build()

    private val storyListStore = StoreBuilder.from(
        fetcher = Fetcher.of { type: StoryType ->
            val endpoint = when (type) {
                StoryType.TOP -> "topstories"
                StoryType.NEW -> "newstories"
                StoryType.BEST -> "beststories"
                StoryType.ASK -> "askstories"
                StoryType.SHOW -> "showstories"
                StoryType.JOB -> "jobstories"
            }
            api.getStories(endpoint)
        },
        sourceOfTruth = SourceOfTruth.of(
            reader = { type: StoryType ->
                storyTypeDao.getStoryType(type.name)
                    .map { entity ->
                        entity?.storyIds?.split(",")
                            ?.map { it.toLong() }
                    }.flowOn(dispatcherProvider.databaseRead)
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

    suspend fun getStories(type: StoryType, refresh: Boolean = false): List<Story> {
        return try {
            val ids = if (refresh) {
                storyListStore.fresh(type)
            } else {
                storyListStore.get(type)
            }

            ids.take(20).map { id ->
                storyStore.get(id)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            // Fallback to cached stories for the specific type
            val cachedIds = storyTypeDao.getStoryTypeSnapshot(type.name)?.storyIds
                ?.split(",")?.map { it.toLong() } ?: emptyList()

            cachedIds.take(20).mapNotNull { id ->
                // We might not have the story details cached, but we try
                try {
                    storyStore.get(id)
                } catch (e: Exception) {
                    e.printStackTrace()
                    null
                }
            }
        }
    }

    suspend fun getStory(id: Long): Story {
        return storyStore.get(id)
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

    private fun NetworkStory.toEntity(): StoryEntity {
        return StoryEntity(
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
}
