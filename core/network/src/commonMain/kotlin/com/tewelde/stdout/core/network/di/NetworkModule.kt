package com.tewelde.stdout.core.network.di

import com.tewelde.stdout.core.network.HackerNewsApi
import me.tatarka.inject.annotations.Provides
import software.amazon.lastmile.kotlin.inject.anvil.AppScope
import software.amazon.lastmile.kotlin.inject.anvil.ContributesTo
import software.amazon.lastmile.kotlin.inject.anvil.SingleIn

@ContributesTo(AppScope::class)
interface NetworkModule {
    @Provides
    @SingleIn(AppScope::class)
    fun provideHackerNewsApi(): HackerNewsApi = HackerNewsApi()
}
