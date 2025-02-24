package fr.gradignan.rpgmaps.feature.login.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import fr.gradignan.rpgmaps.core.common.Resource
import fr.gradignan.rpgmaps.core.common.updateIfIs
import fr.gradignan.rpgmaps.core.model.AuthRepository
import fr.gradignan.rpgmaps.core.model.Auth
import fr.gradignan.rpgmaps.core.model.DataError
import fr.gradignan.rpgmaps.core.model.Result
import fr.gradignan.rpgmaps.core.model.onError
import fr.gradignan.rpgmaps.core.model.onSuccess
import fr.gradignan.rpgmaps.core.ui.error.toUiText
import fr.gradignan.rpgmaps.feature.login.model.LogInState
import fr.gradignan.rpgmaps.feature.login.model.LogInUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.math.log

class LogInViewModel(private val authRepository: AuthRepository): ViewModel() {

    private val _uiState = MutableStateFlow<LogInState>(LogInState.CheckingToken)
    val uiState: StateFlow<LogInState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            authRepository.checkToken()
                .onSuccess {
                    _uiState.update { LogInState.Success }
                }
                .onError { error ->
                    when (error) {
                        DataError.Http.FORBIDDEN -> _uiState.update { LogInState.LogIn() }
                        DataError.Http.UNAUTHORIZED -> _uiState.update { LogInState.LogIn(error = error.toUiText()) }
                        else -> _uiState.update { LogInState.ConnectionError(error.toUiText()) }
                    }
                }
        }
    }

    fun onNameChange(username: String) {
        _uiState.updateIfIs<LogInState.LogIn> { state ->
            state.copy(
                username = username,
                isSubmitEnabled = isFormValid(username, state.password),
                error = null,
            )
        }
    }

    fun onPasswordChange(password: String) {
        _uiState.updateIfIs<LogInState.LogIn> { state ->
            state.copy(
                password = password,
                isSubmitEnabled = isFormValid(state.username, password),
                error = null,
            )
        }
    }

    fun onSubmit() {
        _uiState.updateIfIs<LogInState.LogIn> { state ->
            state.copy(
                isSubmitEnabled = false,
                isLoading = true
            )
        }
        logIn(_uiState.value as? LogInState.LogIn ?: return)
    }

    private fun logIn(state: LogInState.LogIn) {
        viewModelScope.launch {
            val user = Auth(state.username, state.password)
            authRepository.logIn(user)
                .onSuccess { _uiState.update { LogInState.Success } }
                .onError { error ->
                    _uiState.update { state.copy(
                        isLoading = false,
                        error = error.toUiText()
                    ) }
                }
        }
    }

    private fun isFormValid(username: String, password: String): Boolean {
        return username.isNotBlank() && password.isNotBlank()
    }
}

