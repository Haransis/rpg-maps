package fr.gradignan.rpgmaps.feature.game.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.touchlab.kermit.Logger
import fr.gradignan.rpgmaps.core.common.updateIfIs
import fr.gradignan.rpgmaps.core.model.DataError
import fr.gradignan.rpgmaps.core.model.MapActionRepository
import fr.gradignan.rpgmaps.core.model.MapCharacter
import fr.gradignan.rpgmaps.core.model.MapUpdate
import fr.gradignan.rpgmaps.core.model.Result
import fr.gradignan.rpgmaps.core.model.RoomRepository
import fr.gradignan.rpgmaps.core.model.onSuccess
import fr.gradignan.rpgmaps.core.ui.error.toUiText
import fr.gradignan.rpgmaps.feature.game.model.GmState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.update

class GmViewModel(
    private val username: String,
    private val mapActionRepository: MapActionRepository,
    private val roomRepository: RoomRepository
): ViewModel() {
    private var mapCharacters: List<MapCharacter> = emptyList()
    private val _gmState: MutableStateFlow<GmState> = MutableStateFlow(GmState.Loading)
    val gmState: StateFlow<GmState> = _gmState.asStateFlow()

    init {
        fetchBoards()
        fetchMapCharacters()
        fetchAllCharacters()
    }

    private fun fetchBoards() = viewModelScope.launch {
        roomRepository.getBoards()
            .onSuccess { boards ->
                if (_gmState.value !is GmState.Gm) {
                    _gmState.update { GmState.Gm(boards = boards) }
                } else {
                    _gmState.updateIfIs<GmState.Gm> { state -> state.copy( boards = boards ) }
                }
            }
            .handleDataError()
    }

    private fun fetchAllCharacters() = viewModelScope.launch {
        roomRepository.getAllCharacters()
            .onSuccess { characters ->
                _gmState.updateIfIs<GmState.Gm> { currentState ->
                    currentState.copy(availableCharacters = characters)
                }
            }
            .handleDataError()
    }

    private fun fetchMapCharacters() {
        mapActionRepository.getMapUpdatesFlow().onEach { result ->
            result.onSuccess {
                if (it is MapUpdate.GMGetMap) {
                    mapCharacters = it.mapCharacters
                }
                if (it is MapUpdate.Initiate) {
                    mapCharacters = it.mapCharacters
                }
            }.handleDataError()
        }.launchIn(viewModelScope)
    }

    fun onBoardSelect(board: String) {
        _gmState.updateIfIs<GmState.Gm> { currentState ->
            currentState.copy(selectedBoard = currentState.boards.firstOrNull { it.name == board })
        }
    }

    fun onCharacterSelect(name: String) {
        _gmState.updateIfIs<GmState.Gm> {
            it.copy(selectedChar = name)
        }
    }

    fun onBoardSubmit() {
        (_gmState.value as? GmState.Gm)?.let {
            if (it.selectedBoard == null) return
            viewModelScope.launch {
                mapActionRepository
                    .sendLoadMap(MapUpdate.LoadMap(it.selectedBoard.id, it.selectedBoard.name, 0f))
                    .handleDataError()
            }
        }
    }

    fun onCharacterSubmit() {
        (_gmState.value as? GmState.Gm)?.let { state ->
            val character = state.availableCharacters.firstOrNull { it.name == state.selectedChar }
            if (character == null) return
            viewModelScope.launch {
                Logger.d("mapCharacters: ${mapCharacters}, ${mapCharacters.size}")
                mapActionRepository.sendAddCharacter(
                    characterId = character.id,
                    owner = username,
                    order = mapCharacters.map { it.cmId } + (mapCharacters.size+1)
                ).handleDataError()
            }
        }
    }

    fun onStartGame() {
        viewModelScope.launch {
            mapActionRepository.startGame()
                .handleDataError()
        }
    }

    private fun <T, E: DataError> Result<T, E>.handleDataError(): Result<T, E> {
        if (this is Result.Error) {
            _gmState.update { GmState.Error(error.toUiText()) }
        }
        return this
    }

}
