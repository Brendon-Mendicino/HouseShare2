package lol.terabrendon.houseshare2.presentation.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import lol.terabrendon.houseshare2.data.repository.ExpenseRepository
import lol.terabrendon.houseshare2.domain.mapper.ExpenseBalanceMapper
import lol.terabrendon.houseshare2.domain.usecase.GetSelectedGroupUseCase
import javax.inject.Inject

@HiltViewModel
class BillingViewModel @Inject constructor(
    private val expenseRepository: ExpenseRepository,
    private val expenseBalanceMapper: ExpenseBalanceMapper,
    getSelectedGroupUseCase: GetSelectedGroupUseCase,
) : ViewModel() {

    private val currentGroup = getSelectedGroupUseCase.execute()

    init {
        // TODO: remove when having a decent refreshing system
        viewModelScope.launch {
            currentGroup.filterNotNull().collect {
                expenseRepository.refreshByGroupId(it.info.groupId)
            }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val expenses = currentGroup
        .filterNotNull()
        .flatMapLatest { group ->
            expenseRepository.findByGroupId(group.info.groupId)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    val balances = expenses
        .map(expenseBalanceMapper::map)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())
}

// qual e il mio tasssskkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkk???????????????????????????????????????????????????????????????????????????????????