package fr.gradignan.rpgmaps.core.network.di

import com.russhwolf.settings.Settings
import fr.gradignan.rpgmaps.core.network.NetworkHttpClient
import fr.gradignan.rpgmaps.core.network.WebSocketClient
import fr.gradignan.rpgmaps.core.network.ktor.KtorHttpClient
import fr.gradignan.rpgmaps.core.network.ktor.KtorWebSocketClient
import io.ktor.client.HttpClient
import io.ktor.client.plugins.HttpResponseValidator
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.providers.BearerTokens
import io.ktor.client.plugins.auth.providers.bearer
import io.ktor.client.plugins.cache.HttpCache
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.cookies.HttpCookies
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import org.koin.dsl.module


val NetworkModule = module {
    single<Settings> { Settings() }
    single {
        Json {
            ignoreUnknownKeys = true
            isLenient = true
            explicitNulls = false
            prettyPrint = true
            classDiscriminator = "action"
        }
    }
    single {
        val settings: Settings = get()
        HttpClient {
            install(HttpCache)
            install(HttpCookies)
            install(WebSockets)
            install(Auth) {
                bearer {
                    loadTokens {
                        val token = settings.getStringOrNull("jwt_token")
                        if (token == null) {
                            return@loadTokens null
                        } else {
                            BearerTokens(token, "")
                        }
                    }
                }
            }
            HttpResponseValidator {
                validateResponse { response ->
                    if (response.status == HttpStatusCode.Unauthorized) {
                        settings.remove("jwt_token")
                    }
                }
            }
            install(Logging){
                logger = object: Logger {
                    override fun log(message: String) {
                        //co.touchlab.kermit.Logger.v("HTTP Client", null, message)
                    }
                }
                level = LogLevel.HEADERS
            }
            install(ContentNegotiation) {
                json(get())
            }
        }
    }
    single<NetworkHttpClient> {
        KtorHttpClient(get())
    }
    single<WebSocketClient> {
        KtorWebSocketClient(get(),get(), get())
    }
}
