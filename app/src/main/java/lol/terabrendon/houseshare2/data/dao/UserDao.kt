package lol.terabrendon.houseshare2.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow
import lol.terabrendon.houseshare2.data.entity.User
import lol.terabrendon.houseshare2.data.entity.composite.UserWithGroups

@Dao
interface UserDao {
    @Query("select * from User")
    fun findAll(): Flow<List<User>>

    @Query("select * from User where id=:id")
    fun findById(id: Long): Flow<User?>

    @Query("select exists (select * from User where id=:id)")
    suspend fun existById(id: Long): Boolean

    @Query("select * from User where id=:userId")
    @Transaction
    fun findGroupsByUserId(userId: Long): Flow<UserWithGroups?>

    @Query("select * from User where id in (:ids)")
    fun findAllById(ids: List<Long>): Flow<List<User>>

    @Insert
    suspend fun insert(user: User): Long

    @Upsert
    suspend fun upsert(user: User): Long
}