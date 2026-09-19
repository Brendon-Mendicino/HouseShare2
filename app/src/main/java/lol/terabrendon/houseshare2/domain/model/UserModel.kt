package lol.terabrendon.houseshare2.domain.model

import android.net.Uri
import java.util.UUID
import kotlin.random.Random

data class UserModel(
    val id: Long,
    val username: String,
    val email: String?,
    override val firstName: String?,
    override val lastName: String?,
    override val picture: Uri?,
) : AvatarModel {
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

        @JvmStatic
        fun random() = UserModel(
            id = Random.nextLong(),
            username = UUID.randomUUID().toString(),
            email = null,
            firstName = null,
            lastName = null,
            picture = null,
        )
    }
}
