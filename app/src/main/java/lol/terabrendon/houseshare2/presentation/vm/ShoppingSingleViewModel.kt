package lol.terabrendon.houseshare2.presentation.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import lol.terabrendon.houseshare2.data.repository.ShoppingItemRepository
import lol.terabrendon.houseshare2.presentation.navigation.HomepageNavigation

@HiltViewModel(assistedFactory = ShoppingSingleViewModel.Factory::class)
class ShoppingSingleViewModel @AssistedInject constructor(
    @Assisted
    val route: HomepageNavigation.ShoppingItem,
    shoppingItemRepository: ShoppingItemRepository,
) : ViewModel() {
    @AssistedFactory
    interface Factory {
        fun create(route: HomepageNavigation.ShoppingItem): ShoppingSingleViewModel
    }

//    private val route = savedStateHandle.toRoute<HomepageNavigation.ShoppingItem>()

    val shoppingItem = shoppingItemRepository
        .findById(route.shoppingItemId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), null)
}