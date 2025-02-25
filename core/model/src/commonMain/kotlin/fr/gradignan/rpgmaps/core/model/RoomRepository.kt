package fr.gradignan.rpgmaps.core.model

interface RoomRepository {
    suspend fun getRooms(): Result<List<Room>, DataError.Http>
    suspend fun getBoards(): Result<List<Board>, DataError.Http>
}
