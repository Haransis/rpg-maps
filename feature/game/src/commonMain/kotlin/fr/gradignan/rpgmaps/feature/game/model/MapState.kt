package fr.gradignan.rpgmaps.feature.game.model

import androidx.compose.ui.geometry.Offset
import fr.gradignan.rpgmaps.core.model.Board
import fr.gradignan.rpgmaps.core.model.DataCharacter
import fr.gradignan.rpgmaps.core.model.MapCharacter
import fr.gradignan.rpgmaps.core.model.MapEffect
import fr.gradignan.rpgmaps.core.ui.error.UiText


sealed class MapState {
    data object Loading: MapState()
    data class Error(val error: UiText): MapState()
    data class Game(
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
