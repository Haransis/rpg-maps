package fr.gradignan.rpgmaps.core.network

import fr.gradignan.rpgmaps.core.model.DataError
import fr.gradignan.rpgmaps.core.model.EmptyResult
import fr.gradignan.rpgmaps.core.model.Result
import fr.gradignan.rpgmaps.core.network.model.Payload
import fr.gradignan.rpgmaps.core.network.model.ServerMessage
import kotlinx.coroutines.flow.Flow

interface WebSocketClient {
    suspend fun sendMessage(serverMessage: ServerMessage): EmptyResult<DataError>
    fun getPayloadsFlow(): Flow<Result<Payload, DataError>>
}
