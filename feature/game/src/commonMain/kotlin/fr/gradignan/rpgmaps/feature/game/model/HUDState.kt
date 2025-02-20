package fr.gradignan.rpgmaps.feature.game.model

import androidx.compose.ui.geometry.Offset
import fr.gradignan.rpgmaps.core.model.Character

data class HUDState(
    val sprintEnabled: Boolean = false,
    val previewPath: List<Offset> = emptyList(),
    val selectedCharacter: Character? = null
)
