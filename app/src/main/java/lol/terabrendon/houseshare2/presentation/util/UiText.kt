package lol.terabrendon.houseshare2.presentation.util

import android.content.Context
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

sealed class UiText {
    data class Dyn(val value: String) : UiText()

    /**
     * If you pass any [UiText] as [args] they will be converted to text.
     */
    class Res(@StringRes val id: Int, val args: Array<Any> = emptyArray()) : UiText()
    class Multi(val uiTexts: List<UiText>, val separator: String = " ") : UiText()

    @Composable
    fun text(): String = text(LocalContext.current)

    fun text(context: Context): String = when (this) {
        is Dyn -> value
        is Res -> context.getString(
            id,
            *(args.map { if (it is UiText) it.text(context) else it }.toTypedArray())
        )

        is Multi -> uiTexts.joinToString(separator = separator) { text(context) }
    }

    operator fun plus(rhs: UiText): UiText = Multi(listOf(this, rhs))
}