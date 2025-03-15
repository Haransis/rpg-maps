package fr.gradignan.rpgmaps.feature.game.model


import fr.gradignan.rpgmaps.core.model.DataError
import fr.gradignan.rpgmaps.core.model.MapUpdate
import fr.gradignan.rpgmaps.core.model.Result
import fr.gradignan.rpgmaps.core.ui.error.toUiText


class MapUpdateReducer(
    private val playerName: String,
    private val isAdmin: Boolean
) {
    fun reduceMapUpdateResult(currentState: GameState, mapUpdateResult: Result<MapUpdate, DataError>): GameState =
        when (mapUpdateResult) {
            is Result.Error -> reduceError(currentState, mapUpdateResult.error)
            is Result.Success -> reduce(currentState, mapUpdateResult.data)
        }

    private fun reduce(currentState: GameState, mapUpdate: MapUpdate): GameState =
        when (currentState) {
            is GameState.Active ->
                currentState.copy(
                    mapState = reduceMapState(currentState.mapState, mapUpdate),
                    hudState = reduceHudState(currentState.hudState, mapUpdate),
                )
            else -> GameState.Active(
                mapState = reduceMapState(MapState(isAdmin = isAdmin), mapUpdate),
                hudState = reduceHudState(HUDState(isAdmin = isAdmin), mapUpdate),
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

    private fun reduceMapState(state: MapState, mapUpdate: MapUpdate): MapState {
        return when (mapUpdate) {
            is MapUpdate.AddCharacter -> state.copy(mapCharacters = state.mapCharacters + mapUpdate.character)
            is MapUpdate.GMGetMap -> if (isAdmin) {
                state.copy(mapCharacters = mapUpdate.mapCharacters)
            } else {
                state
            }
            is MapUpdate.Initiate -> state.copy(mapCharacters = mapUpdate.mapCharacters)
            is MapUpdate.LoadMap -> state.copy(imageUrl = mapUpdate.mapFilename)
            is MapUpdate.Move -> state.moveCharacter(mapUpdate)
            is MapUpdate.Next -> {
                val playingCharacter = state.mapCharacters.find { it.cmId == mapUpdate.id }
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
            MapUpdate.NewTurn,
            is MapUpdate.InitiativeOrder,
            is MapUpdate.Connect -> state
        }
    }

    private fun reduceHudState(state: HUDState, mapUpdate: MapUpdate): HUDState {
        return when (mapUpdate) {
            is MapUpdate.AddCharacter -> state.appendLog("- ${mapUpdate.character.name} added")
                .copy(characters = state.characters + mapUpdate.character.toCharItem(state.characters.size))
            is MapUpdate.Connect -> state.appendLog("- ${mapUpdate.user} connected")
            is MapUpdate.GMGetMap -> if (isAdmin) {
                state.copy(characters = mapUpdate.mapCharacters.toCharItems())
            } else {
                state
            }
            is MapUpdate.Initiate -> state.appendLog("- Starting game").copy(
                characters = mapUpdate.mapCharacters.toCharItems()
            )
            is MapUpdate.InitiativeOrder -> state.copy(
                characters = state.characters.sortedBy { it.index in mapUpdate.order }
            )
            is MapUpdate.LoadMap -> state.appendLog("- Loading map")
            MapUpdate.NewTurn -> state.appendLog("- New Turn")
            is MapUpdate.Next -> {
                val playingCharacter = state.characters.find { it.optionalId == mapUpdate.id }
                if (playingCharacter != null) {
                    state.appendLog("- ${playingCharacter.name} is playing")
                        .copy(isPlayerTurn = playingCharacter.owner == playerName)
                } else {
                    state
                }
            }
            is MapUpdate.Move -> state
        }
    }

    private fun MapState.moveCharacter(move: MapUpdate.Move): MapState {
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
