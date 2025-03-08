package fr.gradignan.rpgmaps.feature.game.di

import fr.gradignan.rpgmaps.core.common.di.CommonModule
import fr.gradignan.rpgmaps.core.data.di.MapActionModule
import fr.gradignan.rpgmaps.feature.game.ui.viewmodel.GameViewModel
import fr.gradignan.rpgmaps.feature.game.ui.viewmodel.GmViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val GameModule = module {
    includes(MapActionModule, CommonModule)
    viewModelOf(::GameViewModel)
    viewModelOf(::GmViewModel)
}
