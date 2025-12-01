package com.tewelde.stdout.convention

import com.android.build.api.dsl.CommonExtension
import com.android.build.api.dsl.LibraryExtension
import org.gradle.api.JavaVersion
import org.gradle.api.Project
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

internal fun Project.configureKotlinAndroid(
    extension: CommonExtension<*, *, *, *, *, *>
) = extension.apply {

    //get module name from module path
    val moduleName = path.split(":").drop(2).joinToString(".")
    namespace = if (moduleName.isNotEmpty()) "com.tewelde.stdout.$moduleName" else "com.tewelde.stdout"

    compileSdk = Versions.compileSdk
    defaultConfig {
        minSdk = Versions.minSdk
        if (extension is LibraryExtension) {
            extension.defaultConfig.consumerProguardFiles("consumer-proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }


    tasks.withType<KotlinCompile>().configureEach {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
        }
    }
}