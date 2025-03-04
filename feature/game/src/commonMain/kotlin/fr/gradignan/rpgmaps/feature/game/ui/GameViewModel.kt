package fr.gradignan.rpgmaps.feature.game.ui

import androidx.compose.ui.geometry.Offset
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.touchlab.kermit.Logger
import fr.gradignan.rpgmaps.core.model.MapCharacter
import fr.gradignan.rpgmaps.core.model.DataError
import fr.gradignan.rpgmaps.core.model.MapActionRepository
import fr.gradignan.rpgmaps.core.model.MapEffect
import fr.gradignan.rpgmaps.core.model.MapUpdate
import fr.gradignan.rpgmaps.core.model.Result
import fr.gradignan.rpgmaps.core.model.onError
import fr.gradignan.rpgmaps.core.ui.error.toUiText
import fr.gradignan.rpgmaps.feature.game.model.CharItem
import fr.gradignan.rpgmaps.feature.game.model.MapState
import fr.gradignan.rpgmaps.feature.game.model.DistancePath
import fr.gradignan.rpgmaps.feature.game.model.StatusState
import fr.gradignan.rpgmaps.feature.game.model.toCharItem
import fr.gradignan.rpgmaps.feature.game.model.toCharItems
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.Float.Companion.POSITIVE_INFINITY

class GameViewModel(
    private val playerName: String,
    private val isAdmin: Boolean,
    private val mapActionRepository: MapActionRepository,
): ViewModel() {
    private val _statusState: MutableStateFlow<StatusState> = MutableStateFlow(StatusState(
        isAdmin = isAdmin
    ))
    val statusState: StateFlow<StatusState> = _statusState.asStateFlow()

    private val mapResultUpdates: Flow<Result<MapUpdate, DataError>> = mapActionRepository.getMapUpdatesFlow()
    private val mapEffects: Flow<MapEffect> = mapActionRepository.getMapEffectsFlow()

    private val _mapState: MutableStateFlow<MapState> = MutableStateFlow(MapState.Loading)
    private val _gameState: MutableStateFlow<MapState.Game> = MutableStateFlow(MapState.Game(
        isAdmin = isAdmin,
    ))
    val mapState: StateFlow<MapState> = combine(_mapState, _gameState) { gameState, updatedState ->
        if (gameState is MapState.Game) {
            updatedState
        } else {
            gameState
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = MapState.Loading
    )

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
        _gameState.update {
            it.copy(
                pings = it.pings + ping
            )
        }
        viewModelScope.launch {
            delay(3000)
            _gameState.update { state ->
                state.copy(
                    pings = state.pings.filterNot { it.x == ping.x && it.y == ping.y }
                )
            }
        }
    }

    private fun handleMapUpdateError(error: DataError) {
        if (error is DataError.Http) {
            _mapState.update { MapState.Error(error.toUiText()) }
        } else {
            _gameState.update { it.copy(error = error.toUiText()) }
        }
    }

    private fun handleMapUpdateSuccess(mapUpdate: MapUpdate) {
        _mapState.update { MapState.Game() }
        update(mapUpdate)
    }

    private fun update(mapUpdate: MapUpdate) {
        when (mapUpdate) {
            is MapUpdate.Connect -> {
                updateStateLogs("- ${mapUpdate.user} connected")
            }
            is MapUpdate.GMGetMap -> if (isAdmin) {
                _statusState.update { it.copy(characters = mapUpdate.mapCharacters.toCharItems()) }
                _gameState.update { it.copy(mapCharacters = mapUpdate.mapCharacters) }
            }
            is MapUpdate.Initiate -> {
                _statusState.update { it.appendLog("- Starting game").copy(
                    characters = mapUpdate.mapCharacters.toCharItems()
                ) }
                _gameState.update { it.copy(mapCharacters = mapUpdate.mapCharacters) }
            }
            is MapUpdate.LoadMap -> {
                updateStateLogs("- Loading map: ${
                        mapUpdate.map.substringAfterLast("/").substringBeforeLast(".")
                    }"
                )
                _gameState.update { it.copy (imageUrl = mapUpdate.map) }
            }
            is MapUpdate.Move -> {
                _gameState.update { it.moveCharacter(mapUpdate) }
            }
            MapUpdate.NewTurn -> updateStateLogs("- New Turn")
            is MapUpdate.Next -> {
                val playingCharacter = _gameState.value.mapCharacters.find { it.cmId == mapUpdate.id }
                if (playingCharacter != null) {
                    _gameState.update {
                        it.copy(
                            playingMapCharacter = playingCharacter,
                            isGmChecked = false,
                            isSprintChecked = false,
                        )
                    }
                    _statusState.update {
                        it.appendLog("- ${playingCharacter.name} is playing")
                            .copy(isPlayerTurn = playingCharacter.owner == playerName)
                    }
                }
            }

            is MapUpdate.AddCharacter -> {
                _gameState.update {
                    it.copy(mapCharacters = it.mapCharacters + mapUpdate.character)
                }
                _statusState.update {
                    it.appendLog("- ${mapUpdate.character.name} added")
                        .copy(characters = it.characters + mapUpdate.character.toCharItem())
                }
            }
            is MapUpdate.InitiativeOrder -> {
                _statusState.update { status ->
                    status.copy(
                        characters = status.characters.sortedBy { it.index in mapUpdate.order }
                    )
                }
            }
        }
    }

    private fun MapState.Game.moveCharacter(move: MapUpdate.Move): MapState.Game {
        val updatedCharacters = this.mapCharacters.toMutableList().apply {
            this.indexOfFirst { it.cmId == move.id }.takeIf { it != -1 }
                ?.let { index ->
                    val updatedCharacter =
                        this[index].copy(x = move.x, y = move.y)
                    set(index, updatedCharacter)
                }
        }

        var newState = this.copy(mapCharacters = updatedCharacters)
        if (move.owner == playerName) {
            newState = newState.copy(
                playingMapCharacter = playingMapCharacter?.copy(speed = playingMapCharacter.speed - previewPath.totalDistance)
            )
            newState = newState.deselectCharacter()
        }
        return newState
    }

    fun onEndTurn() {
        _gameState.update { it.deselectCharacter() }
        viewModelScope.launch {
            mapActionRepository.endTurn(
                (_mapState.value as? MapState.Game)?.playingMapCharacter?.cmId ?: -1
            ).onError { handleMapUpdateError(it) }
        }
    }

    fun onSprintCheck(checked: Boolean) {
        _gameState.update { state ->
            Logger.d("checked: $checked, playing: ${state.playingMapCharacter}")
            if (state.playingMapCharacter == null) return@update state
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
        _gameState.update { currentState ->
            return@update when {
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
                        currentState.selectedMapCharacter?.owner == playerName || currentState.isGmChecked -> currentState.appendPath(point)
                        else -> currentState.deselectCharacter()
                    }
                }
            }
        }
    }

    private fun MapState.Game.findClickedCharacter(point: Offset): MapCharacter? {
        return this.mapCharacters.firstOrNull { character ->
            val center = Offset(character.x.toFloat(), character.y.toFloat())
            center.getDistanceTo(point) <= CHARACTER_RADIUS
        }
    }

    private fun selectCharacter(state: MapState.Game, mapCharacter: MapCharacter): MapState.Game {
        if (
            (mapCharacter.owner == playerName && _statusState.value.isPlayerTurn) || state.isGmChecked
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

    private fun MapState.Game.appendPath(end: Offset): MapState.Game {
        if (previewPath.reachable.isEmpty()) return this
        val start = previewPath.reachable.first()
        val distance = start.getDistanceTo(end) / mapScale
        val speed = playingMapCharacter?.speed?.coerceAtLeast(0f) ?: 0f
        return if (distance > speed) this else copy(
            previewPath = previewPath.extendPath(end, distance)
        )
    }

    private fun MapState.Game.updatePath(end: Offset): MapState.Game {
        val reachablePath = this.previewPath.reachable
        if (reachablePath.size < 2 || (!_statusState.value.isPlayerTurn && !isGmChecked)) return this

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
        _gameState.update {
            it.deselectCharacter()
        }
        onPingCheck(false)
        onRulerCheck(false)
    }

    private fun MapState.Game.deselectCharacter(): MapState.Game =
        this.copy(
            selectedMapCharacter = null,
            previewPath = DistancePath()
        )

    fun onDoubleClick() {
        with(_gameState.value) {
            if (
                selectedMapCharacter == null
                || previewPath.reachable.isEmpty()
                || previewPath.unreachableStop != null
            ) return
            viewModelScope.launch {
                mapActionRepository.sendMove(
                    MapUpdate.Move(
                        name = selectedMapCharacter.name,
                        x = previewPath.reachable.last().x.toInt(),
                        y = previewPath.reachable.last().y.toInt(),
                        owner = playerName,
                        id = selectedMapCharacter.cmId,
                    )
                ).onError { handleMapUpdateError(it) }
            }
        }
    }

    private fun List<Offset>.totalDistance(): Float
        = this.zipWithNext { current, next -> current.getDistanceTo(next) }.sum()

    fun onPointerMove(offset: Offset) {
        _gameState.update {
            return@update when {
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
        _gameState.update {
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
        _gameState.update {
            it.copy(
                isPingChecked = if (change) false else it.isPingChecked,
                laserPosition = if (change) null else it.laserPosition,
                ruler = if (!change) DistancePath() else it.ruler,
                isRulerChecked = change
            ).deselectCharacter()
        }
    }

    fun onPingCheck(change: Boolean) {
        _gameState.update {
            it.copy(
                isRulerChecked = if (change) false else it.isRulerChecked,
                ruler = if (change) DistancePath() else it.ruler,
                laserPosition = if (!change) null else it.laserPosition,
                isPingChecked = change
            ).deselectCharacter()
        }
    }

    private fun StatusState.appendLog(message: String): StatusState =
        this.copy(logs = this.logs + message)

    private fun updateStateLogs(message: String) {
        _statusState.update { it.copy(logs = it.logs + message) }
    }

}
