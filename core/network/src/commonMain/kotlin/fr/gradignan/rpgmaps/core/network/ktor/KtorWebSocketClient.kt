package fr.gradignan.rpgmaps.core.network.ktor

import co.touchlab.kermit.Logger
import com.russhwolf.settings.Settings
import fr.gradignan.rpgmaps.core.model.DataError
import fr.gradignan.rpgmaps.core.model.EmptyResult
import fr.gradignan.rpgmaps.core.model.MapAction
import fr.gradignan.rpgmaps.core.model.Result
import fr.gradignan.rpgmaps.core.model.after
import fr.gradignan.rpgmaps.core.model.map
import fr.gradignan.rpgmaps.core.network.BuildKonfig
import fr.gradignan.rpgmaps.core.network.WebSocketClient
import fr.gradignan.rpgmaps.core.network.model.IncomingPayload
import fr.gradignan.rpgmaps.core.network.model.toMapAction
import fr.gradignan.rpgmaps.core.network.model.toOutgoingPayload
import io.ktor.client.HttpClient
import io.ktor.client.plugins.websocket.webSocketSession
import io.ktor.http.HttpHeaders
import io.ktor.util.network.UnresolvedAddressException
import io.ktor.websocket.Frame
import io.ktor.websocket.WebSocketSession
import io.ktor.websocket.close
import io.ktor.websocket.readText
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.isActive
import kotlinx.serialization.SerializationException
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.concurrent.atomics.AtomicInt
import kotlin.concurrent.atomics.ExperimentalAtomicApi
import kotlin.coroutines.coroutineContext

@OptIn(ExperimentalAtomicApi::class)
class KtorWebSocketClient(
    private val client: HttpClient,
    private val encoder: Json,
    private val settings: Settings
) : WebSocketClient {

    private val activeOperations = AtomicInt(0)
    private var session: WebSocketSession? = null

    private suspend fun connect(): EmptyResult<DataError.Http> {
        activeOperations.addAndFetch(1)
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
            Logger.e("$e")
            return Result.Error(DataError.Http.NO_INTERNET)
        } catch (e: Exception) {
            coroutineContext.ensureActive() // ensure we respect cancellation.
            Logger.e("$e")
            return Result.Error(DataError.Http.UNKNOWN)
        }
    }


    override suspend fun sendMessage(mapAction: MapAction): EmptyResult<DataError> =
        when (val connectResult = connect()) {
            is Result.Success -> {
                val ws = session
                if (ws == null || !ws.isActive) {
                    Logger.e("session closed unexpectedly")
                    Result.Error(DataError.WebSocket.UNKNOWN)
                }
                try {
                    val jsonString = encoder.encodeToString(mapAction.toOutgoingPayload())
                    ws!!.send(Frame.Text(jsonString))
                    Result.Success(Unit)
                } catch (e: SerializationException) {
                    Result.Error(DataError.WebSocket.SERIALIZATION)
                } catch (e: Exception) {
                    coroutineContext.ensureActive()
                    Logger.e("$e")
                    Result.Error(DataError.WebSocket.UNKNOWN)
                }
            }
            is Result.Error -> connectResult
        }.after { close() }

    override fun getPayloadsFlow(): Flow<Result<MapAction, DataError>> = flow {
            when (val connectResult = connect()) {
                is Result.Success -> {
                    val ws = session
                    if (ws == null || !ws.isActive) {
                        Logger.e("session closed unexpectedly")
                        throw IllegalStateException("session closed unexpectedly")
                    } else {
                        for (frame in ws.incoming) {
                            if (frame is Frame.Text) {
                                val jsonString = frame.readText()
                                emit(processIncomingMessage(jsonString))
                            }
                        }
                    }
                }
                is Result.Error -> emit(connectResult)
            }
    }
        .map(::toMapActionResult)
        .catch { cause ->
            when (cause) {
                is IllegalStateException -> emit(Result.Error(DataError.Http.CLOSED))
                else -> {
                    Logger.e("$cause")
                    emit(Result.Error(DataError.Http.UNKNOWN))
                }
            }
        }
        .onCompletion { close() }

    private suspend fun close() {
        if (activeOperations.addAndFetch(-1) == 0) {
            session?.close()
            session = null
        }
    }

    private fun processIncomingMessage(jsonString: String): Result<IncomingPayload, DataError> {
        return try {
            val message = encoder.decodeFromString<IncomingPayload>(jsonString)
            Logger.d { "Received server message: $message" }
            Result.Success(message)
        } catch (e: SerializationException) {
            Logger.e("$e")
            Result.Error(DataError.WebSocket.SERIALIZATION)
        } catch (e: Exception) {
            Logger.e("$e")
            Result.Error(DataError.WebSocket.UNKNOWN)
        }
    }

    private fun toMapActionResult(payload: Result<IncomingPayload, DataError>): Result<MapAction, DataError> {
        return payload.map { it.toMapAction() }
    }

}
