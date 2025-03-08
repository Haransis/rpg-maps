package fr.gradignan.rpgmaps.feature.game.model

import androidx.compose.ui.geometry.Offset

fun Offset.getDistanceTo(target: Offset): Float {
    return this.minus(target).getDistance()
}
