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
    class Res(@StringRes val id: Int, val args: List<Any> = emptyList()) : UiText() {
        constructor(@StringRes id: Int, vararg args: Any) : this(id, args.toList())
    }

    class Multi(val uiTexts: List<UiText>, val separator: String = " ") : UiText() {
        constructor(vararg uiTexts: UiText, separator: String = " ") : this(
            uiTexts.toList(),
            separator
        )
    }

    @Composable
    fun text(): String = text(LocalContext.current)

    fun text(context: Context): String = when (this) {
        is Dyn -> value
        is Res -> context.getString(
            id,
            *(args.map { if (it is UiText) it.text(context) else it }.toTypedArray())
        )

        is Multi -> uiTexts.joinToString(separator = separator) { it.text(context) }
    }

    operator fun plus(rhs: UiText): UiText = Multi(this, rhs, separator = "")

    operator fun plus(rhs: String): UiText = Multi(this, Dyn(rhs), separator = "")
}