package lol.terabrendon.houseshare2.domain.mapper

import lol.terabrendon.houseshare2.data.entity.composite.ShoppingItemWithUser
import lol.terabrendon.houseshare2.domain.model.ShoppingItemInfoModel
import lol.terabrendon.houseshare2.domain.model.ShoppingItemModel
import lol.terabrendon.houseshare2.domain.model.UserModel
import javax.inject.Inject

class ShoppingItemModelMapper @Inject constructor() {
    fun map(item: ShoppingItemWithUser): ShoppingItemModel {
        return ShoppingItemModel(
            info = ShoppingItemInfoModel.from(item.item),
            itemOwner = UserModel.from(item.itemOwner),
        )
    }
}