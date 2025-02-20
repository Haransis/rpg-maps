package fr.gradignan.rpgmaps.feature.game.model

import fr.gradignan.rpgmaps.core.model.Character

sealed class MapOverlayState {
    data object Loading : MapOverlayState()
    data class UiStateMap(
        val characters: List<Character> = emptyList(),
        val map: String = "",
        val isPlayerTurn: Boolean = false,
        val logs: List<String> = emptyList(),
        val errorMessage: String? = null
    ) : MapOverlayState()
}
