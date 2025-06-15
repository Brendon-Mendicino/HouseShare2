package lol.terabrendon.houseshare2.domain.model

import io.github.brendonmendicino.aformvalidator.annotation.FormState
import io.github.brendonmendicino.aformvalidator.annotation.NotBlank
import io.github.brendonmendicino.aformvalidator.annotation.Size

@FormState
data class GroupFormState(
    @NotBlank
    val name: String = "",
    val description: String? = null,
    @Size(min = 1)
    val users: List<UserModel> = emptyList(),
)