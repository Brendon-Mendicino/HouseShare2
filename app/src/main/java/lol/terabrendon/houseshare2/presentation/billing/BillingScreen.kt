package lol.terabrendon.houseshare2.presentation.billing

import androidx.annotation.StringRes
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.outlined.AccountBalance
import androidx.compose.material.icons.outlined.Receipt
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch
import lol.terabrendon.houseshare2.R
import lol.terabrendon.houseshare2.model.BillingBalanceModel
import lol.terabrendon.houseshare2.model.ExpenseModel
import lol.terabrendon.houseshare2.presentation.components.AvatarIcon
import lol.terabrendon.houseshare2.presentation.vm.BillingViewModel
import lol.terabrendon.houseshare2.util.currencyFormat
import lol.terabrendon.houseshare2.util.inlineFormat


/**
 * Class to store the information of Composable [Tab]
 */
private data class TabItem(
    @StringRes
    val title: Int,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
)

private val tabItems = listOf(
    TabItem(
        title = R.string.expenses,
        selectedIcon = Icons.Filled.Receipt,
        unselectedIcon = Icons.Outlined.Receipt,
    ),
    TabItem(
        title = R.string.balance,
        selectedIcon = Icons.Filled.AccountBalance,
        unselectedIcon = Icons.Outlined.AccountBalance,
    ),
)

@Composable
fun BillingScreen(
    billingViewModel: BillingViewModel = hiltViewModel()
) {
    val expenses by billingViewModel.expenses.collectAsStateWithLifecycle()
    val balances by billingViewModel.balances.collectAsStateWithLifecycle()

    BillingInnerScreen(expenses = expenses, balances = balances)
}

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
private fun BillingInnerScreen(
    expenses: List<ExpenseModel>,
    balances: List<BillingBalanceModel>,
) {
    val pagerState = rememberPagerState { tabItems.size }
    val scope = rememberCoroutineScope()

    Column(modifier = Modifier.fillMaxSize()) {
        PrimaryTabRow(selectedTabIndex = pagerState.currentPage) {
            tabItems.forEachIndexed { index, item ->
                Tab(
                    selected = index == pagerState.currentPage,
                    onClick = {
                        scope.launch { pagerState.animateScrollToPage(index) }
                    },
                    text = { Text(stringResource(item.title)) },
                    icon = {
                        Icon(
                            imageVector = if (index == pagerState.currentPage) item.selectedIcon else item.unselectedIcon,
                            contentDescription = stringResource(
                                id = item.title
                            )
                        )
                    }
                )
            }
        }

        HorizontalPager(state = pagerState, modifier = Modifier.fillMaxSize()) { pageIndex ->
            when (pageIndex) {
                0 -> ExpenseList(expenses = expenses, modifier = Modifier.fillMaxSize())
                1 -> AccountBalance(balances = balances, modifier = Modifier.fillMaxSize())
                else -> Text("2")
            }
        }
    }
}

@Composable
private fun AccountBalance(modifier: Modifier = Modifier, balances: List<BillingBalanceModel>) {
    LazyColumn(modifier = modifier) {
        items(balances, key = { it.user.id }) { balance ->
            AccountBalanceItem(modifier = Modifier.fillMaxWidth(), billingBalance = balance)
        }
    }
}

@Composable
private fun AccountBalanceItem(modifier: Modifier = Modifier, billingBalance: BillingBalanceModel) {
    val userBillingColor = if (billingBalance.finalBalance > 0) Color.Green else Color.Red

    Column(modifier = modifier) {
        Row(
            modifier = Modifier
                .padding(horizontal = 0.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Spacer(Modifier.requiredWidth(16.dp))

            AvatarIcon(
                firstName = billingBalance.user.username,
                lastName = billingBalance.user.username
            )

            Spacer(Modifier.requiredWidth(16.dp))

            Text(
                text = billingBalance.user.username,
                fontWeight = FontWeight.Bold,
            )

            Spacer(
                Modifier
                    .requiredWidth(16.dp)
                    .weight(1f)
            )

            Text(
                text = billingBalance.finalBalance.currencyFormat(),
                fontWeight = FontWeight.Bold,
                color = userBillingColor,
            )

            Spacer(Modifier.requiredWidth(8.dp))
        }
    }
}

@Composable
private fun ExpenseList(modifier: Modifier = Modifier, expenses: List<ExpenseModel>) {
    LazyColumn(modifier = modifier) {
        items(expenses, key = { it.id }) { expense ->
            ExpenseItem(expense = expense, modifier = Modifier.fillMaxWidth())
            HorizontalDivider()
        }
    }
}


@Composable
private fun ExpenseItem(modifier: Modifier = Modifier, expense: ExpenseModel) {
    var isExpanded by rememberSaveable { mutableStateOf(false) }

    Column(
        modifier
            .animateContentSize()
            .clickable { isExpanded = !isExpanded }) {
        Row(modifier = Modifier.padding(horizontal = 0.dp, vertical = 8.dp)) {
            Spacer(Modifier.requiredWidth(16.dp))
            Icon(
                expense.category.toImageVector(),
                contentDescription = null,
                modifier = Modifier
                    .align(Alignment.CenterVertically)
                    .clip(CircleShape)
                    .background(color = MaterialTheme.colorScheme.surfaceBright)
            )
            Spacer(Modifier.requiredWidth(16.dp))

            Column {
                Text(
                    "${expense.title} • ${expense.creationTimestamp.inlineFormat()}",
                    maxLines = 1,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    stringResource(
                        R.string.paid,
                        expense.expenseOwner,
                        expense.amount.currencyFormat()
                    )
                )
            }

            Spacer(
                Modifier
                    .requiredWidth(16.dp)
                    .weight(1f)
            )
            Icon(
                Icons.Filled.Visibility,
                contentDescription = null,
                modifier = Modifier.align(Alignment.CenterVertically)
            )
            Spacer(Modifier.requiredWidth(16.dp))
        }

        if (isExpanded) {
            Column(modifier = Modifier.fillMaxHeight()) {
                expense.userExpenses.forEach { item ->
                    Text("${item.partAmount} ${item.user.username}")
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ExpensesPreview() {
    val e = (0..5L).map { ExpenseModel.default().copy(id = it) }.toList()

    ExpenseList(expenses = e)
}

@Preview(showBackground = true)
@Composable
fun AccountBalancePreview() {
    val balances = generateSequence { BillingBalanceModel.default() }.mapIndexed { i, b ->
        b.copy(
            user = b.user.copy(id = i.toLong()),
            finalBalance = if (i % 2 == 0) 10.0 else -10.0,
        )
    }.take(5).toList()

    AccountBalance(balances = balances)
}
