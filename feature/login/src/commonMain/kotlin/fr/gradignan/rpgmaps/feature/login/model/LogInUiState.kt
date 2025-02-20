package fr.gradignan.rpgmaps.feature.login.model

data class LogInUiState(
    val logInState: LogInState = LogInState.CheckingToken,
    val username: String = "",
    val password: String = "",
    val isSubmitEnabled: Boolean = false
)
