package fr.gradignan.rpgmaps.core.model

sealed interface Result<out D, out E: Error> {
    data class Success<out D>(val data: D): Result<D, Nothing>
    data class Error<out E: fr.gradignan.rpgmaps.core.model.Error>(val error: E):
        Result<Nothing, E>
}

inline fun <T, E: Error, R> Result<T, E>.map(map: (T) -> R): Result<R, E> {
    return when(this) {
        is Result.Error -> Result.Error(error)
        is Result.Success -> Result.Success(map(data))
    }
}

inline fun <T, E: Error> Result<T, E>.mapError(map: (E) -> E): Result<T, E> {
    return when(this) {
        is Result.Error -> Result.Error(map(error))
        is Result.Success -> Result.Success(data)
    }
}

inline fun <T, E: Error> Result<T, E>.mapErrorIf(e: E, map: (E) -> E): Result<T, E> {
    return when(this) {
        is Result.Error -> {
            if (error==e) {
                return Result.Error(map(error))
            } else {
                return Result.Error(error)
            }
        }
        is Result.Success -> Result.Success(data)
    }
}

fun <T, E: Error> Result<T, E>.asEmptyDataResult(): EmptyResult<E> {
    return map {  }
}

inline fun <T, E: Error> Result<T, E>.onSuccess(action: (T) -> Unit): Result<T, E> {
    return when(this) {
        is Result.Error -> this
        is Result.Success -> {
            action(data)
            this
        }
    }
}

inline fun <T, E: Error> Result<T, E>.ifSuccess(action: (T) -> Result<T, E>): Result<T, E> {
    return when(this) {
        is Result.Error -> this
        is Result.Success -> {
            action(data)
        }
    }
}

inline fun <T, E: Error> Result<T, E>.onError(action: (E) -> Unit): Result<T, E> {
    return when(this) {
        is Result.Error -> {
            action(error)
            this
        }
        is Result.Success -> this
    }
}

suspend fun <T, E: Error> Result<T, E>.after(block: suspend () -> Unit): Result<T, E> {
    block()
    return this
}

typealias EmptyResult<E> = Result<Unit, E>
