package lol.terabrendon.houseshare2.util

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Maps the inner values if a flow holds a collection.
 *
 * # Example
 *
 * ```kotlin
 * flowOf(listOf(1, 2, 3, 4))
 *      .mapInner { it to (it % 2 == 0) }
 *      .first()
 *      .onEach { println(it) }
 *
 * // (1, false)
 * // (2, true)
 * // (3, true)
 * // (4, true)
 * ```
 */
inline fun <T, R> Flow<List<T>>.mapInner(crossinline transform: (value: T) -> R): Flow<List<R>> =
    map { inner -> inner.map(transform) }
