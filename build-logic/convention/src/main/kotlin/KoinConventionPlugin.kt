import fr.gradignan.rpgmaps.libs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

class KoinConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        with(pluginManager) {
            apply(libs.findPlugin("kotlinMultiplatform").get().get().pluginId)
        }

        extensions.configure<KotlinMultiplatformExtension> {
            sourceSets.apply {
                commonMain.dependencies {
                    implementation(project.dependencies.platform(libs.findLibrary("koin.bom").get()))
                    implementation(libs.findLibrary("koin.compose").get())
                    implementation(libs.findLibrary("koin.compose.viewmodel").get())
                    implementation(libs.findLibrary("koin.compose.viewmodel.navigation").get())
                }
            }
        }
    }
}