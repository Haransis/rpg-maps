package fr.gradignan.rpgmaps

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import fr.gradignan.rpgmaps.di.AppModule
import org.koin.core.context.startKoin

fun main() = application {
    startKoin { modules(AppModule) }
    Window(
        onCloseRequest = ::exitApplication,
        title = "rpg-maps",
    ) {
        App()
    }
}