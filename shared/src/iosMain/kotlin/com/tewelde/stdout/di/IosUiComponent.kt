package com.tewelde.stdout.di

import com.tewelde.stdout.StdoutUiViewController
import com.tewelde.stdout.common.di.UiScope
import com.tewelde.stdout.shared.di.SharedUiComponent
import me.tatarka.inject.annotations.Provides
import platform.UIKit.UIViewController
import software.amazon.lastmile.kotlin.inject.anvil.AppScope
import software.amazon.lastmile.kotlin.inject.anvil.ContributesSubcomponent
import software.amazon.lastmile.kotlin.inject.anvil.SingleIn

@SingleIn(UiScope::class)
@ContributesSubcomponent(UiScope::class)
interface IosUiComponent : SharedUiComponent {

    val uiViewControllerFactory: () -> UIViewController

    @Provides
    @SingleIn(UiScope::class)
    fun uiViewController(impl: StdoutUiViewController): UIViewController = impl()

    @ContributesSubcomponent.Factory(AppScope::class)
    interface Factory {
        fun create(): IosUiComponent
    }
}
