package com.tewelde.stdout

import com.tewelde.stdout.common.di.ComponentHolder

object IosComponentHolder {
    fun addComponent(component: Any) {
        ComponentHolder.components += component
    }
}