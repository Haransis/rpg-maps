package fr.gradignan.rpgmaps.core.model

sealed class MapUpdate: MapAction {
    data class Connect(val user: String) : MapUpdate()
    data class Initiate(val mapCharacters: List<MapCharacter>) : MapUpdate()
    data class GMGetMap(val mapCharacters: List<MapCharacter>) : MapUpdate()
    data class LoadMap(val id: Int, val mapFilename: String, val mapScale: Float) : MapUpdate()
    data class AddCharacter(val character: MapCharacter, val order: List<Int>) : MapUpdate()
    data class InitiativeOrder(val order: List<Int>) : MapUpdate()
    data class Move(val name: String, val x: Int, val y: Int, val owner: String, val id: Int) : MapUpdate()
    data object NewTurn : MapUpdate()
    data class Next(val id: Int) : MapUpdate()
}
