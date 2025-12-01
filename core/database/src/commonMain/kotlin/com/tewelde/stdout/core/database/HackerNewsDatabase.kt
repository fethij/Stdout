package com.tewelde.stdout.core.database

import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor

@Database(
    entities = [
        StoryEntity::class,
        CommentEntity::class
    ],
    version = 1
)
@ConstructedBy(HackerNewsDatabaseConstructor::class)
abstract class HackerNewsDatabase : RoomDatabase() {
    abstract fun storyDao(): StoryDao
    abstract fun commentDao(): CommentDao

    companion object {
        const val DATABASE_NAME = "hackernews.db"
    }
}

@Suppress("KotlinNoActualForExpect")
expect object HackerNewsDatabaseConstructor : RoomDatabaseConstructor<HackerNewsDatabase> {
    override fun initialize(): HackerNewsDatabase
}
