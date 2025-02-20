package fr.gradignan.rpgmaps.core.data

import fr.gradignan.rpgmaps.core.model.DataError
import fr.gradignan.rpgmaps.core.model.Result
import fr.gradignan.rpgmaps.core.model.Room
import fr.gradignan.rpgmaps.core.model.RoomRepository
import fr.gradignan.rpgmaps.core.model.map
import fr.gradignan.rpgmaps.core.network.NetworkHttpClient
import fr.gradignan.rpgmaps.core.network.model.toExternal

class DefaultRoomRepository(private val client: NetworkHttpClient): RoomRepository {
    override suspend fun getRooms(): Result<List<Room>, DataError.Http> =
        client.getRooms().map { it.toExternal() }
}
