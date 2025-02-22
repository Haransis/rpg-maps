package fr.gradignan.rpgmaps.core.data

import fr.gradignan.rpgmaps.core.model.DataError
import fr.gradignan.rpgmaps.core.model.map
import fr.gradignan.rpgmaps.core.model.MapAction
import fr.gradignan.rpgmaps.core.model.MapActionRepository
import fr.gradignan.rpgmaps.core.model.MapEffect
import fr.gradignan.rpgmaps.core.model.MapUpdate
import fr.gradignan.rpgmaps.core.model.Result
import fr.gradignan.rpgmaps.core.network.WebSocketClient
import fr.gradignan.rpgmaps.core.network.model.Payload
import fr.gradignan.rpgmaps.core.network.model.ServerMessage
import fr.gradignan.rpgmaps.core.network.model.toMapAction
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterNot
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.launch

class ResultMapActionRepository(
    private val webSocketClient: WebSocketClient,
    private val externalScope: CoroutineScope
): MapActionRepository {

    private val actionsFlow: Flow<Result<MapAction, DataError>> = webSocketClient.getPayloadsFlow()
        .map(::toMapActionResult)
        .shareIn(externalScope, SharingStarted.WhileSubscribed())

    // Using Flow<T>.filterIsInstance here is not possible due to type erasure
    @Suppress("UNCHECKED_CAST")
    private val mapUpdatesFlow = actionsFlow.filterNot(::isMapEffect)
        .map {  it as Result.Success<MapUpdate> }
    @Suppress("UNCHECKED_CAST")
    private val mapEffectsFlow = actionsFlow.filter(::isMapEffect)
        .map { (it as Result.Success<MapEffect>).data }

    private fun isMapEffect(action: Result<MapAction, DataError>): Boolean =
        action is Result.Success && action.data is MapEffect

    private fun toMapActionResult(payload: Result<Payload, DataError>): Result<MapAction, DataError> {
        return payload.map { it.toMapAction() }
    }

    override fun getMapUpdatesFlow(): Flow<Result<MapUpdate, DataError>> = mapUpdatesFlow
    override fun getMapEffectsFlow(): Flow<MapEffect> = mapEffectsFlow
    override fun endTurn() {
        externalScope.launch {
            webSocketClient.sendMessage(ServerMessage("Next", Payload.ServerNext(0)))
        }
    }
}
