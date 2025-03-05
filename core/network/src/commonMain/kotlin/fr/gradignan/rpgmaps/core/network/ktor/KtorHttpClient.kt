package fr.gradignan.rpgmaps.core.network.ktor

import fr.gradignan.rpgmaps.core.model.DataError
import fr.gradignan.rpgmaps.core.model.EmptyResult
import fr.gradignan.rpgmaps.core.model.Result
import fr.gradignan.rpgmaps.core.network.BuildKonfig
import fr.gradignan.rpgmaps.core.network.NetworkHttpClient
import fr.gradignan.rpgmaps.core.network.model.NetworkAuth
import fr.gradignan.rpgmaps.core.network.model.NetworkBoard
import fr.gradignan.rpgmaps.core.network.model.NetworkDataCharacter
import fr.gradignan.rpgmaps.core.network.model.NetworkMapCharacter
import fr.gradignan.rpgmaps.core.network.model.NetworkRoom
import fr.gradignan.rpgmaps.core.network.model.NetworkToken
import fr.gradignan.rpgmaps.core.network.safeCall
import io.ktor.client.HttpClient
import io.ktor.client.plugins.auth.authProvider
import io.ktor.client.plugins.auth.providers.BearerAuthProvider
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType

class KtorHttpClient(private val client: HttpClient): NetworkHttpClient {
    override fun clearToken() {
        client.authProvider<BearerAuthProvider>()?.clearToken()
    }

    override suspend fun logIn(auth: NetworkAuth): Result<NetworkToken, DataError.Http> = safeCall {
        client.post("${BuildKonfig.baseUrl}/users/login") {
            contentType(ContentType.Application.Json)
            setBody(auth)
        }
    }

    override suspend fun checkToken(): EmptyResult<DataError.Http> = safeCall {
        client.post("${BuildKonfig.baseUrl}/users/check-token")
    }

    override suspend fun getRooms(): Result<List<NetworkRoom>, DataError.Http> = safeCall {
        client.get("${BuildKonfig.baseUrl}/rooms")
    }

    override suspend fun getBoards(): Result<List<NetworkBoard>, DataError.Http> = safeCall {
        client.get("${BuildKonfig.baseUrl}/maps/get-maps")
    }

    override suspend fun getAllCharacters(): Result<List<NetworkDataCharacter>, DataError.Http> = safeCall {
        client.get("${BuildKonfig.baseUrl}/chars/get-all")
    }
}
