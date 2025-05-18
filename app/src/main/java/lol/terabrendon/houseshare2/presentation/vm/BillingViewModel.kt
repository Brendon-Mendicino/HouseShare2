package lol.terabrendon.houseshare2.presentation.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import lol.terabrendon.houseshare2.model.BillingBalanceModel
import lol.terabrendon.houseshare2.repository.ExpenseRepository
import javax.inject.Inject

@HiltViewModel
class BillingViewModel @Inject constructor(
    private val expenseRepository: ExpenseRepository
) : ViewModel() {

    val expenses = expenseRepository.findAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), listOf())

    val balances = expenses
        .map { expenses ->
            expenses
                .asSequence()
                // Get the single balance amount per user in the list of expense parts
                // if the user is the payer of the expense, the other are in
                // debt with him.
                .flatMap { expense ->
                    expense.userExpenses.map { userPart ->
                        userPart.copy(
                            partAmount =
                                if (userPart.user == expense.expensePayer) userPart.partAmount
                                else -userPart.partAmount
                        )
                    }
                }
                .groupingBy { it.user }
                .fold({ user, _ -> BillingBalanceModel(user, 0.0) }) { _, balance, expense ->
                    balance.copy(finalBalance = balance.finalBalance + expense.partAmount)
                }
                .values
                .toList()
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), listOf())
}