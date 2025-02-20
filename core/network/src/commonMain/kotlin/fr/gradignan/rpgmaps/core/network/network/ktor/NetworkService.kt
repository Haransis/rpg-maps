package fr.gradignan.rpgmaps.core.network.network.ktor

import fr.gradignan.rpgmaps.core.common.Resource
import fr.gradignan.rpgmaps.core.network.BuildKonfig
import fr.gradignan.rpgmaps.core.network.network.HttpClientService
import fr.gradignan.rpgmaps.core.network.network.model.ErrorResponse
import fr.gradignan.rpgmaps.core.network.network.model.NetworkAuth
import fr.gradignan.rpgmaps.core.network.network.model.NetworkToken
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.auth.authProvider
import io.ktor.client.plugins.auth.providers.BearerAuthProvider
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType

class NetworkService(private val client: HttpClient): HttpClientService {
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
}
