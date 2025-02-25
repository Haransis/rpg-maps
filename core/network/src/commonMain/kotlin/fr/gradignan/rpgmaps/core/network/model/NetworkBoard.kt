package fr.gradignan.rpgmaps.core.network.model

import fr.gradignan.rpgmaps.core.model.Board
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Serializable
data class NetworkBoard(
    @SerialName("ID") val id: Int,
    @SerialName("name") val name: String,
    @SerialName("url") val url: String,
    @SerialName("owner") val owner: String
)

fun NetworkBoard.toExternal(): Board {
    return Board(
        id = id,
        name = name,
        url = url,
        owner = owner
    )
}

fun List<NetworkBoard>.toExternal() = map(NetworkBoard::toExternal)
