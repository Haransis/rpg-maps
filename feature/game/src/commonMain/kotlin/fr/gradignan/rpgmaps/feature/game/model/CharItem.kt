package fr.gradignan.rpgmaps.feature.game.model

import fr.gradignan.rpgmaps.core.model.MapCharacter


data class CharItem(val index: Int, val name: String, val optionalId: Int? = null)

fun List<MapCharacter>.toCharItems(): List<CharItem> = this.mapIndexed { index, mapCharacter -> mapCharacter.toCharItem(index) }
fun MapCharacter.toCharItem(index: Int): CharItem =
    CharItem(
        index = index,
        name = this.name,
        optionalId = this.cmId
    )
