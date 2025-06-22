package lol.terabrendon.houseshare2.presentation.provider

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateMapOf

class MenuActionManager {
    val actions = mutableStateMapOf<String, (@Composable () -> Unit)>()
}