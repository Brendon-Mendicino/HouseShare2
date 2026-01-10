package lol.terabrendon.houseshare2.domain.usecase

import android.net.Uri
import com.github.michaelbull.result.Result
import lol.terabrendon.houseshare2.data.repository.GroupRepository
import lol.terabrendon.houseshare2.domain.error.DataError
import javax.inject.Inject

class AcceptInviteUseCase @Inject constructor(
    private val groupRepository: GroupRepository,
) {
    suspend operator fun invoke(groupId: Long, inviteUri: Uri): Result<Unit, DataError> {
        val query = { name: String ->
            inviteUri.getQueryParameter(name)
                ?: throw IllegalStateException("The query parameter param=$name was not present in the redirectUri! redirectUri=$inviteUri")
        }

        return groupRepository.acceptInvite(
            groupId = groupId,
            expires = query("expires").toLong(),
            nonce = query("nonce"),
            signature = query("signature")
        )
    }
}