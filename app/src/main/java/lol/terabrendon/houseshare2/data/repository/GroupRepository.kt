package lol.terabrendon.houseshare2.data.repository

import com.github.michaelbull.result.Result
import kotlinx.coroutines.flow.Flow
import lol.terabrendon.houseshare2.domain.error.DataError
import lol.terabrendon.houseshare2.domain.model.GroupModel

interface GroupRepository {
    fun findById(groupId: Long): Flow<GroupModel?>

    suspend fun insert(group: GroupModel)

    suspend fun acceptInvite(
        groupId: Long,
        expires: Long,
        nonce: String,
        signature: String,
    ): Result<Unit, DataError>
}