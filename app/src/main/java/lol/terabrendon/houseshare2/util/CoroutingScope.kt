package lol.terabrendon.houseshare2.util

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

fun <T> CoroutineScope.mapLaunch(
    it: Iterable<T>,
    context: CoroutineContext = EmptyCoroutineContext,
    start: CoroutineStart = CoroutineStart.DEFAULT,
    transform: suspend CoroutineScope.(T) -> Unit,
) = it.map { launch(context, start) { transform(it) } }