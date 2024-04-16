package lol.terabrendon.houseshare2.presentation

import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
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
    val currentDestination by mainViewModel.currentDestination.collectAsStateWithLifecycle()

    HouseShareMainInner(
        currentDestination = currentDestination,
        setCurrentDestination = mainViewModel::setCurrentDestination,
        appBarActions = {
            AppBarActions(mainDestination = currentDestination)
        },
    )
}

@Composable
private fun HouseShareMainInner(
    currentDestination: MainDestination,
    setCurrentDestination: (MainDestination) -> Unit,
    appBarActions: @Composable (MainDestination) -> Unit,
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val navController = rememberNavController()
    var previousDestination by rememberSaveable {
        mutableStateOf(currentDestination)
    }
    val scope = rememberCoroutineScope()

    LaunchedEffect(key1 = currentDestination) {
        if (currentDestination != previousDestination && previousDestination != MainDestination.Loading) {
            navController.navigate(currentDestination.name) {
                popUpTo(previousDestination.name) {
                    inclusive = true
                }
            }
        }

        previousDestination = currentDestination

        drawerState.close()
    }

    ModalNavigationDrawer(drawerState = drawerState, drawerContent = {
        MainDrawerSheet(
            currentDestination = currentDestination,
            onDestinationClick = { newDest -> setCurrentDestination(newDest) }
        )
    }) {
        Box {
            Scaffold(
                topBar = {
                    MainTopBar(
                        mainDestination = currentDestination,
                        onNavigationClick = { scope.launch { drawerState.open() } },
                        actions = appBarActions
                    )
                },
                modifier = Modifier.fillMaxSize()
            ) { contentPadding ->

                if (currentDestination == MainDestination.Loading) {
                    Log.d(TAG, "HouseShareMain: starting loading screen")
                    // TODO: extract into splash screen
                    Box(
                        modifier = Modifier
                            .padding(contentPadding)
                            .fillMaxSize()
                    ) {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                    }

                    return@Scaffold
                }

                NavHost(
                    navController = navController,
                    startDestination = currentDestination.name,
                    modifier = Modifier.padding(contentPadding)
                ) {
                    composable(route = MainDestination.Cleaning.name) {
                        CleaningScreen()
                        Text(text = "Cleaning")
                    }
                    composable(route = MainDestination.Shopping.name) {
                        ShoppingScreen()
                    }
                }
            }

            AnimatedFab(
                currentDestination = currentDestination,
                modifier = Modifier.align(Alignment.BottomEnd)
            ) { onBack ->
                Surface(modifier = Modifier.fillMaxSize()) {
                    ShoppingItemForm(
                        onFinish = {},
                        onBack = { onBack() },
                        modifier = Modifier.fillMaxSize()
                    )
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

@Composable
fun MainFab(modifier: Modifier = Modifier, onClick: () -> Unit) {
    ExtendedFloatingActionButton(
        text = { Text(stringResource(R.string.create)) },
        icon = {
            Icon(
                imageVector = Icons.Filled.Add,
                contentDescription = stringResource(R.string.create)
            )
        },
        onClick = { onClick() },
        modifier = modifier
    )
}
