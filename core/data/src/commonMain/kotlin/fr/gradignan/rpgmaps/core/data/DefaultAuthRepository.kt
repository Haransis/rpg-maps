package fr.gradignan.rpgmaps.core.data

import com.russhwolf.settings.Settings
import fr.gradignan.rpgmaps.core.common.Resource
import fr.gradignan.rpgmaps.core.common.asResource
import fr.gradignan.rpgmaps.core.common.map
import fr.gradignan.rpgmaps.core.model.map
import fr.gradignan.rpgmaps.core.model.Auth
import fr.gradignan.rpgmaps.core.model.AuthRepository
import fr.gradignan.rpgmaps.core.model.DataError
import fr.gradignan.rpgmaps.core.model.EmptyResult
import fr.gradignan.rpgmaps.core.model.Result
import fr.gradignan.rpgmaps.core.model.Token
import fr.gradignan.rpgmaps.core.model.asEmptyDataResult
import fr.gradignan.rpgmaps.core.model.mapError
import fr.gradignan.rpgmaps.core.model.mapErrorIf
import fr.gradignan.rpgmaps.core.model.onSuccess
import fr.gradignan.rpgmaps.core.network.NetworkHttpClient
import fr.gradignan.rpgmaps.core.network.model.toExternal
import fr.gradignan.rpgmaps.core.network.model.toNetwork

class DefaultAuthRepository(
    private val clientService: NetworkHttpClient,
    private val settings: Settings
): AuthRepository {
    override suspend fun checkToken(): EmptyResult<DataError.Http> {
        return clientService.checkToken()
    }

    override suspend fun logIn(auth: Auth): EmptyResult<DataError.Http> =
        clientService.logIn(auth.toNetwork())
            .onSuccess {
                settings.putString("username", auth.name)
                settings.putString("jwt_token", it.token)
                clientService.clearToken()
            }
            .mapErrorIf(DataError.Http.UNAUTHORIZED) { DataError.Http.WRONG_CREDENTIALS }
            .asEmptyDataResult()

    override fun getUsername(): Result<String, DataError.Local> {
        val username = settings.getStringOrNull("username")
        return if (username != null) {
            Result.Success(username)
        } else {
            Result.Error(DataError.Local.NO_DATA)
        }
    }

}
