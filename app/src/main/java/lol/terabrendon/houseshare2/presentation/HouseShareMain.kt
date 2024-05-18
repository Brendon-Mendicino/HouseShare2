package lol.terabrendon.houseshare2.presentation

import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.annotation.StringRes
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.findViewTreeViewModelStoreOwner
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.launch
import lol.terabrendon.houseshare2.R
import lol.terabrendon.houseshare2.presentation.billing.BillingScreen
import lol.terabrendon.houseshare2.presentation.billing.NewExpenseForm
import lol.terabrendon.houseshare2.presentation.navigation.MainDestination
import lol.terabrendon.houseshare2.presentation.vm.MainViewModel
import lol.terabrendon.houseshare2.presentation.vm.ShoppingViewModel

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
                    composable(route = MainDestination.Billing.name) {
                        BillingScreen()
                    }
                }
            }

            AnimatedFab(
                currentDestination = currentDestination,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
            ) { onBack ->
                val shoppingViewModel: ShoppingViewModel =
                    hiltViewModel(LocalView.current.findViewTreeViewModelStoreOwner()!!)

                BackHandler {
                    onBack()
                }

                when (currentDestination) {
                    MainDestination.Shopping -> Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        ShoppingItemForm(
                            onFinish = { item ->
                                shoppingViewModel.addShoppingItem(item)
                                onBack()
                            },
                            onBack = { onBack() },
                            modifier = Modifier
                                .padding(4.dp)
                                .fillMaxSize()
                        )
                    }

                    MainDestination.Billing -> Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        NewExpenseForm(
                            onFinish = { onBack() },
                        )
                    }

                    MainDestination.Cleaning -> {}

                    MainDestination.Loading -> onBack()
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
        NavigationDrawerItem(
            label = { Text(stringResource(R.string.billing)) },
            icon = { Icon(Icons.Filled.Payments, contentDescription = null) },
            selected = currentDestination == MainDestination.Billing,
            onClick = { onDestinationClick(MainDestination.Billing) },
            modifier = Modifier.padding(itemPadding)
        )
    }
}

@Composable
fun MainFab(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    @StringRes text: Int,
    onClick: () -> Unit
) {
    ExtendedFloatingActionButton(
        text = { Text(stringResource(text)) },
        icon = {
            Icon(
                imageVector = icon,
                contentDescription = stringResource(text)
            )
        },
        onClick = { onClick() },
        modifier = modifier.animateContentSize()
    )
}
