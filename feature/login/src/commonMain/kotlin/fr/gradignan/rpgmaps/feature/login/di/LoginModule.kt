package fr.gradignan.rpgmaps.feature.login.di

import fr.gradignan.rpgmaps.feature.login.ui.LogInViewModel
import fr.gradignan.rpgmaps.core.data.auth.di.AuthModule
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val LoginModule = module {
    includes(AuthModule)
    viewModelOf(::LogInViewModel)
}
