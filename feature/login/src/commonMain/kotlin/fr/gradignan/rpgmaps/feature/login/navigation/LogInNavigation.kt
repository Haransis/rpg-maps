package fr.gradignan.rpgmaps.feature.login.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import fr.gradignan.rpgmaps.feature.login.ui.LogInScreen
import kotlinx.serialization.Serializable

@Serializable
object LogIn

fun NavGraphBuilder.logInScreen(
    onLogIn: () -> Unit,
) {
    composable<LogIn> {
        LogInScreen(
            onLogIn = onLogIn
        )
    }
}