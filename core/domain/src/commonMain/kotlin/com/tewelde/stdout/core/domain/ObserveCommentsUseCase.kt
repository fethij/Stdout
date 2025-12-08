package com.tewelde.stdout.core.domain

import com.tewelde.stdout.common.LoadState
import com.tewelde.stdout.core.data.HackerNewsRepository
import com.tewelde.stdout.core.model.Comment
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filterNot
import kotlinx.coroutines.flow.mapNotNull
import me.tatarka.inject.annotations.Inject
import org.mobilenativefoundation.store.store5.StoreReadResponse
import org.mobilenativefoundation.store.store5.StoreReadResponseOrigin

/**
 * Use case to observe comments
 */
@Inject
class ObserveCommentsUseCase(
    private val repo: HackerNewsRepository
) {
    @Suppress("UNCHECKED_CAST")
    operator fun invoke(storyId: Long): Flow<LoadState<List<Comment>>> =
        repo.observeComments(storyId = storyId, refresh = true)
            .filterNot { it is StoreReadResponse.Loading || it is StoreReadResponse.NoNewData }
            .mapNotNull { response ->
                when (response) {
                    is StoreReadResponse.Data<*> -> {
                        response.dataOrNull()?.let { comments ->
                            // this prevents emitting an empty list from the database cache while waiting for fresh network data
                            if (comments.isEmpty() && response.origin == StoreReadResponseOrigin.SourceOfTruth) {
                                return@mapNotNull null
                            }
                            LoadState.Loaded(comments)
                        }
                    }
                    is StoreReadResponse.Error -> {
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
                        LoadState.Error(error) as LoadState<List<Comment>>
                    }
                    else -> null
                }
            }
}