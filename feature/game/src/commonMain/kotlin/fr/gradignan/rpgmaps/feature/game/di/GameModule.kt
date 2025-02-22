package fr.gradignan.rpgmaps.feature.game.di

import fr.gradignan.rpgmaps.core.common.di.CommonModule
import fr.gradignan.rpgmaps.core.data.di.MapActionModule
import fr.gradignan.rpgmaps.feature.game.navigation.Game
import fr.gradignan.rpgmaps.feature.game.ui.GameViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val GameModule = module {
    includes(MapActionModule, CommonModule)
    //factory { (game: Game) -> GameViewModel(game, get()) }
    viewModelOf(::GameViewModel)
}
