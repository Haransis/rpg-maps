package fr.gradignan.rpgmaps.feature.game.ui

import androidx.compose.ui.geometry.Offset
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import fr.gradignan.rpgmaps.core.common.updateIfIs
import fr.gradignan.rpgmaps.core.model.Character
import fr.gradignan.rpgmaps.core.model.DataError
import fr.gradignan.rpgmaps.core.model.MapActionRepository
import fr.gradignan.rpgmaps.core.model.MapEffect
import fr.gradignan.rpgmaps.core.model.MapUpdate
import fr.gradignan.rpgmaps.core.model.Result
import fr.gradignan.rpgmaps.core.model.onError
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

class GameViewModel(
    private val username: String,
    private val roomId: Int,
    private val admin: Boolean,
    private val mapActionRepository: MapActionRepository
): ViewModel() {
    private val mapResourceUpdates: Flow<Result<MapUpdate, DataError>> = mapActionRepository.getMapUpdatesFlow()
    private val mapEffects: Flow<MapEffect> = mapActionRepository.getMapEffectsFlow()
    private val _gameState: MutableStateFlow<GameState> = MutableStateFlow(GameState.Loading)
    val gameState: StateFlow<GameState> = _gameState.asStateFlow()
    init {
        mapResourceUpdates.onEach { result ->
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
            when (it) {
                is GameState.Game -> it.copy(error = error.toUiText())
                else -> GameState.Error(error.toUiText())
            }
        }
    }

    private fun handleMapUpdateSuccess(mapUpdate: MapUpdate) {
        _gameState.update {
            when (it) {
                is GameState.Game -> it.update(mapUpdate)
                else -> GameState.Game(
                    playerName = username,
                    admin = admin,
                ).update(mapUpdate)
            }
        }
    }

    private fun GameState.Game.update(mapUpdate: MapUpdate): GameState.Game =
        when(mapUpdate) {
            is MapUpdate.Connect -> this.copy(logs = logs + "- ${mapUpdate.user} connected")
            is MapUpdate.Initiate -> this.copy(
                logs = logs + "- Starting game",
                characters = mapUpdate.characters
            )
            is MapUpdate.LoadMap -> this.copy(
                logs = logs + "- Loading map: ${mapUpdate.map}",
                map = mapUpdate.map
            )
            is MapUpdate.Move -> {
                val updatedCharacters = this.characters.toMutableList().apply {
                    this.indexOfFirst { it.id == mapUpdate.id }.takeIf { it != -1 }?.let { index ->
                        val updatedCharacter = this[index].copy(x = mapUpdate.x, y = mapUpdate.y)
                        set(index, updatedCharacter)
                    }
                }

                var newState = this.copy(characters = updatedCharacters)
                if (mapUpdate.owner == playerName) {
                    newState = newState.deselectCharacter()
                }
                newState
            }
            MapUpdate.NewTurn -> this.copy(logs = logs + "- New Turn")
            is MapUpdate.Next -> {
                val playingCharacter = characters.find { it.cmId == mapUpdate.id }
                if (playingCharacter != null) {
                    this.copy(
                        logs = logs + "- ${playingCharacter.name} is playing",
                        isPlayerTurn = playingCharacter.owner == playerName,
                        playingCharacter = playingCharacter
                    )
                } else this
            }
        }

    fun onEndTurn() = viewModelScope.launch {
        mapActionRepository.endTurn((_gameState.value as? GameState.Game)?.playingCharacter?.cmId ?: -1)
            .onError { handleMapUpdateError(it) }
    }

    fun onSprintChecked(checked: Boolean) {
        _gameState.updateIfIs<GameState.Game> { it.copy(sprintEnabled = checked) }
    }

    fun onMapClick(point: Offset) {
        _gameState.updateIfIs<GameState.Game> { currentState ->
            val clickedCharacter = findClickedCharacter(currentState, point)

            when {
                clickedCharacter != null -> selectCharacter(currentState, clickedCharacter)
                currentState.selectedCharacter?.owner == currentState.playerName -> currentState.updatePath(point)
                else -> currentState.deselectCharacter()
            }
        }
    }

    private fun findClickedCharacter(state: GameState.Game, point: Offset): Character? {
        return state.characters.firstOrNull { character ->
            val center = Offset(character.x.toFloat(), character.y.toFloat())
            center.getDistanceTo(point) <= CHARACTER_RADIUS
        }
    }

    private fun selectCharacter(state: GameState.Game, character: Character): GameState.Game {
        if (character.owner != state.playerName) {
            return state.copy(selectedCharacter = character)
        } else {
            val characterPosition = Offset(character.x.toFloat(), character.y.toFloat())
            return state.copy(
                selectedCharacter = character,
                previewPath = PreviewPath(listOf(characterPosition), state.playingCharacter?.speed ?: 0f)
            )
        }
    }

    private fun GameState.Game.updatePath(point: Offset): GameState.Game {
        val distance = this.previewPath.path.last().getDistanceTo(point)
        val remainingDistance = this.previewPath.maxDistance - distance / this.mapScale
        return if (remainingDistance > 0f) {
            this.copy(
                playingCharacter = playingCharacter?.copy(speed = remainingDistance),
                previewPath = PreviewPath(
                    this.previewPath.path + point,
                    remainingDistance
                ),
            )
        } else this
    }

    fun onUnselect() {
        _gameState.updateIfIs<GameState.Game> {
            it.deselectCharacter()
        }
    }

    private fun GameState.Game.deselectCharacter(): GameState.Game =
        this.copy(
            selectedCharacter = null,
            previewPath = PreviewPath(emptyList(), playingCharacter?.speed ?: 0f)
        )

    private fun onValidateMove() {
        when(val state = _gameState.value) {
            is GameState.Game -> {
                if (state.selectedCharacter == null || state.previewPath.path.size <= 1) return
                viewModelScope.launch {
                    mapActionRepository.sendMove(MapUpdate.Move(
                        name = state.selectedCharacter.name,
                        x = state.previewPath.path.last().x.toInt(),
                        y = state.previewPath.path.last().y.toInt(),
                        owner = state.playerName,
                        id = state.selectedCharacter.characterId,
                    )).onError { handleMapUpdateError(it) }
                }
            }
            else -> {}
        }
    }

    private fun List<Offset>.totalDistance(): Float
        = this.zipWithNext { current, next -> current.getDistanceTo(next) }.sum()

    fun onDoubleClick(point: Offset) {
        if ((_gameState.value as? GameState.Game)?.selectedCharacter == null) return
        _gameState.updateIfIs<GameState.Game> { it.updatePath(point) }
        onValidateMove()
    }

}
