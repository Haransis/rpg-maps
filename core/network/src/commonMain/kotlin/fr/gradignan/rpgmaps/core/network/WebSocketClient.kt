package fr.gradignan.rpgmaps.core.network

import fr.gradignan.rpgmaps.core.model.DataError
import fr.gradignan.rpgmaps.core.model.EmptyResult
import fr.gradignan.rpgmaps.core.model.MapAction
import fr.gradignan.rpgmaps.core.model.MapUpdate
import fr.gradignan.rpgmaps.core.model.Result
import fr.gradignan.rpgmaps.core.network.model.Payload
import fr.gradignan.rpgmaps.core.network.model.ServerMessage
import kotlinx.coroutines.flow.Flow

interface WebSocketClient {
    suspend fun sendMessage(mapAction: MapAction): EmptyResult<DataError>
    fun getPayloadsFlow(): Flow<Result<MapAction, DataError>>
}
