package fr.gradignan.rpgmaps.core.network.model

import fr.gradignan.rpgmaps.core.model.MapAction
import fr.gradignan.rpgmaps.core.network.BuildKonfig
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonClassDiscriminator


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
        is PayloadConnect -> MapAction.Connect(data.username)
        is PayloadInitiate -> MapAction.Initiate(data.characters.toExternal())
        is PayloadLoadMapInput -> MapAction.LoadMap(data.id, "${BuildKonfig.baseUrl}/maps/map-image/${data.id}", data.mapScale)
        is PayloadMove -> MapAction.Move(data.name, data.x, data.y, data.owner, data.id)
        is PayloadNewTurn -> MapAction.NewTurn
        is PayloadNext -> MapAction.Next(data.cmId)
        is PayloadPing -> MapAction.Ping(data.x.toInt(), data.y.toInt())
        is PayloadGMGetMap -> MapAction.GMGetMap(data.characters.toExternal())
        is PayloadAddCharacterOutput -> MapAction.AddCharacter(data.character.toExternal(), emptyList())
        is PayloadInitiativeOrder -> MapAction.InitiativeOrder(data.order)
        is PayloadAddCharacterInput, is PayloadStartGame -> throw IllegalStateException("not handled")
    }

fun MapAction.toOutgoingPayload(): OutgoingPayload =
    when (this) {
        is MapAction.Ping -> PayloadPing(PingData(x.toFloat(), y.toFloat()))
        is MapAction.Connect -> PayloadConnect(ConnectData(user))
        is MapAction.Initiate -> PayloadStartGame()
        is MapAction.LoadMap -> PayloadLoadMapOutput(LoadMapDataOutput(id))
        is MapAction.Move -> PayloadMove(MoveData(name, x, y, owner, id))
        MapAction.NewTurn -> PayloadNewTurn()
        is MapAction.Next -> PayloadNext(NextData(id))
        is MapAction.GMGetMap -> PayloadGMGetMap(GMGetMapData(mapCharacters.toNetwork()))
        is MapAction.AddCharacter -> PayloadAddCharacterInput(AddCharacterInputData(character.toNetwork(), order))
        is MapAction.InitiativeOrder -> PayloadInitiativeOrder(InitiativeOrderData(order))
    }
