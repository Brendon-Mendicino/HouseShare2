package lol.terabrendon.houseshare2.domain.model

data class ShoppingItemModel(
    val info: ShoppingItemInfoModel,
    val itemOwner: UserModel,
    val checkoffState: CheckoffStateModel?,
) {
    companion object {
        @JvmStatic
        fun default() = ShoppingItemModel(
            info = ShoppingItemInfoModel.default(),
            itemOwner = UserModel.default(),
            checkoffState = null,
        )
    }
}