package lol.terabrendon.houseshare2.presentation.shopping

import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation3.runtime.EntryProviderScope
import lol.terabrendon.houseshare2.presentation.navigation.HomepageNavigation
import lol.terabrendon.houseshare2.presentation.navigation.MainNavigation
import lol.terabrendon.houseshare2.presentation.navigation.Navigator
import lol.terabrendon.houseshare2.presentation.shopping.form.ShoppingItemFormScreen
import lol.terabrendon.houseshare2.presentation.vm.ShoppingSingleViewModel

fun EntryProviderScope<MainNavigation>.shoppingNavigation(navigator: Navigator<MainNavigation>) {
    entry<HomepageNavigation.Shopping> {
        ShoppingScreen(navigate = navigator::navigate)
    }

    entry<HomepageNavigation.ShoppingItem> { key ->
        // Factory for viewModels with route bind dependencies
        val viewModel =
            hiltViewModel<ShoppingSingleViewModel, ShoppingSingleViewModel.Factory>(creationCallback = { factory ->
                factory.create(key)
            })
        ShoppingItemScreen(viewModel = viewModel)
    }

    entry<HomepageNavigation.ShoppingForm> {
        ShoppingItemFormScreen(onBack = navigator::pop)
    }
}