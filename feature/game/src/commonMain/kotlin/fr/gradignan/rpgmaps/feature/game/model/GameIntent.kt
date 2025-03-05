package fr.gradignan.rpgmaps.feature.game.model

import androidx.compose.ui.geometry.Offset

sealed class GameIntent {
    data object EndTurn : GameIntent()
    data class SprintCheck(val checked: Boolean) : GameIntent()
    data class MapClick(val point: Offset) : GameIntent()
    data object Unselect : GameIntent()
    data object DoubleClick : GameIntent()
    data class PointerMove(val offset: Offset) : GameIntent()
    data class GmCheck(val checked: Boolean) : GameIntent()
    data class ChangeInitiative(val order: List<Int>) : GameIntent()
    data class RulerCheck(val change: Boolean) : GameIntent()
    data class PingCheck(val change: Boolean) : GameIntent()
    data class DeleteChar(val index: Int) : GameIntent()
}
