package fr.gradignan.rpgmaps.core.data.mapAction

import fr.gradignan.rpgmaps.core.common.Resource
import fr.gradignan.rpgmaps.core.model.MapAction
import fr.gradignan.rpgmaps.core.model.MapEffect
import fr.gradignan.rpgmaps.core.model.MapUpdate
import kotlinx.coroutines.flow.Flow

interface MapActionRepository {
    fun getMapUpdatesFlow(): Flow<Resource<MapUpdate>>
    fun getMapEffectsFlow(): Flow<MapEffect>
    fun endTurn()
}