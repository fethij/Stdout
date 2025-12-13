plugins {
    alias(libs.plugins.stdout.kotlinMultiplatform)
    alias(libs.plugins.stdout.featureMultiplatform)
    alias(libs.plugins.ksp)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(projects.core.common)
            api(projects.core.model)
            api(projects.core.data)

            implementation(libs.androidx.paging.common)
        }
    }
}

dependencies {
    ksp(libs.kotlinInject.compiler)
    ksp(libs.kotlinInject.anvil.compiler)
}
