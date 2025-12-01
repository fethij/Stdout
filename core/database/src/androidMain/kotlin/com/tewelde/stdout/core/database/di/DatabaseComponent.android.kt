package com.tewelde.stdout.core.database.di

import android.app.Application
import androidx.room.Room
import androidx.room.RoomDatabase
import com.tewelde.stdout.core.database.HackerNewsDatabase
import com.tewelde.stdout.core.database.HackerNewsDatabase.Companion.DATABASE_NAME
import me.tatarka.inject.annotations.Provides
import software.amazon.lastmile.kotlin.inject.anvil.AppScope
import software.amazon.lastmile.kotlin.inject.anvil.SingleIn

actual interface PlatformDatabaseComponent{

    @Provides
    @SingleIn(AppScope::class)
    fun provideDatabaseBuilder(
        application: Application
    ): RoomDatabase.Builder<HackerNewsDatabase> {
        val appContext = application.applicationContext
        val dbFile = appContext.getDatabasePath(DATABASE_NAME)
        return Room.databaseBuilder<HackerNewsDatabase>(
            context = appContext,
            name = dbFile.absolutePath
        )
    }
}