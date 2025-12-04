package com.tewelde.com.tewelde.stdout.di

import android.app.Activity
import com.tewelde.stdout.common.di.UiScope
import com.tewelde.stdout.shared.di.SharedUiComponent
import software.amazon.lastmile.kotlin.inject.anvil.AppScope
import software.amazon.lastmile.kotlin.inject.anvil.ContributesSubcomponent
import software.amazon.lastmile.kotlin.inject.anvil.SingleIn

@SingleIn(UiScope::class)
@ContributesSubcomponent(UiScope::class)
interface AndroidUiComponent : SharedUiComponent {

    @ContributesSubcomponent.Factory(AppScope::class)
    interface Factory {
        fun create(activity: Activity): AndroidUiComponent
    }
}
