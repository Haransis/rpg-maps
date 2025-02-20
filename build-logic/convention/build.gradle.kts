plugins {
    `kotlin-dsl`
}

group = "fr.gradignan.rpgmaps.buildlogic"

dependencies {
    compileOnly(libs.plugins.composeCompiler.toDep())
    compileOnly(libs.plugins.composeMultiplatform.toDep())
    compileOnly(libs.plugins.kotlinMultiplatform.toDep())
    compileOnly(libs.plugins.kotlinSerialization.toDep())
}

fun Provider<PluginDependency>.toDep() = map {
    "${it.pluginId}:${it.pluginId}.gradle.plugin:${it.version}"
}

tasks {
    validatePlugins {
        enableStricterValidation = true
        failOnWarning = true
    }
}

gradlePlugin {
    plugins {
        register("kotlinMultiplatform"){
            id = "rpgmaps.kotlinMultiplatform"
            implementationClass = "KotlinMultiplatformConventionPlugin"
        }
        register("composeMultiplatform"){
            id = "rpgmaps.composeMultiplatform"
            implementationClass = "ComposeMultiplatformConventionPlugin"
        }
        register("koinConventionPlugin"){
            id = "rpgmaps.koin"
            implementationClass = "KoinConventionPlugin"
        }
    }
}
