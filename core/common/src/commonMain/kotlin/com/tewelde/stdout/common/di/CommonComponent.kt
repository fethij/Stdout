package com.tewelde.stdout.common.di

import com.tewelde.stdout.common.coroutines.DispatcherProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import me.tatarka.inject.annotations.Provides
import software.amazon.lastmile.kotlin.inject.anvil.AppScope
import software.amazon.lastmile.kotlin.inject.anvil.ContributesTo
import software.amazon.lastmile.kotlin.inject.anvil.SingleIn

@ContributesTo(AppScope::class)
interface CommonComponent {
    @SingleIn(AppScope::class)
    @Provides
    fun provideCoroutineDispatchers(): DispatcherProvider =
        DispatcherProvider(
            io = Dispatchers.IO,
            databaseWrite = Dispatchers.IO.limitedParallelism(1),
            databaseRead = Dispatchers.IO.limitedParallelism(4),
            computation = Dispatchers.Default,
            main = Dispatchers.Main,
        )
}