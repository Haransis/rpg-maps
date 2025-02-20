package fr.gradignan.rpgmaps.feature.login.model

sealed class LogInState {
    data object Empty: LogInState()
    data object Loading: LogInState()
    data object CheckingToken: LogInState()
    data class Error(val error: Throwable): LogInState()
    data object Success: LogInState()
}
