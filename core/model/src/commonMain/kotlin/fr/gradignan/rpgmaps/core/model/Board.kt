package fr.gradignan.rpgmaps.core.model

data class Board(
    val id: Int,
    val name: String,
    val filename: String,
    val owner: String,
    val scale: Float
)
