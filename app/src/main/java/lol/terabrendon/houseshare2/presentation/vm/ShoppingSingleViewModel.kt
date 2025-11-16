package lol.terabrendon.houseshare2.presentation.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.brendonmendicino.aformvalidator.annotation.ValidationError
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import lol.terabrendon.houseshare2.data.repository.ShoppingItemRepository
import lol.terabrendon.houseshare2.domain.mapper.toForm
import lol.terabrendon.houseshare2.domain.model.ShoppingItemFormState
import lol.terabrendon.houseshare2.domain.model.ShoppingItemInfoModel
import lol.terabrendon.houseshare2.domain.model.toMoneyOrNull
import lol.terabrendon.houseshare2.domain.model.toValidator
import lol.terabrendon.houseshare2.domain.usecase.GetLoggedUserUseCase
import lol.terabrendon.houseshare2.presentation.navigation.HomepageNavigation
import lol.terabrendon.houseshare2.presentation.shopping.ShoppingItemEvent
import javax.inject.Inject

@HiltViewModel(assistedFactory = ShoppingSingleViewModel.Factory::class)
class ShoppingSingleViewModel @AssistedInject constructor(
    @Assisted
    val route: HomepageNavigation.ShoppingItem,
    val shoppingItemRepository: ShoppingItemRepository,
) : ViewModel() {
    @AssistedFactory
    interface Factory {
        fun create(route: HomepageNavigation.ShoppingItem): ShoppingSingleViewModel
    }

    // If the constructor has @AssistedInject I cannot this class directly
    @Inject
    lateinit var getLoggedUserUseCase: GetLoggedUserUseCase

    sealed class UiEvent {
        data class Error(val error: ValidationError) : UiEvent()
    }

    private val uiChannel = Channel<UiEvent>()
    val uiEvent = uiChannel.receiveAsFlow()

    data class State(
        val pending: Int = 0,
    )

    private val _state = MutableStateFlow(State())
    val state = _state.asStateFlow()

    val shoppingItem = shoppingItemRepository
        .findById(route.shoppingItemId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), null)


    private suspend inline fun withPending(crossinline action: suspend () -> Unit) {
        _state.update { it.copy(pending = it.pending + 1) }
        try {
            action()
        } finally {
            _state.update { it.copy(pending = it.pending - 1) }
        }
    }

    fun onEvent(event: ShoppingItemEvent) {
        viewModelScope.launch {
            withPending {
                when (event) {
                    is ShoppingItemEvent.NameChanged -> update(
                        copyForm = { copy(name = event.name) },
                        copyInfo = { copy(name = event.name) },
                    )

                    is ShoppingItemEvent.PriceChanged -> update(
                        copyForm = { copy(priceStr = event.price) },
                        copyInfo = { copy(price = event.price.toMoneyOrNull()) },
                    )

                    is ShoppingItemEvent.QuantityChanged -> update(
                        copyForm = { copy(amountStr = event.quantity) },
                        copyInfo = { copy(amount = event.quantity.toInt()) },
                    )

                    ShoppingItemEvent.Toggled -> onItemToggle()
                }
            }
        }
    }

    private suspend fun update(
        copyForm: ShoppingItemFormState.() -> ShoppingItemFormState,
        copyInfo: ShoppingItemInfoModel.() -> ShoppingItemInfoModel,
    ) {
        val info = shoppingItem.value?.info ?: return

        val validationError = info.toForm().copyForm().toValidator().error

        if (validationError != null) {
            uiChannel.send(UiEvent.Error(validationError))
            return
        }

        val newInfo = info.copyInfo()
        if (newInfo == info)
            return

        shoppingItemRepository.update(newInfo)
    }

    private suspend fun onItemToggle() {
        val item = shoppingItem.value

        if (item == null)
            return

        val loggedUser = getLoggedUserUseCase().filterNotNull().first()

        if (item.checkoffState == null) {
            shoppingItemRepository.checkoffItems(
                item.info.groupId,
                listOf(item.info.id),
                loggedUser.id
            )
        } else {
            shoppingItemRepository.uncheckItems(item.info.groupId, listOf(item.info.id))
        }
    }
}