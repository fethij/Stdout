package com.tewelde.stdout.core.database.di

import androidx.room.RoomDatabase
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.tewelde.stdout.common.coroutines.DispatcherProvider
import com.tewelde.stdout.core.database.CommentDao
import com.tewelde.stdout.core.database.HackerNewsDatabase
import com.tewelde.stdout.core.database.StoryDao
import com.tewelde.stdout.core.database.StoryTypeDao
import me.tatarka.inject.annotations.Provides
import software.amazon.lastmile.kotlin.inject.anvil.AppScope
import software.amazon.lastmile.kotlin.inject.anvil.ContributesTo
import software.amazon.lastmile.kotlin.inject.anvil.SingleIn

expect interface PlatformDatabaseComponent

@ContributesTo(AppScope::class)
interface DatabaseComponent : PlatformDatabaseComponent {

    @Provides
    @SingleIn(AppScope::class)
    fun provideDatabase(
        builder: RoomDatabase.Builder<HackerNewsDatabase>,
        dispatcherProvider: DispatcherProvider
    ): HackerNewsDatabase = builder
        .fallbackToDestructiveMigration(true)
        .setDriver(BundledSQLiteDriver())
        .setQueryCoroutineContext(dispatcherProvider.io)
        .build()

    @Provides
    @SingleIn(AppScope::class)
    fun provideCommentDao(
        database: HackerNewsDatabase
    ): CommentDao = database.commentDao()

    @Provides
    @SingleIn(AppScope::class)
    fun provideStoryDao(
        database: HackerNewsDatabase
    ): StoryDao = database.storyDao()

    @Provides
    @SingleIn(AppScope::class)
    fun provideStoryTypeDao(
        database: HackerNewsDatabase
    ): StoryTypeDao = database.storyTypeDao()
}