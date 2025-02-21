package fr.gradignan.rpgmaps.core.network

import fr.gradignan.rpgmaps.core.common.Resource
import fr.gradignan.rpgmaps.core.model.DataError
import fr.gradignan.rpgmaps.core.model.EmptyResult
import fr.gradignan.rpgmaps.core.model.Result
import fr.gradignan.rpgmaps.core.model.Room
import fr.gradignan.rpgmaps.core.network.model.NetworkAuth
import fr.gradignan.rpgmaps.core.network.model.NetworkRoom
import fr.gradignan.rpgmaps.core.network.model.NetworkToken

interface NetworkHttpClient {
    fun clearToken()
    suspend fun logIn(auth: NetworkAuth): Result<NetworkToken, DataError.Http>
    suspend fun checkToken(): EmptyResult<DataError.Http>
    suspend fun getRooms(): Result<List<NetworkRoom>, DataError.Http>
}
