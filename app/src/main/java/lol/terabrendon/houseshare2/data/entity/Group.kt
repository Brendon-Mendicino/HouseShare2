package lol.terabrendon.houseshare2.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Group(
    @PrimaryKey(autoGenerate = true)
    val id: Long,
    val name: String,
)
