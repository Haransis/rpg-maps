package fr.gradignan.rpgmaps

import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPlacement
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import fr.gradignan.rpgmaps.di.AppModule
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonClassDiscriminator
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

/*
@OptIn(ExperimentalSerializationApi::class)
@Serializable
@JsonClassDiscriminator("action")
sealed interface IncomingPayload

@OptIn(ExperimentalSerializationApi::class)
@Serializable
@JsonClassDiscriminator("action")
sealed interface OutgoingPayload

@Serializable
@SerialName("MOVE")
data class PayloadMove(
    val payload: MoveData
) : IncomingPayload, OutgoingPayload
@Serializable
data class MoveData(
    val field1: Int,
    val field2: String
) {
    fun toUIModel(): MapActionMove = MapActionMove(field1, field2)
}

data class MapActionMove(
    val field1: Int,
    val field2: String
)

@Serializable
@SerialName("CHAT")
data class PayloadChat(
    val payload: ChatData
) : IncomingPayload, OutgoingPayload
@Serializable
data class ChatData(
    val tryAnother: String,
    val liste: List<String>
)

interface WebSocketClient {
    fun send(payload: OutgoingPayload)
    fun processMessage(jsonString: String): IncomingPayload
}

class MyWebSocketClient : WebSocketClient {
    private val json = Json {
        classDiscriminator = "action"
    }

    override fun send(payload: OutgoingPayload) {
        val jsonString = json.encodeToString(OutgoingPayload.serializer(), payload)
        println("Sending: $jsonString")
    }

    override fun processMessage(jsonString: String): IncomingPayload {
        val message = json.decodeFromString(IncomingPayload.serializer(), jsonString)
        println("Received: $message")
        return message
    }
}

fun main() {
    val client = MyWebSocketClient()

    val movePayload = PayloadMove(MoveData(field1 = 12, field2 = "character"))
    client.send(movePayload)
    val chatPayload = PayloadChat(ChatData(
        tryAnother = "TODO()",
        liste = listOf("coucou", "salut")
    ))
    client.send(chatPayload)

    val jsonMessage = """{
        "action": "MOVE",
        "payload": {
            "field1": 42,
            "field2": "hero"
        }
    }"""
    val incoming = client.processMessage(jsonMessage)
    val newJsonMessage = """{
        "action": "CHAT",
        "payload": {
            "tryAnother": "42",
            "liste": ["hero", "finally"]
        }
    }"""
    client.processMessage(newJsonMessage)

    // Optionally, convert to your UI model if it is a move message.
    if (incoming is PayloadMove) {
        val uiModel: MapActionMove = incoming.payload.toUIModel()
        println("Converted to UI model: $uiModel")
    }
}
*/
