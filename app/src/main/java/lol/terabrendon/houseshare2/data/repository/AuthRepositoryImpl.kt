package lol.terabrendon.houseshare2.data.repository

import com.github.michaelbull.result.map
import com.github.michaelbull.result.onFailure
import lol.terabrendon.houseshare2.data.local.dao.UserDao
import lol.terabrendon.houseshare2.data.local.util.LocalResult
import lol.terabrendon.houseshare2.data.local.util.localSafe
import lol.terabrendon.houseshare2.data.remote.api.NetResult
import lol.terabrendon.houseshare2.data.remote.api.UserApi
import lol.terabrendon.houseshare2.domain.mapper.toEntity
import lol.terabrendon.houseshare2.domain.mapper.toModel
import lol.terabrendon.houseshare2.domain.model.UserModel
import timber.log.Timber
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val userDao: UserDao,
    private val userApi: UserApi,
) : AuthRepository {
    override suspend fun fetchLoggedUser(): NetResult<UserModel> {
        return userApi.getLoggedUser().map { it.toModel() }
            .onFailure { err -> Timber.w("could not fetch the logged user. err=%s", err) }
    }

    override suspend fun saveUser(user: UserModel): LocalResult<Unit> = localSafe<Unit> {
        userDao.upsert(user.toEntity())
    }
}
