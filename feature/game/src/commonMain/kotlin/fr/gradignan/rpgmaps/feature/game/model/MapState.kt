package fr.gradignan.rpgmaps.feature.game.model

import androidx.compose.ui.geometry.Offset
import fr.gradignan.rpgmaps.core.model.MapAction
import fr.gradignan.rpgmaps.core.model.MapCharacter
import fr.gradignan.rpgmaps.core.ui.error.UiText
import kotlin.Float.Companion.POSITIVE_INFINITY


data class MapState(
    val isAdmin: Boolean,
    val laserPosition: Offset? = null,
    val isPingChecked: Boolean = false,
    val isRulerChecked: Boolean = false,
    val mapScale: Float = 30f,
    val mapCharacters: List<MapCharacter> = emptyList(),
    val selectedMapCharacter: MapCharacter? = null,
    val hoveredCharacterId: Int? = null,
    val playingMapCharacter: MapCharacter? = null,
    val pings: List<MapAction.Ping> = emptyList(),
    val error: UiText? = null,
    val isSprintChecked: Boolean = false,
    val isGmChecked: Boolean = false,
    val ruler: DistancePath = DistancePath(),
    val previewPath: DistancePath = DistancePath(),
    val imageUrl: String? = null,
    val isPlayerTurn: Boolean = false,
)

data class HUDState (
    val isAdmin: Boolean,
    val characters: List<CharItem> = emptyList(),
    val logs: List<String> = emptyList(),
    val isPlayerTurn: Boolean = false,
)

sealed class GameState {
    data object Loading: GameState()
    data class Error(val error: UiText): GameState()
    data class Active(
        val mapState: MapState,
        val hudState: HUDState,
    ): GameState()
}

fun Offset.interpolate(target: Offset, factor: Float) = Offset(
    x + (target.x - x) * factor,
    y + (target.y - y) * factor
)

fun List<Offset>.totalDistance(): Float
        = this.zipWithNext { current, next -> current.getDistanceTo(next) }.sum()

fun MapState.deselectCharacter(): MapState =
    this.copy(
        selectedMapCharacter = null,
        previewPath = DistancePath()
    )

fun MapState.appendPath(end: Offset): MapState {
    if (previewPath.reachable.isEmpty()) return this
    val start = previewPath.reachable.first()
    val distance = start.getDistanceTo(end) / mapScale
    val speed = playingMapCharacter?.speed?.coerceAtLeast(0f) ?: 0f
    return if (distance > speed) this else copy(
        previewPath = previewPath.extendPath(end, distance)
    )
}

fun MapState.updatePath(end: Offset): MapState {
    val reachablePath = this.previewPath.reachable
    if (this.previewPath.reachable.size < 2 || (!this.isPlayerTurn && !isGmChecked)) return this

    val start = reachablePath[reachablePath.lastIndex - 1]
    val previousDistance = reachablePath.dropLast(1).totalDistance() / mapScale
    val newSegmentDistance = start.getDistanceTo(end) / mapScale
    val speed = if (isGmChecked) POSITIVE_INFINITY else this.playingMapCharacter?.speed?.coerceAtLeast(0f) ?: 0f
    val totalDistance = previousDistance + newSegmentDistance

    return copy(
        previewPath = if (totalDistance > speed) {
            val maxReachable = start.interpolate(end, (speed - previousDistance) / newSegmentDistance)
            DistancePath(reachable = reachablePath.dropLast(1) + maxReachable, unreachableStop = end, totalDistance = totalDistance)
        } else {
            DistancePath(reachable = reachablePath.dropLast(1) + end, unreachableStop = null, totalDistance = totalDistance)
        }
    )
}
