package fr.gradignan.rpgmaps

import org.gradle.api.Project
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

@OptIn(org.jetbrains.kotlin.gradle.ExperimentalWasmDsl::class)
internal fun Project.configureKotlinMultiplatform(
    extension: KotlinMultiplatformExtension
) = extension.apply {
    jvmToolchain(17)

    jvm("desktop")

    wasmJs { browser() }

    applyDefaultHierarchyTemplate()

    sourceSets.apply {
        val commonMain = getByName("commonMain") {
            dependencies {
                implementation(libs.findLibrary("kotlinx.coroutines.core").get())
                implementation(libs.findLibrary("kermit").get())
            }
        }
        val jvmMain = maybeCreate("jvmMain").apply {
            dependsOn(commonMain)
        }
        getByName("desktopMain") {
            dependsOn(jvmMain)
            dependencies {
                implementation(libs.findLibrary("kotlinx.coroutines.swing").get())
            }
        }
    }
}