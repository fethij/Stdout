import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget

plugins {
    alias(libs.plugins.stdout.kotlinMultiplatform)
    alias(libs.plugins.stdout.featureMultiplatform)
    alias(libs.plugins.stdout.composeMultiplatform)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(projects.core.common)
            api(libs.kotlinInject.runtime)
            api(projects.core.model)
            api(projects.core.network)
            api(projects.core.database)
            api(projects.core.navigation)
            api(projects.core.designsystem)
            api(projects.core.data)

            api(projects.features.feed)
            api(projects.features.details)
        }
        targets.withType<KotlinNativeTarget>().configureEach {
            binaries.framework {
                isStatic = true
                baseName = "Shared"

                binaryOption("bundleId", "com.tewelde.stdout")
            }
        }
    }
}