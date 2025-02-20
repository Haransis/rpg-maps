package fr.gradignan.rpgmaps.feature.home.model

import fr.gradignan.rpgmaps.core.model.Room
import fr.gradignan.rpgmaps.core.ui.error.UiText


sealed class HomeUiState {
    data object Loading: HomeUiState()
    data class Success(
        val username: String,
        val isLoadingRooms: Boolean = true,
        val errorMessage: UiText? = null,
        val rooms: List<Room> = emptyList()
    ): HomeUiState()
    data class Error(val message: String): HomeUiState()
}
