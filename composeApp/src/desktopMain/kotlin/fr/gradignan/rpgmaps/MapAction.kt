package fr.gradignan.rpgmaps

import co.touchlab.kermit.Logger
import kotlinx.serialization.*
import kotlinx.serialization.json.*

/*
@Serializable
data class WebSocketMessage<T : Payload>(
    val action: MessageAction,
    val payload: T
)

@Serializable
enum class MessageAction {
    MOVE,
    ATTACK,
    JOIN,
    LEAVE,
}

interface Payload

interface IncomingPayload : Payload
interface OutgoingPayload : Payload
interface BidirectionalPayload : IncomingPayload, OutgoingPayload

object PayloadSerializer : JsonContentPolymorphicSerializer<OutgoingPayload>(OutgoingPayload::class) {
    override fun selectDeserializer(element: JsonElement): DeserializationStrategy<OutgoingPayload> {
        val parentObject = element.jsonObject
        return when (parentObject["action"]?.jsonPrimitive?.content) {
            "MOVE" -> PayloadMove.serializer()
            "ATTACK" -> PayloadAttack.serializer()
            "LEAVE" -> PayloadLeave.serializer()
            else -> throw SerializationException("Unknown action type")
        }
    }
}

@Serializable
data class PayloadMove(
    val field1: Int,
    val field2: String
) : BidirectionalPayload

@Serializable
data class PayloadAttack(
    val targetId: String,
    val damage: Int
) : OutgoingPayload

// Example of an incoming-only payload
@Serializable
data class PayloadJoin(
    val playerId: String,
    val playerName: String
) : IncomingPayload

@Serializable
data class PayloadLeave(
    val playerId: String,
    val reason: String?
) : BidirectionalPayload

// Domain model for Move action
data class MapActionMove(
    val value: Int,
    val character: String
)

// Domain model for Move action
data class MapActionJoin(
    val playerId: String,
    val playerName: String
)

// Extension functions for converting between payloads and domain models
fun PayloadMove.toDomain(): MapActionMove =
    MapActionMove(field1, field2)

fun MapActionMove.toPayload(): PayloadMove =
    PayloadMove(value, character)

fun PayloadJoin.toDomain(): MapActionJoin =
    MapActionJoin(playerId, playerName)

fun MapActionJoin.toPayload(): PayloadJoin =
    PayloadJoin(playerId, playerName)

class WebSocketClient(private val json: Json = Json { ignoreUnknownKeys = true }) {
    fun send(action: MessageAction, payload: OutgoingPayload) {
        val message = WebSocketMessage(action, payload)
        val jsonString = json.encodeToString(WebSocketMessage.serializer(PayloadSerializer), message)
        Logger.d("jsonString: $jsonString")
    }

    fun processMessage(jsonString: String) {
        val jsonElement = json.parseToJsonElement(jsonString)
        val action = MessageAction.valueOf(jsonElement.jsonObject["action"]?.jsonPrimitive?.content ?:
        throw IllegalArgumentException("Invalid message: missing action"))

        when (action) {
            MessageAction.MOVE -> {
                val message = json.decodeFromString<WebSocketMessage<PayloadMove>>(jsonString)
            }
            MessageAction.JOIN -> {
                val message = json.decodeFromString<WebSocketMessage<PayloadJoin>>(jsonString)
            }
            MessageAction.ATTACK -> TODO()
            MessageAction.LEAVE -> TODO()
        }
    }
}
*/
