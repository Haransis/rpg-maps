package fr.gradignan.rpgmaps

import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPlacement
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import fr.gradignan.rpgmaps.di.AppModule
import org.koin.core.context.startKoin

fun main() = application {
    startKoin { modules(AppModule) }
    val windowState = rememberWindowState(
        placement = WindowPlacement.Maximized,
        width = 1080.dp,
        height = 800.dp,
    )
    Window(
        state = windowState,
        onCloseRequest = ::exitApplication,
        title = "rpg-maps",
    ) {
        App()
    }
}