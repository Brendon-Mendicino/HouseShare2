package lol.terabrendon.houseshare2.presentation.provider

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import java.util.UUID

abstract class StateManager<T : Any> {

    data class Key(val key: UUID)

    protected open val lazyMessage: String
        get() = "Configuring multiple states without resetting!"
    protected var stateKey: Key? = null
    protected val lock = Any()
    protected val _state = mutableStateOf<T?>(null)
    protected val state: State<T?> get() = _state

    protected fun newActionId(): Key {
        while (true) {
            val uuid = UUID.randomUUID()
            if (uuid != stateKey) {
                stateKey = Key(uuid)
                return Key(uuid)
            }
        }
    }

    fun setState(newState: T) {
        synchronized(lock) {
            check(_state.value == null) { lazyMessage }
            _state.value = newState
        }
    }

    fun overrideState(newState: T): Key {
        return synchronized(lock) {
            _state.value = newState
            newActionId()
        }
    }

    fun resetState(actionId: Key? = null) {
        synchronized(lock) {
            if (actionId == this.stateKey) {
                _state.value = null
                this.stateKey = null
            }
        }
    }
}