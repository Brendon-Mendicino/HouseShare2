package lol.terabrendon.houseshare2.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Group(
    @PrimaryKey(autoGenerate = true)
    val id: Int,
    val name: String,
)
