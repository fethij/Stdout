package com.tewelde.stdout.core.data

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.tewelde.stdout.core.data.store.CommentsStore
import com.tewelde.stdout.core.data.store.StoriesStore
import com.tewelde.stdout.core.model.Comment
import com.tewelde.stdout.core.model.Story
import com.tewelde.stdout.core.model.StoryType
import kotlinx.coroutines.flow.Flow
import me.tatarka.inject.annotations.Inject
import org.mobilenativefoundation.store.store5.Store
import org.mobilenativefoundation.store.store5.StoreReadRequest
import org.mobilenativefoundation.store.store5.StoreReadResponse
import org.mobilenativefoundation.store.store5.impl.extensions.get
import software.amazon.lastmile.kotlin.inject.anvil.AppScope
import software.amazon.lastmile.kotlin.inject.anvil.SingleIn

@Inject
@SingleIn(AppScope::class)
class StoryRepository(
    storiesStoreFactory: StoriesStore.Factory,
    commentsStoreFactory: CommentsStore.Factory
) {
    private val storyStore: Store<Long, Story> = storiesStoreFactory.createStoryStore()
    private val storyListStore: Store<StoryType, List<Long>> = storiesStoreFactory.createStoryListStore()
    private val commentStore: Store<Long, Comment> = commentsStoreFactory.create()

    /**
     * Observes a story by ID with automatic caching and network-first fallback.
     */
    fun observeStory(id: Long, refresh: Boolean = false): Flow<StoreReadResponse<Story>> {
        return storyStore.stream(
            if (refresh) {
                StoreReadRequest.fresh(id)
            } else {
                StoreReadRequest.cached(id, refresh = false)
            }
        )
    }

    /**
     * Observes story IDs for a given type with automatic caching and network-first fallback.
     */
    fun observeStoryIds(type: StoryType, refresh: Boolean = false): Flow<StoreReadResponse<List<Long>>> {
        return storyListStore.stream(
            if (refresh) {
                StoreReadRequest.fresh(type)
            } else {
                StoreReadRequest.cached(type, refresh = false)
            }
        )
    }

    /**
     * Gets stories for a given type.
     * Network-first with automatic fallback to database when network fails.
     */
    suspend fun getStories(type: StoryType, refresh: Boolean = false): List<Story> {
        // Get story IDs through the store (network-first with DB fallback)
        val ids = storyListStore.get(
            if (refresh) {
                StoreReadRequest.fresh(type)
            } else {
                StoreReadRequest.cached(type, refresh = true)
            }
        )

        // Fetch individual stories through the store
        return ids.take(20).map { id ->
            storyStore.get(
                StoreReadRequest.cached(id, refresh = true)
            )
        }
    }

    /**
     * Gets story IDs for a given type.
     * Network-first with automatic fallback to database when network fails.
     */
    suspend fun getStoryIds(type: StoryType): List<Long> {
        return storyListStore.get(
            StoreReadRequest.cached(type, refresh = true)
        )
    }

    /**
     * Gets a single story by ID.
     * Network-first with automatic fallback to database when network fails.
     */
    suspend fun getStory(id: Long, refresh: Boolean = false): Story {
        return storyStore.get(
            if (refresh) {
                StoreReadRequest.fresh(id)
            } else {
                StoreReadRequest.cached(id, refresh = true)
            }
        )
    }

    /**
     * Gets a single comment by ID.
     * Network-first with automatic fallback to database when network fails.
     */
    suspend fun getComment(id: Long, refresh: Boolean = false): Comment {
        return commentStore.get(
            if (refresh) {
                StoreReadRequest.fresh(id)
            } else {
                StoreReadRequest.cached(id, refresh = true)
            }
        )
    }

    /**
     * Gets multiple comments by their IDs.
     * Network-first with automatic fallback to database when network fails.
     */
    suspend fun getComments(ids: List<Long>): List<Comment> {
        return ids.mapNotNull { id ->
            try {
                getComment(id)
            } catch (e: Exception) {
                null
            }
        }
    }

    fun getStoriesPaged(type: StoryType): Flow<PagingData<Story>> {
        return Pager(
            config = PagingConfig(
                pageSize = 20,
                enablePlaceholders = false
            ),
            pagingSourceFactory = { StoryPagingSource(this, type) }
        ).flow
    }

    fun getCommentsPaged(commentIds: List<Long>): Flow<PagingData<Comment>> {
        return Pager(
            config = PagingConfig(
                pageSize = 20,
                enablePlaceholders = false
            ),
            pagingSourceFactory = { CommentPagingSource(this, commentIds) }
        ).flow
    }
}
