package fr.gradignan.rpgmaps.core.network.model

import fr.gradignan.rpgmaps.core.model.Character
import fr.gradignan.rpgmaps.core.model.DataCharacter
import fr.gradignan.rpgmaps.core.model.MapCharacter
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class NetworkMapCharacter(
    val owner: String? = null,
    val name: String? = null,
    val speed: Int? = null,
    val color: String? = null,
    @SerialName("cm_ID")
    val cmId: Int? = null,
    @SerialName("map_ID")
    val mapId: Int? = null,
    @SerialName("char_ID")
    val characterId: Int? = null,
    val x: Int? = null,
    val y: Int? = null
)

fun NetworkMapCharacter.toExternal(): MapCharacter {
    return MapCharacter(
            owner = owner!!,
            name = name!!,
            speed = speed!!.toFloat(),
            color = color!!,
            cmId = cmId!!,
            mapId = mapId!!,
            characterId = characterId!!,
            x = x!!,
            y = y!!
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

fun NetworkMapCharacter.toAddCharacter(): MapCharacter =
    MapCharacter(
        owner = owner!!,
        name = "",
        speed = 0f,
        color = "",
        cmId = 0,
        mapId = 0,
        characterId = characterId!!,
        x = 0,
        y = 0
    )

