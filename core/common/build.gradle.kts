plugins {
    alias(libs.plugins.stdout.kotlinMultiplatform)
    alias(libs.plugins.ksp)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(libs.kotlinx.coroutines.core)
            api(libs.bundles.kotlinInjectAnvil)
        }
    }
}

dependencies {
    ksp(libs.kotlinInject.compiler)
    ksp(libs.kotlinInject.anvil.compiler)
}
