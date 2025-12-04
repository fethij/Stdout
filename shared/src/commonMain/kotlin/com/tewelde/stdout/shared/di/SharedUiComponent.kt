package com.tewelde.stdout.shared.di

import com.tewelde.stdout.common.di.UiScope
import com.tewelde.stdout.shared.AppUi
import software.amazon.lastmile.kotlin.inject.anvil.SingleIn


@SingleIn(UiScope::class)
interface SharedUiComponent {
    val appUi: AppUi
}