package fr.gradignan.rpgmaps.core.common.di

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

actual fun provideIoDispatcher(): CoroutineDispatcher = Dispatchers.IO
