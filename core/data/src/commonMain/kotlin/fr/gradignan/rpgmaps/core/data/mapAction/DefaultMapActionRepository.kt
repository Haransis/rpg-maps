package fr.gradignan.rpgmaps.core.data.mapAction

import com.russhwolf.settings.Settings
import fr.gradignan.rpgmaps.core.common.Resource
import fr.gradignan.rpgmaps.core.model.map
import fr.gradignan.rpgmaps.core.model.MapAction
import fr.gradignan.rpgmaps.core.model.MapEffect
import fr.gradignan.rpgmaps.core.model.MapUpdate
import fr.gradignan.rpgmaps.core.network.network.WebSocketService
import fr.gradignan.rpgmaps.core.network.network.model.Payload
import fr.gradignan.rpgmaps.core.network.network.model.ServerMessage
import fr.gradignan.rpgmaps.core.network.network.model.toMapAction
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterNot
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.launch

class DefaultMapActionRepository(
    private val webSocketService: WebSocketService,
    private val settings: Settings,
    private val externalScope: CoroutineScope
): MapActionRepository {

    private val actionsFlow: Flow<Resource<MapAction>> = webSocketService.getPayloadsFlow()
        .map (::toMapActionResource)
        .onStart { externalScope.launch {
            val token = settings.getString("jwt_token", "no_token")
            webSocketService.connect(token)
        } }
        .onCompletion { externalScope.launch { webSocketService.close() } }
        .shareIn(externalScope, SharingStarted.WhileSubscribed())

    // Using Flow<T>.filterIsInstance here is not possible due to type erasure
    @Suppress("UNCHECKED_CAST")
    private val mapUpdatesFlow = actionsFlow.filterNot(::isMapEffect)
        .map {  it as Resource<MapUpdate> }
    @Suppress("UNCHECKED_CAST")
    private val mapEffectsFlow = actionsFlow.filter(::isMapEffect)
        .map { (it as Resource.Success<MapEffect>).data }

    private fun isMapEffect(action: Resource<MapAction>): Boolean =
        action is Resource.Success && action.data is MapEffect

    private fun toMapActionResource(payload: Resource<Payload>): Resource<MapAction> {
        return payload.map { it.toMapAction() }
    }

    override fun getMapUpdatesFlow(): Flow<Resource<MapUpdate>> = mapUpdatesFlow
    override fun getMapEffectsFlow(): Flow<MapEffect> = mapEffectsFlow
    override fun endTurn() {
        webSocketService.sendMessage(ServerMessage("Next", Payload.ServerNext(0)))
    }
}
