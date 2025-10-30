package lol.terabrendon.houseshare2.presentation.shopping

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import lol.terabrendon.houseshare2.presentation.navigation.HomepageNavigation
import lol.terabrendon.houseshare2.presentation.navigation.ShoppingFormNavigation
import lol.terabrendon.houseshare2.presentation.shopping.form.ShoppingItemFormScreen

fun NavGraphBuilder.shoppingNavigation(navController: NavHostController) {
    composable<HomepageNavigation.Shopping> {
        ShoppingScreen()
    }

    navigation<HomepageNavigation.ShoppingForm>(startDestination = ShoppingFormNavigation.ShoppingItem) {
        composable<ShoppingFormNavigation.ShoppingItem> {
            ShoppingItemFormScreen(navController = navController)
        }
    }
}