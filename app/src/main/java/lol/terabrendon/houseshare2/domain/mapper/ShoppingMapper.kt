package lol.terabrendon.houseshare2.domain.mapper

import lol.terabrendon.houseshare2.data.entity.composite.CheckoffStateWithUser
import lol.terabrendon.houseshare2.data.entity.composite.ShoppingItemWithUser
import lol.terabrendon.houseshare2.domain.model.CheckoffStateModel
import lol.terabrendon.houseshare2.domain.model.ShoppingItemInfoModel
import lol.terabrendon.houseshare2.domain.model.ShoppingItemModel
import lol.terabrendon.houseshare2.domain.model.UserModel
import javax.inject.Inject

class ShoppingItemModelMapper @Inject constructor(
    val checkoffMapper: Mapper<CheckoffStateWithUser, CheckoffStateModel>,
) : Mapper<ShoppingItemWithUser, ShoppingItemModel> {
    override fun map(it: ShoppingItemWithUser): ShoppingItemModel = ShoppingItemModel(
        info = ShoppingItemInfoModel.from(it.item),
        itemOwner = UserModel.from(it.itemOwner),
        checkoffState = it.checkoffState?.let { checkoffMapper.map(it) },
    )
}

class CheckoffModelMapper @Inject constructor() :
    Mapper<CheckoffStateWithUser, CheckoffStateModel> {
    override fun map(it: CheckoffStateWithUser): CheckoffStateModel = CheckoffStateModel(
        checkoffTime = it.checkoffState.checkoffTimestamp,
        checkoffUser = UserModel.from(it.checkoffStateUser),
    )
}
