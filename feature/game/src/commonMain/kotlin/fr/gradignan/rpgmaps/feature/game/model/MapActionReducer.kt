package fr.gradignan.rpgmaps.feature.game.model


import fr.gradignan.rpgmaps.core.model.DataError
import fr.gradignan.rpgmaps.core.model.MapAction
import fr.gradignan.rpgmaps.core.model.Result
import fr.gradignan.rpgmaps.core.ui.error.toUiText


class MapActionReducer(
    private val playerName: String,
    private val isAdmin: Boolean
) {
    fun reduceMapActionResult(currentState: GameState, mapActionResult: Result<MapAction, DataError>): GameState =
        when (mapActionResult) {
            is Result.Error -> reduceError(currentState, mapActionResult.error)
            is Result.Success -> reduce(currentState, mapActionResult.data)
        }

    private fun reduce(currentState: GameState, mapAction: MapAction): GameState =
        when (currentState) {
            is GameState.Active ->
                currentState.copy(
                    mapState = reduceMapState(currentState.mapState, mapAction),
                    hudState = reduceHudState(currentState.hudState, mapAction),
                )
            else -> GameState.Active(
                mapState = reduceMapState(MapState(isAdmin = isAdmin), mapAction),
                hudState = reduceHudState(HUDState(isAdmin = isAdmin), mapAction),
            )
        }

    fun reduceError(currentState: GameState, error: DataError): GameState =
        when (error) {
            is DataError.Http -> GameState.Error(error.toUiText())
            is DataError.WebSocket -> reduceWebSocketError(currentState, error)
            is DataError.Local -> currentState
        }

    private fun reduceWebSocketError(currentState: GameState, error: DataError.WebSocket): GameState =
        when (currentState) {
            is GameState.Active ->
                currentState.copy(
                    mapState = reduceMapState(currentState.mapState, error),
                )
            is GameState.Error -> currentState
            is GameState.Loading -> GameState.Active(
                mapState = MapState(isAdmin = isAdmin, error = error.toUiText()),
                hudState = HUDState(isAdmin = isAdmin),
            )
        }

    private fun reduceMapState(mapState: MapState, error: DataError.WebSocket): MapState {
        return mapState.copy(
            error = error.toUiText()
        )
    }

    private fun reduceMapState(state: MapState, mapAction: MapAction): MapState {
        return when (mapAction) {
            is MapAction.AddCharacter -> state.copy(mapCharacters = state.mapCharacters + mapAction.character)
            is MapAction.GMGetMap -> if (isAdmin) {
                state.copy(mapCharacters = mapAction.mapCharacters)
            } else {
                state
            }
            is MapAction.Initiate -> state.copy(mapCharacters = mapAction.mapCharacters)
            is MapAction.LoadMap -> state.copy(imageUrl = mapAction.mapFilename, mapScale = mapAction.mapScale)
            is MapAction.Move -> state.moveCharacter(mapAction)
            is MapAction.Next -> {
                val playingCharacter = state.mapCharacters.find { it.cmId == mapAction.id }
                if (playingCharacter != null) {
                    state.copy(
                        playingMapCharacter = playingCharacter,
                        isGmChecked = false,
                        isSprintChecked = false,
                        isPlayerTurn = playerName == playingCharacter.owner
                    )
                } else {
                    state
                }
            }
            is MapAction.Ping -> {
                state.copy(pings = if (state.pings.contains(mapAction)){
                    state.pings.filterNot { it.x == mapAction.x && it.y == mapAction.y }
                } else {
                    state.pings + mapAction
                })
            }
            MapAction.NewTurn,
            is MapAction.InitiativeOrder,
            is MapAction.Connect -> state
        }
    }

    private fun reduceHudState(state: HUDState, mapAction: MapAction): HUDState {
        return when (mapAction) {
            is MapAction.AddCharacter -> state.appendLog("- ${mapAction.character.name} added")
                .copy(characters = state.characters + mapAction.character.toCharItem(state.characters.size))
            is MapAction.Connect -> state.appendLog("- ${mapAction.user} connected")
            is MapAction.GMGetMap -> if (isAdmin) {
                state.copy(characters = mapAction.mapCharacters.toCharItems())
            } else {
                state
            }
            is MapAction.Initiate -> state.appendLog("- Starting game").copy(
                characters = mapAction.mapCharacters.toCharItems()
            )
            is MapAction.InitiativeOrder -> state.copy(
                characters = state.characters.sortedBy { it.index in mapAction.order }
            )
            is MapAction.LoadMap -> state.appendLog("- Loading map")
            MapAction.NewTurn -> state.appendLog("- New Turn")
            is MapAction.Next -> {
                val playingCharacter = state.characters.find { it.optionalId == mapAction.id }
                if (playingCharacter != null) {
                    state.appendLog("- ${playingCharacter.name} is playing")
                        .copy(isPlayerTurn = playingCharacter.owner == playerName)
                } else {
                    state
                }
            }
            is MapAction.Ping,
            is MapAction.Move -> state
        }
    }

    private fun MapState.moveCharacter(move: MapAction.Move): MapState {
        val updatedCharacters = this.mapCharacters.toMutableList().apply {
            this.indexOfFirst { it.cmId == move.id }.takeIf { it != -1 }
                ?.let { index ->
                    val updatedCharacter =
                        this[index].copy(x = move.x, y = move.y)
                    set(index, updatedCharacter)
                }
        }

        var newState = this.copy(mapCharacters = updatedCharacters)
        if (move.owner == playerName) {
            newState = newState.copy(
                playingMapCharacter = playingMapCharacter?.copy(speed = playingMapCharacter.speed - previewPath.totalDistance)
            )
            newState = newState.deselectCharacter()
        }
        return newState
    }

    private fun HUDState.appendLog(message: String): HUDState =
        this.copy(logs = this.logs + message)
}
