package fr.gradignan.rpgmaps.core.model

sealed class MapEffect: MapAction {
    data class Ping(val x: Int, val y: Int) : MapEffect()
}
