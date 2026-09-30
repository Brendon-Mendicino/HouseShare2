package lol.terabrendon.houseshare2.domain.model

import android.net.Uri
import java.util.UUID
import kotlin.random.Random

data class GroupMemberModel(
    val id: Long,
    override val firstName: String,
    override val lastName: String?,
    override val picture: Uri?,
    val groupId: Long,
    val userId: Long?,
) : AvatarModel {
    val fullName = if (lastName?.isBlank() != false) firstName else "$firstName $lastName"

    companion object {
        @JvmStatic
        fun default() = GroupMemberModel(
            id = 0,
            firstName = "Name",
            lastName = null,
            picture = null,
            groupId = 0,
            userId = 0,
        )

        @JvmStatic
        fun random() = GroupMemberModel(
            id = Random.nextLong(),
            firstName = UUID.randomUUID().toString(),
            lastName = null,
            picture = null,
            groupId = Random.nextLong(),
            userId = null,
        )
    }
}
