package lol.terabrendon.houseshare2.domain.mapper

import lol.terabrendon.houseshare2.data.entity.Expense
import lol.terabrendon.houseshare2.data.entity.ExpensePart
import lol.terabrendon.houseshare2.data.remote.dto.ExpenseDto
import lol.terabrendon.houseshare2.data.remote.dto.ExpensePartDto
import lol.terabrendon.houseshare2.domain.model.ExpenseModel
import lol.terabrendon.houseshare2.util.toOffsetDateTime

fun ExpenseDto.toEntity() = Expense(
    id = id,
    amount = amount,
    ownerId = ownerId,
    payerId = payerId,
    groupId = groupId,
    category = category,
    title = title,
    description = description,
    creationTimestamp = createdAt.toLocalDateTime()
)

fun ExpensePartDto.toEntity() = ExpensePart(
    id = id,
    expenseId = expenseId,
    userId = userId,
    partAmount = partAmount,
)

fun ExpenseModel.toDto() = ExpenseDto(
    id = id,
    amount = amount,
    category = category,
    title = title,
    description = description,
    ownerId = expenseOwner.id,
    payerId = expensePayer.id,
    groupId = groupId,
    createdAt = creationTimestamp.toOffsetDateTime(),
    expenseParts = userExpenses.map {
        ExpensePartDto(id = 0, expenseId = id, userId = it.user.id, partAmount = it.partAmount)
    }
)