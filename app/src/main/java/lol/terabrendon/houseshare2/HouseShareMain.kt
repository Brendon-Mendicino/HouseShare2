package lol.terabrendon.houseshare2

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.launch
import lol.terabrendon.houseshare2.ui.CleaningScreen
import lol.terabrendon.houseshare2.ui.MainTopBar
import lol.terabrendon.houseshare2.ui.navigation.MainDestination

@Composable
fun HouseShareMain() {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val navController = rememberNavController()
    val backStackEntry = navController.currentBackStackEntryAsState()
    val scope = rememberCoroutineScope()
    val currentDestination = MainDestination.valueOf(
        backStackEntry.value?.destination?.route ?: MainDestination.Cleaning.name
    )

    ModalNavigationDrawer(drawerState = drawerState, drawerContent = {
        MainDrawerSheet(
            currentDestination = currentDestination,
            onDestinationClick = {
                navController.navigate(it.name)
                scope.launch { drawerState.close() }
            }
        )
    }) {
        Scaffold(
            topBar = {
                MainTopBar(onNavigationClick = { scope.launch { drawerState.open() } })
            }
        ) { contentPadding ->
            NavHost(
                navController = navController,
                startDestination = MainDestination.Cleaning.name,
                modifier = Modifier.padding(contentPadding)
            ) {
                composable(route = MainDestination.Cleaning.name) {
                    CleaningScreen()
                    Text(text = "AAAAAAAAAAAAAAAAAAAAa")
                }
                composable(route = MainDestination.Shopping.name) {
                    Text(text = "alksdj")
                }
            }
        }
    }
}

@Composable
private fun MainDrawerSheet(
    modifier: Modifier = Modifier,
    onDestinationClick: (MainDestination) -> Unit,
    currentDestination: MainDestination,
) {
    val textPadding = PaddingValues(horizontal = 28.dp, vertical = 16.dp)
    val itemPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp)

    ModalDrawerSheet(modifier) {
        Text(stringResource(R.string.house_activities), modifier = Modifier.padding(textPadding))

        NavigationDrawerItem(
            label = { Text(stringResource(R.string.cleaning)) },
            icon = { Icon(Icons.Filled.CleaningServices, contentDescription = null) },
            selected = currentDestination == MainDestination.Cleaning,
            onClick = { onDestinationClick(MainDestination.Cleaning) },
            modifier = Modifier.padding(itemPadding)
        )
        NavigationDrawerItem(
            label = { Text(stringResource(R.string.shopping)) },
            icon = { Icon(Icons.Filled.ShoppingCart, contentDescription = null) },
            selected = currentDestination == MainDestination.Shopping,
            onClick = { onDestinationClick(MainDestination.Shopping) },
            modifier = Modifier.padding(itemPadding)
        )
    }
}