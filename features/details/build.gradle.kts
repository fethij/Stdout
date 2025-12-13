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
            implementation(projects.core.domain)

            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(libs.androidx.paging.compose)
            implementation(libs.richeditor)
            implementation(libs.ksoup)
        }
    }
}
