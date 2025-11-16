package lol.terabrendon.houseshare2.data.remote.dto

data class ExpensePartDto(
    val id: Long,
    val expenseId: Long,
    val userId: Long,
    val partAmount: Long,
)
