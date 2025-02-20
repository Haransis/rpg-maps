package fr.gradignan.rpgmaps.core.data.di

import fr.gradignan.rpgmaps.core.model.AuthRepository
import fr.gradignan.rpgmaps.core.data.DefaultAuthRepository
import fr.gradignan.rpgmaps.core.network.di.NetworkModule
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val AuthModule = module {
    includes(NetworkModule)
    singleOf(::DefaultAuthRepository) { bind<AuthRepository>() }
}
