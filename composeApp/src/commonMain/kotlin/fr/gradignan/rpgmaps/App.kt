package fr.gradignan.rpgmaps

import androidx.compose.runtime.*
import fr.gradignan.rpgmaps.core.ui.theme.AppTheme
import org.jetbrains.compose.ui.tooling.preview.Preview
import fr.gradignan.rpgmaps.navigation.RpgMapsNavGraph

@Composable
@Preview
fun App() {
    AppTheme {
        //TODO("Use a scaffold to display appbar (https://github.com/JetBrains/compose-multiplatform/blob/a6961385ccf0dee7b6d31e3f73d2c8ef91005f1a/examples/nav_cupcake/composeApp/src/commonMain/kotlin/org/jetbrains/nav_cupcake/CupcakeScreen.kt#L50)")
        RpgMapsNavGraph()
    }
}
