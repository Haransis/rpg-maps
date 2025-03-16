package fr.gradignan.rpgmaps.feature.game.ui.viewmodel

import androidx.compose.ui.geometry.Offset
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import fr.gradignan.rpgmaps.core.model.DataError
import fr.gradignan.rpgmaps.core.model.EmptyResult
import fr.gradignan.rpgmaps.core.model.MapActionRepository
import fr.gradignan.rpgmaps.core.model.MapAction
import fr.gradignan.rpgmaps.core.model.Result
import fr.gradignan.rpgmaps.feature.game.model.GameIntent
import fr.gradignan.rpgmaps.feature.game.model.GameIntentReducer
import fr.gradignan.rpgmaps.feature.game.model.GameState
import fr.gradignan.rpgmaps.feature.game.model.MapActionReducer
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
    private val mapActionRepository: MapActionRepository,
    isAdmin: Boolean,
): ViewModel() {

    private val mapActionReducer = MapActionReducer(playerName, isAdmin)
    private val gameIntentReducer = GameIntentReducer(playerName)
    private val mapActions: Flow<Result<MapAction, DataError>> = mapActionRepository.getMapActionsFlow()

    private val _gameState: MutableStateFlow<GameState> = MutableStateFlow(GameState.Loading)
    val gameState = _gameState.asStateFlow()

    init {
        mapActions.onEach { result ->
            if (result is Result.Success && result.data is MapAction.Ping) {
                handleNewPing(result.data as MapAction.Ping)
            }
            _gameState.update { mapActionReducer.reduceMapActionResult(it, result) }
        }.launchIn(viewModelScope)
    }

    private fun handleNewPing(ping: MapAction.Ping) {
        viewModelScope.launch {
            delay(3000)
            _gameState.update {
                mapActionReducer.reduceMapActionResult(
                    it, Result.Success(MapAction.Ping(ping.x, ping.y))
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
                mapActionRepository.sendPing(MapAction.Ping(point.x.toInt(), point.y.toInt()))
                    .handleError()
            }
        }
    }

    private fun EmptyResult<DataError>.handleError() {
        if (this is Result.Error)
            _gameState.update { mapActionReducer.reduceError(it, error) }
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
                    MapAction.Move(
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
            mapActionRepository.sendInitiativeOrder(MapAction.InitiativeOrder(order))
                .handleError()
        }

}
