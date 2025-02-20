package fr.gradignan.rpgmaps.di

import fr.gradignan.rpgmaps.feature.createMap.di.CreateMapModule
import fr.gradignan.rpgmaps.feature.game.di.GameModule
import fr.gradignan.rpgmaps.feature.home.di.HomeModule
import fr.gradignan.rpgmaps.feature.login.di.LoginModule
import org.koin.dsl.module

val AppModule = module {
    includes(LoginModule, CreateMapModule, GameModule, HomeModule)
}