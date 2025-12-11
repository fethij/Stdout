package com.tewelde.stdout.core.domain

import com.tewelde.stdout.common.LoadState
import com.tewelde.stdout.core.data.HackerNewsRepository
import com.tewelde.stdout.core.model.Story
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filterNot
import kotlinx.coroutines.flow.mapNotNull
import me.tatarka.inject.annotations.Inject
import org.mobilenativefoundation.store.store5.StoreReadResponse
import org.mobilenativefoundation.store.store5.StoreReadResponseOrigin

/**
 * Use case to observe a story.
 */
@Inject
class ObserveStoryUseCase(
    private val repo: HackerNewsRepository
) {
    @Suppress("UNCHECKED_CAST")
    operator fun invoke(storyId: Long): Flow<LoadState<Story>> =
        repo.observeStory(id = storyId, refresh = true)
            .filterNot { it is StoreReadResponse.Loading || it is StoreReadResponse.NoNewData }
            .mapNotNull { response ->
                when (response) {
                    is StoreReadResponse.Data<*> -> {
                        response.dataOrNull()?.let { story ->
                            LoadState.Loaded(story)
                        }
                    }

                    is StoreReadResponse.Error -> {
                        if (response.origin is StoreReadResponseOrigin.Fetcher) {
                            return@mapNotNull null
                        }

                        val error = when (response) {
                            is StoreReadResponse.Error.Exception -> {
                                response.error
                            }

                            is StoreReadResponse.Error.Message -> {
                                Exception(response.message)
                            }

                            is StoreReadResponse.Error.Custom<*> -> {
                                Exception(response.error.toString())
                            }
                        }
                        LoadState.Error(error) as LoadState<Story>
                    }

                    else -> null
                }
            }
}
