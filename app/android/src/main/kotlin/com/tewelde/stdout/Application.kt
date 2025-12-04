package com.tewelde.stdout

import android.app.Application
import com.tewelde.stdout.common.di.ComponentHolder
import com.tewelde.com.tewelde.stdout.di.AndroidAppComponent
import com.tewelde.com.tewelde.stdout.di.create
import kotlinx.coroutines.DelicateCoroutinesApi

class Application : Application() {

    val androidComponent by lazy { AndroidAppComponent::class.create(this) }

    @OptIn(DelicateCoroutinesApi::class)
    override fun onCreate() {
        super.onCreate()
        ComponentHolder.components += androidComponent
    }
}