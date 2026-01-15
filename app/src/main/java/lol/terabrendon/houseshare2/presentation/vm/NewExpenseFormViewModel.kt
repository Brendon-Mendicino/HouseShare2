package lol.terabrendon.houseshare2.presentation.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.michaelbull.result.getOrElse
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import lol.terabrendon.houseshare2.data.repository.ExpenseRepository
import lol.terabrendon.houseshare2.domain.form.ExpenseFormState
import lol.terabrendon.houseshare2.domain.form.UserPart
import lol.terabrendon.houseshare2.domain.form.toValidator
import lol.terabrendon.houseshare2.domain.mapper.ExpenseModelMapper
import lol.terabrendon.houseshare2.domain.model.Money
import lol.terabrendon.houseshare2.domain.model.toMoney
import lol.terabrendon.houseshare2.domain.usecase.GetLoggedUserUseCase
import lol.terabrendon.houseshare2.domain.usecase.GetSelectedGroupUseCase
import lol.terabrendon.houseshare2.presentation.billing.ExpenseFormEvent
import lol.terabrendon.houseshare2.presentation.util.SnackbarController
import lol.terabrendon.houseshare2.presentation.util.SnackbarEvent
import lol.terabrendon.houseshare2.presentation.util.toUiText
import lol.terabrendon.houseshare2.util.update
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class NewExpenseFormViewModel @Inject constructor(
    private val expenseRepository: ExpenseRepository,
    getSelectedGroup: GetSelectedGroupUseCase,
    getLoggedUserUseCase: GetLoggedUserUseCase,
) : ViewModel() {
    sealed class UiEvent {
        data object Finish : UiEvent()
    }

    private val expenseModelMapper: ExpenseModelMapper = ExpenseModelMapper()

    private val eventChannel = Channel<UiEvent>()

    /**
     * This channel is used to terminate the screen when all the operations
     * are finished
     */
    val eventChannelFlow = eventChannel.receiveAsFlow()

    private val loggedUser = getLoggedUserUseCase()
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    private val selectedGroup = getSelectedGroup()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)


    val users = selectedGroup
        .filterNotNull()
        .map { it.users }
        .onEach {
            Timber.i("onEach: Getting updated list of users from the database.")
        }
        .stateIn(
            viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
        )

    private var _expenseFormState = MutableStateFlow(ExpenseFormState().toValidator())
    val expenseFormState = _expenseFormState.asStateFlow()

    init {
        viewModelScope.launch {
            users.collect { users ->
                _expenseFormState.update {
                    it.update {
                        userParts = List(users.size) { UserPart() }
                    }
                }
            }
        }

        viewModelScope.launch {
            loggedUser.collect { user ->
                _expenseFormState.update { state ->
                    state.update {
                        payer = user
                    }
                }
            }
        }
    }

    private val _userSelected = MutableStateFlow(emptyList<Boolean>())
    val userSelected = _userSelected.asStateFlow()

    init {
        viewModelScope.launch {
            users.collect {
                _userSelected.value = it.map { true }
            }
        }
    }

    val simpleDivisionParts =
        combine(_userSelected, users, expenseFormState) { selectedUsers, users, formState ->
            val total = formState.totalAmountMoney.value
            val noSelected = selectedUsers.count { it }

            val moneyEach = if (noSelected > 0) total / noSelected
            else 0.toMoney()

            var remainder = if (noSelected > 0) total - (moneyEach * noSelected)
            else 0.toMoney()

            val getCent = {
                if (remainder > 0) {
                    remainder -= Money.ATOM
                    Money.ATOM
                } else {
                    Money.ZERO
                }
            }

            selectedUsers.map { selected ->
                if (selected) moneyEach + getCent()
                else 0.toMoney()
            }.also {
                check(remainder == 0.toMoney()) { "Remainder check failed! remainder=$remainder" }
            }
        }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    fun onEvent(event: ExpenseFormEvent) {
        when (event) {
            is ExpenseFormEvent.CategoryToggled -> {
                _expenseFormState.update { it.update { category = event.category } }
            }

            is ExpenseFormEvent.DescriptionChanged -> {
                _expenseFormState.update { it.update { description = event.description } }
            }

            is ExpenseFormEvent.TotalAmountChanged -> {
                _expenseFormState.update { it.update { totalAmount = event.amount } }
            }

            is ExpenseFormEvent.PayerChanged -> {
                _expenseFormState.update { it.update { payer = event.payer } }
            }

            is ExpenseFormEvent.TitleChanged -> {
                _expenseFormState.update { it.update { title = event.title } }
            }

            is ExpenseFormEvent.SimpleDivisionUserToggled -> {
                _userSelected.update {
                    it.toMutableList().apply { set(event.index, !this[event.index]) }.toList()
                }
            }

            is ExpenseFormEvent.SimpleDivisionToggled -> {
                _expenseFormState.update {
                    it.update {
                        simpleDivisionEnabled = !simpleDivisionEnabled
                    }
                }
            }

            is ExpenseFormEvent.UnitChanged -> {
                Timber.i("Updating paymentUnits with index %d", event.index)
                _expenseFormState.update {
                    it.update {
                        userParts =
                            userParts.update(event.index) { part -> part.copy(paymentUnit = event.newUnit) }
                    }
                }
            }

            is ExpenseFormEvent.UserPartChanged -> {
                Timber.i("Updating paymentValueUnits with index %d", event.index)
                _expenseFormState.update {
                    it.update {
                        userParts =
                            userParts.update(event.index) { part -> part.copy(amount = event.newValue) }
                    }
                }
            }

            ExpenseFormEvent.Submit -> viewModelScope.launch { onSubmit() }
        }
    }

    private suspend fun onSubmit() {
        val formState = expenseFormState.value

        // The owner of the expense if the current logged user.
        val owner = loggedUser.value ?: throw IllegalStateException("No logged users!")

        val groupId = selectedGroup.value?.info?.groupId
            ?: run {
                val msg =
                    "There is not selectedGroup! Choose a group first and then proceed with the form!"
                Timber.e("onConfirm: $msg")
                throw IllegalStateException(msg)
            }

        val error = formState.errors.firstOrNull()
        if (error != null) {
            val message = error.second.toUiText(error.first)
            SnackbarController.sendEvent(SnackbarEvent(message = message))
            return
        }

        val state = formState.toData()
        val userParts =
            if (state.simpleDivisionEnabled) simpleDivisionParts.value.zip(users.value)
            else state.convertedValues.zip(users.value)

        val expense = expenseModelMapper
            .map(
                formState = state,
                expenseOwner = owner,
                userParts = userParts.filter { (amount, _) -> amount > 0.toMoney() },
                groupId = groupId,
            )
            .getOrElse {
                val msg = "expense model mapping failed! Error msg: $it"
                Timber.e("onConfirm: $msg")
                throw IllegalStateException(msg)
            }

        Timber.i("Inserting a new expense with title \"${expense.title}\" to the repository")

        expenseRepository.insert(expense)

        eventChannel.send(UiEvent.Finish)
    }
}