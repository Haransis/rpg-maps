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
            implementation(projects.core.data)
            implementation(projects.core.model)
            implementation(projects.core.ui)
            implementation(libs.androidx.lifecycle.viewmodel)
            implementation(libs.androidx.lifecycle.viewmodel.compose)
            implementation(libs.androidx.lifecycle.runtime.compose)
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.multiplatform.settings)
        }
    }
}
