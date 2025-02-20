package fr.gradignan.rpgmaps.core.model

sealed interface DataError: Error {
    enum class Http: DataError {
        NO_INTERNET,
        NOT_FOUND,
        SERIALIZATION,
        SERVER_ERROR,
        FORBIDDEN,
        UNKNOWN
    }
    enum class WebSocket: DataError {
        UNKNOWN
    }
    enum class Local: DataError {
        NO_DATA,
        UNKNOWN
    }
}