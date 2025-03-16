package fr.gradignan.rpgmaps.core.model

sealed class MapAction {
    data class Connect(val user: String) : MapAction()
    data class Initiate(val mapCharacters: List<MapCharacter>) : MapAction()
    data class GMGetMap(val mapCharacters: List<MapCharacter>) : MapAction()
    data class LoadMap(val id: Int, val mapFilename: String, val mapScale: Float) : MapAction()
    data class AddCharacter(val character: MapCharacter, val order: List<Int>) : MapAction()
    data class InitiativeOrder(val order: List<Int>) : MapAction()
    data class Move(val name: String, val x: Int, val y: Int, val owner: String, val id: Int) : MapAction()
    data object NewTurn : MapAction()
    data class Next(val id: Int) : MapAction()
    data class Ping(val x: Int, val y: Int) : MapAction()
}
