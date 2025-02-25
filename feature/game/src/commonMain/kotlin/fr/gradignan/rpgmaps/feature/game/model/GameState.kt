package fr.gradignan.rpgmaps.feature.game.model

import androidx.compose.ui.geometry.Offset
import fr.gradignan.rpgmaps.core.model.Character
import fr.gradignan.rpgmaps.core.model.MapEffect
import fr.gradignan.rpgmaps.core.ui.error.UiText

sealed class GameState {
    data object Loading: GameState()
    data class Error(val error: UiText): GameState()
    data class Game(
        val mapScale: Float = 30f,
        val playerName: String = "",
        val characters: List<Character> = emptyList(),
        val selectedCharacter: Character? = null,
        val hoveredCharacterId: Int? = null,
        val playingCharacter: Character? = null,
        val pings: List<MapEffect.Ping> = emptyList(),
        val error: UiText? = null,
        val isPlayerTurn: Boolean = false,
        val admin: Boolean = false,
        val sprintChecked: Boolean = false,
        val logs: List<String> = emptyList(),
        val previewPath: PreviewPath = PreviewPath(),
        val map: String? = null
    ): GameState()
}

data class PreviewPath(
    val reachable: List<Offset> = emptyList(),
    val unreachableStop: Offset? = null,
    val totalDistance: Float = 0f
)
