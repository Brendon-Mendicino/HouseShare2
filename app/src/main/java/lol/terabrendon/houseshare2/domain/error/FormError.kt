package lol.terabrendon.houseshare2.domain.error

import androidx.annotation.StringRes
import io.github.brendonmendicino.aformvalidator.annotation.error.ValidationError

sealed interface FormError : RootError {
    /**
     * [label] is the string resource naming the field that failed, it is used to build the message.
     */
    data class Validation(
        val error: ValidationError<*>,
        @StringRes val label: Int? = null,
    ) : FormError
}
