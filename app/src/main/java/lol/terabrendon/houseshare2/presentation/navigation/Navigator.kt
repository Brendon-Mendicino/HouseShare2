package lol.terabrendon.houseshare2.presentation.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.coroutines.flow.Flow

interface Navigator<T : NavKey> {
    val backStack: Flow<List<T>>

    fun navigate(dest: T)

    fun replace(dest: T)

    fun pop(elements: Int = 1)
}