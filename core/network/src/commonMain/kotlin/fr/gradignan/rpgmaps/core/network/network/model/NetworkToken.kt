package fr.gradignan.rpgmaps.core.network.network.model

import fr.gradignan.rpgmaps.core.model.Token
import kotlinx.serialization.Serializable

@Serializable
data class NetworkToken(
    val token: String
)

fun NetworkToken.toExternal(): Token {
    return Token(this.token)
}
