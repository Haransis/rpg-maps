package fr.gradignan.rpgmaps.feature.home.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptionsBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navOptions
import androidx.navigation.toRoute
import fr.gradignan.rpgmaps.feature.home.ui.HomeScreen
import kotlinx.serialization.Serializable

@Serializable
internal data object Home

fun NavController.navigateToHome(
    builder: NavOptionsBuilder.() -> Unit
) = navigate(route = Home, navOptions(builder))

fun NavGraphBuilder.homeScreen(
    onCreateMapClick: () -> Unit,
    onGameClick: (String) -> Unit,
) {
    composable<Home> {
        HomeScreen(
            onCreateMapClick = onCreateMapClick,
            onGameClick = onGameClick
        )
    }
}
