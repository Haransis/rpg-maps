package fr.gradignan.rpgmaps.feature.login.ui

import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.input.ImeAction
import fr.gradignan.rpgmaps.core.ui.compose.AnimatedError
import fr.gradignan.rpgmaps.core.ui.theme.spacing
import fr.gradignan.rpgmaps.feature.login.model.LogInState


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
            UserTextField(
                username = uiState.username,
                enabled = uiState.isLoading,
                onNameChange = onNameChange,
                onSubmit = onSubmit,
                modifier = Modifier.focusRequester(first).focusProperties { next = second }
            )
            PasswordTextField(
                password = uiState.password,
                enabled = uiState.isLoading,
                onPasswordChange = onPasswordChange,
                onSubmit = onSubmit,
                modifier = Modifier.focusRequester(second).focusProperties { next = third }
            )
            AnimatedError(uiState.error?.asString())
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
internal fun UserTextField(
    username: String,
    enabled: Boolean,
    onNameChange: (String) -> Unit,
    onSubmit: () -> Unit,
    modifier: Modifier = Modifier
) {
    TextField(
        value = username,
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
        enabled = !enabled,
        modifier = modifier
    )
}
