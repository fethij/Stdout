package com.tewelde.stdout.core.data

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.tewelde.stdout.core.model.Comment

class CommentPagingSource(
    private val repository: StoryRepository,
    private val commentIds: List<Long>
) : PagingSource<Int, Comment>() {

    override fun getRefreshKey(state: PagingState<Int, Comment>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            val anchorPage = state.closestPageToPosition(anchorPosition)
            anchorPage?.prevKey?.plus(1) ?: anchorPage?.nextKey?.minus(1)
        }
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Comment> {
        return try {
            val page = params.key ?: 0

            val start = page * params.loadSize
            val end = minOf(start + params.loadSize, commentIds.size)

            if (start >= commentIds.size) {
                return LoadResult.Page(
                    data = emptyList(),
                    prevKey = if (page > 0) page - 1 else null,
                    nextKey = null
                )
            }

            val pageIds = commentIds.subList(start, end)
            val comments = pageIds.mapNotNull { id ->
                try {
                    repository.getComment(id)
                } catch (e: Exception) {
                    null
                }
            }

            LoadResult.Page(
                data = comments,
                prevKey = if (page > 0) page - 1 else null,
                nextKey = if (end < commentIds.size) page + 1 else null
            )
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }
}
