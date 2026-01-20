package lol.terabrendon.houseshare2.presentation.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import lol.terabrendon.houseshare2.data.repository.ShoppingItemRepository
import lol.terabrendon.houseshare2.domain.usecase.GetLoggedUserUseCase
import lol.terabrendon.houseshare2.domain.usecase.GetSelectedGroupUseCase
import lol.terabrendon.houseshare2.presentation.shopping.ShoppingScreenEvent
import lol.terabrendon.houseshare2.presentation.util.SnackbarController
import lol.terabrendon.houseshare2.util.mapState
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class ShoppingViewModel @Inject constructor(
    private val shoppingItemRepository: ShoppingItemRepository,
    private val getLoggedUserUseCase: GetLoggedUserUseCase,
    getSelectedGroupUseCase: GetSelectedGroupUseCase,
) : ViewModel() {
    val currentGroup = getSelectedGroupUseCase().stateIn(
        viewModelScope,
        SharingStarted.Eagerly,
        initialValue = null
    )

    init {
        // TODO: remove when having a decent refreshing system
        viewModelScope.launch {
            currentGroup.filterNotNull().collect {
                shoppingItemRepository.refreshByGroupId(it.info.groupId)
            }
        }
    }

    private val _selectedItems = MutableStateFlow(setOf<Long>())
    val selectedItems = _selectedItems.asStateFlow()

    private val _itemSorting = MutableStateFlow(ShoppingItemRepository.Sorting.CreationDate)
    val itemSorting = _itemSorting.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val shoppingItems = currentGroup
        .filterNotNull()
        .combine(itemSorting) { a, b -> a to b }
        .flatMapLatest { (group, sorting) ->
            shoppingItemRepository.findUnchecked(group.info.groupId, sorting)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    @OptIn(ExperimentalCoroutinesApi::class)
    val checkedItems = currentGroup
        .filterNotNull()
        .combine(itemSorting) { a, b -> a to b }
        .flatMapLatest { (group, sorting) ->
            shoppingItemRepository.findChecked(group.info.groupId, sorting)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    val isAnySelected = _selectedItems.mapState(viewModelScope) { items -> items.isNotEmpty() }

    fun onEvent(event: ShoppingScreenEvent) {
        when (event) {
            is ShoppingScreenEvent.ItemChecked -> {
                Timber.i("onEvent: ShoppingItem@%d was toggled", event.item.id)

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

                Timber.i("onEvent: deleting %d ShoppingItems from the repository.", items.size)

                val (_, err) = shoppingItemRepository.deleteAll(items)
                if (err != null) {
                    SnackbarController.sendError(err)
                    return@launch
                }

                _selectedItems.value = emptySet()
            }

            is ShoppingScreenEvent.ItemsCheckoff -> viewModelScope.launch {
                val items = shoppingItems
                    .value
                    .filter { item -> item.info.id in selectedItems.value }
                    .map { it.info.id }

                val loggedUser = getLoggedUserUseCase().first()!!
                val groupId = currentGroup.value!!.info.groupId

                Timber.i("onEvent: check of %d ShoppingItems from the repository.", items.size)

                val (_, err) = shoppingItemRepository.checkoffItems(groupId, items, loggedUser.id)
                if (err != null) {
                    SnackbarController.sendError(err)
                    return@launch
                }

                _selectedItems.value = emptySet()
            }
        }
    }
}