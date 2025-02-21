package fr.gradignan.rpgmaps.core.model

import fr.gradignan.rpgmaps.core.common.Resource
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    suspend fun logIn(auth: Auth): EmptyResult<DataError.Http>
    fun getUsername(): Result<String, DataError.Local>
    suspend fun checkToken(): EmptyResult<DataError.Http>
}
