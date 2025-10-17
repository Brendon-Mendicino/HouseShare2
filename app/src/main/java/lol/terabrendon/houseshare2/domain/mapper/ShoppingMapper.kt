package lol.terabrendon.houseshare2.domain.mapper

import lol.terabrendon.houseshare2.data.dto.ShoppingItemDto
import lol.terabrendon.houseshare2.data.entity.ShoppingItem
import lol.terabrendon.houseshare2.data.entity.composite.CheckoffStateWithUser
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
        val checkoffMapper: Mapper<CheckoffStateWithUser, CheckoffStateModel>,
    ) : Mapper<ShoppingItemWithUser, ShoppingItemModel> {
        override fun map(it: ShoppingItemWithUser): ShoppingItemModel = ShoppingItemModel(
            info = ShoppingItemInfoModel.from(it.item),
            itemOwner = UserModel.from(it.itemOwner),
            checkoffState = it.checkoffState?.let { checkoffMapper.map(it) },
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

    class CheckoffEntityToModel @Inject constructor() :
        Mapper<CheckoffStateWithUser, CheckoffStateModel> {
        override fun map(it: CheckoffStateWithUser): CheckoffStateModel = CheckoffStateModel(
            checkoffTime = it.checkoffState.checkoffTimestamp,
            checkoffUser = UserModel.from(it.checkoffStateUser),
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
            checkoff = null,
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
            // TODO: change this
            checkoff = null,
        )
    }
}
