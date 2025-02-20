package fr.gradignan.rpgmaps.feature.home.di

import fr.gradignan.rpgmaps.core.data.di.AuthModule
import fr.gradignan.rpgmaps.core.data.di.RoomModule
import fr.gradignan.rpgmaps.core.network.di.NetworkModule
import fr.gradignan.rpgmaps.feature.home.ui.HomeViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val HomeModule = module {
    includes(NetworkModule, AuthModule, RoomModule)
    viewModelOf(::HomeViewModel)
}
