package fr.gradignan.rpgmaps.feature.home.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import fr.gradignan.rpgmaps.core.common.Resource
import fr.gradignan.rpgmaps.core.data.auth.AuthRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class HomeViewModel(authRepository: AuthRepository): ViewModel() {
    private val _username: Flow<Resource<String>> = authRepository.getUsername()
    val username = _username.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = Resource.Loading
    )
}
