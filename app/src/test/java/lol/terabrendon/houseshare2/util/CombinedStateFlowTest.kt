package lol.terabrendon.houseshare2.util

import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.Test
import kotlin.time.Duration.Companion.seconds


class CombinedStateFlowTest {
    @Test
    fun `test flows`() = runTest(timeout = 10.seconds) {
        val users = MutableStateFlow(listOf("a", "b", "c"))
        val mapped =
            CombinedStateFlow(
                "hello" to emptyList<String>(),
                backgroundScope,
                users
            ) { state, users ->
                val (sep, _) = state

                sep to users.map { "$sep $it" }
            }

        val block = async {
            mapped.onEach { println(it) }.take(4).toList()
        }

        delay(500L)
        mapped.emit("bello" to emptyList())
        delay(500L)
        users.emit(listOf("a", "b"))

        val l = block.await()

        assertEq(l[0], "hello" to listOf("hello a", "hello b", "hello c"))
        assertEq(l[1], "bello" to listOf<String>())
        assertEq(l[2], "bello" to listOf("bello a", "bello b", "bello c"))
        assertEq(l[3], "bello" to listOf("bello a", "bello b"))
    }
}