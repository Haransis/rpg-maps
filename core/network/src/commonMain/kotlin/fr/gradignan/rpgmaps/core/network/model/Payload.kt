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

@Serializable
@SerialName("Connect")
data class PayloadConnect(
    val data: ConnectData
) : IncomingPayload, OutgoingPayload

@Serializable
data class ConnectData(
    val username: String,
)

@Serializable
@SerialName("Initiative")
data class PayloadStartGame(
    val data: EmptyData = EmptyData
) : IncomingPayload, OutgoingPayload

@Serializable
data object EmptyData

@Serializable
@SerialName("InitiativeOrder")
data class PayloadInitiativeOrder(
    val data: InitiativeOrderData
) : IncomingPayload, OutgoingPayload

@Serializable
data class InitiativeOrderData(
    val order: List<Int>
)

@Serializable
@SerialName("Initiate")
data class PayloadInitiate(
    val data: InitiateData
) : IncomingPayload, OutgoingPayload

@Serializable
data class InitiateData(
    val characters: List<NetworkMapCharacter>
)

@Serializable
@SerialName("LoadMap")
data class PayloadLoadMapOutput(
    val data: LoadMapDataOutput
) : OutgoingPayload

@Serializable
data class LoadMapDataOutput(
    @SerialName("map_ID") val id: Int
)

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

@Serializable
@SerialName("NewChar")
data class PayloadAddCharacterInput(
    val data: AddCharacterInputData
) : IncomingPayload

@Serializable
data class AddCharacterInputData(
    val character: NetworkMapCharacter
)

@Serializable
@SerialName("AddChar")
data class PayloadAddCharacterOutput(
    val data: AddCharacterOutputData
) : OutgoingPayload

@Serializable
data class AddCharacterOutputData(
    val character: NetworkAddCharacter,
    val order: List<Int>
)

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
    @SerialName("cm_id") val cmId: Int
)

@Serializable
@SerialName("NewTurn")
data class PayloadNewTurn(
    val data: EmptyData = EmptyData
) : IncomingPayload, OutgoingPayload

@Serializable
@SerialName("Next")
data class PayloadNext(
    val data: NextData
) : IncomingPayload, OutgoingPayload

@Serializable
data class NextData(
    @SerialName("cm_ID") val cmId: Int
)

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
        is PayloadMove -> MapAction.Move(data.name, data.x, data.y, data.owner, data.cmId)
        is PayloadNewTurn -> MapAction.NewTurn
        is PayloadNext -> MapAction.Next(data.cmId)
        is PayloadPing -> MapAction.Ping(data.x.toInt(), data.y.toInt())
        is PayloadGMGetMap -> MapAction.GMGetMap(data.characters.toExternal())
        is PayloadInitiativeOrder -> MapAction.InitiativeOrder(data.order)
        is PayloadAddCharacterInput -> MapAction.AddCharacter(data.character.toExternal(), emptyList())
        is PayloadStartGame -> MapAction.Initiate(emptyList())
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
        is MapAction.InitiativeOrder -> PayloadInitiativeOrder(InitiativeOrderData(order))
        is MapAction.AddCharacter -> PayloadAddCharacterOutput(AddCharacterOutputData(character.toAddCharacter(), order))
    }
