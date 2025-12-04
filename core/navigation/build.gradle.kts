plugins {
    alias(libs.plugins.stdout.kotlinMultiplatform)
    alias(libs.plugins.stdout.featureMultiplatform)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(libs.circuit.foundation)
        }
    }
}
