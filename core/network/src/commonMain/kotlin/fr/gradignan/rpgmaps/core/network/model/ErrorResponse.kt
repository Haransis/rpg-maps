package fr.gradignan.rpgmaps.core.network.model

import kotlinx.serialization.Serializable

@Serializable
data class ErrorResponse (
    val detail: String
)
