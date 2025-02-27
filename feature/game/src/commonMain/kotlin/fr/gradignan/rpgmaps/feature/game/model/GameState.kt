package fr.gradignan.rpgmaps.feature.game.model

import androidx.compose.ui.geometry.Offset
import fr.gradignan.rpgmaps.core.model.Board
import fr.gradignan.rpgmaps.core.model.DataCharacter
import fr.gradignan.rpgmaps.core.model.MapCharacter
import fr.gradignan.rpgmaps.core.model.MapEffect
import fr.gradignan.rpgmaps.core.ui.error.UiText

sealed class GameState {
    data object Loading: GameState()
    data class Error(val error: UiText): GameState()
    data class Game(
        val boards: List<Board> = emptyList(),
        val selectedBoard: Board? = null,
        val selectedChar: String? = null,
        val mapScale: Float = 30f,
        val playerName: String = "",
        val availableCharacters: List<DataCharacter> = emptyList(),
        val mapCharacters: List<MapCharacter> = emptyList(),
        val selectedMapCharacter: MapCharacter? = null,
        val hoveredCharacterId: Int? = null,
        val playingMapCharacter: MapCharacter? = null,
        val pings: List<MapEffect.Ping> = emptyList(),
        val error: UiText? = null,
        val isPlayerTurn: Boolean = false,
        val isAdmin: Boolean = false,
        val isSprintChecked: Boolean = false,
        val isGmChecked: Boolean = false,
        val logs: List<String> = emptyList(),
        val previewPath: PreviewPath = PreviewPath(),
        val imageUrl: String? = null
    ): GameState()
}

data class PreviewPath(
    val reachable: List<Offset> = emptyList(),
    val unreachableStop: Offset? = null,
    val totalDistance: Float = 0f
)
