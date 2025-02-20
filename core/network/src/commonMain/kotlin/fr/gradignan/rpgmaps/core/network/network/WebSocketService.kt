package fr.gradignan.rpgmaps.core.network.network

import fr.gradignan.rpgmaps.core.common.Resource
import fr.gradignan.rpgmaps.core.network.network.model.Payload
import fr.gradignan.rpgmaps.core.network.network.model.ServerMessage
import kotlinx.coroutines.flow.Flow

interface WebSocketService {
    fun sendMessage(serverMessage: ServerMessage)
    fun getPayloadsFlow(): Flow<Resource<Payload>>
    suspend fun close()
    suspend fun connect(token: String)
}
