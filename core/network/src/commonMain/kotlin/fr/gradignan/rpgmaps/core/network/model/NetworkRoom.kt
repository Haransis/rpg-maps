package fr.gradignan.rpgmaps.core.network.model

import fr.gradignan.rpgmaps.core.model.Room
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Serializable
data class NetworkRoom(
    @SerialName("room_id") val roomId: Int,
    val username: String,
    @SerialName("role_in_room") val roleInRoom: String,
    @SerialName("room_attr_id") val roomAttrId: Int,
    val name: String,
    val id: Int,
)

fun NetworkRoom.toExternal(): Room {
    return Room(
        roomId = roomId,
        username = username,
        roleInRoom = roleInRoom,
        roomAttrId = roomAttrId,
        name = name,
    )
}

fun List<NetworkRoom>.toExternal(): List<Room> = map(NetworkRoom::toExternal)
