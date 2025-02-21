package fr.gradignan.rpgmaps.feature.login.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
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

@Composable
fun LogInForm(
    uiState: LogInState.LogIn,
    onNameChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onSubmit: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(5),
        modifier = modifier.padding(MaterialTheme.spacing.extraLarge)
            .fillMaxSize()
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(
                MaterialTheme.spacing.medium,
                Alignment.CenterVertically
            )
        ) {
            val (first, second, third) = remember { FocusRequester.createRefs() }
            LaunchedEffect(Unit) {
                first.requestFocus()
            }
            TextField(
                value = uiState.username,
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
                enabled = !uiState.isLoading,
                modifier = Modifier.focusRequester(first).focusProperties { next = second }
            )
            var passwordVisible: Boolean by remember { mutableStateOf(false) }
            TextField(
                value = uiState.password,
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
                enabled = !uiState.isLoading,
                modifier = Modifier.focusRequester(second).focusProperties { next = third }
            )
            AnimatedVisibility(
                visible = uiState.error != null,
                enter = fadeIn() + slideInVertically(initialOffsetY = { it / 2 }),
                exit = fadeOut() + slideOutVertically(targetOffsetY = { it / 2 })
            ) {
                uiState.error?.let { error ->
                    Text(
                        text = error.asString(),
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.animateContentSize()
                    )
                }
            }
            LoadingButton(
                enabled = uiState.isSubmitEnabled,
                isLoading = uiState.isLoading,
                onClick = onSubmit,
                modifier = Modifier.focusable().focusRequester(third).focusProperties { next = first }
            )
        }
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
