package lol.terabrendon.houseshare2.presentation.vm

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import lol.terabrendon.houseshare2.data.repository.ShoppingItemRepository
import lol.terabrendon.houseshare2.presentation.navigation.HomepageNavigation
import javax.inject.Inject

@HiltViewModel
class ShoppingSingleViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val shoppingItemRepository: ShoppingItemRepository,
) : ViewModel() {
    private val route = savedStateHandle.toRoute<HomepageNavigation.ShoppingItem>()

    val shoppingItem = shoppingItemRepository
        .findById(route.shoppingItemId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), null)
}