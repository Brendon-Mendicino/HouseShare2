package lol.terabrendon.houseshare2.presentation.provider

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.util.UUID

abstract class StateManager<T : Any> {

    data class Key(val key: UUID)

    class Node<T>(var data: T, val key: Key)

    protected val lock = Any()

    /**
     * LinkedHashMap maintains insertion order.
     * The last item added is always the "top" of the stack.
     */
    protected val stateMap = LinkedHashMap<Key, Node<T>>()

    protected var stateKey: Key? = null
    protected val _state = MutableStateFlow<T?>(null)
    protected val state: StateFlow<T?> get() = _state.asStateFlow()

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
            val node = Node(newState, key)

            stateMap[key] = node
            stateKey = key
            _state.value = newState

            key
        }
    }

    fun update(key: Key, transform: (T) -> T) {
        synchronized(lock) {
            val node = stateMap[key] ?: return

            // Update the data in the map.
            node.data = transform(node.data)

            // If this is the active state update the flow.
            if (stateKey == key) {
                _state.update { transform(it!!) }
            }
        }
    }

    fun removeState(key: Key) {
        synchronized(lock) {
            val node = stateMap.remove(key)
            check(node != null) { "Node must present! key=$key not found." }

            // If we removed the active state, revert to the previous one in the map.
            if (key == stateKey) {
                val last = stateMap.entries.lastOrNull()
                stateKey = last?.key
                _state.value = last?.value?.data
            }
        }
    }
}