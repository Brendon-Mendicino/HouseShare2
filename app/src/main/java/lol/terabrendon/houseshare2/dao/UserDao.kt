package lol.terabrendon.houseshare2.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import lol.terabrendon.houseshare2.entity.User

@Dao
interface UserDao {
    @Query("select * from User")
    fun findAll(): Flow<List<User>>

    @Query("select * from User where id=:id")
    fun findById(id: Int): Flow<User?>

    @Query("select * from User where id in (:ids)")
    fun findAllById(ids: List<Int>): Flow<List<User>>

    @Insert
    suspend fun insert(user: User)
}