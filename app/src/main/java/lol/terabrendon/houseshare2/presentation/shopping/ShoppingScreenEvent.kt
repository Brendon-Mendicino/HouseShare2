package lol.terabrendon.houseshare2.presentation.shopping

import lol.terabrendon.houseshare2.domain.model.ShoppingItemInfoModel
import lol.terabrendon.houseshare2.presentation.vm.ShoppingViewModel

sealed class ShoppingScreenEvent {
    data class ItemChecked(val item: ShoppingItemInfoModel) : ShoppingScreenEvent()
    data class SortingChanged(val itemSorting: ShoppingViewModel.ItemSorting) :
        ShoppingScreenEvent()

    object ItemsDeleted : ShoppingScreenEvent()
}
