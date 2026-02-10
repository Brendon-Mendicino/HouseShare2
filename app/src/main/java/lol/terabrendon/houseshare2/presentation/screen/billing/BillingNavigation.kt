package lol.terabrendon.houseshare2.presentation.screen.billing

import androidx.navigation3.runtime.EntryProviderScope
import lol.terabrendon.houseshare2.presentation.navigation.HomepageNavigation
import lol.terabrendon.houseshare2.presentation.navigation.MainNavigation
import lol.terabrendon.houseshare2.presentation.navigation.Navigator
import lol.terabrendon.houseshare2.presentation.util.TOP_LEVEL_TRANSITION
import timber.log.Timber

fun EntryProviderScope<MainNavigation>.billingNavigation(navigator: Navigator<MainNavigation>) {
    entry<HomepageNavigation.Billing>(
        metadata = TOP_LEVEL_TRANSITION
    ) {
        BillingScreen(navigate = navigator::navigate)
    }

    entry<HomepageNavigation.ExpenseForm> { entry ->
        NewExpenseForm(
            onFinish = {
                Timber.i("HouseShareMainInner: NewExpense form onFinish called.")
                navigator.pop()
            },
        )
    }
}