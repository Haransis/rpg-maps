package fr.gradignan.rpgmaps.feature.home.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptionsBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navOptions
import fr.gradignan.rpgmaps.core.model.Room
import fr.gradignan.rpgmaps.feature.home.ui.HomeScreen
import kotlinx.serialization.Serializable

@Serializable
internal data object Home

fun NavController.navigateToHome(
    builder: NavOptionsBuilder.() -> Unit
) = navigate(route = Home, navOptions(builder))

fun NavGraphBuilder.homeScreen(
    onCreateMapClick: () -> Unit,
    onRoomClick: (Room) -> Unit,
) {
    composable<Home> {
        HomeScreen(
            onCreateMapClick = onCreateMapClick,
            onRoomClick = onRoomClick
        )
    }
}
