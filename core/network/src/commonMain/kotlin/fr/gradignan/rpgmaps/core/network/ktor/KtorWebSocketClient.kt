package fr.gradignan.rpgmaps.core.network.ktor

import fr.gradignan.rpgmaps.core.common.Resource
import fr.gradignan.rpgmaps.core.network.model.Payload
import fr.gradignan.rpgmaps.core.network.model.ServerMessage
import co.touchlab.kermit.Logger
import fr.gradignan.rpgmaps.core.network.BuildKonfig
import fr.gradignan.rpgmaps.core.network.WebSocketClient
import io.ktor.client.HttpClient
import io.ktor.client.plugins.websocket.webSocket
import io.ktor.http.HttpHeaders
import io.ktor.websocket.Frame
import io.ktor.websocket.WebSocketSession
import io.ktor.websocket.close
import io.ktor.websocket.readText
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class KtorWebSocketClient(
    private val client: HttpClient,
    private val encoder: Json
) : WebSocketClient {

    private val _incomingPayloads = MutableSharedFlow<Resource<Payload>>()

    private var session: WebSocketSession? = null
    private val _outgoingServerMessages = MutableSharedFlow<ServerMessage>(extraBufferCapacity = 10)

    override suspend fun connect(token: String) {
        try {
            client.webSocket(
                urlString = "ws://${BuildKonfig.hostName}/ws/1",
                request = {
                    headers.append(HttpHeaders.SecWebSocketProtocol, token)
                },
            ) {
                session = this
                send(Frame.Text("{\"action\": \"Connect\"}"))

                val receiveJob = launch {
                    for (frame in incoming) {
                        if (frame is Frame.Text) {
                            val jsonString = frame.readText()
                            val serverMessage = processIncomingMessage(jsonString)
                            _incomingPayloads.emit(serverMessage)
                        }
                    }
                }

                val sendJob = launch {
                    _outgoingServerMessages.collect { serverMessage ->
                        val jsonString = encoder.encodeToString(serverMessage)
                        Logger.d { jsonString }
                        send(Frame.Text(jsonString))
                    }
                }

                joinAll(receiveJob, sendJob)
            }
        } catch (e: Exception) {
            Logger.d { "could not connect: ${e.message}" }
        }
    }

    override fun sendMessage(serverMessage: ServerMessage) {
        _outgoingServerMessages.tryEmit(serverMessage)
    }

    override fun getPayloadsFlow(): Flow<Resource<Payload>> = _incomingPayloads

    override suspend fun close() {
        session?.close()
        session = null
    }

    private fun processIncomingMessage(jsonString: String): Resource<Payload> {
        return try {
            val message = encoder.decodeFromString<ServerMessage>(jsonString)
            Logger.d { "Received server message: ${message.action}" }
            Resource.Success(message.payload)
        } catch (e: Exception) {
            Logger.e { "Deserialization error: ${e.message}" }
            Resource.Error(e)
        }
    }
}
