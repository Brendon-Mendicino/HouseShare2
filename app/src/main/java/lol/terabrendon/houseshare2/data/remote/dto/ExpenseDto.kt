package lol.terabrendon.houseshare2.data.remote.dto

import lol.terabrendon.houseshare2.domain.model.ExpenseCategory
import java.time.OffsetDateTime

data class ExpenseDto(
    val id: Long,
    val category: ExpenseCategory,
    val title: String,
    val description: String?,
    val ownerId: Long,
    val payerId: Long,
    val groupId: Long,
    val createdAt: OffsetDateTime,
    val expenseParts: List<ExpensePartDto>,
)