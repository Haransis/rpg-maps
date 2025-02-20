package fr.gradignan.rpgmaps.feature.createMap.di

import fr.gradignan.rpgmaps.feature.createMap.ui.CreateMapViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val CreateMapModule = module {
    viewModelOf(::CreateMapViewModel)
}
