package fr.gradignan.rpgmaps.feature.login.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import fr.gradignan.rpgmaps.core.ui.theme.spacing
import fr.gradignan.rpgmaps.feature.login.model.LogInState
import fr.gradignan.rpgmaps.feature.login.model.LogInUiState
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun LogInScreen(
    onLogIn: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: LogInViewModel = koinViewModel<LogInViewModel>()
) {
    Surface (
        shape = RoundedCornerShape(5),
        modifier = modifier.padding(MaterialTheme.spacing.extraLarge)
    ) {
        val uiState = viewModel.uiState.collectAsState(LogInUiState())
        when (uiState.value.logInState) {
            LogInState.CheckingToken -> CircularProgressIndicator()
            is LogInState.Success -> onLogIn()
            else -> LogInForm(
                uiState = uiState,
                onNameChange = viewModel::onNameChange,
                onPasswordChange = viewModel::onPasswordChange,
                onSubmit = viewModel::onSubmit
            )
        }
    }
}

@Composable
fun LogInForm(uiState: State<LogInUiState>, onNameChange: (String) -> Unit, onPasswordChange: (String) -> Unit, onSubmit: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(
            MaterialTheme.spacing.medium,
            Alignment.CenterVertically
        ),
        modifier = modifier
    ) {
        val (first, second, third) = remember { FocusRequester.createRefs() }
        LaunchedEffect(Unit) {
            first.requestFocus()
        }
        TextField(
            value = uiState.value.username,
            placeholder = { Text("Username") },
            onValueChange = onNameChange,
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                imeAction = ImeAction.Done,
                showKeyboardOnFocus = true
            ),
            keyboardActions = KeyboardActions(
                onDone = { onSubmit() }
            ),
            enabled = uiState.value.logInState !is LogInState.Loading,
            modifier = Modifier.focusRequester(first).focusProperties { next = second }
        )
        var passwordVisible: Boolean by remember { mutableStateOf(false) }
        TextField(
            value = uiState.value.password,
            placeholder = { Text("Password") },
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                val image =
                    if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                val desc = if (passwordVisible) "Hide password" else "Show password"

                IconButton(
                    onClick = { passwordVisible = !passwordVisible }
                ) {
                    Icon(imageVector = image, contentDescription = desc)
                }
            },
            onValueChange = onPasswordChange,
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Done,
                showKeyboardOnFocus = true
            ),
            keyboardActions = KeyboardActions(
                onDone = { onSubmit() }
            ),
            enabled = uiState.value.logInState !is LogInState.Loading,
            modifier = Modifier.focusRequester(second).focusProperties { next = third }
        )
        val error = remember {
            if (uiState.value.logInState is LogInState.Error)
                (uiState.value.logInState as LogInState.Error).error.message ?: "Unknown error"
            else ""
        }
        AnimatedVisibility(error.isNotEmpty()) {
            Text(
                color = MaterialTheme.colorScheme.error,
                text = error
            )
        }
        LoadingButton(
            enabled = uiState.value.isSubmitEnabled,
            isLoading = uiState.value.logInState is LogInState.Loading,
            onClick = onSubmit,
            modifier = Modifier.focusable().focusRequester(third).focusProperties { next = first }
        )
    }
}

@Composable
private fun LoadingButton(
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isLoading: Boolean = false,
) {
    Button(
        enabled = enabled,
        onClick = onClick,
        modifier = modifier
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(ButtonDefaults.IconSpacing),
            modifier = Modifier.animateContentSize()
        ) {
            AnimatedVisibility(isLoading) {
                CircularProgressIndicator(
                    color = Color.White,
                    modifier = Modifier.size(ButtonDefaults.IconSize),
                    strokeWidth = 2.dp
                )
            }
            Text("Log In")
        }
    }
}
