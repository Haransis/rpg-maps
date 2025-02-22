package fr.gradignan.rpgmaps.core.model

import fr.gradignan.rpgmaps.core.common.Resource
import kotlinx.coroutines.flow.Flow

interface MapActionRepository {
    fun getMapUpdatesFlow(): Flow<Result<MapUpdate, DataError>>
    fun getMapEffectsFlow(): Flow<MapEffect>
    fun endTurn()
}