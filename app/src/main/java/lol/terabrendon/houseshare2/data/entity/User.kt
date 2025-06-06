package lol.terabrendon.houseshare2.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import lol.terabrendon.houseshare2.domain.model.UserModel

@Entity
data class User(
    @PrimaryKey(autoGenerate = true)
    val id: Long,
    val username: String,
) {
    companion object {
        @JvmStatic
        fun from(user: UserModel): User = User(
            id = 0,
            username = user.username,
        )
    }
}
