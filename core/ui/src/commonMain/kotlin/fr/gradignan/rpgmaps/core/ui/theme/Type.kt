package fr.gradignan.rpgmaps.core.ui.theme

import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.runtime.Composable
import org.jetbrains.compose.resources.Font
import rpg_maps.core.ui.generated.resources.HermainitaBold
import rpg_maps.core.ui.generated.resources.Poliphile_W00_Normal
import rpg_maps.core.ui.generated.resources.Res

object Fonts {
    @Composable
    fun hermainitaBold() = FontFamily(
        Font(
            Res.font.HermainitaBold,
            FontWeight.Normal,
            FontStyle.Normal
        )
    )
    @Composable
    fun poliphileNormal() = FontFamily(
        Font(
            Res.font.Poliphile_W00_Normal,
            FontWeight.Normal,
            FontStyle.Normal
        )
    )
}
