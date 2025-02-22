package fr.gradignan.rpgmaps.core.data.di

import fr.gradignan.rpgmaps.core.common.di.CommonModule
import fr.gradignan.rpgmaps.core.common.di.provideApplicationScope
import fr.gradignan.rpgmaps.core.common.di.provideIoDispatcher
import fr.gradignan.rpgmaps.core.model.MapActionRepository
import fr.gradignan.rpgmaps.core.data.ResultMapActionRepository
import fr.gradignan.rpgmaps.core.network.di.NetworkModule
import org.koin.dsl.module

val MapActionModule = module {
    includes(NetworkModule, CommonModule)
    single<MapActionRepository> {
        ResultMapActionRepository (
            webSocketClient = get(),
            externalScope = provideApplicationScope(provideIoDispatcher())
        )
    }
}
