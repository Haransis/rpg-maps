package fr.gradignan.rpgmaps.feature.game.model

import androidx.compose.ui.geometry.Offset
import fr.gradignan.rpgmaps.core.model.MapCharacter
import fr.gradignan.rpgmaps.feature.game.CHARACTER_RADIUS


class GameIntentReducer(
    private val playerName: String
) {
    fun reduce(currentState: GameState, intent: GameIntent): GameState =
        when (currentState) {
            is GameState.Active ->
                currentState.copy(
                    mapState = reduceMapState(currentState.mapState, intent),
                    hudState = currentState.hudState
                )
            else -> currentState
        }

    private fun reduceMapState(state: MapState, intent: GameIntent): MapState =
        when (intent) {
            GameIntent.EndTurn -> state.deselectCharacter()
            is GameIntent.GmCheck -> {
                state.copy(isGmChecked = intent.checked)
            }
            is GameIntent.MapClick -> {
                if (state.isRulerChecked) {
                    state.copy(
                        ruler = DistancePath(listOf(intent.point, intent.point))
                    )
                } else if (!state.isPingChecked) {
                    val clickedCharacter = findClickedCharacter(state, intent.point)

                    when {
                        clickedCharacter != null -> selectCharacter(state, clickedCharacter)
                        state.selectedMapCharacter?.owner == playerName || state.isGmChecked -> state.appendPath(intent.point)
                        else -> state.deselectCharacter()
                    }
                } else {
                    state
                }
            }
            is GameIntent.PingCheck -> state.pingCheck(intent.change)
            is GameIntent.PointerMove -> when {
                state.isPingChecked -> state.copy(laserPosition = intent.offset)
                state.isRulerChecked && state.ruler.reachable.isNotEmpty() -> {
                    val start = state.ruler.reachable.first()
                    val distance = start.getDistanceTo(intent.offset) / state.mapScale
                    state.copy(ruler = DistancePath(listOf(start, intent.offset), distance))
                }
                else -> {
                    state.copy(
                        hoveredCharacterId = findClickedCharacter(state, intent.offset)?.cmId
                    ).updatePath(intent.offset)
                }
            }
            is GameIntent.RulerCheck -> state.rulerCheck(intent.change)
            is GameIntent.SprintCheck -> {
                val playingCharacter = state.playingMapCharacter
                if (playingCharacter == null) {
                    state
                } else {
                    val baseSpeed = state.mapCharacters.firstOrNull { it.cmId == playingCharacter.cmId }?.speed ?: 0f
                    val updatedSpeed = (if (intent.checked) baseSpeed + playingCharacter.speed
                    else playingCharacter.speed - baseSpeed)
                    state.copy(
                        playingMapCharacter = playingCharacter.copy(speed = updatedSpeed),
                        isSprintChecked = intent.checked
                    ).deselectCharacter()
                }
            }
            GameIntent.Unselect -> state
                .deselectCharacter()
                .pingCheck(false)
                .rulerCheck(false)
            is GameIntent.ChangeInitiative,
            is GameIntent.DeleteChar,
            GameIntent.DoubleClick -> state
        }

    private fun MapState.rulerCheck(change: Boolean): MapState =
        this.copy(
            isPingChecked = if (change) false else this.isPingChecked,
            laserPosition = if (change) null else this.laserPosition,
            ruler = if (!change) DistancePath() else this.ruler,
            isRulerChecked = change
        ).deselectCharacter()

    private fun MapState.pingCheck(change: Boolean): MapState =
        this.copy(
            isRulerChecked = if (change) false else this.isRulerChecked,
            ruler = if (change) DistancePath() else this.ruler,
            laserPosition = if (!change) null else this.laserPosition,
            isPingChecked = change
        ).deselectCharacter()


    private fun findClickedCharacter(state: MapState, point: Offset): MapCharacter? {
        return state.mapCharacters.firstOrNull { character ->
            val center = Offset(character.x.toFloat(), character.y.toFloat())
            center.getDistanceTo(point) <= CHARACTER_RADIUS
        }
    }

    private fun selectCharacter(state: MapState, mapCharacter: MapCharacter): MapState {
        if (
            (mapCharacter.owner == playerName && state.isPlayerTurn) || state.isGmChecked
        ) {
            val characterPosition = Offset(mapCharacter.x.toFloat(), mapCharacter.y.toFloat())
            return state.copy(
                selectedMapCharacter = mapCharacter,
                previewPath = DistancePath(
                    reachable = listOf(characterPosition, characterPosition),
                    unreachableStop = null,
                    totalDistance = 0f
                )
            )
        } else {
            return state.copy(selectedMapCharacter = mapCharacter)
        }
    }
}
