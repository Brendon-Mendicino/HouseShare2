package lol.terabrendon.houseshare2.data.repository

import lol.terabrendon.houseshare2.data.local.util.LocalResult
import lol.terabrendon.houseshare2.data.remote.api.NetResult
import lol.terabrendon.houseshare2.domain.model.UserModel

interface AuthRepository {
    /**
     * Asks the server who owns the current session.
     */
    suspend fun fetchLoggedUser(): NetResult<UserModel>

    suspend fun saveUser(user: UserModel): LocalResult<Unit>
}
