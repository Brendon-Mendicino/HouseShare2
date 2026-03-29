package lol.terabrendon.houseshare2.domain.model

import android.net.Uri

data class GroupInfoModel(
    val groupId: Long,
    val name: String,
    val description: String?,
    val imageUrl: Uri?,
) {
    companion object {
        fun default() = GroupInfoModel(
            groupId = 0,
            name = "GroupInfoModel",
            description = null,
            imageUrl = null,
        )
    }
}