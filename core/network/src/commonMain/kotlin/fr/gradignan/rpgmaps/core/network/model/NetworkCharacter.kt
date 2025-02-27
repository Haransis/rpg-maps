package fr.gradignan.rpgmaps.core.network.model

import co.touchlab.kermit.Logger
import fr.gradignan.rpgmaps.core.model.Character
import fr.gradignan.rpgmaps.core.model.DataCharacter
import fr.gradignan.rpgmaps.core.model.MapCharacter
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class NetworkCharacter(
    val owner: String? = null,
    val name: String? = null,
    val speed: Int? = null,
    @SerialName("ID")
    val id: Int? = null,
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

inline fun <reified T : Character> NetworkCharacter.toExternal(): T {
    return when (T::class) {
        DataCharacter::class -> DataCharacter(
            owner = owner,
            name = name!!,
            speed = speed!!.toFloat(),
            id = id!!,
            color = color!!
        ) as T

        MapCharacter::class -> MapCharacter(
            owner = owner!!,
            name = name!!,
            speed = speed!!.toFloat(),
            id = id!!,
            color = color!!,
            cmId = cmId!!,
            mapId = mapId!!,
            characterId = characterId!!,
            x = x!!,
            y = y!!
        ) as T

        else -> throw IllegalArgumentException("Unsupported type: ${T::class}")
    }
}

inline fun <reified T : Character> List<NetworkCharacter>.toExternal(): List<T> = map(NetworkCharacter::toExternal) /*map { it.toExternal<T>() }*/

fun MapCharacter.toNetwork(): NetworkCharacter {
    return NetworkCharacter(
        owner = owner,
        name = name,
        speed = speed.toInt(),
        id = id,
        color = color,
        cmId = cmId,
        mapId = mapId,
        characterId = characterId,
        x = x,
        y = y
    )
}

fun List<MapCharacter>.toNetwork(): List<NetworkCharacter> = map(MapCharacter::toNetwork)

fun NetworkCharacter.toAddCharacter(): MapCharacter =
    MapCharacter(
        owner = owner!!,
        name = "",
        speed = 0f,
        id = 0,
        color = "",
        cmId = 0,
        mapId = 0,
        characterId = characterId!!,
        x = 0,
        y = 0
    )

