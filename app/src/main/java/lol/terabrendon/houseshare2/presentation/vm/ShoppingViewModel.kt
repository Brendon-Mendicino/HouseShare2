package lol.terabrendon.houseshare2.presentation.vm

import android.util.Log
import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import lol.terabrendon.houseshare2.R
import lol.terabrendon.houseshare2.data.repository.ShoppingItemRepository
import lol.terabrendon.houseshare2.presentation.shopping.ShoppingScreenEvent
import lol.terabrendon.houseshare2.util.mapState
import javax.inject.Inject

@HiltViewModel
class ShoppingViewModel @Inject constructor(
    private val shoppingItemRepository: ShoppingItemRepository
) : ViewModel() {
    companion object {
        private const val TAG = "ShoppingViewModel"
    }

    enum class ItemSorting {
        CreationDate,
        Priority,
        Name,
        Username,
        ;

        @StringRes
        fun toStringRes(): Int = when (this) {
            CreationDate -> R.string.creation_date
            Priority -> R.string.priority
            Name -> R.string.name
            Username -> R.string.username
        }
    }

    private val _selectedItems = MutableStateFlow<Set<Long>>(setOf<Long>())
    val selectedItems = _selectedItems.asStateFlow()

    private val _itemSorting = MutableStateFlow(ItemSorting.CreationDate)
    val itemSorting = _itemSorting.asStateFlow()

    val shoppingItems = shoppingItemRepository
        .findAllByGroupId(1)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), listOf())

    val isAnySelected = _selectedItems.mapState(viewModelScope) { items -> items.isNotEmpty() }

    fun onEvent(event: ShoppingScreenEvent) {
        when (event) {
            is ShoppingScreenEvent.ItemChecked -> {
                Log.i(TAG, "onEvent: ShoppingItem@${event.item.id} was toggled")

                _selectedItems.update {
                    if (event.item.id !in it)
                        it + event.item.id
                    else
                        it - event.item.id
                }
            }

            is ShoppingScreenEvent.SortingChanged -> _itemSorting.value = event.itemSorting

            is ShoppingScreenEvent.ItemsDeleted -> viewModelScope.launch {
                val items = shoppingItems
                    .value
                    .filter { item -> item.info.id in selectedItems.value }
                    .map { it.info }

                Log.i(TAG, "onEvent: deleting ${items.size} ShoppingItems from the repository.")

                shoppingItemRepository.deleteAll(items)

                _selectedItems.value = mutableSetOf()
            }
        }
    }
}