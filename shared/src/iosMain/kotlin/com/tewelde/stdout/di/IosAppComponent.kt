package com.tewelde.stdout.di

import com.tewelde.stdout.shared.di.SharedAppComponent
import me.tatarka.inject.annotations.Provides
import platform.UIKit.UIApplication
import software.amazon.lastmile.kotlin.inject.anvil.AppScope
import software.amazon.lastmile.kotlin.inject.anvil.MergeComponent
import software.amazon.lastmile.kotlin.inject.anvil.SingleIn
import kotlin.reflect.KClass

@SingleIn(AppScope::class)
@MergeComponent(AppScope::class)
abstract class IosAppComponent(
    @get:Provides val app: UIApplication
) : SharedAppComponent {

    companion object {
        fun create(
            app: UIApplication
        ) = IosAppComponent::class.createComponent(app)
    }
}

/**
 * The `actual fun` will be generated for each iOS specific target. See [MergeComponent] for more
 * details.
 */
@MergeComponent.CreateComponent
expect fun KClass<IosAppComponent>.createComponent(app: UIApplication): IosAppComponent