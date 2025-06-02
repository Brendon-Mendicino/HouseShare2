package lol.terabrendon.houseshare2.presentation.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import lol.terabrendon.houseshare2.data.repository.ExpenseRepository
import lol.terabrendon.houseshare2.domain.mapper.ExpenseBalanceMapper
import javax.inject.Inject

@HiltViewModel
class BillingViewModel @Inject constructor(
    private val expenseRepository: ExpenseRepository,
) : ViewModel() {
    // TODO: move this in the constructor (?)
    private val expenseBalanceMapper = ExpenseBalanceMapper()

    val expenses = expenseRepository.findAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), listOf())

    val balances = expenses
        .map(expenseBalanceMapper::map)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), listOf())
}