package lol.terabrendon.houseshare2.presentation.billing

import android.util.Log
import androidx.navigation3.runtime.EntryProviderScope
import lol.terabrendon.houseshare2.presentation.navigation.HomepageNavigation
import lol.terabrendon.houseshare2.presentation.navigation.MainNavigation

const val TAG = "BillingNavigation"

fun EntryProviderScope<MainNavigation>.billingNavigation() {
    entry<HomepageNavigation.Billing> {
        BillingScreen()
    }

    entry<HomepageNavigation.ExpenseForm> { entry ->
        // TODO: fix later
//        val parentEntry =
//            remember(entry) { navController.getBackStackEntry<HomepageNavigation.ExpenseForm>() }
//        val viewModel =
//            hiltViewModel<NewExpenseFormViewModel>(parentEntry)

        NewExpenseForm(onFinish = {
            Log.i(
                TAG,
                "HouseShareMainInner: NewExpense form onFinish called."
            )
//            navController.popBackStack<ExpenseFormNavigation.Expense>(
//                inclusive = true
//            )
        })
    }
}