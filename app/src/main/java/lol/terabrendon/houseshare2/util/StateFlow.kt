@file:Suppress("unused")

package lol.terabrendon.houseshare2.util

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

fun <T, M> StateFlow<T>.mapState(
    coroutineScope: CoroutineScope,
    started: SharingStarted = SharingStarted.WhileSubscribed(5000),
    mapper: (value: T) -> M
): StateFlow<M> = map { mapper(it) }.stateIn(
    coroutineScope,
    started,
    mapper(value)
)

fun <T, U, M> StateFlow<T>.combineState(
    coroutineScope: CoroutineScope,
    flow: StateFlow<U>,
    started: SharingStarted = SharingStarted.WhileSubscribed(5000),
    transform: (T, U) -> M
): StateFlow<M> = combine(flow) { a, b -> transform(a, b) }.stateIn(
    coroutineScope,
    started,
    transform(value, flow.value)
)

fun <T1, T2, T3, M> combineState(
    coroutineScope: CoroutineScope,
    flow1: StateFlow<T1>,
    flow2: StateFlow<T2>,
    flow3: StateFlow<T3>,
    started: SharingStarted = SharingStarted.WhileSubscribed(5000),
    transform: (T1, T2, T3) -> M,
): StateFlow<M> = combine(flow1, flow2, flow3) { f1, f2, f3 -> transform(f1, f2, f3) }
    .stateIn(coroutineScope, started, transform(flow1.value, flow2.value, flow3.value))

fun <T1, T2, T3, T4, M> combineState(
    coroutineScope: CoroutineScope,
    flow1: StateFlow<T1>,
    flow2: StateFlow<T2>,
    flow3: StateFlow<T3>,
    flow4: StateFlow<T4>,
    started: SharingStarted = SharingStarted.WhileSubscribed(5000),
    transform: (T1, T2, T3, T4) -> M,
): StateFlow<M> =
    combine(flow1, flow2, flow3, flow4) { f1, f2, f3, f4 -> transform(f1, f2, f3, f4) }
        .stateIn(
            coroutineScope,
            started,
            transform(flow1.value, flow2.value, flow3.value, flow4.value)
        )
