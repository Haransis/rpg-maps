package fr.gradignan.rpgmaps.core.network.model

import co.touchlab.kermit.Logger
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.*
import kotlinx.serialization.encoding.*
import kotlinx.serialization.json.*

@Serializable(with = ServerMessage.Serializer::class)
data class ServerMessage(
    val action: String,
    val payload: Payload
) {
    object Serializer : KSerializer<ServerMessage> {
        override val descriptor: SerialDescriptor = buildClassSerialDescriptor("ServerMessage") {
            element<String>("action")
        }

        override fun deserialize(decoder: Decoder): ServerMessage {
            require(decoder is JsonDecoder) { "This serializer only supports JSON" }
            val jsonElement = decoder.decodeJsonElement()
            val jsonObject = jsonElement.jsonObject
            Logger.d("jsonObject: $jsonObject")

            val action = jsonObject["action"]?.jsonPrimitive?.content
                ?: throw SerializationException("Missing 'action' field")

            val payload: Payload = when (action) {
                "Connect" -> decoder.json.decodeFromJsonElement(Payload.ServerConnect.serializer(), jsonObject)
                "GMGetMap" -> decoder.json.decodeFromJsonElement(Payload.ServerGMGetMap.serializer(), jsonObject)
                "Initiate" -> decoder.json.decodeFromJsonElement(Payload.ServerInitiate.serializer(), jsonObject)
                "Initiative" -> Payload.ServerStartGame
                "InitiativeOrder" -> decoder.json.decodeFromJsonElement(Payload.ServerInitiativeOrder.serializer(), jsonObject)
                "LoadMap" -> decoder.json.decodeFromJsonElement(Payload.ServerLoadMap.serializer(), jsonObject)
                "Move" -> decoder.json.decodeFromJsonElement(Payload.ServerMove.serializer(), jsonObject)
                "NewChar" -> decoder.json.decodeFromJsonElement(Payload.ServerAddCharacterOutput.serializer(), jsonObject)
                "NewTurn" -> Payload.ServerNewTurn
                "Next" -> decoder.json.decodeFromJsonElement(Payload.ServerNext.serializer(), jsonObject)
                "Ping" -> decoder.json.decodeFromJsonElement(Payload.ServerPing.serializer(), jsonObject)
                else -> throw SerializationException("Unknown action: $action")
            }
            return ServerMessage(action, payload)
        }

        override fun serialize(encoder: Encoder, value: ServerMessage) {
            require(encoder is JsonEncoder) { "This serializer only supports JSON" }
            val jsonObject = buildJsonObject {
                put("action", JsonPrimitive(value.action))
                when (val payload = value.payload) {
                    is Payload.ServerConnect -> {
                        put("user", JsonPrimitive(payload.user))
                    }
                    is Payload.ServerInitiate -> {
                        put("characters", encoder.json.encodeToJsonElement(Payload.ServerInitiate.serializer(), payload))
                    }
                    is Payload.ServerLoadMap -> {
                        put("map_id", JsonPrimitive(payload.id))
                        put("map", JsonPrimitive(payload.map))
                    }
                    Payload.ServerNewTurn -> { }
                    Payload.ServerStartGame -> { }
                    is Payload.ServerMove -> {
                        put("name", JsonPrimitive(payload.name))
                        put("x", JsonPrimitive(payload.x))
                        put("y", JsonPrimitive(payload.y))
                        put("owner", JsonPrimitive(payload.owner))
                        put("id", JsonPrimitive(payload.id))
                    }
                    is Payload.ServerNext -> {
                        put("ID", JsonPrimitive(payload.id))
                    }
                    is Payload.ServerPing -> {
                        put("x", JsonPrimitive(payload.x))
                        put("y", JsonPrimitive(payload.y))
                    }
                    is Payload.ServerGMGetMap -> {
                        put("characters", encoder.json.encodeToJsonElement(Payload.ServerGMGetMap.serializer(), payload))
                    }
                    is Payload.ServerAddCharacterInput -> {
                        put("character", encoder.json.encodeToJsonElement(payload.character))
                        put("order", encoder.json.encodeToJsonElement(payload.order))
                    }
                    is Payload.ServerAddCharacterOutput -> throw IllegalStateException("not handled")
                    is Payload.ServerInitiativeOrder -> {
                        put("order", encoder.json.encodeToJsonElement(payload.order))
                    }
                }
            }
            encoder.encodeJsonElement(jsonObject)
        }
    }
}
