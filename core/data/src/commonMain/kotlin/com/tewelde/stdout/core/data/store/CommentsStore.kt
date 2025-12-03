package com.tewelde.stdout.core.data.store

import com.tewelde.stdout.common.coroutines.DispatcherProvider
import com.tewelde.stdout.core.database.CommentDao
import com.tewelde.stdout.core.database.CommentEntity
import com.tewelde.stdout.core.model.Comment
import com.tewelde.stdout.core.network.HackerNewsApi
import com.tewelde.stdout.core.network.model.NetworkComment
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import me.tatarka.inject.annotations.Inject
import org.mobilenativefoundation.store.store5.Fetcher
import org.mobilenativefoundation.store.store5.SourceOfTruth
import org.mobilenativefoundation.store.store5.Store
import org.mobilenativefoundation.store.store5.StoreBuilder
import software.amazon.lastmile.kotlin.inject.anvil.AppScope
import software.amazon.lastmile.kotlin.inject.anvil.SingleIn

object CommentsStore {
    @Inject
    @SingleIn(AppScope::class)
    class Factory(
        private val api: HackerNewsApi,
        private val commentDao: CommentDao,
        private val dispatcherProvider: DispatcherProvider
    ) {
        fun create(): Store<Long, Comment> {
            return StoreBuilder.from(
                fetcher = Fetcher.of { id: Long ->
                    withContext(dispatcherProvider.io) {
                        api.getComment(id).toDomain()
                    }
                },
                sourceOfTruth = SourceOfTruth.of(
                    reader = { id: Long ->
                        commentDao.getComment(id)
                            .map { it?.toDomain() }
                            .flowOn(dispatcherProvider.databaseRead)
                    },
                    writer = { _: Long, comment: Comment ->
                        withContext(dispatcherProvider.databaseWrite) {
                            commentDao.insertComment(comment.toEntity())
                        }
                    },
                    delete = { id: Long ->
                        // Optional: implement deletion if needed
                    }
                )
            ).build()
        }
    }
}

private fun NetworkComment.toDomain(): Comment {
    return Comment(
        id = id,
        text = text ?: "",
        author = by ?: "",
        time = time ?: 0,
        parent = parent ?: 0,
        kids = kids ?: emptyList()
    )
}

private fun CommentEntity.toDomain(): Comment {
    return Comment(
        id = id,
        text = text,
        author = author,
        time = time,
        parent = parent,
        kids = kids.split(",").mapNotNull { it.toLongOrNull() }
    )
}

private fun Comment.toEntity(): CommentEntity {
    return CommentEntity(
        id = id,
        text = text,
        author = author,
        time = time,
        parent = parent,
        kids = kids.joinToString(",")
    )
}
