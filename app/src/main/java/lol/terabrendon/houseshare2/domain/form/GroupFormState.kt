package lol.terabrendon.houseshare2.domain.form

import io.github.brendonmendicino.aformvalidator.annotation.annotations.NotBlank
import io.github.brendonmendicino.aformvalidator.annotation.annotations.Size
import io.github.brendonmendicino.aformvalidator.core.FormState
import lol.terabrendon.houseshare2.domain.model.UserModel

@FormState
data class GroupFormState(
    @NotBlank
    @Size(max = 250)
    val name: String = "",
    @NotBlank
    @Size(max = 250)
    val description: String? = null,
    @Size(max = 100)
    val users: List<UserModel> = emptyList(),
)