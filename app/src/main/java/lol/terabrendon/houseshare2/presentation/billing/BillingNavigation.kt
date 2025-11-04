package lol.terabrendon.houseshare2.presentation.billing

import android.util.Log
import androidx.navigation3.runtime.EntryProviderScope
import lol.terabrendon.houseshare2.presentation.navigation.HomepageNavigation
import lol.terabrendon.houseshare2.presentation.navigation.MainNavigation
import lol.terabrendon.houseshare2.presentation.navigation.Navigator

const val TAG = "BillingNavigation"

fun EntryProviderScope<MainNavigation>.billingNavigation(navigator: Navigator<MainNavigation>) {
    entry<HomepageNavigation.Billing> {
        BillingScreen()
    }

    entry<HomepageNavigation.ExpenseForm> { entry ->
        NewExpenseForm(
            onFinish = {
                Log.i(TAG, "HouseShareMainInner: NewExpense form onFinish called.")
                navigator.pop()
            },
        )
    }
}