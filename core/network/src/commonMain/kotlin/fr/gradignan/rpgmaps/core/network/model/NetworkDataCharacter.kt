package fr.gradignan.rpgmaps.core.network.model

import fr.gradignan.rpgmaps.core.model.DataCharacter
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class NetworkDataCharacter(
    val owner: String,
    val name: String,
    val speed: Int,
    val color: String,
    @SerialName("ID") val id: Int
)

fun NetworkDataCharacter.toExternal(): DataCharacter {
    return DataCharacter(
            owner = owner,
            name = name,
            speed = speed.toFloat(),
            color = color,
            cmId = id
        )
    }


fun List<NetworkDataCharacter>.toExternal(): List<DataCharacter> = map(NetworkDataCharacter::toExternal)

fun DataCharacter.toNetwork(): NetworkDataCharacter {
    return NetworkDataCharacter(
        owner = owner,
        name = name,
        speed = speed.toInt(),
        color = color,
        id = cmId
    )
}

fun List<DataCharacter>.toNetwork(): List<NetworkDataCharacter> = map(DataCharacter::toNetwork)

fun NetworkDataCharacter.toAddCharacter(): DataCharacter =
    DataCharacter(
        owner = owner,
        name = "",
        speed = 0f,
        color = "",
        cmId = id
    )

