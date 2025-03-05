package fr.gradignan.rpgmaps.core.data

import fr.gradignan.rpgmaps.core.model.DataError
import fr.gradignan.rpgmaps.core.model.EmptyResult
import fr.gradignan.rpgmaps.core.model.MapAction
import fr.gradignan.rpgmaps.core.model.MapActionRepository
import fr.gradignan.rpgmaps.core.model.MapEffect
import fr.gradignan.rpgmaps.core.model.MapUpdate
import fr.gradignan.rpgmaps.core.model.Result
import fr.gradignan.rpgmaps.core.network.WebSocketClient
import fr.gradignan.rpgmaps.core.network.model.NetworkMapCharacter
import fr.gradignan.rpgmaps.core.network.model.toAddCharacter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterNot
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.shareIn

class DefaultMapActionRepository(
    private val webSocketClient: WebSocketClient,
    externalScope: CoroutineScope
): MapActionRepository {

    private val actionsFlow: Flow<Result<MapAction, DataError>> = webSocketClient.getPayloadsFlow()
        .shareIn(externalScope, SharingStarted.WhileSubscribed())

    // Using Flow<T>.filterIsInstance here is not possible due to type erasure
    @Suppress("UNCHECKED_CAST")
    private val mapUpdatesFlow = actionsFlow.filterNot(::isMapEffect)
        .map {  it as Result<MapUpdate, DataError> }
    @Suppress("UNCHECKED_CAST")
    private val mapEffectsFlow = actionsFlow.filter(::isMapEffect)
        .map { (it as Result.Success<MapEffect>).data }

    private fun isMapEffect(action: Result<MapAction, DataError>): Boolean =
        action is Result.Success && action.data is MapEffect


    override fun getMapUpdatesFlow(): Flow<Result<MapUpdate, DataError>> = mapUpdatesFlow
    override fun getMapEffectsFlow(): Flow<MapEffect> = mapEffectsFlow
    override suspend fun endTurn(id: Int): EmptyResult<DataError> =
        webSocketClient.sendMessage(MapUpdate.Next(id))

    override suspend fun sendMove(move: MapUpdate.Move): EmptyResult<DataError> =
        webSocketClient.sendMessage(move)

    override suspend fun sendLoadMap(loadMap: MapUpdate.LoadMap): EmptyResult<DataError> =
        webSocketClient.sendMessage(loadMap)

    override suspend fun sendAddCharacter(
        characterId: Int,
        owner: String,
        order: List<Int>
    ): EmptyResult<DataError> = webSocketClient.sendMessage(MapUpdate.AddCharacter(
        NetworkMapCharacter(owner = owner, characterId = characterId).toAddCharacter(), order
    ))

    override suspend fun startGame(): EmptyResult<DataError> =
        webSocketClient.sendMessage(MapUpdate.Initiate(emptyList()))

    override suspend fun sendInitiativeOrder(initiativeOrder: MapUpdate.InitiativeOrder): EmptyResult<DataError> =
        webSocketClient.sendMessage(initiativeOrder)

    override suspend fun sendPing(ping: MapEffect.Ping): EmptyResult<DataError> =
        webSocketClient.sendMessage(ping)
}
