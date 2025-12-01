package com.tewelde.stdout.convention

import org.gradle.api.Project
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

@OptIn(ExperimentalWasmDsl::class)
internal fun Project.configureKotlinMultiplatform(
    extension: KotlinMultiplatformExtension
) = extension.apply {
    jvmToolchain(17)
    applyDefaultHierarchyTemplate()

    if (pluginManager.hasPlugin("com.android.library")) {
        androidTarget()
    }
    listOf(iosArm64(), iosSimulatorArm64())

    sourceSets.apply {
        commonMain {
            dependencies {
                implementation(libs.findLibrary("kotlinx.coroutines.core").get())
            }

            androidMain {
                dependencies {
//                    implementation(libs.findLibrary("kotlinx.coroutines.android").get())
                }
            }
        }
    }
}