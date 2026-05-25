package lol.terabrendon.houseshare2.presentation.screen.billing

import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.outlined.AccountBalance
import androidx.compose.material.icons.outlined.Receipt
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import lol.terabrendon.houseshare2.R
import lol.terabrendon.houseshare2.domain.model.BillingBalanceModel
import lol.terabrendon.houseshare2.domain.model.ExpenseModel
import lol.terabrendon.houseshare2.domain.model.UserExpenseModel
import lol.terabrendon.houseshare2.domain.model.UserModel
import lol.terabrendon.houseshare2.domain.model.toMoney
import lol.terabrendon.houseshare2.presentation.components.AvatarIcon
import lol.terabrendon.houseshare2.presentation.components.ChooseGroup
import lol.terabrendon.houseshare2.presentation.components.UsersAvatar
import lol.terabrendon.houseshare2.presentation.navigation.HomepageNavigation
import lol.terabrendon.houseshare2.presentation.navigation.MainNavigation
import lol.terabrendon.houseshare2.presentation.provider.FabConfig
import lol.terabrendon.houseshare2.presentation.provider.RegisterFabConfig
import lol.terabrendon.houseshare2.presentation.util.SnackbarController
import lol.terabrendon.houseshare2.presentation.util.UiText
import lol.terabrendon.houseshare2.presentation.vm.BillingViewModel
import lol.terabrendon.houseshare2.ui.theme.HouseShare2Theme
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
    billingViewModel: BillingViewModel = hiltViewModel(),
    navigate: (MainNavigation) -> Unit,
) {
    val groupAvailable = billingViewModel.currentGroup.collectAsStateWithLifecycle().value != null
    val expenses by billingViewModel.expenses.collectAsStateWithLifecycle()
    val balances by billingViewModel.balances.collectAsStateWithLifecycle()

    val scope = rememberCoroutineScope()

    RegisterFabConfig(
        config = FabConfig.Fab(
            // TODO: when having a nice config management put the groupAvailable here
            expanded = true,
            text = UiText.Res(R.string.create),
            onClick = {
                if (groupAvailable) navigate(HomepageNavigation.ExpenseForm)
                else scope.launch { SnackbarController.sendRes(R.string.select_group_before_expense) }
            },
        ),
        route = HomepageNavigation.Billing::class,
    )

    if (!groupAvailable) {
        ChooseGroup(modifier = Modifier.fillMaxSize())
        return
    }

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

        HorizontalPager(state = pagerState, modifier = Modifier.fillMaxWidth()) { pageIndex ->
            when (pageIndex) {
                0 -> ExpenseList(expenses = expenses, modifier = Modifier.fillMaxSize())
                1 -> AccountBalance(balances = balances, modifier = Modifier.fillMaxSize())
                else -> throw RuntimeException("Page index out of bounds! pageIndex=$pageIndex")
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
    val userBillingColor = when {
        billingBalance.finalBalance > 0.toMoney() -> Color(168, 213, 186)
        billingBalance.finalBalance < 0.toMoney() -> MaterialTheme.colorScheme.error
        else -> Color.Gray
    }

    Box(modifier = modifier) {
        Row(
            modifier = Modifier
                .padding(horizontal = 0.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Spacer(Modifier.requiredWidth(16.dp))

            AvatarIcon(user = billingBalance.user)

            Spacer(Modifier.requiredWidth(16.dp))

            Text(
                text = billingBalance.user.username,
                modifier = Modifier.weight(1f),
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )

            Spacer(Modifier.requiredWidth(16.dp))

            Text(
                text = billingBalance.finalBalance.toCurrency(),
                fontWeight = FontWeight.Bold,
                color = userBillingColor,
            )

            Spacer(Modifier.requiredWidth(8.dp))
        }
    }
}

@Composable
private fun ExpenseList(modifier: Modifier = Modifier, expenses: List<ExpenseModel>) {
    if (expenses.isEmpty()) {
        NoItems(modifier = modifier)
        return
    }

    LazyColumn(modifier = modifier) {
        items(expenses, key = { it.id }) { expense ->
            var isExpanded by rememberSaveable { mutableStateOf(false) }

            ExpenseItem(
                expense = expense,
                modifier = Modifier.fillMaxWidth(),
                isExpanded = isExpanded,
                onExpandedToggle = { isExpanded = !isExpanded },
            )
            HorizontalDivider()
        }
    }
}


@Composable
private fun ExpenseItem(
    modifier: Modifier = Modifier,
    expense: ExpenseModel,
    isExpanded: Boolean = false,
    onExpandedToggle: () -> Unit = {},
) {
    Column(
        modifier
            .animateContentSize()
            .clickable { onExpandedToggle() },
    ) {
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

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "${expense.title} • ${expense.creationTimestamp.inlineFormat()}",
                    maxLines = if (!isExpanded) 1 else Int.MAX_VALUE,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.titleMedium,
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(
                            R.string.paid,
                            expense.expensePayer.username,
                            expense.amount.toCurrency()
                        ),
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.bodyMedium
                    )

                    AnimatedVisibility(
                        visible = !isExpanded,
                        enter = fadeIn() + slideInHorizontally { it / 2 } + scaleIn(),
                        exit = fadeOut() + slideOutHorizontally(
                            targetOffsetX = { it },
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                stiffness = Spring.StiffnessLow
                            )
                        ) + shrinkHorizontally() + scaleOut()
                    ) {
                        UsersAvatar(
                            modifier = Modifier.padding(start = 8.dp),
                            users = expense.userExpenses.map { it.user },
                            avatarSize = 24.dp,
                        )
                    }
                }
            }

            Spacer(Modifier.requiredWidth(16.dp))

            Icon(
                if (!isExpanded) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                contentDescription = null,
                modifier = Modifier
                    .align(Alignment.CenterVertically)
                    .requiredSize(24.dp),
            )

            Spacer(Modifier.requiredWidth(16.dp))
        }

        if (isExpanded) {
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                HorizontalDivider()

                Spacer(Modifier.requiredHeight(8.dp))

                Text(
                    text = stringResource(R.string.expense_shares),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )

                expense.userExpenses.forEach { item ->
                    Row(
                        Modifier
                            .padding(vertical = 8.dp)
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        AvatarIcon(user = item.user, size = 24.dp)

                        Spacer(Modifier.requiredWidth(16.dp))

                        Text(
                            text = "${item.user.username}: ${item.partAmount.toCurrency()}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun NoItems(modifier: Modifier = Modifier) {
    val rotation = remember { Animatable(0f) }

    LaunchedEffect(Unit) {

        while (true) {
            delay(3000)
            rotation.animateTo(
                targetValue = rotation.value + 360f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                )
            )
        }
    }

    Box(
        modifier = modifier.padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Filled.Receipt,
                contentDescription = null,
                modifier = Modifier
                    .size(64.dp)
                    .graphicsLayer { rotationZ = rotation.value },
                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
            )

            Spacer(Modifier.height(24.dp))

            Text(
                stringResource(R.string.no_expenses_yet),
                style = MaterialTheme.typography.displaySmallEmphasized,
                textAlign = TextAlign.Center,
            )

            Spacer(Modifier.height(16.dp))

            Text(
                stringResource(R.string.add_an_expense_to_get_started),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Preview
@Composable
private fun NoExpensePreview() {
    HouseShare2Theme {
        Surface(modifier = Modifier.fillMaxSize()) {
            NoItems()
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ExpensesPreview() {
    val users = List(5) { UserModel.random().copy(id = it.toLong(), username = "User $it") }

    val e = listOf(
        ExpenseModel.random(
            id = 1,
            title = "Pizza night",
            amount = 30.0.toMoney(),
            expensePayer = users[0],
            userExpenses = users.take(3).map { UserExpenseModel(it, 10.0.toMoney()) }
        ),
        ExpenseModel.random(
            id = 2,
            title = "Electricity bill",
            amount = 54.20.toMoney(),
            expensePayer = users[1],
            userExpenses = listOf(
                UserExpenseModel(users[0], 27.10.toMoney()),
                UserExpenseModel(users[1], 27.10.toMoney())
            )
        ),
        ExpenseModel.random(
            id = 3,
            title = "Groceries",
            amount = 15.50.toMoney(),
            expensePayer = users[2],
            userExpenses = users.map { UserExpenseModel(it, 3.10.toMoney()) }
        ),
        ExpenseModel.random(
            id = 4,
            title = "Internet bill",
            amount = 29.99.toMoney(),
            expensePayer = users[0],
            userExpenses = listOf(
                UserExpenseModel(users[0], 15.0.toMoney()),
                UserExpenseModel(users[1], 14.99.toMoney())
            )
        )
    )

    HouseShare2Theme {
        Surface {
            ExpenseList(expenses = e, modifier = Modifier.fillMaxWidth())
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ExpenseItemPreview() {
    val alice = UserModel.default().copy(id = 1, username = "Alice")
    val bob = UserModel.default().copy(id = 2, username = "Bob")
    val charlie = UserModel.default().copy(id = 3, username = "Charlie")

    val expense = ExpenseModel.default().copy(
        id = 1,
        title = "Very looooooooooooooooooooooooooooooooooooooooooooooooooong title",
        amount = 100.0.toMoney(),
        expensePayer = alice,
        userExpenses = listOf(
            UserExpenseModel(alice, 33.33.toMoney()),
            UserExpenseModel(bob, 33.33.toMoney()),
            UserExpenseModel(charlie, 33.34.toMoney())
        )
    )

    HouseShare2Theme {
        Surface {
            ExpenseItem(expense = expense, modifier = Modifier.fillMaxWidth(), isExpanded = true)
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun AccountBalancePreview() {
    val balances = generateSequence { BillingBalanceModel.default() }.mapIndexed { i, b ->
        b.copy(
            user = b.user.copy(id = i.toLong()),
            finalBalance = when (i % 3) {
                0 -> 10.0
                1 -> -10.0
                else -> 0.0
            }.toMoney(),
        )
    }.take(6).toList()

    AccountBalance(balances = balances)
}
