plugins {
    alias(libs.plugins.rpgmaps.composeMultiplatform)
    alias(libs.plugins.rpgmaps.kotlinMultiplatform)
}


kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(projects.core.model)
        }
    }
}
