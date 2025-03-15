package fr.gradignan.rpgmaps

import androidx.compose.runtime.*
import coil3.ImageLoader
import coil3.compose.setSingletonImageLoaderFactory
import io.ktor.client.HttpClient
import coil3.network.ktor3.KtorNetworkFetcherFactory
import fr.gradignan.rpgmaps.core.ui.theme.AppTheme
import org.jetbrains.compose.ui.tooling.preview.Preview
import fr.gradignan.rpgmaps.navigation.RpgMapsNavGraph
import org.koin.compose.koinInject

@Composable
@Preview
fun App() {
    val httpClient: HttpClient = koinInject()
    setSingletonImageLoaderFactory { context ->
        ImageLoader.Builder(context)
            .components {
                add(KtorNetworkFetcherFactory(httpClient))
            }
            .build()
    }
    AppTheme {
        RpgMapsNavGraph()
    }
}
