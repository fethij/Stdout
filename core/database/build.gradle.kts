import com.tewelde.stdout.convention.addKspDependencyForAllTargets

plugins {
    alias(libs.plugins.stdout.kotlinMultiplatform)
    alias(libs.plugins.stdout.featureMultiplatform)
    alias(libs.plugins.room)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(projects.core.model)
            implementation(projects.core.common)

            api(libs.androidx.room.runtime)
            implementation(libs.androidx.sqlite.bundled)
        }
    }
}

room {
    schemaDirectory("$projectDir/schemas")
}

addKspDependencyForAllTargets(libs.androidx.room.compiler)