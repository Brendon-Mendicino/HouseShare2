package lol.terabrendon.houseshare2.presentation.util

import android.content.Context
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

sealed class UiText {
    data class Dyn(val value: String) : UiText()
    class Res(@StringRes val id: Int, val args: Array<Any> = emptyArray()) : UiText()

    @Composable
    fun text(): String = when (this) {
        is Dyn -> value
        is Res -> LocalContext.current.getString(id, *args)
    }

    fun text(context: Context): String = when (this) {
        is Dyn -> value
        is Res -> context.getString(id, *args)
    }
}