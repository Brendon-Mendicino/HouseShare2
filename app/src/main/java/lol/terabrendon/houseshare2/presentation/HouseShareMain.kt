package lol.terabrendon.houseshare2.presentation

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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.launch
import lol.terabrendon.houseshare2.R
import lol.terabrendon.houseshare2.presentation.navigation.MainDestination
import lol.terabrendon.houseshare2.presentation.vm.MainViewModel

private const val TAG = "HouseShareMain"

@Composable
fun HouseShareMain(
) {
    val mainViewModel: MainViewModel = hiltViewModel()

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val navController = rememberNavController()
    val currentDestination by mainViewModel.currentDestination.collectAsState()
    val previousDestination by mainViewModel.previousDestination.collectAsState()

    val scope = rememberCoroutineScope()

    LaunchedEffect(key1 = currentDestination) {
        val prev = previousDestination ?: return@LaunchedEffect

        navController.navigate(currentDestination.name) {
            popUpTo(prev.name) {
                inclusive = true
            }
        }

        drawerState.close()
    }

    ModalNavigationDrawer(drawerState = drawerState, drawerContent = {
        MainDrawerSheet(
            currentDestination = currentDestination,
            onDestinationClick = { newDest -> mainViewModel.setCurrentDestination(newDest) }
        )
    }) {
        Scaffold(
            topBar = {
                MainTopBar(
                    mainDestination = currentDestination,
                    onNavigationClick = { scope.launch { drawerState.open() } })
            }
        ) { contentPadding ->
            NavHost(
                navController = navController,
                startDestination = MainDestination.Cleaning.name,
                modifier = Modifier.padding(contentPadding)
            ) {
                composable(route = MainDestination.Cleaning.name) {
                    CleaningScreen()
                    Text(text = "Cleaning")
                }
                composable(route = MainDestination.Shopping.name) {
                    Text(text = "Shopping")
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
            label = { Text(stringResource(R.string.shopping_list)) },
            icon = { Icon(Icons.Filled.ShoppingCart, contentDescription = null) },
            selected = currentDestination == MainDestination.Shopping,
            onClick = { onDestinationClick(MainDestination.Shopping) },
            modifier = Modifier.padding(itemPadding)
        )
    }
}