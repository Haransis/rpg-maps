package fr.gradignan.rpgmaps.core.common

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart

sealed interface Resource<out T> {
    data object Loading : Resource<Nothing>
    data class Success<T>(val data: T) : Resource<T>
    data class Error(val exception: Throwable) :
        Resource<Nothing>
}

fun <T> Flow<T>.asResource(): Flow<Resource<T>> = map<T, Resource<T>> { Resource.Success(it) }
    .onStart { emit(Resource.Loading) }
    .catch { emit(Resource.Error(it)) }

fun <T, R> Resource<T>.map(transform: (value: T) -> R): Resource<R> =
    when (this) {
        is Resource.Success<T> -> Resource.Success(transform(this.data))
        is Resource.Error -> Resource.Error(this.exception)
        Resource.Loading -> Resource.Loading
    }
