plugins {
    `kotlin-dsl`
}

dependencies {
    compileOnly(libs.plugins.kotlinxSerialization.toDep())
    compileOnly(libs.plugins.androidApplication.toDep())
    compileOnly(libs.plugins.androidLibrary.toDep())
    compileOnly(libs.plugins.composeMultiplatform.toDep())
    compileOnly(libs.plugins.kotlinMultiplatform.toDep())
    compileOnly(libs.plugins.composeCompiler.toDep())
    compileOnly(libs.plugins.ksp.toDep())
}

fun Provider<PluginDependency>.toDep() = map {
    "${it.pluginId}:${it.pluginId}.gradle.plugin:${it.version}"
}

gradlePlugin {
    plugins {
        register("kotlinMultiplatform") {
            id = "com.tewelde.stdout.kotlinMultiplatform"
            implementationClass = "KotlinMultiplatformConventionPlugin"
        }
        register("composeMultiplatform") {
            id = "com.tewelde.stdout.composeMultiplatform"
            implementationClass = "ComposeMultiplatformConventionPlugin"
        }
        register("featureMultiplatform") {
            id = "com.tewelde.stdout.featureMultiplatform"
            implementationClass = "FeatureMultiplatformConventionPlugin"
        }
        register("androidApplication") {
            id = "com.tewelde.stdout.androidApplication"
            implementationClass = "AndroidApplicationConventionPlugin"
        }
    }
}
