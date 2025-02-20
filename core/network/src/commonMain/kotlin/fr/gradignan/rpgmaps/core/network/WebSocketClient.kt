package fr.gradignan.rpgmaps.core.network

import fr.gradignan.rpgmaps.core.common.Resource
import fr.gradignan.rpgmaps.core.network.model.Payload
import fr.gradignan.rpgmaps.core.network.model.ServerMessage
import kotlinx.coroutines.flow.Flow

interface WebSocketClient {
    fun sendMessage(serverMessage: ServerMessage)
    fun getPayloadsFlow(): Flow<Resource<Payload>>
    suspend fun close()
    suspend fun connect(token: String)
}
