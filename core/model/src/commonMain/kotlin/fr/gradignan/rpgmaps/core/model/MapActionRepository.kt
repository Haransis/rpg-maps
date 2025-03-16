package fr.gradignan.rpgmaps.core.model

import kotlinx.coroutines.flow.Flow

interface MapActionRepository {
    fun getMapActionsFlow(): Flow<Result<MapAction, DataError>>
    suspend fun endTurn(id: Int): EmptyResult<DataError>
    suspend fun sendMove(move: MapAction.Move): EmptyResult<DataError>
    suspend fun sendLoadMap(loadMap: MapAction.LoadMap): EmptyResult<DataError>
    suspend fun sendAddCharacter(characterId: Int, owner: String, order: List<Int>): EmptyResult<DataError>
    suspend fun startGame(): EmptyResult<DataError>
    suspend fun sendInitiativeOrder(initiativeOrder: MapAction.InitiativeOrder): EmptyResult<DataError>
    suspend fun sendPing(ping: MapAction.Ping): EmptyResult<DataError>
}
