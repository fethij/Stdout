plugins {
    alias(libs.plugins.stdout.kotlinMultiplatform)
    alias(libs.plugins.stdout.featureMultiplatform)
    alias(libs.plugins.stdout.composeMultiplatform)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(projects.core.common)
            implementation(projects.core.model)
            implementation(projects.core.navigation)
            implementation(projects.core.designsystem)
            implementation(projects.core.data)
            
            implementation(libs.coil.compose)
            implementation(libs.coil.network.ktor)
        }
    }
}
