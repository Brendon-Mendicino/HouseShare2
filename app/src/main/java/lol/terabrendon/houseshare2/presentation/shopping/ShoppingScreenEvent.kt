package lol.terabrendon.houseshare2.presentation.shopping

import lol.terabrendon.houseshare2.data.repository.ShoppingItemRepository
import lol.terabrendon.houseshare2.domain.model.ShoppingItemInfoModel

sealed class ShoppingScreenEvent {
    data class ItemChecked(val item: ShoppingItemInfoModel) : ShoppingScreenEvent()
    data class SortingChanged(val itemSorting: ShoppingItemRepository.Sorting) :
        ShoppingScreenEvent()

    object ItemsDeleted : ShoppingScreenEvent()

    object ItemsCheckoff : ShoppingScreenEvent()
}
