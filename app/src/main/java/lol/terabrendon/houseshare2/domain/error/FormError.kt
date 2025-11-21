package lol.terabrendon.houseshare2.domain.error

import io.github.brendonmendicino.aformvalidator.annotation.error.ValidationError

sealed interface FormError : RootError {
    data class Validation(val error: ValidationError<*>, val label: Any? = null) : FormError
}