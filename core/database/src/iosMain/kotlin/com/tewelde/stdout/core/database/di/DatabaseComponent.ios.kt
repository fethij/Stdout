package com.tewelde.stdout.core.database.di

import androidx.room.Room
import androidx.room.RoomDatabase
import com.tewelde.stdout.core.database.HackerNewsDatabase
import com.tewelde.stdout.core.database.HackerNewsDatabase.Companion.DATABASE_NAME
import kotlinx.cinterop.ExperimentalForeignApi
import me.tatarka.inject.annotations.Provides
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSUserDomainMask
import software.amazon.lastmile.kotlin.inject.anvil.AppScope
import software.amazon.lastmile.kotlin.inject.anvil.SingleIn

actual interface PlatformDatabaseComponent {

    @Provides
    @SingleIn(AppScope::class)
    fun provideDatabaseBuilder(): RoomDatabase.Builder<HackerNewsDatabase> {
        val dbFilePath = documentDirectory() + "/$DATABASE_NAME"
        return Room.databaseBuilder<HackerNewsDatabase>(
            name = dbFilePath
        )
    }

    @OptIn(ExperimentalForeignApi::class)
    private fun documentDirectory(): String {
        val documentDirectory = NSFileManager.defaultManager.URLForDirectory(
            directory = NSDocumentDirectory,
            inDomain = NSUserDomainMask,
            appropriateForURL = null,
            create = false,
            error = null,
        )
        return requireNotNull(documentDirectory?.path)
    }
}