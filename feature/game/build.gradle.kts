plugins {
    alias(libs.plugins.rpgmaps.composeMultiplatform)
    alias(libs.plugins.rpgmaps.kotlinMultiplatform)
    alias(libs.plugins.rpgmaps.koin)
    alias(libs.plugins.kotlinSerialization)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(projects.core.common)
            implementation(projects.core.ui)
            implementation(projects.core.data)
            implementation(projects.core.model)
            implementation(projects.core.network)
            implementation(libs.androidx.lifecycle.viewmodel)
            implementation(libs.androidx.lifecycle.viewmodel.compose)
            implementation(libs.androidx.lifecycle.runtime.compose)
            implementation(libs.coil)
            implementation(libs.coil.ktor)
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.reorderable)
        }
    }
}
