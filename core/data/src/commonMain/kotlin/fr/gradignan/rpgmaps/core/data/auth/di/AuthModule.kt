package fr.gradignan.rpgmaps.core.data.auth.di

import fr.gradignan.rpgmaps.core.data.auth.AuthRepository
import fr.gradignan.rpgmaps.core.data.auth.DefaultAuthRepository
import fr.gradignan.rpgmaps.core.network.network.di.NetworkModule
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val AuthModule = module {
    includes(NetworkModule)
    singleOf(::DefaultAuthRepository) { bind<AuthRepository>() }
}
