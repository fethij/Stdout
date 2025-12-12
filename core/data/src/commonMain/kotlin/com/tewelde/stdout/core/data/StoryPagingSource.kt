package com.tewelde.stdout.core.data

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.tewelde.stdout.core.model.Story
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope

class StoryPagingSource(
    private val repository: HackerNewsRepository,
    private val ids: List<Long>
) : PagingSource<Int, Story>() {

    override fun getRefreshKey(state: PagingState<Int, Story>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            val anchorPage = state.closestPageToPosition(anchorPosition)
            anchorPage?.prevKey?.plus(1) ?: anchorPage?.nextKey?.minus(1)
        }
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Story> {
        return try {
            val page = params.key ?: 0

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
            val stories = coroutineScope {
                pageIds.map { id ->
                    async {
                        try {
                            repository.getStory(id)
                        } catch (_: Exception) {
                            null
                        }
                    }
                }.awaitAll().filterNotNull()
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
