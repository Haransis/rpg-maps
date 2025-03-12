package fr.gradignan.rpgmaps.core.network.model

import fr.gradignan.rpgmaps.core.model.Board
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Serializable
data class NetworkBoard(
    @SerialName("ID") val id: Int,
    val name: String,
    val filename: String,
    val owner: String,
    val scale: Float
)

fun NetworkBoard.toExternal(): Board {
    return Board(
        id = id,
        name = name,
        filename = filename,
        owner = owner,
        scale = scale
    )
}

fun List<NetworkBoard>.toExternal() = map(NetworkBoard::toExternal)
