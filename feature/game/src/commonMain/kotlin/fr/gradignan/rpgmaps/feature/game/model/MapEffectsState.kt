package fr.gradignan.rpgmaps.feature.game.model

import fr.gradignan.rpgmaps.core.model.MapEffect

data class MapEffectsState(
    val pings: List<MapEffect.Ping> = emptyList()
)
