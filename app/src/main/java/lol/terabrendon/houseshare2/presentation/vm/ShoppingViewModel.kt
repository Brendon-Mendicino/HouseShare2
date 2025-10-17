package lol.terabrendon.houseshare2.presentation.vm

import android.util.Log
import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import lol.terabrendon.houseshare2.R
import lol.terabrendon.houseshare2.data.repository.ShoppingItemRepository
import lol.terabrendon.houseshare2.domain.usecase.GetLoggedUserUseCase
import lol.terabrendon.houseshare2.domain.usecase.GetSelectedGroupUseCase
import lol.terabrendon.houseshare2.presentation.shopping.ShoppingScreenEvent
import lol.terabrendon.houseshare2.util.mapState
import javax.inject.Inject

@HiltViewModel
class ShoppingViewModel @Inject constructor(
    private val shoppingItemRepository: ShoppingItemRepository,
    private val getLoggedUserUseCase: GetLoggedUserUseCase,
    private val getSelectedGroupUseCase: GetSelectedGroupUseCase,
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


    private val currentGroup = getSelectedGroupUseCase.execute()

    init {
        // TODO: remove when having a decent refreshing system
        viewModelScope.launch {
            currentGroup.collect {
                if (it == null) return@collect
                shoppingItemRepository.refreshByGroupId(it.info.groupId)
            }
        }
    }

    private val _selectedItems = MutableStateFlow(setOf<Long>())
    val selectedItems = _selectedItems.asStateFlow()

    private val _itemSorting = MutableStateFlow(ItemSorting.CreationDate)
    val itemSorting = _itemSorting.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val shoppingItems = currentGroup
        .flatMapConcat { group ->
            group
                ?.let { shoppingItemRepository.findAllByGroupId(it.info.groupId) }
                ?: flowOf(emptyList())
        }
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

                _selectedItems.value = emptySet()
            }

            is ShoppingScreenEvent.ItemsCheckoff -> viewModelScope.launch {
                val items = shoppingItems
                    .value
                    .filter { item -> item.info.id in selectedItems.value }
                    .map { it.info.id }

                val loggedUser = getLoggedUserUseCase.execute().first()!!

                Log.i(TAG, "onEvent: checkoff of ${items.size} ShoppingItems from the repository.")

                shoppingItemRepository.checkoffItems(items, loggedUser)

                _selectedItems.value = emptySet()
            }
        }
    }
}