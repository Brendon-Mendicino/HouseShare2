package lol.terabrendon.houseshare2.data.repository

import com.github.michaelbull.result.Result
import lol.terabrendon.houseshare2.domain.model.UserModel

interface AuthRepository {
    suspend fun finishLogin(): Result<UserModel, Throwable>
}