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
    is HomepageNavigation.Shopping -> Icons.Filled.AddShoppingCart
    is HomepageNavigation.Cleaning -> Icons.Filled.Add
    is HomepageNavigation.Billing -> Icons.Filled.Receipt
    is MainNavigation.Loading -> Icons.Filled.Add
    is HomepageNavigation.Groups -> Icons.Filled.Add
    is HomepageNavigation.GroupForm -> Icons.Filled.Add
    is GroupFormNavigation.SelectUsers -> Icons.AutoMirrored.Filled.ArrowForward
    is GroupFormNavigation.GroupInfo -> Icons.Filled.Check
    is ExpenseFormNavigation.Expense -> Icons.Filled.Check
    is HomepageNavigation.ExpenseForm -> Icons.Filled.Add
    is HomepageNavigation.ShoppingForm -> Icons.Filled.Add
    is ShoppingFormNavigation.ShoppingItem -> Icons.Filled.Check
    is HomepageNavigation.ShoppingItem -> Icons.Filled.Add
    is LoginNavigation.UserLogin -> Icons.Filled.Add
    is MainNavigation.Homepage -> Icons.Filled.Add
    is MainNavigation.Login -> Icons.Filled.Add
}

@StringRes
private fun MainNavigation.fabText(): Int = when (this) {
    // TODO: change this
    else -> R.string.create
}

private fun MainNavigation.fabExpanded(): Boolean = when (this) {
    is HomepageNavigation.Billing,
    is HomepageNavigation.Cleaning,
    is HomepageNavigation.GroupForm,
    is HomepageNavigation.Groups,
    is HomepageNavigation.Shopping -> true


    is HomepageNavigation.ExpenseForm,
    is HomepageNavigation.ShoppingItem,
    is HomepageNavigation.ShoppingForm,
    is ExpenseFormNavigation.Expense,
    is ShoppingFormNavigation.ShoppingItem,
    is LoginNavigation.UserLogin,
    is GroupFormNavigation.GroupInfo,
    is GroupFormNavigation.SelectUsers,
    is MainNavigation.Homepage,
    is MainNavigation.Login,
    is MainNavigation.Loading -> false
}

private fun MainNavigation.fabVisible(): Boolean = when (this) {
    is HomepageNavigation.Billing,
    is HomepageNavigation.Cleaning,
    is HomepageNavigation.Shopping,
    is HomepageNavigation.GroupForm,
    is HomepageNavigation.Groups,
    is GroupFormNavigation.GroupInfo,
    is GroupFormNavigation.SelectUsers -> true


    is MainNavigation.Login,
    is MainNavigation.Homepage,
    is MainNavigation.Loading,
    is LoginNavigation.UserLogin,
    is ShoppingFormNavigation.ShoppingItem,
    is HomepageNavigation.ShoppingForm,
    is HomepageNavigation.ShoppingItem,
    is HomepageNavigation.ExpenseForm,
    is ExpenseFormNavigation.Expense -> false
}