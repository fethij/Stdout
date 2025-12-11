package com.tewelde.stdout.core.data

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.tewelde.stdout.common.coroutines.DispatcherProvider
import com.tewelde.stdout.core.database.CommentDao
import com.tewelde.stdout.core.database.CommentEntity
import com.tewelde.stdout.core.database.StoryDao
import com.tewelde.stdout.core.database.StoryEntity
import com.tewelde.stdout.core.database.StoryTypeDao
import com.tewelde.stdout.core.database.StoryTypeEntity
import com.tewelde.stdout.core.model.Comment
import com.tewelde.stdout.core.model.Story
import com.tewelde.stdout.core.model.StoryType
import com.tewelde.stdout.core.network.HackerNewsApi
import com.tewelde.stdout.core.network.model.NetworkComment
import com.tewelde.stdout.core.network.model.NetworkStory
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import me.tatarka.inject.annotations.Inject
import org.mobilenativefoundation.store.store5.Fetcher
import org.mobilenativefoundation.store.store5.SourceOfTruth
import org.mobilenativefoundation.store.store5.StoreBuilder
import org.mobilenativefoundation.store.store5.StoreReadRequest
import org.mobilenativefoundation.store.store5.StoreReadResponse
import org.mobilenativefoundation.store.store5.impl.extensions.fresh
import org.mobilenativefoundation.store.store5.impl.extensions.get
import software.amazon.lastmile.kotlin.inject.anvil.AppScope
import software.amazon.lastmile.kotlin.inject.anvil.ContributesBinding
import software.amazon.lastmile.kotlin.inject.anvil.SingleIn


interface HackerNewsRepository {
    fun observeStories(type: StoryType): Flow<PagingData<Story>>

    /**
     * Observe a story with its comments.
     *
     * @param storyId The ID of the story.
     * @param refresh Whether to refresh the data from the network.
     */
    fun observeComments(
        storyId: Long, refresh: Boolean = true
    ): Flow<StoreReadResponse<List<Comment>>>

    /**
     * Observe a single story.
     *
     * @param id The ID of the story.
     * @param refresh Whether to refresh the data from the network.
     */
    fun observeStory(
        id: Long, refresh: Boolean = true
    ): Flow<StoreReadResponse<Story>>

    suspend fun getStoryIds(type: StoryType): List<Long>
    suspend fun getStory(id: Long): Story
}

@Inject
@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class)
class RealHackerNewsRepository(
    private val api: HackerNewsApi,
    private val storyDao: StoryDao,
    private val storyTypeDao: StoryTypeDao,
    private val commentDao: CommentDao,
    private val dispatcherProvider: DispatcherProvider
) : HackerNewsRepository {
    private val storyStore = StoreBuilder.from(fetcher = Fetcher.of { id: Long ->
        withContext(dispatcherProvider.io) {
            api.getStory(id)
        }
    }, sourceOfTruth = SourceOfTruth.of(reader = { id: Long ->
        storyDao.getStory(id).map { it?.toDomain() }.flowOn(dispatcherProvider.databaseRead)
    }, writer = { id: Long, story: NetworkStory ->
        withContext(dispatcherProvider.databaseWrite) {
            storyDao.insertStory(story.toEntity())
        }
    }, delete = { id: Long ->
//                withContext(dispatcherProvider.databaseWrite) {
//                    storyDao.deleteStory(id)
//                }
    })).build()

    private val storyListStore = StoreBuilder.from(fetcher = Fetcher.of { type: StoryType ->
        val endpoint = when (type) {
            StoryType.TOP -> "topstories"
            StoryType.NEW -> "newstories"
            StoryType.BEST -> "beststories"
            StoryType.ASK -> "askstories"
            StoryType.SHOW -> "showstories"
            StoryType.JOB -> "jobstories"
        }
        api.getStories(endpoint)
    }, sourceOfTruth = SourceOfTruth.of(reader = { type: StoryType ->
        storyTypeDao.getStoryType(type.name).map { entity ->
            entity?.storyIds?.split(",")?.map { it.toLong() }
        }.flowOn(dispatcherProvider.databaseRead)
    }, writer = { type: StoryType, ids: List<Long> ->
        withContext(dispatcherProvider.databaseWrite) {
            storyTypeDao.insertStoryType(
                StoryTypeEntity(
                    type = type.name, storyIds = ids.joinToString(",")
                )
            )
        }
    })).build()

    override suspend fun getStoryIds(type: StoryType): List<Long> {
        return try {
            storyListStore.fresh(type)
        } catch (e: Exception) {
            e.printStackTrace()
            storyListStore.get(type)
        }
    }

    override fun observeStories(type: StoryType): Flow<PagingData<Story>> {
        return Pager(
            config = PagingConfig(
                pageSize = 20,
                enablePlaceholders = false
            ), pagingSourceFactory = { StoryPagingSource(this, type) }).flow
    }

    override suspend fun getStory(id: Long): Story {
        return storyStore.get(id)
    }

    override fun observeStory(id: Long, refresh: Boolean): Flow<StoreReadResponse<Story>> {
        return storyStore.stream(
            StoreReadRequest.cached(
                key = id,
                refresh = refresh
            )
        )
    }


    private val commentStore = StoreBuilder.from(
        fetcher = Fetcher.of { storyId: Long ->
            val story = storyStore.get(storyId)
            val kids = story.kids ?: emptyList()
            fetchNetworkCommentsRecursively(kids)
        }, sourceOfTruth = SourceOfTruth.of(reader = { storyId: Long ->
            storyDao.getStoryWithComments(storyId).map { relation ->
                relation?.comments?.map { entity ->
                    Comment(
                        id = entity.id,
                        text = entity.text,
                        author = entity.author,
                        time = entity.time,
                        parent = entity.parent,
                        kids = entity.kids.split(",").filter { it.isNotEmpty() }
                            .map { it.toLong() })
                }
            }.flowOn(dispatcherProvider.databaseRead)
        }, writer = { storyId: Long, comments: List<NetworkComment> ->
            withContext(dispatcherProvider.databaseWrite) {
                val entities = comments.map { networkComment ->
                    CommentEntity(
                        id = networkComment.id,
                        storyId = storyId,
                        text = networkComment.text ?: "",
                        author = networkComment.by ?: "",
                        time = networkComment.time ?: 0,
                        parent = networkComment.parent ?: 0,
                        kids = networkComment.kids?.joinToString(",") ?: ""
                    )
                }
                commentDao.insertComments(entities)
            }
        }
        ))
        .disableCache()
        .build()

    private suspend fun fetchNetworkCommentsRecursively(
        ids: List<Long>
    ): List<NetworkComment> = coroutineScope {
        if (ids.isEmpty()) return@coroutineScope emptyList()

        // Fetch current level
        val currentLevelComments = ids.map { id ->
            async {
                try {
                    api.getComment(id)
                } catch (e: Exception) {
                    null
                }
            }
        }.awaitAll().filterNotNull()

        // Recursively fetch children
        val childComments = currentLevelComments.map { comment ->
            async {
                comment.kids?.let { childrenIds ->
                    fetchNetworkCommentsRecursively(childrenIds)
                } ?: emptyList()
            }
        }.awaitAll().flatten()

        currentLevelComments + childComments
    }

    override fun observeComments(
        storyId: Long,
        refresh: Boolean
    ): Flow<StoreReadResponse<List<Comment>>> {
        return commentStore.stream(
            StoreReadRequest.cached(
                key = storyId, refresh = refresh
            )
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
            kids = kids?.split(",")?.map { it.toLong() })
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
