package com.tewelde.stdout.core.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface StoryTypeDao {
    @Query("SELECT * FROM story_types WHERE type = :type")
    fun getStoryType(type: String): Flow<StoryTypeEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStoryType(entity: StoryTypeEntity)

    @Query("SELECT * FROM story_types WHERE type = :type")
    suspend fun getStoryTypeSnapshot(type: String): StoryTypeEntity?
}
