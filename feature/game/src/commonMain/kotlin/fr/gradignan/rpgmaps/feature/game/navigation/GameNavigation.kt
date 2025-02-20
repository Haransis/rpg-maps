package fr.gradignan.rpgmaps.feature.game.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import fr.gradignan.rpgmaps.feature.game.ui.GameScreen
import fr.gradignan.rpgmaps.feature.game.ui.GameScreenRoute
import kotlinx.serialization.Serializable

@Serializable
internal class Game(val username: String)

fun NavController.navigateToGame(
    username: String,
    navOptions: NavOptions? = null
) = navigate(route = Game(username), navOptions)

fun NavGraphBuilder.gameScreen(
    onBack: () -> Unit,
) {
    composable<Game> { entry ->
        val username = entry.toRoute<Game>().username
        GameScreenRoute(
            username = username,
            onBack = onBack
        )
    }
}