package fr.gradignan.rpgmaps.core.network.model

import fr.gradignan.rpgmaps.core.model.MapCharacter
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class NetworkMapCharacter(
    val owner: String,
    val name: String,
    val speed: Int,
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

fun NetworkMapCharacter.toExternal(): MapCharacter {
    return MapCharacter(
            owner = owner,
            name = name,
            speed = speed.toFloat(),
            color = color,
            cmId = cmId,
            mapId = mapId,
            characterId = characterId,
            x = x,
            y = y
        )
    }


fun List<NetworkMapCharacter>.toExternal(): List<MapCharacter> = map(NetworkMapCharacter::toExternal)

fun MapCharacter.toNetwork(): NetworkMapCharacter {
    return NetworkMapCharacter(
        owner = owner,
        name = name,
        speed = speed.toInt(),
        color = color,
        cmId = cmId,
        mapId = mapId,
        characterId = characterId,
        x = x,
        y = y
    )
}

fun List<MapCharacter>.toNetwork(): List<NetworkMapCharacter> = map(MapCharacter::toNetwork)
