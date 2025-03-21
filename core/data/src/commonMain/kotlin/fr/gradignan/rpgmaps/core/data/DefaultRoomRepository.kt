package fr.gradignan.rpgmaps.core.data

import fr.gradignan.rpgmaps.core.model.DataError
import fr.gradignan.rpgmaps.core.model.Result
import fr.gradignan.rpgmaps.core.model.Room
import fr.gradignan.rpgmaps.core.model.Board
import fr.gradignan.rpgmaps.core.model.DataCharacter
import fr.gradignan.rpgmaps.core.model.RoomRepository
import fr.gradignan.rpgmaps.core.model.map
import fr.gradignan.rpgmaps.core.network.NetworkHttpClient
import fr.gradignan.rpgmaps.core.network.model.toExternal

class DefaultRoomRepository(private val client: NetworkHttpClient): RoomRepository {
    override suspend fun getRooms(): Result<List<Room>, DataError.Http> =
        client.getRooms().map { it.toExternal() }

    override suspend fun getBoards(): Result<List<Board>, DataError.Http> =
        client.getBoards().map { it.toExternal() }

    override suspend fun getAllCharacters(): Result<List<DataCharacter>, DataError.Http> =
        client.getAllCharacters().map { it.toExternal() }
}
