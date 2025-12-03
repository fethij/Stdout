plugins {
    alias(libs.plugins.stdout.kotlinMultiplatform)
    alias(libs.plugins.stdout.featureMultiplatform)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(projects.core.model)
            implementation(projects.core.network)
            implementation(projects.core.database)
            implementation(libs.store5)
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.bundles.kotlinInjectAnvil)
            implementation(libs.androidx.paging.common)
            implementation(libs.ksoup)
            implementation(libs.ksoup.network)
        }
    }
}
