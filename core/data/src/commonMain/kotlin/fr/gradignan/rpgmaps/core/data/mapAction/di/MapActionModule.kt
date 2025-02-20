package fr.gradignan.rpgmaps.core.data.mapAction.di

import fr.gradignan.rpgmaps.core.common.di.CommonModule
import fr.gradignan.rpgmaps.core.common.di.RpgMapsDispatchers
import fr.gradignan.rpgmaps.core.common.di.provideApplicationScope
import fr.gradignan.rpgmaps.core.common.di.provideIoDispatcher
import fr.gradignan.rpgmaps.core.data.mapAction.MapActionRepository
import fr.gradignan.rpgmaps.core.data.mapAction.DefaultMapActionRepository
import fr.gradignan.rpgmaps.core.network.network.di.NetworkModule
import org.koin.core.qualifier.named
import org.koin.dsl.module

val MapActionModule = module {
    includes(NetworkModule, CommonModule)
    single<MapActionRepository> {
        DefaultMapActionRepository (
            webSocketService = get(),
            settings = get(),
            externalScope = provideApplicationScope(provideIoDispatcher())
        )
    }
}
