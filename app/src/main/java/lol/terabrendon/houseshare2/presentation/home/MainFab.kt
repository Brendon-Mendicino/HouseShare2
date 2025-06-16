package lol.terabrendon.houseshare2.presentation.home

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddShoppingCart
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import lol.terabrendon.houseshare2.R
import lol.terabrendon.houseshare2.presentation.navigation.GroupFormNavigation
import lol.terabrendon.houseshare2.presentation.navigation.MainNavigation

@Composable
fun MainFab(
    modifier: Modifier = Modifier,
    currentDestination: MainNavigation,
    onClick: () -> Unit,
) {
    val fabIcon = currentDestination.fabIcon()
    val fabText = stringResource(currentDestination.fabText())

    ExtendedFloatingActionButton(
        modifier = modifier,
        text = { Text(fabText) },
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
    is GroupFormNavigation.GroupInfo -> TODO()
    is GroupFormNavigation.SelectUsers -> TODO()
}

@StringRes
private fun MainNavigation.fabText(): Int = when (this) {
    // TODO: change this
    else -> R.string.create
}