package fr.gradignan.rpgmaps.feature.game.model

import androidx.compose.ui.geometry.Offset


data class DistancePath(
    val reachable: List<Offset> = emptyList(),
    val totalDistance: Float = 0f,
    val unreachableStop: Offset? = null,
)
