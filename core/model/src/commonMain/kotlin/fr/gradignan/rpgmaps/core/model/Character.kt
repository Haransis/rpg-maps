package fr.gradignan.rpgmaps.core.model

data class Character(
    val owner: String,
    val name: String,
    val speed: Int,
    val id: Int,
    val color: String,
    val cmId: Int,
    val mapId: Int,
    val characterId: Int,
    val x: Int,
    val y: Int
)
