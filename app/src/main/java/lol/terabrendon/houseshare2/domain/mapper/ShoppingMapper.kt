package lol.terabrendon.houseshare2.domain.mapper

import androidx.annotation.StringRes
import lol.terabrendon.houseshare2.R
import lol.terabrendon.houseshare2.data.entity.ShoppingItem
import lol.terabrendon.houseshare2.data.entity.composite.ShoppingItemWithUser
import lol.terabrendon.houseshare2.data.remote.dto.ShoppingItemDto
import lol.terabrendon.houseshare2.data.repository.ShoppingItemRepository
import lol.terabrendon.houseshare2.domain.model.CheckoffStateModel
import lol.terabrendon.houseshare2.domain.model.ShoppingItemFormState
import lol.terabrendon.houseshare2.domain.model.ShoppingItemInfoModel
import lol.terabrendon.houseshare2.domain.model.ShoppingItemModel
import lol.terabrendon.houseshare2.domain.model.UserModel
import java.time.LocalDateTime
import java.time.OffsetDateTime

fun ShoppingItemWithUser.toModel() = ShoppingItemModel(
    info = ShoppingItemInfoModel.from(item),
    itemOwner = UserModel.from(itemOwner),
    checkoffState = if (checkingUser == null || item.check == null) null
    else CheckoffStateModel(
        checkoffTime = item.check.checkoffTimestamp,
        checkoffUser = checkingUser.toModel(),
    ),
)

fun ShoppingItemFormState.toModel() = ShoppingItemInfoModel(
    id = 0,
    ownerId = ownerId,
    groupId = groupId,
    name = name,
    amount = amount ?: throw IllegalStateException("`amount` is null!"),
    price = price,
    creationTimestamp = LocalDateTime.now(),
    priority = priority,
)

fun ShoppingItemInfoModel.toDto() = ShoppingItemDto(
    id = id,
    ownerId = ownerId,
    groupId = groupId,
    name = name,
    amount = amount,
    price = price,
    priority = priority,
    createdAt = OffsetDateTime.now(),
    check = null,
)

fun ShoppingItemInfoModel.toEntity() = ShoppingItem(
    id = id,
    ownerId = ownerId,
    groupId = groupId,
    name = name,
    amount = amount,
    price = price,
    priority = priority,
    check = null,
)

fun ShoppingItemDto.toEntity() = ShoppingItem(
    id = id,
    ownerId = ownerId,
    groupId = groupId,
    name = name,
    amount = amount,
    price = price,
    creationTimestamp = createdAt.toLocalDateTime(),
    priority = priority,
    check = check?.let { dto ->
        ShoppingItem.CheckoffState(
            checkingUserId = dto.checkingUserId,
            checkoffTimestamp = dto.checkoffTimestamp.toLocalDateTime(),
        )
    },
)

@StringRes
fun ShoppingItemRepository.Sorting.toStringRes(): Int = when (this) {
    ShoppingItemRepository.Sorting.CreationDate -> R.string.creation_date
    ShoppingItemRepository.Sorting.Priority -> R.string.priority
    ShoppingItemRepository.Sorting.Name -> R.string.name
    ShoppingItemRepository.Sorting.Username -> R.string.username
}