package lol.terabrendon.houseshare2.data.repository

import kotlinx.coroutines.flow.Flow
import lol.terabrendon.houseshare2.domain.model.GroupInfoModel
import lol.terabrendon.houseshare2.domain.model.UserModel

interface UserRepository {

    fun findAll(): Flow<List<UserModel>>

    fun findById(id: Long): Flow<UserModel?>

    fun findAllById(ids: List<Long>): Flow<List<UserModel>>

    fun findGroupsByUserId(userId: Long): Flow<List<GroupInfoModel>>
}