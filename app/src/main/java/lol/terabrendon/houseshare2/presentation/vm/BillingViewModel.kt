package lol.terabrendon.houseshare2.presentation.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.michaelbull.result.onFailure
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import lol.terabrendon.houseshare2.data.repository.ExpenseRepository
import lol.terabrendon.houseshare2.domain.mapper.DebtSettlementMapper
import lol.terabrendon.houseshare2.domain.mapper.ExpenseBalanceMapper
import lol.terabrendon.houseshare2.domain.model.BillingBalanceModel
import lol.terabrendon.houseshare2.domain.model.DebtModel
import lol.terabrendon.houseshare2.domain.model.GroupMemberModel
import lol.terabrendon.houseshare2.domain.model.toMoney
import lol.terabrendon.houseshare2.domain.usecase.GetLoggedMemberUseCase
import lol.terabrendon.houseshare2.domain.usecase.GetSelectedGroupUseCase
import lol.terabrendon.houseshare2.presentation.util.SnackbarController
import javax.inject.Inject

@HiltViewModel
class BillingViewModel @Inject constructor(
    private val expenseRepository: ExpenseRepository,
    private val expenseBalanceMapper: ExpenseBalanceMapper,
    private val debtSettlementMapper: DebtSettlementMapper,
    getSelectedGroupUseCase: GetSelectedGroupUseCase,
    getLoggedMemberUseCase: GetLoggedMemberUseCase,
) : ViewModel() {

    /**
     * The payments that settle the group, split by how they concern the logged member.
     */
    data class DebtsUiState(
        val youOwe: List<DebtModel> = emptyList(),
        val owedToYou: List<DebtModel> = emptyList(),
        val others: List<DebtModel> = emptyList(),
    )

    companion object {
        @JvmStatic
        private fun addMissingUsers(
            expenses: Map<Long, BillingBalanceModel>,
            groupUsers: Map<Long, GroupMemberModel>,
        ): List<BillingBalanceModel> {
            val missingUsers = groupUsers.keys - expenses.keys

            val completeBalance = expenses + missingUsers.map { userId ->
                userId to BillingBalanceModel(
                    groupUsers[userId]!!,
                    0.toMoney()
                )
            }

            return completeBalance.toSortedMap().values.toList()
        }

    }

    val currentGroup = getSelectedGroupUseCase()
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    init {
        // TODO: remove when having a decent refreshing system
        viewModelScope.launch {
            val group = currentGroup.filterNotNull().first()
            expenseRepository.refreshByGroupId(group.info.groupId)
                .onFailure { SnackbarController.sendError(it) }
        }
    }

    val groupMembers = currentGroup.filterNotNull()
        .map { it.members.associateBy { user -> user.id } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyMap())

    @OptIn(ExperimentalCoroutinesApi::class)
    val expenses = currentGroup
        .filterNotNull()
        .flatMapLatest { group ->
            expenseRepository.findByGroupId(group.info.groupId)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    private val partialBalances = expenses
        .map(expenseBalanceMapper::map)
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyMap())

    val balances = combine(partialBalances, groupMembers, ::addMissingUsers)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    // Members without expenses have no balance, so they can't be part of any debt.
    val debts = combine(
        partialBalances.map { debtSettlementMapper.map(it.values) },
        getLoggedMemberUseCase(),
    ) { debts, me ->
        DebtsUiState(
            youOwe = debts.filter { it.debtor.id == me?.id },
            owedToYou = debts.filter { it.creditor.id == me?.id },
            others = debts.filter { it.debtor.id != me?.id && it.creditor.id != me?.id },
        )
    }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), DebtsUiState())
}

// qual e il mio tasssskkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkk???????????????????????????????????????????????????????????????????????????????????