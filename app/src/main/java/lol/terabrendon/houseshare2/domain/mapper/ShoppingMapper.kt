package lol.terabrendon.houseshare2.domain.mapper

import lol.terabrendon.houseshare2.data.dto.ShoppingItemDto
import lol.terabrendon.houseshare2.data.entity.ShoppingItem
import lol.terabrendon.houseshare2.data.entity.User
import lol.terabrendon.houseshare2.data.entity.composite.ShoppingItemWithUser
import lol.terabrendon.houseshare2.domain.model.CheckoffStateModel
import lol.terabrendon.houseshare2.domain.model.ShoppingItemFormState
import lol.terabrendon.houseshare2.domain.model.ShoppingItemInfoModel
import lol.terabrendon.houseshare2.domain.model.ShoppingItemModel
import lol.terabrendon.houseshare2.domain.model.UserModel
import java.time.LocalDateTime
import java.time.OffsetDateTime
import javax.inject.Inject

object ShoppingMapper {
    class EntityToModel @Inject constructor(
        private val userMapper: Mapper<User, UserModel>,
    ) : Mapper<ShoppingItemWithUser, ShoppingItemModel> {
        override fun map(it: ShoppingItemWithUser): ShoppingItemModel = ShoppingItemModel(
            info = ShoppingItemInfoModel.from(it.item),
            itemOwner = UserModel.from(it.itemOwner),
            checkoffState = if (it.checkingUser == null || it.item.check == null) null
            else CheckoffStateModel(
                checkoffTime = it.item.check.checkoffTimestamp,
                checkoffUser = userMapper.map(it.checkingUser),
            ),
        )
    }

    class FormToModel @Inject constructor() : Mapper<ShoppingItemFormState, ShoppingItemInfoModel> {
        override fun map(
            it: ShoppingItemFormState,
        ) = ShoppingItemInfoModel(
            id = 0,
            ownerId = it.ownerId,
            groupId = it.groupId,
            name = it.name,
            amount = it.amount ?: throw IllegalStateException("`amount` is null!"),
            price = it.price,
            creationTimestamp = LocalDateTime.now(),
            priority = it.priority,
        )
    }

    class ModelToDto @Inject constructor() : Mapper<ShoppingItemInfoModel, ShoppingItemDto> {
        override fun map(it: ShoppingItemInfoModel) = ShoppingItemDto(
            id = it.id,
            ownerId = it.ownerId,
            groupId = it.groupId,
            name = it.name,
            amount = it.amount,
            price = it.price,
            priority = it.priority,
            createdAt = OffsetDateTime.now(),
            check = null,
        )
    }

    class ModelToEntity @Inject constructor() : Mapper<ShoppingItemInfoModel, ShoppingItem> {
        override fun map(it: ShoppingItemInfoModel) = ShoppingItem(
            id = it.id,
            ownerId = it.ownerId,
            groupId = it.groupId,
            name = it.name,
            amount = it.amount,
            price = it.price,
            priority = it.priority,
            check = null,
        )
    }

    class DtoToEntity @Inject constructor() : Mapper<ShoppingItemDto, ShoppingItem> {
        override fun map(it: ShoppingItemDto) = ShoppingItem(
            id = it.id,
            ownerId = it.ownerId,
            groupId = it.groupId,
            name = it.name,
            amount = it.amount,
            price = it.price,
            creationTimestamp = it.createdAt.toLocalDateTime(),
            priority = it.priority,
            check = it.check?.let { dto ->
                ShoppingItem.CheckoffState(
                    checkingUserId = dto.checkingUserId,
                    checkoffTimestamp = dto.checkoffTimestamp.toLocalDateTime(),
                )
            },
        )
    }
}
