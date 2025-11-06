package lol.terabrendon.houseshare2.presentation.vm

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import lol.terabrendon.houseshare2.data.repository.ShoppingItemRepository
import lol.terabrendon.houseshare2.domain.mapper.toModel
import lol.terabrendon.houseshare2.domain.model.ShoppingItemFormState
import lol.terabrendon.houseshare2.domain.model.toValidator
import lol.terabrendon.houseshare2.domain.usecase.GetLoggedUserUseCase
import lol.terabrendon.houseshare2.domain.usecase.GetSelectedGroupUseCase
import lol.terabrendon.houseshare2.presentation.shopping.form.ShoppingItemFormEvent
import lol.terabrendon.houseshare2.presentation.shopping.form.ShoppingItemFormUiEvent
import lol.terabrendon.houseshare2.presentation.util.errorText
import javax.inject.Inject

@HiltViewModel
class ShoppingItemFormViewModel @Inject constructor(
    private val shoppingItemRepository: ShoppingItemRepository,
    private val getLoggedUserUseCase: GetLoggedUserUseCase,
    private val getSelectedGroupUseCase: GetSelectedGroupUseCase,
    @ApplicationContext private val context: Context,
) : ViewModel() {
    companion object {
        private const val TAG: String = "ShoppingItemFormViewModel"
    }

    private val _uiEvents = Channel<ShoppingItemFormUiEvent>()
    val uiEvents = _uiEvents.receiveAsFlow()

    private val _formState = MutableStateFlow(ShoppingItemFormState().toValidator())
    val formState = _formState.asStateFlow()

    fun onEvent(event: ShoppingItemFormEvent) {
        when (event) {
            is ShoppingItemFormEvent.AmountChanged -> _formState.update { state ->
                state.copy(amountStr = state.amountStr.update(event.amount))
            }

            is ShoppingItemFormEvent.NameChanged -> _formState.update { state ->
                state.copy(name = state.name.update(event.name))
            }

            is ShoppingItemFormEvent.PriceChanged -> _formState.update { state ->
                state.copy(priceStr = state.priceStr.update(event.price))
            }

            is ShoppingItemFormEvent.PriorityChanged -> _formState.update { state ->
                state.copy(priority = state.priority.update(event.priority))
            }

            is ShoppingItemFormEvent.Submit -> viewModelScope.launch { onSubmit() }
        }
    }

    private suspend fun onSubmit() {
        val formState = _formState.value
        // TODO: leave these here or not?
        val loggedUser = getLoggedUserUseCase().first()!!
        val selectedGroup = getSelectedGroupUseCase.execute().first()!!

        if (formState.isError) {
            val (property, error) = formState.errors.first()

            _uiEvents.send(
                ShoppingItemFormUiEvent.SubmitFailure(
                    error = error.errorText(
                        property.name,
                        context
                    )
                )
            )

            return
        }

        val shoppingItem = formState
            .toData()
            .copy(ownerId = loggedUser.id, groupId = selectedGroup.info.groupId)
            .toModel()

        Log.i(
            TAG,
            "onSubmit: Inserting a new ShoppingItem(name=\"${shoppingItem.name}\") to the repository."
        )

        shoppingItemRepository.insert(shoppingItem)

        _uiEvents.send(ShoppingItemFormUiEvent.SubmitSuccess)
    }
}