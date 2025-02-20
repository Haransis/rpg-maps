package fr.gradignan.rpgmaps.core.common.di

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.koin.core.qualifier.named
import org.koin.dsl.module

val CommonModule = module {
    single(named(RpgMapsDispatchers.IO)) { provideIoDispatcher() }
    single(named(RpgMapsDispatchers.Default)) { Dispatchers.Default }
    single<CoroutineScope> {
        provideApplicationScope(get(named(RpgMapsDispatchers.Default)))
    }
}

expect fun provideIoDispatcher(): CoroutineDispatcher
fun provideApplicationScope(dispatcher: CoroutineDispatcher): CoroutineScope =
    CoroutineScope(SupervisorJob() + dispatcher)

enum class RpgMapsDispatchers {
    IO, Default
}
