package lol.terabrendon.houseshare2.presentation.home

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.HorizontalDivider
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
import lol.terabrendon.houseshare2.presentation.navigation.HomepageNavigation.Billing
import lol.terabrendon.houseshare2.presentation.navigation.HomepageNavigation.Cleaning
import lol.terabrendon.houseshare2.presentation.navigation.HomepageNavigation.Groups
import lol.terabrendon.houseshare2.presentation.navigation.HomepageNavigation.Shopping
import lol.terabrendon.houseshare2.presentation.navigation.HomepageNavigation.UserProfile
import lol.terabrendon.houseshare2.presentation.navigation.MainNavigation

private fun MainNavigation.toDrawer(): TopLevelRoute =
    when (this) {
        is Cleaning -> TopLevelRoute(
            name = R.string.cleaning,
            route = this,
            icon = Icons.Filled.CleaningServices
        )

        is Shopping -> TopLevelRoute(
            name = R.string.shopping_list,
            route = this,
            icon = Icons.Filled.ShoppingCart
        )

        is Billing -> TopLevelRoute(
            name = R.string.billing,
            route = this,
            icon = Icons.Filled.Payments
        )

        is Groups -> TopLevelRoute(
            name = R.string.groups,
            route = this,
            icon = Icons.Filled.Groups
        )

        is UserProfile -> TopLevelRoute(
            name = R.string.profile,
            route = this,
            icon = Icons.Filled.Person,
        )

        else -> throw IllegalStateException("Destination is not a top level route! destination=$this")
    }

@Composable
fun MainDrawerSheet(
    modifier: Modifier = Modifier,
    itemSelected: (TopLevelRoute) -> Boolean = { false },
    onItemClick: (TopLevelRoute) -> Unit = {},
) {
    val textPadding = PaddingValues(horizontal = 28.dp, vertical = 16.dp)

    val firstSection = listOf(
        Groups,
        Shopping,
        Billing,
        Cleaning,
    )

    ModalDrawerSheet(modifier) {
        Text(
            stringResource(R.string.house_activities),
            modifier = Modifier.padding(textPadding)
        )

        firstSection.forEach {
            DrawerItem(
                route = it.toDrawer(),
                itemSelected = itemSelected,
                onItemClick = onItemClick
            )
        }

        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))


        DrawerItem(
            route = UserProfile.toDrawer(),
            itemSelected = itemSelected,
            onItemClick = onItemClick
        )
    }
}

@Composable
private fun DrawerItem(
    route: TopLevelRoute,
    itemSelected: (TopLevelRoute) -> Boolean,
    onItemClick: (TopLevelRoute) -> Unit,
    modifier: Modifier = Modifier,
) {
    val itemPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp)

    NavigationDrawerItem(
        modifier = modifier.padding(itemPadding),
        label = { Text(stringResource(route.name)) },
        icon = {
            Icon(
                route.icon,
                contentDescription = stringResource(route.name)
            )
        },
        selected = itemSelected(route),
        onClick = { onItemClick(route) },
    )
}

@Preview(showBackground = true)
@Composable
private fun MainDrawerSheetPreview() {
    MainDrawerSheet()
}