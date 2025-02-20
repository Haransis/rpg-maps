import com.codingfeline.buildkonfig.compiler.FieldSpec.Type.STRING
import com.codingfeline.buildkonfig.gradle.TargetConfigDsl

plugins {
    alias(libs.plugins.rpgmaps.koin)
    alias(libs.plugins.rpgmaps.kotlinMultiplatform)
    alias(libs.plugins.buildKonfig)
}

kotlin {
    sourceSets {
        val desktopMain by getting

        commonMain.dependencies {
            implementation(projects.core.common)
            implementation(projects.core.model)
            implementation(libs.ktor.client.auth)
            implementation(libs.ktor.client.content.negotiation)
            implementation(libs.ktor.client.core)
            implementation(libs.ktor.client.logging)
            implementation(libs.ktor.client.websockets)
            implementation(libs.ktor.serialization.kotlinx.json)
            implementation(libs.multiplatform.settings)
        }
        desktopMain.dependencies {
            implementation(libs.ktor.client.java)
        }
    }
}

buildkonfig {
    packageName = "fr.gradignan.rpgmaps.core.network"

    // default config is required
    defaultConfigs {
        val hostName = "megalofidi.fr"
        buildConfigField(STRING, "hostName", hostName)
        buildConfigField(STRING, "baseUrl", "https://$hostName")
    }
    // flavor is passed as a first argument of defaultConfigs
    defaultConfigs("dev") {
        val hostName = "localhost:8000"
        buildConfigField(STRING, "hostName", hostName)
        buildConfigField(STRING, "baseUrl", "http://$hostName")
    }
}

//afterEvaluate { tasks.getByPath(":app:preBuild").dependsOn(tasks.getByName("installGitHooks")) }
