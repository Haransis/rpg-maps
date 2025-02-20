package fr.gradignan.rpgmaps.core.data.di

import fr.gradignan.rpgmaps.core.model.RoomRepository
import fr.gradignan.rpgmaps.core.network.di.NetworkModule
import fr.gradignan.rpgmaps.core.data.DefaultRoomRepository
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val RoomModule = module {
    includes(NetworkModule)
    singleOf(::DefaultRoomRepository) { bind<RoomRepository>() }
}
