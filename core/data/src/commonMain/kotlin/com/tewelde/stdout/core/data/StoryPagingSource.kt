package com.tewelde.stdout.core.data

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.tewelde.stdout.core.model.Story
import com.tewelde.stdout.core.model.StoryType

class StoryPagingSource(
    private val repository: StoryRepository,
    private val type: StoryType
) : PagingSource<Int, Story>() {

    private var storyIds: List<Long>? = null

    override fun getRefreshKey(state: PagingState<Int, Story>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            val anchorPage = state.closestPageToPosition(anchorPosition)
            anchorPage?.prevKey?.plus(1) ?: anchorPage?.nextKey?.minus(1)
        }
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Story> {
        return try {
            val page = params.key ?: 0

            if (storyIds == null) {
                storyIds = repository.getStoryIds(type)
            }

            val ids = storyIds ?: emptyList()
            val start = page * params.loadSize
            val end = minOf(start + params.loadSize, ids.size)

            if (start >= ids.size) {
                return LoadResult.Page(
                    data = emptyList(),
                    prevKey = if (page > 0) page - 1 else null,
                    nextKey = null
                )
            }

            val pageIds = ids.subList(start, end)
            val stories = pageIds.mapNotNull { id ->
                try {
                    repository.getStory(id)
                } catch (e: Exception) {
                    null
                }
            }

            LoadResult.Page(
                data = stories,
                prevKey = if (page > 0) page - 1 else null,
                nextKey = if (end < ids.size) page + 1 else null
            )
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }
}
