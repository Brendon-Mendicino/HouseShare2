package lol.terabrendon.houseshare2.domain.model

import android.net.Uri

interface AvatarModel {
    val firstName: String?
    val lastName: String?
    val picture: Uri?
}