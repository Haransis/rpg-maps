package fr.gradignan.rpgmaps.feature.home.di

import fr.gradignan.rpgmaps.feature.home.ui.HomeViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val HomeModule = module {
    viewModelOf(::HomeViewModel)
}
