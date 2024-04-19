package lol.terabrendon.houseshare2.presentation.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import lol.terabrendon.houseshare2.repository.ExpenseRepository
import javax.inject.Inject

@HiltViewModel
class BillingViewModel @Inject constructor(
    private val expenseRepository: ExpenseRepository
) : ViewModel() {

    val expenses = expenseRepository.findAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), listOf())
}