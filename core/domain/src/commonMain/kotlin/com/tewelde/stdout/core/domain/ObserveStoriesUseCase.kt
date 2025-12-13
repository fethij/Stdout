package com.tewelde.stdout.core.domain

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.tewelde.stdout.core.data.HackerNewsRepository
import com.tewelde.stdout.core.data.StoryPagingSource
import com.tewelde.stdout.core.model.Story
import com.tewelde.stdout.core.model.StoryType
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import me.tatarka.inject.annotations.Inject

const val PAGE_SIZE = 20

@Inject
class ObserveStoriesUseCase(
    private val repository: HackerNewsRepository
) {
    @OptIn(ExperimentalCoroutinesApi::class)
    operator fun invoke(type: StoryType): Flow<PagingData<Story>> {
        return repository
            .observeStoryIds(type)
            .distinctUntilChanged { old, new -> old.size == new.size }
            .flatMapLatest { ids ->
                Pager(
                    config = PagingConfig(
                        pageSize = PAGE_SIZE,
                        enablePlaceholders = false
                    ),
                    pagingSourceFactory = { StoryPagingSource(repository, ids) }).flow
            }
    }
}
