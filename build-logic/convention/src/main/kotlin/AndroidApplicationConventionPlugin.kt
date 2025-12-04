import com.android.build.api.dsl.ApplicationExtension
import com.android.build.api.variant.ApplicationAndroidComponentsExtension
import com.tewelde.stdout.convention.configureKotlinAndroid
import com.tewelde.stdout.convention.libs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure

class AndroidApplicationConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply(libs.findPlugin("composeCompiler").get().get().pluginId)
                apply(libs.findPlugin("androidApplication").get().get().pluginId)
                apply(libs.findPlugin("kotlin-android").get().get().pluginId)
            }

            extensions.configure<ApplicationExtension>(::configureKotlinAndroid)

            // Add resource exclusions to just release builds
            androidComponents {
                onVariants(selector().withBuildType("release")) {
                    it.packaging.resources.excludes.addAll(
                        // Exclude AndroidX version files
                        "META-INF/*.version",
                        // Exclude consumer proguard files
                        "META-INF/proguard/*",
                        // Exclude the Firebase/Fabric/other random properties files
                        "/*.properties",
                        "fabric/*.properties",
                        "META-INF/*.properties",
                        // License files
                        "LICENSE*",
                        // Exclude Kotlin unused files
                        "META-INF/**/previous-compilation-data.bin",
                    )
                }
            }
        }
    }
}

private fun Project.androidComponents(action: ApplicationAndroidComponentsExtension.() -> Unit) =
    extensions.configure<ApplicationAndroidComponentsExtension>(action)
