package lol.terabrendon.houseshare2.presentation.provider

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import java.util.UUID

abstract class StateManager<T : Any> {

    data class Key(val key: UUID)

    class Node<T>(var data: T, val key: Key)

    protected val lock = Any()

    // Why this garbage?
    // 1. LinkedHashMap is only usable on API >= 35
    // 2. I'm not bothered to implement a linked list for fast retrieval
    protected val nodes = mutableListOf<Node<T>>()
    protected val stateMap = mutableMapOf<Key, Node<T>>()

    protected var stateKey: Key? = null
    protected val _state = mutableStateOf<T?>(null)
    protected val state: State<T?> get() = _state

    protected fun newKey(): Key {
        while (true) {
            val key = Key(UUID.randomUUID())
            if (key !in stateMap) {
                return key
            }
        }
    }

    fun putState(newState: T): Key {
        return synchronized(lock) {
            val key = newKey()

            stateKey = key
            _state.value = newState

            val node = Node(newState, key)
            nodes.add(node)
            stateMap.put(key, node)

            key
        }
    }

    fun update(key: Key, transform: (T) -> T) {
        synchronized(lock) {
            val node = stateMap[key]
            if (node != null) {
                node.data = transform(node.data)
            }

            if (stateKey == key) {
                _state.value = transform(_state.value!!)
            }
        }
    }

    fun removeState(key: Key) {
        synchronized(lock) {
            val node = stateMap.remove(key)
            check(nodes.remove(node)) { "Node must present!" }

            if (key == stateKey) {
                val last = nodes.lastOrNull()
                stateKey = last?.key
                _state.value = last?.data
            }
        }
    }
}