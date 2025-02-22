package fr.gradignan.rpgmaps.core.model

sealed interface DataError: Error {
    enum class Http: DataError {
        NO_INTERNET,
        NOT_FOUND,
        SERIALIZATION,
        SERVER_ERROR,
        FORBIDDEN,
        UNAUTHORIZED,
        WRONG_CREDENTIALS,
        UNKNOWN
    }
    enum class WebSocket: DataError {
        SERIALIZATION,
        UNKNOWN
    }
    enum class Local: DataError {
        NO_DATA,
        UNKNOWN
    }
}