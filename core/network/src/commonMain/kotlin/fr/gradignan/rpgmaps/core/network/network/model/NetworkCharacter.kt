package fr.gradignan.rpgmaps.core.network.network.model

import fr.gradignan.rpgmaps.core.model.Character
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class NetworkCharacter(
    val owner: String,
    val name: String,
    val speed: Int,
    @SerialName("ID")
    val id: Int,
    val color: String,
    @SerialName("cm_ID")
    val cmId: Int,
    @SerialName("map_ID")
    val mapId: Int,
    @SerialName("char_ID")
    val characterId: Int,
    val x: Int,
    val y: Int
)

fun NetworkCharacter.toExternal(): Character {
    return Character(
        owner = owner,
        name = name,
        speed = speed,
        id = id,
        color = color,
        cmId = cmId,
        mapId = mapId,
        characterId = characterId,
        x = x,
        y = y
    )
}

fun List<NetworkCharacter>.toExternal(): List<Character> = map(NetworkCharacter::toExternal)
