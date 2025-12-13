package com.tewelde.stdout.core.data

import com.tewelde.stdout.common.coroutines.DispatcherProvider
import com.tewelde.stdout.core.data.Mapper.toDomain
import com.tewelde.stdout.core.data.Mapper.toEntity
import com.tewelde.stdout.core.database.CommentDao
import com.tewelde.stdout.core.database.CommentEntity
import com.tewelde.stdout.core.database.StoryDao
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
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.withContext
import me.tatarka.inject.annotations.Inject
import org.mobilenativefoundation.store.store5.Fetcher
import org.mobilenativefoundation.store.store5.SourceOfTruth
import org.mobilenativefoundation.store.store5.StoreBuilder
import org.mobilenativefoundation.store.store5.StoreReadRequest
import org.mobilenativefoundation.store.store5.StoreReadResponse
import org.mobilenativefoundation.store.store5.impl.extensions.get
import software.amazon.lastmile.kotlin.inject.anvil.AppScope
import software.amazon.lastmile.kotlin.inject.anvil.ContributesBinding
import software.amazon.lastmile.kotlin.inject.anvil.SingleIn

interface HackerNewsRepository {

    /**
     * Observe a list of comments for a story.
     *
     * @param storyId The ID of the story.
     * @param refresh Whether to refresh the data from the network.
     */
    fun observeComments(
        storyId: Long, refresh: Boolean = true
    ): Flow<StoreReadResponse<List<Comment>>>

    /**
     * Observe a list of story IDs.
     *
     * @param type The type of stories to observe.
     */
    fun observeStoryIds(type: StoryType): Flow<List<Long>>

    /**
     * Observe a single story.
     *
     * @param id The ID of the story.
     * @param refresh Whether to refresh the data from the network.
     */
    fun observeStory(
        id: Long,
        refresh: Boolean = true
    ): Flow<StoreReadResponse<Story>>

    /**
     * Gets a story from cache
     */
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

    private val storyStore = StoreBuilder.from(
        fetcher = Fetcher.of { id: Long ->
            withContext(dispatcherProvider.io) {
                api.getStory(id)
            }
        },
        sourceOfTruth = SourceOfTruth.of(
            reader = { id: Long ->
                storyDao.getStory(id).map { it?.toDomain() }.flowOn(dispatcherProvider.databaseRead)
            }, writer = { _, story: NetworkStory ->
                withContext(dispatcherProvider.databaseWrite) {
                    storyDao.insertStory(story.toEntity())
                }
            }
        )
    )
        .disableCache()
        .build()

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
        }, sourceOfTruth = SourceOfTruth.of(
            reader = { type: StoryType ->
                storyTypeDao.getStoryType(type.name).map { entity ->
                    entity?.storyIds?.split(",")?.map { it.toLong() }
                }.flowOn(dispatcherProvider.databaseRead)
            },
            writer = { type: StoryType, ids: List<Long> ->
                withContext(dispatcherProvider.databaseWrite) {
                    storyTypeDao.insertStoryType(
                        StoryTypeEntity(
                            type = type.name, storyIds = ids.joinToString(",")
                        )
                    )
                }
            })
    )
        .disableCache()
        .build()

    override fun observeStoryIds(type: StoryType): Flow<List<Long>> {
        return storyListStore.stream(
            StoreReadRequest.cached(
                key = type,
                refresh = true
            )
        ).mapNotNull { it.dataOrNull() }
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
        },
        sourceOfTruth = SourceOfTruth.of(
            reader = { storyId: Long ->
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
            },
            writer = { storyId: Long, comments: List<NetworkComment> ->
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
                } catch (_: Exception) {
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
}
