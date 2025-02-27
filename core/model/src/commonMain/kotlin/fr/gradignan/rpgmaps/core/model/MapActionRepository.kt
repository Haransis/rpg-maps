package fr.gradignan.rpgmaps.core.model

import kotlinx.coroutines.flow.Flow

interface MapActionRepository {
    fun getMapUpdatesFlow(): Flow<Result<MapUpdate, DataError>>
    fun getMapEffectsFlow(): Flow<MapEffect>
    suspend fun endTurn(id: Int): EmptyResult<DataError>
    suspend fun sendMove(move: MapUpdate.Move): EmptyResult<DataError>
    suspend fun sendLoadMap(loadMap: MapUpdate.LoadMap): EmptyResult<DataError>
    suspend fun sendAddCharacter(characterId: Int, owner: String, order: List<Int>): EmptyResult<DataError>
    suspend fun startGame(): EmptyResult<DataError>
    suspend fun sendInitiativeOrder(initiativeOrder: MapUpdate.InitiativeOrder): EmptyResult<DataError>
}
