package fr.gradignan.rpgmaps.feature.game.model

import fr.gradignan.rpgmaps.core.model.MapCharacter


data class CharItem(val index: Int, val name: String, val optionalId: Int? = null)

fun List<MapCharacter>.toCharItems(): List<CharItem> = this.map { it.toCharItem() }
fun MapCharacter.toCharItem(): CharItem =
    CharItem(
        index = this.cmId,
        name = this.name,
        optionalId = this.id
    )
