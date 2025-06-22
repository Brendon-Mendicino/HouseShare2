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

    private val selectedItems = MutableStateFlow<Set<Long>>(mutableSetOf<Long>())

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

    fun onEvent(event: ShoppingScreenEvent) {
        when (event) {
            is ShoppingScreenEvent.ItemChecked -> selectedItems.update {
                Log.i(TAG, "onEvent: ShoppingItem@${event.item.id} was toggled")
                it.toMutableSet().apply {
                    if (!contains(event.item.id)) add(event.item.id)
                    else remove(event.item.id)
                }
            }

            is ShoppingScreenEvent.ItemsDeleted -> viewModelScope.launch {
                val items = shoppingItems
                    .map { items -> items.filter { item -> item.selected } }
                    .first()

                Log.i(TAG, "onEvent: deleting ${items.size} ShoppingItems from the repository.")

                shoppingItemRepository.deleteAll(items)

                selectedItems.value = mutableSetOf()
            }
        }
    }
}