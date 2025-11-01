package lol.terabrendon.houseshare2.util

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

inline fun <T, R> Flow<List<T>>.mapInner(crossinline transform: (value: T) -> R): Flow<List<R>> =
    map { inner -> inner.map(transform) }
