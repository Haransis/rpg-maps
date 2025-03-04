package fr.gradignan.rpgmaps.feature.game.model

import fr.gradignan.rpgmaps.core.model.Board
import fr.gradignan.rpgmaps.core.model.DataCharacter
import fr.gradignan.rpgmaps.core.ui.error.UiText


sealed class GmState {
    data object Loading: GmState()
    data class Error(val error: UiText): GmState()
    data class Gm(
        val boards: List<Board> = emptyList(),
        val selectedBoard: Board? = null,
        val selectedChar: String? = null,
        val availableCharacters: List<DataCharacter> = emptyList(),
        val isAdmin: Boolean = false
    ): GmState()
}
