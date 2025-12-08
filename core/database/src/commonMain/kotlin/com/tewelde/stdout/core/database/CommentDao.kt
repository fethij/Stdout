package com.tewelde.stdout.core.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface CommentDao {
    @Query("SELECT * FROM comments WHERE id = :id")
    suspend fun getComment(id: Long): CommentEntity?

    @Query("SELECT * FROM comments WHERE parent = :parentId")
    fun getCommentsForParent(parentId: Long): Flow<List<CommentEntity>>

    @Query("SELECT * FROM comments WHERE storyId = :storyId")
    fun getCommentsForStory(storyId: Long): Flow<List<CommentEntity>>

    @Query("DELETE FROM comments WHERE storyId = :storyId")
    suspend fun deleteCommentsForStory(storyId: Long)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertComment(comment: CommentEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertComments(comments: List<CommentEntity>)
}
