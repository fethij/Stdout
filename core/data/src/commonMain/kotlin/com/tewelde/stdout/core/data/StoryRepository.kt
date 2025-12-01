package com.tewelde.stdout.core.data

import com.tewelde.stdout.core.database.CommentDao
import com.tewelde.stdout.core.database.StoryDao
import com.tewelde.stdout.core.database.StoryEntity
import com.tewelde.stdout.core.model.Story
import com.tewelde.stdout.core.network.HackerNewsApi
import kotlinx.coroutines.flow.map
import me.tatarka.inject.annotations.Inject
import org.mobilenativefoundation.store.store5.Fetcher
import org.mobilenativefoundation.store.store5.SourceOfTruth
import org.mobilenativefoundation.store.store5.StoreBuilder
import org.mobilenativefoundation.store.store5.impl.extensions.get
import software.amazon.lastmile.kotlin.inject.anvil.AppScope
import software.amazon.lastmile.kotlin.inject.anvil.SingleIn

@Inject
 @SingleIn(AppScope::class)
class StoryRepository(
    private val api: HackerNewsApi,
    private val storyDao: StoryDao,
    private val commentDao: CommentDao
) {
    private val storyStore = StoreBuilder.from(
        fetcher = Fetcher.of { id: Long -> api.getStory(id) },
        sourceOfTruth = SourceOfTruth.of(
            reader = { id: Long -> storyDao.getStory(id).map { it?.toDomain() } },
            writer = { id: Long, story: Story -> storyDao.insertStory(story.toEntity()) },
            delete = { id: Long -> }
        )
    ).build()

    suspend fun getTopStories(refresh: Boolean = false): List<Story> {
        // In a real app, we'd cache the list of top IDs too.
        // For now, we fetch IDs fresh, then resolve stories from Store.
        val ids = api.getTopStories().take(20) // Limit to 20 for now
        return ids.map { id ->
            storyStore.get(id)
        }
    }

    private fun StoryEntity.toDomain(): Story {
        return Story(
            id = id,
            title = title,
            url = url,
            author = author,
            score = score,
            time = time,
            descendants = descendants,
            kids = if (kids.isEmpty()) emptyList() else kids.split(",").map { it.toLong() }
        )
    }

    private fun Story.toEntity(): StoryEntity {
        return StoryEntity(
            id = id,
            title = title,
            url = url,
            author = author,
            score = score,
            time = time,
            descendants = descendants,
            kids = kids.joinToString(",")
        )
    }
}
