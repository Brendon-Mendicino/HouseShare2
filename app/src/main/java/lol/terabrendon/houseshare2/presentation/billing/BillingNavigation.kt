package lol.terabrendon.houseshare2.presentation.billing

import android.util.Log
import androidx.compose.runtime.remember
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import lol.terabrendon.houseshare2.presentation.navigation.ExpenseFormNavigation
import lol.terabrendon.houseshare2.presentation.navigation.HomepageNavigation
import lol.terabrendon.houseshare2.presentation.vm.NewExpenseFormViewModel

const val TAG = "BillingNavigation"

fun NavGraphBuilder.billingNavigation(navController: NavHostController) {
    composable<HomepageNavigation.Billing> {
        BillingScreen()
    }

    navigation<HomepageNavigation.ExpenseForm>(startDestination = ExpenseFormNavigation.Expense) {
        composable<ExpenseFormNavigation.Expense> { entry ->
            val parentEntry =
                remember(entry) { navController.getBackStackEntry<HomepageNavigation.ExpenseForm>() }
            val viewModel =
                hiltViewModel<NewExpenseFormViewModel>(parentEntry)

            NewExpenseForm(viewModel = viewModel, onFinish = {
                Log.i(
                    TAG,
                    "HouseShareMainInner: NewExpense form onFinish called."
                )
                navController.popBackStack<ExpenseFormNavigation.Expense>(
                    inclusive = true
                )
            })
        }
    }
}