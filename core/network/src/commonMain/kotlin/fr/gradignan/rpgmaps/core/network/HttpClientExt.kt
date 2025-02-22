package fr.gradignan.rpgmaps.core.network

import fr.gradignan.rpgmaps.core.model.DataError
import fr.gradignan.rpgmaps.core.model.Result
import io.ktor.client.call.NoTransformationFoundException
import io.ktor.client.call.body
import io.ktor.client.statement.HttpResponse
import io.ktor.util.network.UnresolvedAddressException
import kotlin.coroutines.coroutineContext
import kotlinx.coroutines.ensureActive

suspend inline fun <reified T> safeCall(
    execute: () -> HttpResponse
): Result<T, DataError.Http> {
    val response = try {
        execute()
    } catch (e: UnresolvedAddressException) {
        return Result.Error(DataError.Http.NO_INTERNET)
    } catch (e: Exception) {
        coroutineContext.ensureActive() // ensure we respect cancellation.
        return Result.Error(DataError.Http.UNKNOWN)
    }
    return responseToResult(response)
}

suspend inline fun <reified T> responseToResult(
    response: HttpResponse
): Result<T, DataError.Http> {
    return when(response.status.value) {
        in 200..299 -> {
            try {
                Result.Success(response.body<T>())
            } catch (e: NoTransformationFoundException) {
                Result.Error(DataError.Http.SERIALIZATION)
            }
        }
        401 -> Result.Error(DataError.Http.UNAUTHORIZED)
        403 -> Result.Error(DataError.Http.FORBIDDEN)
        404 -> Result.Error(DataError.Http.NOT_FOUND)
        in 500..599 -> Result.Error(DataError.Http.SERVER_ERROR)
        else -> Result.Error(DataError.Http.UNKNOWN)
    }
}
