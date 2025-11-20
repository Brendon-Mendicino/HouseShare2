package lol.terabrendon.houseshare2.data.remote.dto

data class InviteUrlDto(
    val inviteUri: String,
    val expires: Long,
    val nonce: String,
    val signature: String,
)