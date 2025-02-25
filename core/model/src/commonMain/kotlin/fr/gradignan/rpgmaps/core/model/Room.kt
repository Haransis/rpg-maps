package fr.gradignan.rpgmaps.core.model

data class Room(
    val roomId: Int,
    val username: String,
    val roleInRoom: String,
    val roomAttrId: Int,
    val name: String,
)
