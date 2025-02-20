package fr.gradignan.rpgmaps.core.network.network

import fr.gradignan.rpgmaps.core.common.Resource
import fr.gradignan.rpgmaps.core.network.network.model.NetworkAuth
import fr.gradignan.rpgmaps.core.network.network.model.NetworkToken

interface HttpClientService {
    fun clearToken()
    suspend fun logIn(auth: NetworkAuth): Resource<NetworkToken>
    suspend fun checkToken(): Resource<Unit>
}
