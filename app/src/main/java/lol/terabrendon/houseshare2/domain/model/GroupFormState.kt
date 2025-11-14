package lol.terabrendon.houseshare2.domain.model

import io.github.brendonmendicino.aformvalidator.annotation.FormState
import io.github.brendonmendicino.aformvalidator.annotation.NotBlank
import io.github.brendonmendicino.aformvalidator.annotation.Size

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