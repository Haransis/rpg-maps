package fr.gradignan.rpgmaps.core.model

sealed interface DataError: Error {
    enum class Http: DataError {
        UNAUTHORIZED,
        SERVER_ERROR,
        NO_INTERNET,
        NOT_FOUND,
        UNKNOWN
    }
    enum class WebSocket: DataError {
        UNKNOWN
    }
}