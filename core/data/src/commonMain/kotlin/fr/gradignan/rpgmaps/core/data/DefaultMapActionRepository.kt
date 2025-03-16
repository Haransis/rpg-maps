package fr.gradignan.rpgmaps.core.data

import fr.gradignan.rpgmaps.core.model.DataError
import fr.gradignan.rpgmaps.core.model.EmptyResult
import fr.gradignan.rpgmaps.core.model.MapActionRepository
import fr.gradignan.rpgmaps.core.model.MapAction
import fr.gradignan.rpgmaps.core.model.Result
import fr.gradignan.rpgmaps.core.network.WebSocketClient
import fr.gradignan.rpgmaps.core.network.model.NetworkAddCharacter
import fr.gradignan.rpgmaps.core.network.model.toMapCharacter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.shareIn

class DefaultMapActionRepository(
    private val webSocketClient: WebSocketClient,
    externalScope: CoroutineScope
): MapActionRepository {

    private val actionsFlow: SharedFlow<Result<MapAction, DataError>> = webSocketClient.getPayloadsFlow()
        .shareIn(externalScope, SharingStarted.WhileSubscribed())

    override fun getMapActionsFlow(): Flow<Result<MapAction, DataError>> = actionsFlow
    override suspend fun endTurn(id: Int): EmptyResult<DataError> =
        webSocketClient.sendMessage(MapAction.Next(id))

    override suspend fun sendMove(move: MapAction.Move): EmptyResult<DataError> =
        webSocketClient.sendMessage(move)

    override suspend fun sendLoadMap(loadMap: MapAction.LoadMap): EmptyResult<DataError> =
        webSocketClient.sendMessage(loadMap)

    override suspend fun sendAddCharacter(
        characterId: Int,
        owner: String,
        order: List<Int>
    ): EmptyResult<DataError> = webSocketClient.sendMessage(MapAction.AddCharacter(
        NetworkAddCharacter(owner = owner, characterId = characterId).toMapCharacter(), order
    ))

    override suspend fun startGame(): EmptyResult<DataError> =
        webSocketClient.sendMessage(MapAction.Initiate(emptyList()))

    override suspend fun sendInitiativeOrder(initiativeOrder: MapAction.InitiativeOrder): EmptyResult<DataError> =
        webSocketClient.sendMessage(initiativeOrder)

    override suspend fun sendPing(ping: MapAction.Ping): EmptyResult<DataError> =
        webSocketClient.sendMessage(ping)
}
