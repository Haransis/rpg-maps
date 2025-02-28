package fr.gradignan.rpgmaps.feature.game.ui

import androidx.compose.ui.geometry.Offset
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.touchlab.kermit.Logger
import fr.gradignan.rpgmaps.core.common.updateIfIs
import fr.gradignan.rpgmaps.core.model.MapCharacter
import fr.gradignan.rpgmaps.core.model.DataError
import fr.gradignan.rpgmaps.core.model.MapActionRepository
import fr.gradignan.rpgmaps.core.model.MapEffect
import fr.gradignan.rpgmaps.core.model.MapUpdate
import fr.gradignan.rpgmaps.core.model.Result
import fr.gradignan.rpgmaps.core.model.onError
import fr.gradignan.rpgmaps.core.ui.error.toUiText
import fr.gradignan.rpgmaps.feature.game.model.GameState
import fr.gradignan.rpgmaps.feature.game.model.DistancePath
import fr.gradignan.rpgmaps.feature.game.model.MapState
import fr.gradignan.rpgmaps.feature.game.model.StatusState
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.Float.Companion.POSITIVE_INFINITY

class GameViewModel(
    private val username: String,
    private val isAdmin: Boolean,
    private val mapActionRepository: MapActionRepository,
): ViewModel() {
    private val _statusState: MutableStateFlow<StatusState> = MutableStateFlow(StatusState())
    val statusState: StateFlow<StatusState> = _statusState.asStateFlow()
    private val _mapState: MutableStateFlow<MapState> = MutableStateFlow(MapState.Loading)
    val mapState: StateFlow<MapState> = _mapState.asStateFlow()

    private val mapResultUpdates: Flow<Result<MapUpdate, DataError>> = mapActionRepository.getMapUpdatesFlow()
    private val mapEffects: Flow<MapEffect> = mapActionRepository.getMapEffectsFlow()
    private val _gameState: MutableStateFlow<GameState> = MutableStateFlow(GameState.Loading)
    val gameState: StateFlow<GameState> = _gameState.asStateFlow()
    init {
        mapResultUpdates.onEach { result ->
            when (result) {
                is Result.Error -> handleMapUpdateError(result.error)
                is Result.Success -> handleMapUpdateSuccess(result.data)
            }
        }.launchIn(viewModelScope)
        mapEffects.onEach { effect ->
            when (effect) {
                is MapEffect.Ping -> handleNewPing(effect)
            }
        }.launchIn(viewModelScope)
    }

    private fun handleNewPing(ping: MapEffect.Ping) {
        _gameState.updateIfIs<GameState.Game> { state ->
            state.copy(
                pings = state.pings + ping
            )
        }
        viewModelScope.launch {
            delay(3000)
            _gameState.updateIfIs<GameState.Game> { state ->
                state.copy(
                    pings = state.pings.filterNot { it.x == ping.x && it.y == ping.y }
                )
            }
        }
    }

    private fun handleMapUpdateError(error: DataError) {
        _gameState.update {
            if (it !is GameState.Game || error is DataError.Http) {
                GameState.Error(error.toUiText())
            } else {
                it.copy(error = error.toUiText())
            }
        }
    }

    private fun handleMapUpdateSuccess(mapUpdate: MapUpdate) {
        _gameState.update {
            when (it) {
                is GameState.Game -> it.update(mapUpdate)
                else -> {
                    GameState.Game(
                        playerName = username,
                        isAdmin = isAdmin,
                    ).update(mapUpdate)
                }
            }
        }
    }

    private fun GameState.Game.update(mapUpdate: MapUpdate): GameState.Game =
        when(mapUpdate) {
            is MapUpdate.Connect -> this.copy(logs = logs + "- ${mapUpdate.user} connected")
            is MapUpdate.GMGetMap -> if (isAdmin) this.copy(
                mapCharacters = mapUpdate.mapCharacters
            ) else this
            is MapUpdate.Initiate -> this.copy(
                logs = logs + "- Starting game",
                mapCharacters = mapUpdate.mapCharacters
            )
            is MapUpdate.LoadMap -> this.copy(
                logs = logs + "- Loading map: ${mapUpdate.map.substringAfterLast("/").substringBeforeLast(".")}",
                imageUrl = mapUpdate.map
            )
            is MapUpdate.Move -> {
                val updatedCharacters = this.mapCharacters.toMutableList().apply {
                    this.indexOfFirst { it.cmId == mapUpdate.id }.takeIf { it != -1 }?.let { index ->
                        val updatedCharacter = this[index].copy(x = mapUpdate.x, y = mapUpdate.y)
                        set(index, updatedCharacter)
                    }
                }

                var newState = this.copy(mapCharacters = updatedCharacters)
                if (mapUpdate.owner == playerName) {
                    newState = newState.copy(
                        playingMapCharacter = playingMapCharacter?.copy(speed = playingMapCharacter.speed - previewPath.totalDistance)
                    )
                    newState = newState.deselectCharacter()
                }
                newState
            }
            MapUpdate.NewTurn -> this.copy(logs = logs + "- New Turn")
            is MapUpdate.Next -> {
                val playingCharacter = mapCharacters.find { it.cmId == mapUpdate.id }
                if (playingCharacter != null) {
                    this.copy(
                        logs = logs + "- ${playingCharacter.name} is playing",
                        isPlayerTurn = playingCharacter.owner == playerName,
                        playingMapCharacter = playingCharacter,
                        isGmChecked = false,
                        isSprintChecked = false,
                    )
                } else this
            }
            is MapUpdate.AddCharacter -> {
                this.copy(
                    logs = logs + "- ${mapUpdate.character.name} added",
                    mapCharacters = mapCharacters + mapUpdate.character
                )
            }
            is MapUpdate.InitiativeOrder -> {
                this.copy(
                    mapCharacters = mapCharacters.sortedBy { it.cmId in mapUpdate.order }
                )
            }
        }

    fun onEndTurn() {
        _gameState.updateIfIs<GameState.Game> { it.deselectCharacter() }
        viewModelScope.launch {
            mapActionRepository.endTurn(
                (_gameState.value as? GameState.Game)?.playingMapCharacter?.cmId ?: -1
            ).onError { handleMapUpdateError(it) }
        }
    }

    fun onSprintCheck(checked: Boolean) {
        _gameState.updateIfIs<GameState.Game> { state ->
            if (state.playingMapCharacter == null) return@updateIfIs state
            val baseSpeed = state.mapCharacters.firstOrNull { it.cmId == state.playingMapCharacter.cmId }?.speed ?: 0f
            val updatedSpeed = (if (checked) baseSpeed + state.playingMapCharacter.speed
                else state.playingMapCharacter.speed - baseSpeed)
            state.copy(
                playingMapCharacter = state.playingMapCharacter.copy(speed = updatedSpeed),
                isSprintChecked = checked
            ).deselectCharacter()
        }
    }

    fun onMapClick(point: Offset) {
        _gameState.updateIfIs<GameState.Game> { currentState ->
            return@updateIfIs when {
                currentState.isPingChecked -> {
                    viewModelScope.launch {
                        mapActionRepository.sendPing(MapEffect.Ping(point.x.toInt(), point.y.toInt()))
                            .onError { handleMapUpdateError(it) }
                    }
                    currentState
                }
                currentState.isRulerChecked -> {
                    currentState.copy(
                        ruler = DistancePath(listOf(point, point))
                    )
                }
                else -> {
                    val clickedCharacter = currentState.findClickedCharacter(point)

                    when {
                        clickedCharacter != null -> selectCharacter(currentState, clickedCharacter)
                        currentState.selectedMapCharacter?.owner == currentState.playerName || currentState.isGmChecked -> currentState.appendPath(point)
                        else -> currentState.deselectCharacter()
                    }
                }
            }
        }
    }

    private fun GameState.Game.findClickedCharacter(point: Offset): MapCharacter? {
        return this.mapCharacters.firstOrNull { character ->
            val center = Offset(character.x.toFloat(), character.y.toFloat())
            center.getDistanceTo(point) <= CHARACTER_RADIUS
        }
    }

    private fun selectCharacter(state: GameState.Game, mapCharacter: MapCharacter): GameState.Game {
        if (
            (mapCharacter.owner == state.playerName && state.isPlayerTurn) || state.isGmChecked
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

    private fun GameState.Game.appendPath(end: Offset): GameState.Game {
        if (previewPath.reachable.isEmpty()) return this
        val start = previewPath.reachable.first()
        val distance = start.getDistanceTo(end) / mapScale
        val speed = playingMapCharacter?.speed?.coerceAtLeast(0f) ?: 0f
        return if (distance > speed) this else copy(
            previewPath = previewPath.extendPath(end, distance)
        )
    }

    private fun GameState.Game.updatePath(end: Offset): GameState.Game {
        val reachablePath = this.previewPath.reachable
        if (reachablePath.size < 2 || (!isPlayerTurn && !isGmChecked)) return this

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

    private fun DistancePath.extendPath(end: Offset, distance: Float) = DistancePath(
        reachable = reachable + end,
        unreachableStop = null,
        totalDistance = distance
    )

    private fun Offset.interpolate(target: Offset, factor: Float) = Offset(
        x + (target.x - x) * factor,
        y + (target.y - y) * factor
    )

    fun onUnselect() {
        _gameState.updateIfIs<GameState.Game> {
            it.deselectCharacter()
        }
        onPingCheck(false)
        onRulerCheck(false)
    }

    private fun GameState.Game.deselectCharacter(): GameState.Game =
        this.copy(
            selectedMapCharacter = null,
            previewPath = DistancePath()
        )

    fun onDoubleClick() {
        when(val state = _gameState.value) {
            is GameState.Game -> {
                if (
                    state.selectedMapCharacter == null
                    || state.previewPath.reachable.isEmpty()
                    || state.previewPath.unreachableStop != null
                ) return
                viewModelScope.launch {
                    mapActionRepository.sendMove(MapUpdate.Move(
                        name = state.selectedMapCharacter.name,
                        x = state.previewPath.reachable.last().x.toInt(),
                        y = state.previewPath.reachable.last().y.toInt(),
                        owner = state.playerName,
                        id = state.selectedMapCharacter.cmId,
                    )).onError { handleMapUpdateError(it) }
                }
            }
            else -> Logger.e("onDoubleClick called on wrong state")
        }
    }

    private fun List<Offset>.totalDistance(): Float
        = this.zipWithNext { current, next -> current.getDistanceTo(next) }.sum()

    fun onPointerMove(offset: Offset) {
        _gameState.updateIfIs<GameState.Game> {
            return@updateIfIs when {
                it.isPingChecked -> it.copy(laserPosition = offset)
                it.isRulerChecked && it.ruler.reachable.isNotEmpty() -> {
                    val start = it.ruler.reachable.first()
                    val distance = start.getDistanceTo(offset) / it.mapScale
                    it.copy(ruler = DistancePath(listOf(start, offset), distance))
                }
                else -> {
                    it.copy(
                        hoveredCharacterId = it.findClickedCharacter(offset)?.cmId
                    ).updatePath(offset)
                }
            }
        }
    }

    fun onGmCheck(checked: Boolean) {
        _gameState.updateIfIs<GameState.Game> {
            it.copy(isGmChecked = checked)
        }
    }

    fun onDeleteChar(index: Int) {
        TODO("Not yet implemented")
    }

    fun onChangeInitiative(order: List<Int>) {
        viewModelScope.launch {
            mapActionRepository.sendInitiativeOrder(MapUpdate.InitiativeOrder(order))
                .onError { error -> handleMapUpdateError(error) }
        }
    }

    fun onRulerCheck(change: Boolean) {
        _gameState.updateIfIs<GameState.Game> {
            it.copy(
                isPingChecked = if (change) false else it.isPingChecked,
                laserPosition = if (change) null else it.laserPosition,
                ruler = if (!change) DistancePath() else it.ruler,
                isRulerChecked = change
            ).deselectCharacter()
        }
    }

    fun onPingCheck(change: Boolean) {
        _gameState.updateIfIs<GameState.Game> {
            it.copy(
                isRulerChecked = if (change) false else it.isRulerChecked,
                ruler = if (change) DistancePath() else it.ruler,
                laserPosition = if (!change) null else it.laserPosition,
                isPingChecked = change
            ).deselectCharacter()
        }
    }

}
