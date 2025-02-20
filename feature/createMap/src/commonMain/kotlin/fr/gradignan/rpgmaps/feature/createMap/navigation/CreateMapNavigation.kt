package fr.gradignan.rpgmaps.feature.createMap.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.NavOptionsBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navOptions
import fr.gradignan.rpgmaps.feature.createMap.ui.CreateMapScreen
import kotlinx.serialization.Serializable

@Serializable
internal object CreateMap

fun NavController.navigateToCreateMap(
    options: NavOptions? = null
) = navigate(route = CreateMap, options)

fun NavGraphBuilder.createMapScreen(
    onBack: () -> Unit,
) {
    composable<CreateMap> {
        CreateMapScreen(
            onBack
        )
    }
}
