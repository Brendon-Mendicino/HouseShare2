package lol.terabrendon.houseshare2.presentation

import android.annotation.SuppressLint
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
import androidx.compose.runtime.rememberCoroutineScope
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
import lol.terabrendon.houseshare2.presentation.navigation.MainNavigation
import lol.terabrendon.houseshare2.presentation.vm.MainViewModel
import lol.terabrendon.houseshare2.presentation.vm.ShoppingViewModel
import kotlin.reflect.KClass

private const val TAG = "HouseShareMain"

@Composable
fun HouseShareMain(
    mainViewModel: MainViewModel = hiltViewModel()
) {
    val currentNavigation by mainViewModel.currentNavigation.collectAsStateWithLifecycle()

    HouseShareMainInner(
        currentNavigation = currentNavigation,
        setCurrentNavigation = mainViewModel::setCurrentNavigation,
        appBarActions = {
            AppBarActions(mainNavigation = currentNavigation)
        },
    )
}

@SuppressLint("RestrictedApi")
@Composable
private fun HouseShareMainInner(
    currentNavigation: MainNavigation,
    setCurrentNavigation: (KClass<out MainNavigation>) -> Unit,
    appBarActions: @Composable (MainNavigation) -> Unit,
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val navController = rememberNavController()
//    var previousNavigation by rememberSaveable {
//        mutableStateOf(currentNavigation)
//    }
    val scope = rememberCoroutineScope()

    LaunchedEffect(key1 = currentNavigation) {
        // TODO: check if previouisNavigation con be used and if the backstack is popped
//        if (currentNavigation != previousNavigation && previousNavigation::class != MainNavigation.Loading::class) {
//            navController.navigate(currentNavigation) {
//                popUpTo(previousNavigation::class) {
//                    inclusive = true
//                }
//            }
//        }

//        previousNavigation = currentNavigation

        if (currentNavigation::class != MainNavigation.Loading::class)
            navController.navigate(currentNavigation)

        Log.i(TAG, "HouseShareMainInner: ${navController.currentBackStack.value}")
        drawerState.close()
    }

    ModalNavigationDrawer(drawerState = drawerState, drawerContent = {
        MainDrawerSheet(
            currentNavigation = currentNavigation,
            onNavigationClick = { newDest -> setCurrentNavigation(newDest) }
        )
    }) {
        Box {
            Scaffold(
                topBar = {
                    MainTopBar(
                        mainNavigation = currentNavigation,
                        onNavigationClick = { scope.launch { drawerState.open() } },
                        actions = appBarActions
                    )
                },
                modifier = Modifier.fillMaxSize()
            ) { contentPadding ->

                if (currentNavigation::class == MainNavigation.Loading::class) {
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
                    startDestination = currentNavigation::class,
                    modifier = Modifier.padding(contentPadding)
                ) {
                    composable(route = MainNavigation.Cleaning::class) {
                        CleaningScreen()
                        Text(text = "Cleaning")
                    }
                    composable(route = MainNavigation.Shopping::class) {
                        ShoppingScreen()
                    }
                    composable(route = MainNavigation.Billing::class) {
                        BillingScreen()
                    }
                }
            }

            // TODO: refactor this mess...
            AnimatedFab(
                currentDestination = currentNavigation,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
            ) { onBack ->
                val shoppingViewModel: ShoppingViewModel =
                    hiltViewModel(LocalView.current.findViewTreeViewModelStoreOwner()!!)

                BackHandler {
                    onBack()
                }

                when (currentNavigation) {
                    is MainNavigation.Shopping -> Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        ShoppingItemForm(
                            onFinish = { item ->
                                shoppingViewModel.addShoppingItem(item)
                                onBack()
                            },
                            onBack = {
                                Log.i(
                                    TAG,
                                    "HouseShareMainInner: ShoppingItemForm form onFinish called."
                                )
                                onBack()
                            },
                            modifier = Modifier
                                .padding(4.dp)
                                .fillMaxSize()
                        )
                    }

                    is MainNavigation.Billing -> Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        NewExpenseForm(
                            onFinish = {
                                Log.i(TAG, "HouseShareMainInner: NewExpense form onFinish called.")
                                onBack()
                            },
                        )
                    }

                    is MainNavigation.Cleaning -> {}

                    is MainNavigation.Loading -> onBack()
                }
            }
        }
    }
}

@Composable
private fun MainDrawerSheet(
    modifier: Modifier = Modifier,
    onNavigationClick: (KClass<out MainNavigation>) -> Unit,
    currentNavigation: MainNavigation,
) {
    val textPadding = PaddingValues(horizontal = 28.dp, vertical = 16.dp)
    val itemPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp)

    ModalDrawerSheet(modifier) {
        Text(stringResource(R.string.house_activities), modifier = Modifier.padding(textPadding))

        NavigationDrawerItem(
            label = { Text(stringResource(R.string.cleaning)) },
            icon = { Icon(Icons.Filled.CleaningServices, contentDescription = null) },
            selected = currentNavigation::class == MainNavigation.Cleaning::class,
            onClick = { onNavigationClick(MainNavigation.Cleaning::class) },
            modifier = Modifier.padding(itemPadding)
        )
        NavigationDrawerItem(
            label = { Text(stringResource(R.string.shopping_list)) },
            icon = { Icon(Icons.Filled.ShoppingCart, contentDescription = null) },
            selected = currentNavigation::class == MainNavigation.Shopping::class,
            onClick = { onNavigationClick(MainNavigation.Shopping::class) },
            modifier = Modifier.padding(itemPadding)
        )
        NavigationDrawerItem(
            label = { Text(stringResource(R.string.billing)) },
            icon = { Icon(Icons.Filled.Payments, contentDescription = null) },
            selected = currentNavigation::class == MainNavigation.Billing::class,
            onClick = { onNavigationClick(MainNavigation.Billing::class) },
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
