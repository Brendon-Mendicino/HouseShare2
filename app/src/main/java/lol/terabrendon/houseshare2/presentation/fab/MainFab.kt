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
import lol.terabrendon.houseshare2.R
import lol.terabrendon.houseshare2.presentation.navigation.HomepageNavigation
import lol.terabrendon.houseshare2.presentation.navigation.MainNavigation

@Composable
fun MainFab(
    modifier: Modifier = Modifier,
    lastEntry: MainNavigation,
    onClick: () -> Unit,
) {
    AnimatedContent(
        lastEntry
    ) { lastEntry ->
        val fabExpanded = lastEntry.fabExpanded()
        val fabVisible = lastEntry.fabVisible()

        AnimatedVisibility(visible = fabVisible) {
            val fabIcon = lastEntry.fabIcon()
            val fabText = stringResource(lastEntry.fabText())

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
    is HomepageNavigation.Billing -> Icons.Filled.Receipt
    is HomepageNavigation.Groups -> Icons.Filled.Add
    is HomepageNavigation.GroupUsersForm -> Icons.AutoMirrored.Filled.ArrowForward
    is HomepageNavigation.GroupInfoForm -> Icons.Filled.Check
    is HomepageNavigation.ExpenseForm -> Icons.Filled.Check

    is HomepageNavigation.ShoppingForm,
    is HomepageNavigation.ShoppingItem,
    is MainNavigation.Login,
    is HomepageNavigation.Cleaning,
    is MainNavigation.Loading,
    is HomepageNavigation.UserProfile -> Icons.Filled.Add
}

@StringRes
private fun MainNavigation.fabText(): Int = when (this) {
    // TODO: change this
    else -> R.string.create
}

private fun MainNavigation.fabExpanded(): Boolean = when (this) {
    is HomepageNavigation.Billing,
    is HomepageNavigation.Cleaning,
    is HomepageNavigation.Groups,
    is HomepageNavigation.Shopping -> true


    is HomepageNavigation.ExpenseForm,
    is HomepageNavigation.GroupInfoForm,
    is HomepageNavigation.GroupUsersForm,
    is HomepageNavigation.ShoppingItem,
    is HomepageNavigation.ShoppingForm,
    is HomepageNavigation.UserProfile,
    is MainNavigation.Login,
    is MainNavigation.Loading -> false

}

private fun MainNavigation.fabVisible(): Boolean = when (this) {
    is HomepageNavigation.Billing,
    is HomepageNavigation.Cleaning,
    is HomepageNavigation.Shopping,
    is HomepageNavigation.Groups,
    is HomepageNavigation.GroupInfoForm,
    is HomepageNavigation.GroupUsersForm -> true


    is MainNavigation.Login,
    is MainNavigation.Loading,
    is HomepageNavigation.UserProfile,
    is HomepageNavigation.ExpenseForm,
    is HomepageNavigation.ShoppingForm,
    is HomepageNavigation.ShoppingItem -> false
}