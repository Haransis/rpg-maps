package fr.gradignan.rpgmaps.core.data

import com.russhwolf.settings.Settings
import fr.gradignan.rpgmaps.core.common.Resource
import fr.gradignan.rpgmaps.core.common.asResource
import fr.gradignan.rpgmaps.core.common.map
import fr.gradignan.rpgmaps.core.model.map
import fr.gradignan.rpgmaps.core.model.Auth
import fr.gradignan.rpgmaps.core.model.AuthRepository
import fr.gradignan.rpgmaps.core.model.DataError
import fr.gradignan.rpgmaps.core.model.Result
import fr.gradignan.rpgmaps.core.model.Token
import fr.gradignan.rpgmaps.core.network.NetworkHttpClient
import fr.gradignan.rpgmaps.core.network.model.toExternal
import fr.gradignan.rpgmaps.core.network.model.toNetwork
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class DefaultAuthRepository(private val clientService: NetworkHttpClient, private val settings: Settings):
    AuthRepository {
    override suspend fun checkToken(): Resource<Unit> {
        return clientService.checkToken()
    }

    override suspend fun logIn(auth: Auth): Resource<Token> {
        val result = clientService.logIn(auth.toNetwork()).map { it.toExternal() }
        if (result is Resource.Success) {
            settings.putString("username", auth.name)
            settings.putString("jwt_token", result.data.token)
            clientService.clearToken()
            return result
        } else {
            return result
        }
    }

    override fun getUsername(): Result<String, DataError.Local> {
        val username = settings.getStringOrNull("username")
        return if (username != null) {
            Result.Success(username)
        } else {
            Result.Error(DataError.Local.NO_DATA)
        }
    }

}
