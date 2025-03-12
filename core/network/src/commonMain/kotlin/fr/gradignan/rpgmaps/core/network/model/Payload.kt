package fr.gradignan.rpgmaps.core.network.model

import fr.gradignan.rpgmaps.core.model.MapAction
import fr.gradignan.rpgmaps.core.model.MapEffect
import fr.gradignan.rpgmaps.core.model.MapUpdate
import fr.gradignan.rpgmaps.core.network.BuildKonfig
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonClassDiscriminator

/*@Serializable
sealed class Payload {
    @SerialName("Connect")
    @Serializable
    data class ServerConnect(val user: String) : Payload()

    @SerialName("Initiative")
    @Serializable
    data object ServerStartGame : Payload()

    @SerialName("InitiativeOrder")
    @Serializable
    data class ServerInitiativeOrder(val order: List<Int>) : Payload()

    @SerialName("Initiate")
    @Serializable
    data class ServerInitiate(val characters: List<NetworkMapCharacter>) : Payload()

    @SerialName("LoadMap")
    @Serializable
    data class ServerLoadMap(@SerialName("map_ID") val id: Int?, @SerialName("map_filename") val mapFilename: String, @SerialName("map_scale") val mapScale: Float) : Payload()

    @SerialName("AddChar")
    @Serializable
    data class ServerAddCharacterInput(val character: NetworkMapCharacter, val order: List<Int>) : Payload()

    @SerialName("NewChar")
    @Serializable
    data class ServerAddCharacterOutput(val character: NetworkMapCharacter) : Payload()

    @SerialName("Move")
    @Serializable
    data class ServerMove(val name: String, val x: Int, val y: Int, val owner: String, val id: Int) : Payload()

    @SerialName("NewTurn")
    @Serializable
    data object ServerNewTurn : Payload()

    @SerialName("Next")
    @Serializable
    data class ServerNext(@SerialName("cm_ID") val cmId: Int) : Payload()

    @SerialName("Ping")
    @Serializable
    data class ServerPing(val x: Float, val y: Float) : Payload()

    @SerialName("GMGetMap")
    @Serializable
    data class ServerGMGetMap(val characters: List<NetworkMapCharacter>) : Payload()
}*/

// Base interface for all payloads
@OptIn(ExperimentalSerializationApi::class)
@Serializable
@JsonClassDiscriminator("action")
sealed interface IncomingPayload
@OptIn(ExperimentalSerializationApi::class)
@Serializable
@JsonClassDiscriminator("action")
sealed interface OutgoingPayload

// Connect
@Serializable
@SerialName("Connect")
data class PayloadConnect(
    val data: ConnectData
) : IncomingPayload, OutgoingPayload

@Serializable
data class ConnectData(
    val username: String,
)

// StartGame
@Serializable
@SerialName("Initiative")
data class PayloadStartGame(
    val data: EmptyData = EmptyData
) : IncomingPayload, OutgoingPayload

@Serializable
data object EmptyData

// InitiativeOrder
@Serializable
@SerialName("InitiativeOrder")
data class PayloadInitiativeOrder(
    val data: InitiativeOrderData
) : IncomingPayload, OutgoingPayload

@Serializable
data class InitiativeOrderData(
    val order: List<Int>
)

// Initiate
@Serializable
@SerialName("Initiate")
data class PayloadInitiate(
    val data: InitiateData
) : IncomingPayload, OutgoingPayload

@Serializable
data class InitiateData(
    val characters: List<NetworkMapCharacter>
)

// LoadMap
@Serializable
@SerialName("LoadMap")
data class PayloadLoadMapOutput(
    val data: LoadMapDataOutput
) : OutgoingPayload

@Serializable
data class LoadMapDataOutput(
    @SerialName("map_ID") val id: Int
)

// LoadMap
@Serializable
@SerialName("LoadMap")
data class PayloadLoadMapInput(
    val data: LoadMapInputData
) : IncomingPayload

@Serializable
data class LoadMapInputData(
    @SerialName("map_ID") val id: Int,
    @SerialName("map_scale") val mapScale: Float
)

// AddCharacterInput
@Serializable
@SerialName("AddChar")
data class PayloadAddCharacterInput(
    val data: AddCharacterInputData
) : IncomingPayload, OutgoingPayload

@Serializable
data class AddCharacterInputData(
    val character: NetworkMapCharacter,
    val order: List<Int>
)

// AddCharacterOutput
@Serializable
@SerialName("NewChar")
data class PayloadAddCharacterOutput(
    val data: AddCharacterOutputData
) : IncomingPayload, OutgoingPayload

@Serializable
data class AddCharacterOutputData(
    val character: NetworkMapCharacter
)

// Move
@Serializable
@SerialName("Move")
data class PayloadMove(
    val data: MoveData
) : IncomingPayload, OutgoingPayload

@Serializable
data class MoveData(
    val name: String,
    val x: Int,
    val y: Int,
    val owner: String,
    val id: Int
)

// NewTurn
@Serializable
@SerialName("NewTurn")
data class PayloadNewTurn(
    val data: EmptyData = EmptyData
) : IncomingPayload, OutgoingPayload

// Next
@Serializable
@SerialName("Next")
data class PayloadNext(
    val data: NextData
) : IncomingPayload, OutgoingPayload

@Serializable
data class NextData(
    @SerialName("cm_ID") val cmId: Int
)

// Ping
@Serializable
@SerialName("Ping")
data class PayloadPing(
    val data: PingData
) : IncomingPayload, OutgoingPayload

@Serializable
data class PingData(
    val x: Float,
    val y: Float
)

// GMGetMap
@Serializable
@SerialName("GMGetMap")
data class PayloadGMGetMap(
    val data: GMGetMapData
) : IncomingPayload, OutgoingPayload

@Serializable
data class GMGetMapData(
    val characters: List<NetworkMapCharacter>
)

fun IncomingPayload.toMapAction(): MapAction =
    when (this) {
        is PayloadConnect -> MapUpdate.Connect(data.username)
        is PayloadInitiate -> MapUpdate.Initiate(data.characters.toExternal())
        is PayloadLoadMapInput -> MapUpdate.LoadMap(data.id, "${BuildKonfig.baseUrl}/maps/map-image/${data.id}", data.mapScale)
        is PayloadMove -> MapUpdate.Move(data.name, data.x, data.y, data.owner, data.id)
        is PayloadNewTurn -> MapUpdate.NewTurn
        is PayloadNext -> MapUpdate.Next(data.cmId)
        is PayloadPing -> MapEffect.Ping(data.x.toInt(), data.y.toInt())
        is PayloadGMGetMap -> MapUpdate.GMGetMap(data.characters.toExternal())
        is PayloadAddCharacterOutput -> MapUpdate.AddCharacter(data.character.toExternal(), emptyList())
        is PayloadInitiativeOrder -> MapUpdate.InitiativeOrder(data.order)
        is PayloadAddCharacterInput, is PayloadStartGame -> throw IllegalStateException("not handled")
    }

fun MapAction.toOutgoingPayload(): OutgoingPayload =
    when (this) {
        is MapEffect.Ping -> PayloadPing(PingData(x.toFloat(), y.toFloat()))
        is MapUpdate.Connect -> PayloadConnect(ConnectData(user))
        is MapUpdate.Initiate -> PayloadStartGame()
        is MapUpdate.LoadMap -> PayloadLoadMapOutput(LoadMapDataOutput(id))
        is MapUpdate.Move -> PayloadMove(MoveData(name, x, y, owner, id))
        MapUpdate.NewTurn -> PayloadNewTurn()
        is MapUpdate.Next -> PayloadNext(NextData(id))
        is MapUpdate.GMGetMap -> PayloadGMGetMap(GMGetMapData(mapCharacters.toNetwork()))
        is MapUpdate.AddCharacter -> PayloadAddCharacterInput(AddCharacterInputData(character.toNetwork(), order))
        is MapUpdate.InitiativeOrder -> PayloadInitiativeOrder(InitiativeOrderData(order))
    }
