package lol.terabrendon.houseshare2.domain.mapper

import lol.terabrendon.houseshare2.data.entity.Expense
import lol.terabrendon.houseshare2.data.entity.ExpensePart
import lol.terabrendon.houseshare2.data.entity.composite.ExpenseWithMembers
import lol.terabrendon.houseshare2.data.entity.composite.PaymentWithMember
import lol.terabrendon.houseshare2.data.remote.dto.ExpenseDto
import lol.terabrendon.houseshare2.data.remote.dto.ExpensePartDto
import lol.terabrendon.houseshare2.domain.model.ExpenseModel
import lol.terabrendon.houseshare2.domain.model.Money
import lol.terabrendon.houseshare2.domain.model.UserExpenseModel
import lol.terabrendon.houseshare2.domain.model.sum
import lol.terabrendon.houseshare2.util.toOffsetDateTime

fun ExpenseDto.toEntity() = Expense(
    id = id,
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
    memberId = memberId,
    partAmount = partAmount,
)

fun ExpenseModel.toDto() = ExpenseDto(
    id = id,
    category = category,
    title = title,
    description = description,
    ownerId = expenseOwner.id,
    payerId = expensePayer.id,
    groupId = groupId,
    createdAt = creationTimestamp.toOffsetDateTime(),
    expenseParts = userExpenses.map {
        ExpensePartDto(
            id = 0,
            expenseId = id,
            memberId = it.member.id,
            partAmount = it.partAmount.compact
        )
    }
)

fun ExpenseModel.toEntity() = Expense(
    id = id,
    ownerId = expenseOwner.id,
    payerId = expensePayer.id,
    groupId = groupId,
    title = title,
    category = category,
    description = description,
    creationTimestamp = creationTimestamp,
)

fun PaymentWithMember.toModel() = UserExpenseModel(
    member = member.toModel(),
    partAmount = Money.fromCompact(expensePart.partAmount),
)

fun ExpenseWithMembers.toModel() = ExpenseModel(
    id = expense.id,
    amount = expensesWithUser.map { Money.fromCompact(it.expensePart.partAmount) }.sum(),
    expenseOwner = owner.toModel(),
    expensePayer = payer.toModel(),
    groupId = expense.groupId,
    category = expense.category,
    title = expense.title,
    description = expense.description,
    creationTimestamp = expense.creationTimestamp,
    userExpenses = expensesWithUser.map { it.toModel() },
)

