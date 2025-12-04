package com.tewelde.stdout

import com.tewelde.stdout.di.IosAppComponent
import com.tewelde.stdout.di.IosUiComponent
import platform.UIKit.UIApplication

object IosComponentCreator {

    fun createAppComponent(app: UIApplication): IosAppComponent = IosAppComponent.create(app)

    fun createUiComponent(appComponent: IosAppComponent): IosUiComponent =
        (appComponent as IosUiComponent.Factory).create()
}