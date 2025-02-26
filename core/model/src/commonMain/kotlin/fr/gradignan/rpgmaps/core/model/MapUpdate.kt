package fr.gradignan.rpgmaps.core.model

sealed class MapUpdate: MapAction {
    data class Connect(val user: String) : MapUpdate()

    data class Initiate(val characters: List<Character>) : MapUpdate()
    data class GMGetMap(val characters: List<Character>) : MapUpdate()

    data class LoadMap(val id: Int, val map: String) : MapUpdate()

    data class Move(val name: String, val x: Int, val y: Int, val owner: String, val id: Int) : MapUpdate()

    data object NewTurn : MapUpdate()

    data class Next(val id: Int) : MapUpdate()
}
