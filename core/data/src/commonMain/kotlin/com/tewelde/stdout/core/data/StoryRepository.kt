package com.tewelde.stdout.core.data

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.fleeksoft.ksoup.Ksoup
import com.fleeksoft.ksoup.network.parseGetRequest
import com.tewelde.stdout.common.coroutines.DispatcherProvider
import com.tewelde.stdout.core.database.CommentDao
import com.tewelde.stdout.core.database.StoryDao
import com.tewelde.stdout.core.database.StoryEntity
import com.tewelde.stdout.core.database.StoryTypeDao
import com.tewelde.stdout.core.database.StoryTypeEntity
import com.tewelde.stdout.core.model.Comment
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
                    val entity = story.toEntity()
                    // Check if we already have this story and if it has metadata
                    // If we do, we preserve the metadata
                    // This part is tricky with Store, as writer replaces the data.
                    // We can check inside toDomain/toEntity or do a read before write.
                    // But here, we just write. The parsing will happen and update the entity later.
                    storyDao.insertStory(entity)
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
                getStory(id)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            // Fallback to cached stories for the specific type
            val cachedIds = storyTypeDao.getStoryTypeSnapshot(type.name)?.storyIds
                ?.split(",")?.map { it.toLong() } ?: emptyList()

            cachedIds.take(20).mapNotNull { id ->
                // We might not have the story details cached, but we try
                try {
                    getStory(id)
                } catch (e: Exception) {
                    e.printStackTrace()
                    null
                }
            }
        }
    }

    suspend fun getStoryIds(type: StoryType): List<Long> {
        return try {
            storyListStore.get(type)
        } catch (e: Exception) {
            // Fallback to cached stories for the specific type
            storyTypeDao.getStoryTypeSnapshot(type.name)?.storyIds
                ?.split(",")?.map { it.toLong() } ?: emptyList()
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

    suspend fun getStory(id: Long): Story {
        val story = storyStore.get(id)

        // Check if we need to parse metadata
        val url = story.url
        if (url != null && story.imageUrl == null) {
            try {
                // Parse in background
                val parsedMetadata = parseMetadata(url)
                if (parsedMetadata != null) {
                    val updatedStory = story.copy(
                        imageUrl = parsedMetadata.imageUrl,
                        siteName = parsedMetadata.siteName,
                        description = parsedMetadata.description
                    )
                    // Update DB
                     withContext(dispatcherProvider.databaseWrite) {
                        storyDao.insertStory(updatedStory.toEntity())
                    }
                    return updatedStory
                }
            } catch (e: Exception) {
                // Ignore parsing errors
                e.printStackTrace()
            }
        }

        return story
    }

    data class ParsedMetadata(
        val imageUrl: String?,
        val siteName: String?,
        val description: String?
    )

    private suspend fun parseMetadata(url: String): ParsedMetadata? {
        return withContext(dispatcherProvider.io) {
            try {
                val doc = Ksoup.parseGetRequest(url = url)
                val ogImage = doc.select("meta[property=og:image]").attr("content").takeIf { it.isNotEmpty() }
                val ogSiteName = doc.select("meta[property=og:site_name]").attr("content").takeIf { it.isNotEmpty() }
                val ogDescription = doc.select("meta[property=og:description]").attr("content").takeIf { it.isNotEmpty() }

                // Fallback for image
                val image = ogImage ?: doc.select("link[rel=image_src]").attr("href").takeIf { it.isNotEmpty() }

                // Fallback for description
                 val description = ogDescription ?: doc.select("meta[name=description]").attr("content").takeIf { it.isNotEmpty() }

                // Fallback for site name (domain)
                val siteName = ogSiteName ?: try {
                     // simplistic domain extraction
                     val domain = url.substringAfter("://").substringBefore("/")
                     domain
                } catch (e: Exception) { null }

                ParsedMetadata(image, siteName, description)
            } catch (e: Exception) {
                null
            }
        }
    }

    suspend fun getComment(id: Long): Comment {
        // TODO: Implement comment fetching properly with Store if needed, for now direct API or similar
        // Assuming we have a way to get comments, maybe need to add it to Store or just fetch
        // For now, let's assume we fetch it directly or via a new Store
        // But wait, the repository structure suggests using Store.
        // Let's check if we have commentStore or similar.
        // The file content showed commentDao but not commentStore.
        // Let's implement a simple fetch for now using API directly if store is missing, 
        // or better, add a simple fetcher.
        // Re-checking StoryRepository content... it has commentDao.
        // It seems we need to add getComment to API and Repository.
        // The search result showed HackerNewsApi has getComment.
        // So we can just call api.getComment(id) and map it.
        // But we should probably cache it too.
        // For simplicity in this step, let's just fetch from API and map to domain.
        // Wait, I need Comment domain model too.

        val networkComment = api.getComment(id)
        return Comment(
            id = networkComment.id,
            text = networkComment.text ?: "",
            author = networkComment.by ?: "",
            time = networkComment.time ?: 0,
            parent = networkComment.parent ?: 0,
            kids = networkComment.kids ?: emptyList()
        )
    }

    suspend fun getComments(ids: List<Long>): List<Comment> {
        return ids.mapNotNull { id ->
            try {
                getComment(id)
            } catch (e: Exception) {
                null
            }
        }
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
            kids = kids?.split(",")?.map { it.toLong() },
            imageUrl = imageUrl,
            siteName = siteName,
            description = description
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
            kids = kids?.joinToString(","),
            imageUrl = imageUrl,
            siteName = siteName,
            description = description
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
            kids = kids?.joinToString(","),
            imageUrl = null,
            siteName = null,
            description = null
        )
    }
}
