package fr.gradignan.rpgmaps.core.common

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

inline fun <reified T> MutableStateFlow<*>.updateIfIs(
    crossinline function: (T) -> Any
) {
    val current = this.value as? T
    if (current != null) {
        @Suppress("UNCHECKED_CAST")
        (this as MutableStateFlow<T>).update { function(current) as T }
    }
}