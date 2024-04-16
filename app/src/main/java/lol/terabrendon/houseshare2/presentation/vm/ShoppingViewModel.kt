package lol.terabrendon.houseshare2.presentation.vm

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import lol.terabrendon.houseshare2.model.ShoppingItemModel
import lol.terabrendon.houseshare2.repository.ShoppingItemRepository
import lol.terabrendon.houseshare2.util.mapState
import javax.inject.Inject

@HiltViewModel
class ShoppingViewModel @Inject constructor(
    private val shoppingItemRepository: ShoppingItemRepository
) : ViewModel() {
    companion object {
        private const val TAG = "ShoppingViewModel"
    }

    private val selectedItems = MutableStateFlow(mutableSetOf<Int>())

    val shoppingItems = shoppingItemRepository
        .getAll()
        .combine(selectedItems) { items, selected ->
            items.map { item ->
                if (item.id in selected) {
                    item.copy(selected = true)
                } else {
                    item
                }
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), listOf())

    val isAnySelected = selectedItems.mapState(viewModelScope) { items -> items.isNotEmpty() }

    fun onItemSelected(id: Int, selected: Boolean) {
        selectedItems.update {
            Log.i(TAG, "onItemSelected: updating ShoppingItem@$id as selected: $selected")
            it.toMutableSet().apply {
                if (selected) this.add(id)
                else this.remove(id)
            }
        }
    }

    fun addShoppingItem(newItem: ShoppingItemModel) {
        viewModelScope.launch {
            Log.i(TAG, "addShoppingItem: added new ShoppingItem to the repository.")
            shoppingItemRepository.insert(newItem)
        }
    }

    fun onDeleteSelected() {
        viewModelScope.launch {
            val items = shoppingItems
                .map { items -> items.filter { item -> item.selected } }
                .first()

            Log.i(TAG, "addShoppingItem: deleting ${items.size} ShoppingItems from the repository.")

            shoppingItemRepository.deleteAll(items)

            selectedItems.value = mutableSetOf()
        }
    }
}