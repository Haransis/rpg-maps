plugins {
    alias(libs.plugins.rpgmaps.kotlinMultiplatform)
    alias(libs.plugins.rpgmaps.koin)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(projects.core.common)
            implementation(projects.core.model)
            implementation(projects.core.network)
            implementation(libs.multiplatform.settings)
        }
    }
}
