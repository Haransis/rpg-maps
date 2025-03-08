package fr.gradignan.rpgmaps.feature.login.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import fr.gradignan.rpgmaps.feature.login.model.LogInState
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun LogInScreen(
    onLogIn: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: LogInViewModel = koinViewModel<LogInViewModel>()
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier.fillMaxSize()
    ) {
        val uiState by viewModel.uiState.collectAsStateWithLifecycle()
        when (val state = uiState) {
            LogInState.CheckingToken -> CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            is LogInState.ConnectionError -> Text(
                color = MaterialTheme.colorScheme.error,
                text = state.error.asString()
            )
            is LogInState.Success -> onLogIn()
            is LogInState.LogIn -> LogInForm(
                uiState = state,
                onNameChange = viewModel::onNameChange,
                onPasswordChange = viewModel::onPasswordChange,
                onSubmit = viewModel::onSubmit
            )
        }
    }
}
