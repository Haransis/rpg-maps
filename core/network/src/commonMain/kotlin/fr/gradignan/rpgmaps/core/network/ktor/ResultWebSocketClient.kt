package fr.gradignan.rpgmaps.core.network.ktor

import fr.gradignan.rpgmaps.core.network.model.Payload
import fr.gradignan.rpgmaps.core.network.model.ServerMessage
import co.touchlab.kermit.Logger
import com.russhwolf.settings.Settings
import fr.gradignan.rpgmaps.core.model.DataError
import fr.gradignan.rpgmaps.core.model.EmptyResult
import fr.gradignan.rpgmaps.core.model.Result
import fr.gradignan.rpgmaps.core.model.after
import fr.gradignan.rpgmaps.core.network.BuildKonfig
import fr.gradignan.rpgmaps.core.network.WebSocketClient
import io.ktor.client.HttpClient
import io.ktor.client.call.NoTransformationFoundException
import io.ktor.client.plugins.websocket.webSocketSession
import io.ktor.http.HttpHeaders
import io.ktor.util.network.UnresolvedAddressException
import io.ktor.websocket.Frame
import io.ktor.websocket.WebSocketSession
import io.ktor.websocket.close
import io.ktor.websocket.readText
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.isActive
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.coroutines.coroutineContext

class KtorWebSocketClient(
    private val client: HttpClient,
    private val encoder: Json,
    private val settings: Settings
) : WebSocketClient {

    private val connectionLock = Mutex()
    private var session: WebSocketSession? = null

    private suspend fun connect(): EmptyResult<DataError.Http> = connectionLock.withLock {
        if (session?.isActive == true) {
            return Result.Success(Unit)
        }
        try {
            val token = settings.getStringOrNull("jwt_token")
            session = client.webSocketSession(
                urlString = "ws://${BuildKonfig.hostName}/ws/1"
            ) {
                if (token != null) headers.append(HttpHeaders.SecWebSocketProtocol, token)
            }
            session?.send(Frame.Text("{\"action\": \"Connect\"}"))
            return Result.Success(Unit)
        } catch (e: UnresolvedAddressException) {
            return Result.Error(DataError.Http.NO_INTERNET)
        } catch (e: Exception) {
            coroutineContext.ensureActive() // ensure we respect cancellation.
            return Result.Error(DataError.Http.UNKNOWN)
        }
    }

    override suspend fun sendMessage(serverMessage: ServerMessage): EmptyResult<DataError> =
        connectionLock.withLock {
            when (val connectResult = connect()) {
                is Result.Success -> {
                    val ws = session
                    if (ws == null || !ws.isActive) {
                        return Result.Error(DataError.WebSocket.UNKNOWN)
                    }
                    return try {
                        val jsonString = encoder.encodeToString(serverMessage)
                        ws.send(Frame.Text(jsonString))
                        Result.Success(Unit)
                    } catch (e: NoTransformationFoundException) {
                        Result.Error(DataError.WebSocket.SERIALIZATION)
                    } catch (e: Exception) {
                        coroutineContext.ensureActive() // ensure we respect cancellation.
                        Result.Error(DataError.WebSocket.UNKNOWN)
                    }
                }
                is Result.Error -> connectResult
            }
        }.after { close() }

    override fun getPayloadsFlow(): Flow<Result<Payload, DataError>> = flow {
            when (val connectResult = connect()) {
                is Result.Success -> {
                    val ws = session
                    if (ws == null || !ws.isActive) {
                        emit(Result.Error(DataError.WebSocket.UNKNOWN))
                    } else {
                        try {
                            for (frame in ws.incoming) {
                                if (frame is Frame.Text) {
                                    val jsonString = frame.readText()
                                    emit(processIncomingMessage(jsonString))
                                }
                            }
                        } catch (e: Exception) {
                            coroutineContext.ensureActive()
                            emit(Result.Error(DataError.WebSocket.UNKNOWN))
                        }
                    }
                }
                is Result.Error -> emit(connectResult)
            }
    }
        .onCompletion { close() }

    private suspend fun close() = connectionLock.withLock {
        session?.close()
        session = null
    }

    private fun processIncomingMessage(jsonString: String): Result<Payload, DataError> {
        return try {
            val message = encoder.decodeFromString<ServerMessage>(jsonString)
            Logger.d { "Received server message: ${message.action}" }
            Result.Success(message.payload)
        } catch (e: NoTransformationFoundException) {
            Result.Error(DataError.WebSocket.SERIALIZATION)
        } catch (e: Exception) {
            Result.Error(DataError.WebSocket.UNKNOWN)
        }
    }
}

