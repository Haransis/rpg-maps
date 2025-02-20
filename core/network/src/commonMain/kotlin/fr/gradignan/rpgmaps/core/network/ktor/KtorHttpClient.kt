package fr.gradignan.rpgmaps.core.network.ktor

import fr.gradignan.rpgmaps.core.common.Resource
import fr.gradignan.rpgmaps.core.model.DataError
import fr.gradignan.rpgmaps.core.model.Result
import fr.gradignan.rpgmaps.core.model.Room
import fr.gradignan.rpgmaps.core.network.BuildKonfig
import fr.gradignan.rpgmaps.core.network.NetworkHttpClient
import fr.gradignan.rpgmaps.core.network.model.ErrorResponse
import fr.gradignan.rpgmaps.core.network.model.NetworkAuth
import fr.gradignan.rpgmaps.core.network.model.NetworkRoom
import fr.gradignan.rpgmaps.core.network.model.NetworkToken
import fr.gradignan.rpgmaps.core.network.safeCall
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.auth.authProvider
import io.ktor.client.plugins.auth.providers.BearerAuthProvider
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType

class KtorHttpClient(private val client: HttpClient): NetworkHttpClient {
    override fun clearToken() {
        client.authProvider<BearerAuthProvider>()?.clearToken()
    }

    override suspend fun logIn(auth: NetworkAuth): Resource<NetworkToken> {
        try {
            val response: HttpResponse = client.post("${BuildKonfig.baseUrl}/users/login") {
                contentType(ContentType.Application.Json)
                setBody(auth)
            }

            return if (response.status == HttpStatusCode.OK) {
                Resource.Success(response.body())
            } else {
                val error: ErrorResponse = response.body<ErrorResponse>()
                Resource.Error(Throwable(error.detail))
            }
        } catch (e: Throwable) {
            return Resource.Error(e)
        }
    }

    override suspend fun checkToken(): Resource<Unit> {
        try {
            val response: HttpResponse = client.post("${BuildKonfig.baseUrl}/users/check-token")
            return if (response.status == HttpStatusCode.OK) {
                Resource.Success(Unit)
            } else {
                val error: ErrorResponse = response.body<ErrorResponse>()
                Resource.Error(Throwable(error.detail))
            }
        } catch (e: Throwable) {
            return Resource.Error(e)
        }
    }

    override suspend fun getRooms(): Result<List<NetworkRoom>, DataError.Http> = safeCall {
        client.get("${BuildKonfig.baseUrl}/rooms")
    }
}
