package fr.gradignan.rpgmaps.core.network.model

import fr.gradignan.rpgmaps.core.model.MapCharacter
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class NetworkAddCharacter(
    val owner: String,
    @SerialName("char_ID")
    val characterId: Int,
    val x: Int,
    val y: Int
)

fun NetworkAddCharacter.toMapCharacter(): MapCharacter =
    MapCharacter(
        owner = owner,
        name = "",
        speed = 0f,
        color = "",
        cmId = 0,
        mapId = 0,
        characterId = characterId,
        x = x,
        y = y
    )

fun MapCharacter.toAddCharacter(): NetworkAddCharacter =
    NetworkAddCharacter(
        owner = owner,
        characterId = characterId,
        x = x,
        y = y
    )
