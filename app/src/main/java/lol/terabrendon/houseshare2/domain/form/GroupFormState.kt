package lol.terabrendon.houseshare2.domain.form

import io.github.brendonmendicino.aformvalidator.annotation.annotations.NotBlank
import io.github.brendonmendicino.aformvalidator.annotation.annotations.Pattern
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
    @Pattern(regex = """^(?:http[s]?:\/\/.)?(?:www\.)?[-a-zA-Z0-9@%._\+~#=]{2,256}\.[a-z]{2,6}\b(?:[-a-zA-Z0-9@:%_\+.~#?&\/\/=]*)$""")
    val imageUrl: String? = null,
)