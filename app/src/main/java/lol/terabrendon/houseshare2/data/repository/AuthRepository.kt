package lol.terabrendon.houseshare2.data.repository

import com.github.michaelbull.result.Result
import lol.terabrendon.houseshare2.domain.error.DataError
import lol.terabrendon.houseshare2.domain.model.UserModel

interface AuthRepository {
    suspend fun finishLogin(): Result<UserModel, DataError>

    suspend fun loggedUser(): Result<UserModel, DataError>
}