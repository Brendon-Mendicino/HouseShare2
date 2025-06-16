package lol.terabrendon.houseshare2.presentation.home

import androidx.annotation.StringRes
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
import lol.terabrendon.houseshare2.presentation.navigation.GroupFormNavigation
import lol.terabrendon.houseshare2.presentation.navigation.MainNavigation
import lol.terabrendon.houseshare2.presentation.util.currentRoute

@Composable
fun MainFab(
    modifier: Modifier = Modifier,
    currentEntry: NavBackStackEntry? = null,
    onClick: () -> Unit,
) {
    val mainDestination = currentEntry?.currentRoute() ?: MainNavigation.Loading

    val fabIcon = mainDestination.fabIcon()
    val fabText = stringResource(mainDestination.fabText())
    val fabExpanded = mainDestination.fabExpanded()

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

private fun MainNavigation.fabIcon(): ImageVector = when (this) {
    is MainNavigation.Shopping -> Icons.Filled.AddShoppingCart
    is MainNavigation.Cleaning -> Icons.Filled.Add
    is MainNavigation.Billing -> Icons.Filled.Receipt
    is MainNavigation.Loading -> Icons.Filled.Add
    is MainNavigation.Groups -> Icons.Filled.Add
    is MainNavigation.GroupForm -> TODO()
    is GroupFormNavigation.SelectUsers -> Icons.AutoMirrored.Filled.ArrowForward
    is GroupFormNavigation.GroupInfo -> Icons.Filled.Check
}

@StringRes
private fun MainNavigation.fabText(): Int = when (this) {
    // TODO: change this
    else -> R.string.create
}

private fun MainNavigation.fabExpanded(): Boolean = when (this) {
    GroupFormNavigation.GroupInfo -> false
    GroupFormNavigation.SelectUsers -> false
    MainNavigation.Billing -> true
    MainNavigation.Cleaning -> true
    MainNavigation.GroupForm -> true
    is MainNavigation.Groups -> true
    MainNavigation.Loading -> false
    MainNavigation.Shopping -> true
}