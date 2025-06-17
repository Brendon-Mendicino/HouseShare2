package lol.terabrendon.houseshare2.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import lol.terabrendon.houseshare2.domain.model.GroupModel

@Entity
data class Group(
    @PrimaryKey(autoGenerate = true)
    val id: Long,
    val name: String,
    val description: String?,
) {
    companion object {
        @JvmStatic
        fun from(group: GroupModel) = Group(
            id = group.info.groupId,
            name = group.info.name,
            description = group.info.description,
        )
    }
}
