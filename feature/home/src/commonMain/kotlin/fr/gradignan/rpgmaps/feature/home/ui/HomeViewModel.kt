package fr.gradignan.rpgmaps.feature.home.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import fr.gradignan.rpgmaps.core.model.AuthRepository
import fr.gradignan.rpgmaps.core.model.DataError
import fr.gradignan.rpgmaps.core.model.RoomRepository
import fr.gradignan.rpgmaps.core.model.onError
import fr.gradignan.rpgmaps.core.model.onSuccess
import fr.gradignan.rpgmaps.core.model.then
import fr.gradignan.rpgmaps.core.ui.error.toUiText
import fr.gradignan.rpgmaps.feature.home.model.HomeUiState
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class HomeViewModel(
    private val authRepository: AuthRepository,
    private val roomRepository: RoomRepository
): ViewModel() {
    private val _state = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
    val state = _state
        .onStart { loadUsername() }
        .stateIn (
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = HomeUiState.Loading
        )

    private fun loadUsername() {
        viewModelScope.launch {
            authRepository.getUsername()
                .onSuccess {
                    _state.value = HomeUiState.Success(username = it)
                    loadRooms()
                }.onError {
                    when(it) {
                        DataError.Local.NO_DATA -> {
                            _state.value = HomeUiState.Error("No data")
                        }
                        DataError.Local.UNKNOWN -> {
                            _state.value = HomeUiState.Error("Unknown error")
                        }
                    }
                }
        }
    }

    private fun loadRooms() {
        _state.update {
            (it as HomeUiState.Success).copy(
                isLoadingRooms = true,
                rooms = emptyList(),
                errorMessage = null
            )
        }
        viewModelScope.launch {
            roomRepository.getRooms()
                .onSuccess { rooms ->
                    _state.update {
                        (it as HomeUiState.Success).copy(rooms = rooms)
                    }
                }.onError { error ->
                    _state.update {
                        (it as HomeUiState.Success).copy(
                            errorMessage = error.toUiText(),
                            rooms = emptyList()
                        )
                    }
                }.then {
                    _state.update {
                        (it as HomeUiState.Success).copy(isLoadingRooms = false)
                    }
                }
        }
    }
}
