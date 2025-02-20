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
internal class Game(
    val username: String,
    val roomId: Int
)

fun NavController.navigateToGame(
    username: String,
    roomId: Int,
    navOptions: NavOptions? = null
) = navigate(route = Game(username, roomId), navOptions)

fun NavGraphBuilder.gameScreen(
    onBack: () -> Unit,
) {
    composable<Game> { entry ->
        val username = entry.toRoute<Game>().username
        val roomId = entry.toRoute<Game>().roomId
        GameScreenRoute(
            username = username,
            onBack = onBack
        )
    }
}