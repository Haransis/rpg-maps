package fr.gradignan.rpgmaps.feature.login.model

import fr.gradignan.rpgmaps.core.ui.error.UiText

sealed class LogInState {
    data object CheckingToken: LogInState()
    data class ConnectionError(val error: UiText): LogInState()
    data object Success: LogInState()

    data class LogIn(
        val isLoading: Boolean = false,
        val error: UiText? = null,
        val isSubmitEnabled: Boolean = false,
        val username: String = "",
        val password: String = "",
    ): LogInState()
}
