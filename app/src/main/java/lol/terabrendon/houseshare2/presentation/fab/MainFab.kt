package lol.terabrendon.houseshare2.presentation.fab

import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddShoppingCart
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavBackStackEntry
import lol.terabrendon.houseshare2.R
import lol.terabrendon.houseshare2.presentation.navigation.ExpenseFormNavigation
import lol.terabrendon.houseshare2.presentation.navigation.GroupFormNavigation
import lol.terabrendon.houseshare2.presentation.navigation.HomepageNavigation
import lol.terabrendon.houseshare2.presentation.navigation.LoginNavigation
import lol.terabrendon.houseshare2.presentation.navigation.MainNavigation
import lol.terabrendon.houseshare2.presentation.navigation.ShoppingFormNavigation
import lol.terabrendon.houseshare2.presentation.util.currentRoute

@Composable
fun MainFab(
    modifier: Modifier = Modifier,
    currentEntry: NavBackStackEntry? = null,
    onClick: () -> Unit,
) {
    val mainDestination = currentEntry?.currentRoute() ?: MainNavigation.Loading

    AnimatedContent(
        mainDestination
    ) { mainDestination ->
        val fabExpanded = mainDestination.fabExpanded()
        val fabVisible = mainDestination.fabVisible()

        AnimatedVisibility(visible = fabVisible) {
            val fabIcon = mainDestination.fabIcon()
            val fabText = stringResource(mainDestination.fabText())

            ExtendedFloatingActionButton(
                modifier = modifier,
                text = { Text(fabText) },
                expanded = fabExpanded,
                icon = {
                    Icon(
                        imageVector = fabIcon,
                        contentDescription = fabText,
                    )
                },
                onClick = onClick,
            )
        }
    }
}

private fun MainNavigation.fabIcon(): ImageVector = when (this) {
    HomepageNavigation.Shopping -> Icons.Filled.AddShoppingCart
    HomepageNavigation.Cleaning -> Icons.Filled.Add
    HomepageNavigation.Billing -> Icons.Filled.Receipt
    MainNavigation.Loading -> Icons.Filled.Add
    HomepageNavigation.Groups -> Icons.Filled.Add
    HomepageNavigation.GroupForm -> Icons.Filled.Add
    GroupFormNavigation.SelectUsers -> Icons.AutoMirrored.Filled.ArrowForward
    GroupFormNavigation.GroupInfo -> Icons.Filled.Check
    ExpenseFormNavigation.Expense -> Icons.Filled.Check
    HomepageNavigation.ExpenseForm -> Icons.Filled.Add
    HomepageNavigation.ShoppingForm -> Icons.Filled.Add
    ShoppingFormNavigation.ShoppingItem -> Icons.Filled.Check
    LoginNavigation.UserLogin -> Icons.Filled.Add
    MainNavigation.Homepage -> Icons.Filled.Add
    MainNavigation.Login -> Icons.Filled.Add
}

@StringRes
private fun MainNavigation.fabText(): Int = when (this) {
    // TODO: change this
    else -> R.string.create
}

private fun MainNavigation.fabExpanded(): Boolean = when (this) {
    HomepageNavigation.Billing,
    HomepageNavigation.Cleaning,
    HomepageNavigation.GroupForm,
    HomepageNavigation.Groups,
    HomepageNavigation.Shopping -> true


    HomepageNavigation.ExpenseForm,
    HomepageNavigation.ShoppingForm,
    ExpenseFormNavigation.Expense,
    ShoppingFormNavigation.ShoppingItem,
    LoginNavigation.UserLogin,
    GroupFormNavigation.GroupInfo,
    GroupFormNavigation.SelectUsers,
    MainNavigation.Homepage,
    MainNavigation.Login,
    MainNavigation.Loading -> false
}

private fun MainNavigation.fabVisible(): Boolean = when (this) {
    HomepageNavigation.Billing,
    HomepageNavigation.Cleaning,
    HomepageNavigation.Shopping,
    HomepageNavigation.GroupForm,
    HomepageNavigation.Groups,
    GroupFormNavigation.GroupInfo,
    GroupFormNavigation.SelectUsers -> true


    MainNavigation.Login,
    MainNavigation.Homepage,
    MainNavigation.Loading,
    LoginNavigation.UserLogin,
    HomepageNavigation.ShoppingForm,
    ShoppingFormNavigation.ShoppingItem,
    HomepageNavigation.ExpenseForm,
    ExpenseFormNavigation.Expense -> false
}