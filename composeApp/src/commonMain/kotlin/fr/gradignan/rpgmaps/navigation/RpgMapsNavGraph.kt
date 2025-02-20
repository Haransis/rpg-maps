package fr.gradignan.rpgmaps.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import fr.gradignan.rpgmaps.feature.createMap.navigation.createMapScreen
import fr.gradignan.rpgmaps.feature.createMap.navigation.navigateToCreateMap
import fr.gradignan.rpgmaps.feature.game.navigation.gameScreen
import fr.gradignan.rpgmaps.feature.game.navigation.navigateToGame
import fr.gradignan.rpgmaps.feature.home.navigation.homeScreen
import fr.gradignan.rpgmaps.feature.home.navigation.navigateToHome
import fr.gradignan.rpgmaps.feature.login.navigation.LogIn
import fr.gradignan.rpgmaps.feature.login.navigation.logInScreen

@Composable
fun RpgMapsNavGraph(
    modifier: Modifier = Modifier,
    startDestination: Any = LogIn,
    navController: NavHostController = rememberNavController(),
) {
    NavHost(
        modifier = modifier.fillMaxSize(),
        startDestination = startDestination,
        navController = navController,
    ) {
        logInScreen (
            onLogIn = {
                navController.navigateToHome {
                    popUpTo(LogIn) {
                        saveState = false
                        inclusive = true
                    }
                    restoreState = false
                }
            }
        )
        homeScreen(
            onCreateMapClick = { navController.navigateToCreateMap() },
            onGameClick = { username -> navController.navigateToGame(username)}
        )
        gameScreen(
            onBack = navController::navigateUp
        )
        createMapScreen(
            onBack = navController::navigateUp
        )
    }
}