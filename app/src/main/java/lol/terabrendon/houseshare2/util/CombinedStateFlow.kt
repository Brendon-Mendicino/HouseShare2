package lol.terabrendon.houseshare2.util

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Creates a [MutableStateFlow] staring from an initial [value] and
 * a [StateFlow] called [other], by applying a [transform].
 *
 * This combined flow keeps the current state updated in case the actual
 * value is updated, or if the other flow emits a new value. The state
 * is always computed by applying the [transform] function.
 */
@Suppress("FunctionName")
fun <T, U> CombinedStateFlow(
    value: T,
    coroutineScope: CoroutineScope,
    other: StateFlow<U>,
    transform: (T, U) -> T
): MutableStateFlow<T> {
    val curr = MutableStateFlow(transform(value, other.value))

    coroutineScope.launch {
        launch {
            other.collect { otherValue ->
                curr.update { transform(it, otherValue) }
            }
        }

        launch {
            curr.collect {
                curr.update { transform(it, other.value) }
            }
        }
    }

    return curr
}