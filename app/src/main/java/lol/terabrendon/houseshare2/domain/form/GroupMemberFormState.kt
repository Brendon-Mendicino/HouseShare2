package lol.terabrendon.houseshare2.domain.form

import io.github.brendonmendicino.aformvalidator.annotation.annotations.NotBlank
import io.github.brendonmendicino.aformvalidator.annotation.annotations.Size
import io.github.brendonmendicino.aformvalidator.core.FormState
import lol.terabrendon.houseshare2.util.Url

@FormState
data class GroupMemberFormState(
    @NotBlank
    @Size(max = 250)
    val firstName: String = "",
    @Size(max = 250)
    val lastName: String? = null,
    @Url
    val picture: String? = null,
)
