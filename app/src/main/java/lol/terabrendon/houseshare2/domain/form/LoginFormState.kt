package lol.terabrendon.houseshare2.domain.form

import io.github.brendonmendicino.aformvalidator.annotation.annotations.NotBlank
import io.github.brendonmendicino.aformvalidator.core.FormState

@FormState
data class LoginFormState(
    @NotBlank
    val username: String = "",
    @NotBlank
    val password: String = "",
)
