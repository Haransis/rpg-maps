package fr.gradignan.rpgmaps.core.network.model

import fr.gradignan.rpgmaps.core.model.MapAction
import fr.gradignan.rpgmaps.core.model.MapEffect
import fr.gradignan.rpgmaps.core.model.MapUpdate
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
sealed class Payload {
    @SerialName("Connect")
    @Serializable
    data class ServerConnect(val user: String) : Payload()

    @SerialName("Initiate")
    @Serializable
    data class ServerInitiate(val characters: List<NetworkCharacter>) : Payload()

    @SerialName("LoadMap")
    @Serializable
    data class ServerLoadMap(val map: String) : Payload()

    @SerialName("Move")
    @Serializable
    data class ServerMove(val name: String, val x: Int, val y: Int, val owner: String, val id: Int) : Payload()

    @SerialName("NewTurn")
    @Serializable
    data object ServerNewTurn : Payload()

    @SerialName("Next")
    @Serializable
    data class ServerNext(@SerialName("ID") val id: Int) : Payload()

    @SerialName("Ping")
    @Serializable
    data class ServerPing(val x: Float, val y: Float) : Payload()
}

fun Payload.toMapAction(): MapAction =
    when (this) {
        is Payload.ServerConnect -> MapUpdate.Connect(user)
        is Payload.ServerInitiate -> MapUpdate.Initiate(characters.toExternal())
        is Payload.ServerLoadMap -> MapUpdate.LoadMap(map)
        is Payload.ServerMove -> MapUpdate.Move(name, x, y, owner, id)
        Payload.ServerNewTurn -> MapUpdate.NewTurn
        is Payload.ServerNext -> MapUpdate.Next(id)
        is Payload.ServerPing -> MapEffect.Ping(x.toInt(), y.toInt())
    }

fun MapAction.toServerMessage(): ServerMessage =
    when (this) {
        is MapEffect.Ping -> ServerMessage(
            action = "Ping",
            payload = Payload.ServerPing(x.toFloat(), y.toFloat()))
        is MapUpdate.Connect -> ServerMessage(
            action = "Connect",
            payload = Payload.ServerConnect(user))
        is MapUpdate.Initiate -> ServerMessage(
            action = "Initiate",
            payload = Payload.ServerInitiate(characters.toNetwork()))
        is MapUpdate.LoadMap -> ServerMessage(
            action = "LoadMap",
            payload = Payload.ServerLoadMap(map))
        is MapUpdate.Move -> ServerMessage(
            action = "Move",
            payload = Payload.ServerMove(name, x, y, owner, id))
        MapUpdate.NewTurn -> ServerMessage(
            action = "NewTurn",
            payload = Payload.ServerNewTurn)
        is MapUpdate.Next -> ServerMessage(
            action = "Next",
            payload = Payload.ServerNext(id))
    }
