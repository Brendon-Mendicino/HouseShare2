package lol.terabrendon.houseshare2.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Transaction
import lol.terabrendon.houseshare2.data.entity.CheckoffState

@Dao
interface CheckoffStateDao {
    @Insert
    suspend fun insert(checkoff: CheckoffState): Long

    @Insert
    @Transaction
    suspend fun insertCheckoffs(checkoffs: List<CheckoffState>): List<Long> =
        checkoffs.map { insert(it) }
}