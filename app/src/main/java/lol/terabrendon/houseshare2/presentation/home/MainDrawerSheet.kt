package lol.terabrendon.houseshare2.presentation.home

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import lol.terabrendon.houseshare2.R
import lol.terabrendon.houseshare2.presentation.navigation.ExpenseFormNavigation
import lol.terabrendon.houseshare2.presentation.navigation.GroupFormNavigation
import lol.terabrendon.houseshare2.presentation.navigation.MainNavigation
import lol.terabrendon.houseshare2.presentation.navigation.ShoppingFormNavigation

fun mapNavigationToRoute(navigation: MainNavigation): TopLevelRoute =
    when (navigation) {
        is MainNavigation.Cleaning -> TopLevelRoute(
            name = R.string.cleaning,
            route = navigation,
            icon = Icons.Filled.CleaningServices
        )

        is MainNavigation.Shopping -> TopLevelRoute(
            name = R.string.shopping_list,
            route = navigation,
            icon = Icons.Filled.ShoppingCart
        )

        is MainNavigation.Billing -> TopLevelRoute(
            name = R.string.billing,
            route = navigation,
            icon = Icons.Filled.Payments
        )

        is MainNavigation.Groups -> TopLevelRoute(
            name = R.string.groups,
            route = navigation,
            icon = Icons.Filled.Groups
        )

        is MainNavigation.Loading -> TODO()
        is MainNavigation.GroupForm -> TODO()
        is GroupFormNavigation.GroupInfo -> TODO()
        is GroupFormNavigation.SelectUsers -> TODO()
        is ExpenseFormNavigation.Expense -> TODO()
        is MainNavigation.ExpenseForm -> TODO()
        is MainNavigation.ShoppingForm -> TODO()
        is ShoppingFormNavigation.ShoppingItem -> TODO()
    }

@Composable
fun MainDrawerSheet(
    modifier: Modifier = Modifier,
    topLevelRoutes: List<MainNavigation>,
    itemSelected: (TopLevelRoute) -> Boolean = { false },
    onItemClick: (TopLevelRoute) -> Unit = {},
) {
    val textPadding = PaddingValues(horizontal = 28.dp, vertical = 16.dp)
    val itemPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp)

    val mappedTopLevelRoutes = topLevelRoutes.map(::mapNavigationToRoute)

    ModalDrawerSheet(modifier) {
        Text(
            stringResource(R.string.house_activities),
            modifier = Modifier.Companion.padding(textPadding)
        )

        mappedTopLevelRoutes.forEach { topLevelRoute ->
            NavigationDrawerItem(
                modifier = Modifier.Companion.padding(itemPadding),
                label = { Text(stringResource(topLevelRoute.name)) },
                icon = {
                    Icon(
                        topLevelRoute.icon,
                        contentDescription = stringResource(topLevelRoute.name)
                    )
                },
                selected = itemSelected(topLevelRoute),
                onClick = { onItemClick(topLevelRoute) },
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MainDrawerSheetPreview() {
    val topLevelRoutes = listOf(
        MainNavigation.Cleaning,
        MainNavigation.Shopping,
        MainNavigation.Billing,
        MainNavigation.Groups,
    )

    MainDrawerSheet(topLevelRoutes = topLevelRoutes)
}