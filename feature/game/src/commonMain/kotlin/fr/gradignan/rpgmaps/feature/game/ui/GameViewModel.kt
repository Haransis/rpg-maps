package fr.gradignan.rpgmaps.feature.game.ui

import androidx.compose.ui.geometry.Offset
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import fr.gradignan.rpgmaps.core.common.updateIfIs
import fr.gradignan.rpgmaps.core.model.DataError
import fr.gradignan.rpgmaps.core.model.EmptyResult
import fr.gradignan.rpgmaps.core.model.MapActionRepository
import fr.gradignan.rpgmaps.core.model.MapEffect
import fr.gradignan.rpgmaps.core.model.MapUpdate
import fr.gradignan.rpgmaps.core.model.Result
import fr.gradignan.rpgmaps.feature.game.model.MapState
import fr.gradignan.rpgmaps.feature.game.model.GameIntent
import fr.gradignan.rpgmaps.feature.game.model.GameIntentReducer
import fr.gradignan.rpgmaps.feature.game.model.GameState
import fr.gradignan.rpgmaps.feature.game.model.MapUpdateReducer
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class GameViewModel(
    private val playerName: String,
    isAdmin: Boolean,
    private val mapActionRepository: MapActionRepository
): ViewModel() {

    private val mapUpdateReducer = MapUpdateReducer(playerName, isAdmin)
    private val gameIntentReducer = GameIntentReducer(playerName)
    private val mapResultUpdates: Flow<Result<MapUpdate, DataError>> = mapActionRepository.getMapUpdatesFlow()
    private val mapEffects: Flow<MapEffect> = mapActionRepository.getMapEffectsFlow()

    private val _gameState: MutableStateFlow<GameState> = MutableStateFlow(GameState.Loading)
    val gameState = _gameState.asStateFlow()

    init {
        mapResultUpdates.onEach { result ->
            _gameState.update { mapUpdateReducer.reduceMapUpdateResult(it, result) }
        }.launchIn(viewModelScope)
        mapEffects.onEach { effect ->
            when (effect) {
                is MapEffect.Ping -> handleNewPing(effect)
            }
        }.launchIn(viewModelScope)
    }

    private fun handleNewPing(ping: MapEffect.Ping) {
        _gameState.updateIfIs<MapState> {
            it.copy(pings = it.pings + ping)
        }
        viewModelScope.launch {
            delay(3000)
            _gameState.updateIfIs<MapState> { state ->
                state.copy(
                    pings = state.pings.filterNot { it.x == ping.x && it.y == ping.y }
                )
            }
        }
    }

    fun processIntent(intent: GameIntent) {
        _gameState.update { gameIntentReducer.reduce(it, intent) }
        when (intent) {
            GameIntent.EndTurn -> sendEndTurn()
            is GameIntent.MapClick -> sendPing(intent.point)
            is GameIntent.ChangeInitiative -> sendChangeInitiative(intent.order)
            GameIntent.DoubleClick -> sendMove()
            is GameIntent.DeleteChar -> sendDeleteChar(intent.index)
            is GameIntent.PointerMove,
            is GameIntent.GmCheck,
            is GameIntent.RulerCheck,
            is GameIntent.PingCheck,
            GameIntent.Unselect,
            is GameIntent.SprintCheck -> {}
        }
    }

    private fun sendEndTurn() {
        val playingId = (_gameState.value as? GameState.Active)?.mapState?.playingMapCharacter?.cmId
        if (playingId != null) {
            viewModelScope.launch {
                mapActionRepository.endTurn(playingId)
                    .handleError()
            }
        }
    }

    private fun sendPing(point: Offset) {
        val shouldPing = (_gameState.value as? GameState.Active)?.mapState?.isPingChecked
        if (shouldPing == true) {
            viewModelScope.launch {
                mapActionRepository.sendPing(MapEffect.Ping(point.x.toInt(), point.y.toInt()))
                    .handleError()
            }
        }
    }

    private fun EmptyResult<DataError>.handleError() {
        if (this is Result.Error)
            _gameState.update { mapUpdateReducer.reduceError(it, error) }
    }

    private fun sendMove() {
        (_gameState.value as? GameState.Active)?.mapState?.run {
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
                ).handleError()
            }
        }
    }

    private fun sendDeleteChar(index: Int) {
        TODO("Not yet implemented")
    }

    private fun sendChangeInitiative(order: List<Int>) =
        viewModelScope.launch {
            mapActionRepository.sendInitiativeOrder(MapUpdate.InitiativeOrder(order))
                .handleError()
        }

}
