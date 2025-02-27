package fr.gradignan.rpgmaps.core.model

interface Character

data class DataCharacter(
    val owner: String?,
    val name: String,
    val speed: Float,
    val id: Int,
    val color: String
): Character

data class MapCharacter(
    val owner: String,
    val name: String,
    val speed: Float,
    val id: Int,
    val color: String,
    val cmId: Int,
    val mapId: Int,
    val characterId: Int,
    val x: Int,
    val y: Int
): Character
