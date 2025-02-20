package fr.gradignan.rpgmaps.feature.login.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import fr.gradignan.rpgmaps.core.common.Resource
import fr.gradignan.rpgmaps.core.model.AuthRepository
import fr.gradignan.rpgmaps.core.model.Auth
import fr.gradignan.rpgmaps.feature.login.model.LogInState
import fr.gradignan.rpgmaps.feature.login.model.LogInUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class LogInViewModel(private val authRepository: AuthRepository): ViewModel() {

    private val _uiState = MutableStateFlow(LogInUiState())
    val uiState: StateFlow<LogInUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            if (authRepository.checkToken() is Resource.Success) {
                //TODO("When no Internet, print an error message instead of login")
                _uiState.update { it.copy(logInState = LogInState.Success) }
            } else {
                _uiState.update { it.copy(logInState = LogInState.Empty) }
            }
        }
    }

    fun onNameChange(username: String) {
        _uiState.update { currentState ->
            currentState.copy(
                username = username,
                isSubmitEnabled = isFormValid(username, currentState.password),
                logInState = LogInState.Empty
            )
        }
    }

    fun onPasswordChange(password: String) {
        _uiState.update { currentState ->
            currentState.copy(
                password = password,
                isSubmitEnabled = isFormValid(currentState.username, password),
                logInState = LogInState.Empty
            )
        }
    }

    fun onSubmit() {
        _uiState.update { it.copy(logInState = LogInState.Loading) }
        viewModelScope.launch {
            val user = Auth(_uiState.value.username, _uiState.value.password)
            when (val resource = authRepository.logIn(user)) {
                is Resource.Success -> _uiState.update { it.copy(logInState = LogInState.Success) }
                is Resource.Error -> {
                    _uiState.update { it.copy(logInState = LogInState.Error(resource.exception)) }
                }
                Resource.Loading -> {}
            }
        }
    }

    private fun isFormValid(username: String, password: String): Boolean {
        return username.isNotBlank() && password.isNotBlank()
    }
}

