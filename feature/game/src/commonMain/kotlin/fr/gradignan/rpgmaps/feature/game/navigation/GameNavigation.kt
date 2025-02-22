package fr.gradignan.rpgmaps.feature.game.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import fr.gradignan.rpgmaps.core.model.Room
import fr.gradignan.rpgmaps.feature.game.ui.GameScreenRoute
import kotlinx.serialization.Serializable

@Serializable
internal class Game(
    val username: String,
    val roomId: Int,
    val admin: Boolean
)

fun NavController.navigateToGame(
    room: Room,
    navOptions: NavOptions? = null
) = navigate(route = Game(room.username, room.roomId, room.roleInRoom == "mj"), navOptions)

fun NavGraphBuilder.gameScreen(
    onBack: () -> Unit,
) {
    composable<Game> { entry ->
        with(entry.toRoute<Game>()) {
            // pass arguments directly as Game ?
            GameScreenRoute(
                roomId = roomId,
                admin = admin,
                username = username,
                onBack = onBack
            )
        }
    }
}
