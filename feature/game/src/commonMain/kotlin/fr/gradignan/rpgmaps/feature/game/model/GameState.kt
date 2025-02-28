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
        val laserPosition: Offset? = null,
        val isPingChecked: Boolean = false,
        val isRulerChecked: Boolean = false,
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
        val ruler: DistancePath = DistancePath(),
        val previewPath: DistancePath = DistancePath(),
        val imageUrl: String? = null
    ): GameState()
}

sealed class MapState {
    data object Loading: MapState()
    data class Error(val error: UiText): MapState()
    data class Map(
        val laserPosition: Offset? = null,
        val isPingChecked: Boolean = false,
        val isRulerChecked: Boolean = false,
        val mapScale: Float = 30f,
        val mapCharacters: List<MapCharacter> = emptyList(),
        val selectedMapCharacter: MapCharacter? = null,
        val hoveredCharacterId: Int? = null,
        val playingMapCharacter: MapCharacter? = null,
        val pings: List<MapEffect.Ping> = emptyList(),
        val error: UiText? = null,
        val isAdmin: Boolean = false,
        val isSprintChecked: Boolean = false,
        val isGmChecked: Boolean = false,
        val ruler: DistancePath = DistancePath(),
        val previewPath: DistancePath = DistancePath(),
        val imageUrl: String? = null
    ): MapState()
}

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

data class StatusState (
    val characters: List<CharItem> = emptyList(),
    val isAdmin: Boolean = false
)

data class DistancePath(
    val reachable: List<Offset> = emptyList(),
    val totalDistance: Float = 0f,
    val unreachableStop: Offset? = null
)

data class CharItem(val index: Int, val name: String, val optionalId: Int? = null)
