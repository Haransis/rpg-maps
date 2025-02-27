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
import fr.gradignan.rpgmaps.core.model.RoomRepository
import fr.gradignan.rpgmaps.core.model.onError
import fr.gradignan.rpgmaps.core.model.onSuccess
import fr.gradignan.rpgmaps.core.ui.error.toUiText
import fr.gradignan.rpgmaps.feature.game.model.GameState
import fr.gradignan.rpgmaps.feature.game.model.PreviewPath
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.jetbrains.skia.Data
import kotlin.Float.Companion.POSITIVE_INFINITY

class GameViewModel(
    private val username: String,
    private val roomId: Int,
    private val isAdmin: Boolean,
    private val mapActionRepository: MapActionRepository,
    private val roomRepository: RoomRepository
): ViewModel() {
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

    private fun fetchBoards() = viewModelScope.launch {
        roomRepository.getBoards()
            .onSuccess { boards ->
                _gameState.updateIfIs<GameState.Game> { currentState ->
                    currentState.copy(boards = boards)
                }
            }
            .onError { error ->
                _gameState.updateIfIs<GameState.Game> {
                    Logger.e("Error: $error")
                    it.copy(error = error.toUiText())
                }
            }
    }

    private fun fetchAllCharacters() = viewModelScope.launch {
        roomRepository.getAllCharacters()
            .onSuccess { characters ->
                _gameState.updateIfIs<GameState.Game> { currentState ->
                    currentState.copy(availableCharacters = characters)
                }
            }
            .onError { error ->
                _gameState.updateIfIs<GameState.Game> {
                    Logger.e("Error: $error")
                    it.copy(error = error.toUiText())
                }
            }
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
                    if (isAdmin) {
                        fetchBoards()
                        fetchAllCharacters()
                    }
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
        }

    fun onEndTurn() {
        _gameState.updateIfIs<GameState.Game> { it.deselectCharacter() }
        viewModelScope.launch {
            mapActionRepository.endTurn(
                (_gameState.value as? GameState.Game)?.playingMapCharacter?.cmId ?: -1
            ).onError { handleMapUpdateError(it) }
        }
    }

    fun onBoardSelect(board: String) {
        _gameState.updateIfIs<GameState.Game> { currentState ->
            currentState.copy(selectedBoard = currentState.boards.firstOrNull { it.name == board })
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
            val clickedCharacter = currentState.findClickedCharacter(point)

            when {
                clickedCharacter != null -> selectCharacter(currentState, clickedCharacter)
                currentState.selectedMapCharacter?.owner == currentState.playerName || currentState.isGmChecked -> currentState.appendPath(point)
                else -> currentState.deselectCharacter()
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
                previewPath = PreviewPath(
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
                PreviewPath(reachable = reachablePath.dropLast(1) + maxReachable, unreachableStop = end, totalDistance = totalDistance)
            } else {
                PreviewPath(reachable = reachablePath.dropLast(1) + end, unreachableStop = null, totalDistance = totalDistance)
            }
        )
    }


    private fun PreviewPath.extendPath(end: Offset, distance: Float) = PreviewPath(
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
    }

    private fun GameState.Game.deselectCharacter(): GameState.Game =
        this.copy(
            selectedMapCharacter = null,
            previewPath = PreviewPath()
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
            it.copy(
                hoveredCharacterId = it.findClickedCharacter(offset)?.cmId
            ).updatePath(offset)
        }
    }

    fun onGmCheck(checked: Boolean) {
        _gameState.updateIfIs<GameState.Game> {
            it.copy(isGmChecked = checked)
        }
    }

    fun onCharacterSelect(name: String) {
        _gameState.updateIfIs<GameState.Game> {
            it.copy(selectedChar = name)
        }
    }

    fun onBoardSubmit() {
        (_gameState.value as? GameState.Game)?.let {
            if (it.selectedBoard == null) return
            viewModelScope.launch {
                mapActionRepository.sendLoadMap(MapUpdate.LoadMap(it.selectedBoard.id, it.selectedBoard.name))
                    .onError { error -> handleMapUpdateError(error) }
            }
        }
    }

    fun onCharacterSubmit() {
        (_gameState.value as? GameState.Game)?.let { state ->
            val character = state.availableCharacters.firstOrNull { it.name == state.selectedChar }
            if (character == null) return
            viewModelScope.launch {
                mapActionRepository.sendAddCharacter(
                    characterId = character.id,
                    owner = username,
                    order = state.mapCharacters.map { it.cmId } + (state.mapCharacters.size+1)
                ).onError { error -> handleMapUpdateError(error) }
            }
        }
    }

    fun onStartGame() {
        TODO("not implemented")
    }

    fun onDeleteChar(index: Int) {
        TODO("Not yet implemented")
    }

    fun onChangeInitiative(move: ItemMove) {
        TODO("Not yet implemented")
    }

}
