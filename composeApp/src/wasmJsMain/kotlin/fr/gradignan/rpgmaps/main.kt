package fr.gradignan.rpgmaps

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import fr.gradignan.rpgmaps.di.AppModule
import kotlinx.browser.document
import org.koin.core.context.startKoin

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    startKoin { modules(AppModule) }
    ComposeViewport(document.body!!) {
        App()
    }
}