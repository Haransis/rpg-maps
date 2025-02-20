package fr.gradignan.rpgmaps.core.network.network.model

import fr.gradignan.rpgmaps.core.model.Auth
import kotlinx.serialization.Serializable

@Serializable
data class NetworkAuth (
    val username: String,
    val password: String,
)

fun Auth.toNetwork() = NetworkAuth(
    username = name,
    password = password,
)
