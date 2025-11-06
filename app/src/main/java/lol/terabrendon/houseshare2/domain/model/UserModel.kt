package lol.terabrendon.houseshare2.domain.model

import android.net.Uri
import java.util.UUID

data class UserModel(
    val id: Long,
    val username: String,
    val email: String?,
    val firstName: String?,
    val lastName: String?,
    val picture: Uri?,
) {
    companion object {
        @JvmStatic
        fun default(): UserModel = UserModel(
            id = 0,
            username = UUID.randomUUID().toString(),
            email = null,
            firstName = null,
            lastName = null,
            picture = null,
        )
    }
}
