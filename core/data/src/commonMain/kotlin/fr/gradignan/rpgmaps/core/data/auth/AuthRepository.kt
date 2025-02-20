package fr.gradignan.rpgmaps.core.data.auth

import fr.gradignan.rpgmaps.core.common.Resource
import fr.gradignan.rpgmaps.core.model.Auth
import fr.gradignan.rpgmaps.core.model.Token
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    suspend fun logIn(auth: Auth): Resource<Token>
    fun getUsername(): Flow<Resource<String>>
    suspend fun checkToken(): Resource<Unit>
}
