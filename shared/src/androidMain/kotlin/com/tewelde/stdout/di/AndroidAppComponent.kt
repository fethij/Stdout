package com.tewelde.com.tewelde.stdout.di

import android.app.Application
import com.tewelde.stdout.shared.di.SharedAppComponent
import me.tatarka.inject.annotations.Provides
import software.amazon.lastmile.kotlin.inject.anvil.AppScope
import software.amazon.lastmile.kotlin.inject.anvil.MergeComponent
import software.amazon.lastmile.kotlin.inject.anvil.SingleIn

@SingleIn(AppScope::class)
@MergeComponent(AppScope::class)
abstract class AndroidAppComponent(
    @get:Provides val application: Application
) : SharedAppComponent {
}
