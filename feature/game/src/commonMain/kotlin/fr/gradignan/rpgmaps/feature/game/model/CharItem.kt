package fr.gradignan.rpgmaps.feature.game.model

import fr.gradignan.rpgmaps.core.model.MapCharacter
import fr.gradignan.rpgmaps.core.ui.compose.Item


data class CharItem(
    override val index: Int,
    override val name: String,
    override val optionalId: Int?,
    val owner: String,
): Item

fun List<MapCharacter>.toCharItems(): List<CharItem> = this.mapIndexed { index, mapCharacter -> mapCharacter.toCharItem(index) }
fun MapCharacter.toCharItem(index: Int): CharItem =
    CharItem(
        index = index,
        name = this.name,
        owner = this.owner,
        optionalId = this.cmId
    )
