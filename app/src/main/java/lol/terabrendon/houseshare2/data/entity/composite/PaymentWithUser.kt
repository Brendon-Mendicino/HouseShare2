package lol.terabrendon.houseshare2.data.entity.composite

import androidx.room.Embedded
import androidx.room.Relation
import lol.terabrendon.houseshare2.data.entity.ExpensePart
import lol.terabrendon.houseshare2.data.entity.User

data class PaymentWithUser(
    @Embedded
    val expensePart: ExpensePart,
    @Relation(
        parentColumn = "userId",
        entityColumn = "id",
    )
    val user: User,
)