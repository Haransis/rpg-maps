package fr.gradignan.rpgmaps.feature.game.model


data class StatusState (
    val characters: List<CharItem> = emptyList(),
    val logs: List<String> = emptyList(),
    val isAdmin: Boolean = false,
    val isPlayerTurn: Boolean = false,
)
