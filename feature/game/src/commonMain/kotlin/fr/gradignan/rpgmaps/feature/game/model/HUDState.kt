package fr.gradignan.rpgmaps.feature.game.model

import androidx.compose.ui.geometry.Offset
import fr.gradignan.rpgmaps.core.model.MapCharacter

data class HUDState(
    val sprintEnabled: Boolean = false,
    val previewPath: List<Offset> = emptyList(),
    val selectedMapCharacter: MapCharacter? = null
)
