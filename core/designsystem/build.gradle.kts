plugins {
    alias(libs.plugins.stdout.kotlinMultiplatform)
    alias(libs.plugins.stdout.composeMultiplatform)
}

kotlin {
    
    sourceSets {
        commonMain.dependencies {
            implementation(projects.core.model)

            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.ui)
            implementation(compose.components.resources)
        }
    }
}
