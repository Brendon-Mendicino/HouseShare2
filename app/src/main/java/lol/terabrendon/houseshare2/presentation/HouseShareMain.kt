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
import androidx.compose.material.icons.filled.Groups
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
import androidx.compose.material3.Surface
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
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.findViewTreeViewModelStoreOwner
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.dialog
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.launch
import lol.terabrendon.houseshare2.R
import lol.terabrendon.houseshare2.presentation.billing.BillingScreen
import lol.terabrendon.houseshare2.presentation.billing.NewExpenseForm
import lol.terabrendon.houseshare2.presentation.groups.GroupsScreen
import lol.terabrendon.houseshare2.presentation.navigation.MainNavigation
import lol.terabrendon.houseshare2.presentation.vm.MainViewModel
import lol.terabrendon.houseshare2.presentation.vm.ShoppingViewModel
import kotlin.reflect.KClass

private const val TAG = "HouseShareMain"

data class TopLevelRoute(
    @StringRes
    val name: Int,
    val route: MainNavigation,
    val icon: ImageVector,
)

@Composable
fun HouseShareMain(
    mainViewModel: MainViewModel = hiltViewModel()
) {
    val startingDestination by mainViewModel.startingDestination.collectAsStateWithLifecycle()
    val currentNavigation by mainViewModel.currentNavigation.collectAsStateWithLifecycle()
    val topLevelRoutes by mainViewModel.topLevelRoutes.collectAsStateWithLifecycle()

    HouseShareMainInner(
        startingDestination = startingDestination,
        currentNavigation = currentNavigation,
        topLevelRoutes = topLevelRoutes,
        setCurrentNavigation = mainViewModel::setCurrentNavigation,
        appBarActions = {
            AppBarActions(mainNavigation = currentNavigation)
        },
    )
}

@SuppressLint("RestrictedApi")
@Composable
private fun HouseShareMainInner(
    startingDestination: MainNavigation,
    currentNavigation: MainNavigation,
    topLevelRoutes: List<MainNavigation>,
    setCurrentNavigation: (KClass<out MainNavigation>) -> Unit,
    appBarActions: @Composable (MainNavigation) -> Unit,
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentBackStackDestination = navBackStackEntry?.destination

    val scope = rememberCoroutineScope()

    LaunchedEffect(currentBackStackDestination) {
        // TODO: check if previouisNavigation con be used and if the backstack is popped
//        if (currentNavigation != previousNavigation && previousNavigation::class != MainNavigation.Loading::class) {
//            navController.navigate(currentNavigation) {
//                popUpTo(previousNavigation::class) {
//                    inclusive = true
//                }
//            }
//        }

//        previousNavigation = currentNavigation

//        if (currentNavigation::class != MainNavigation.Loading::class)
//            navController.navigate(currentNavigation)

        topLevelRoutes
            .firstOrNull { topLevelRoute ->
                currentBackStackDestination?.hierarchy?.any {
                    it.hasRoute(topLevelRoute::class)
                } == true
            }
            ?.let {
                setCurrentNavigation(it::class)
            }

        Log.i(TAG, "HouseShareMainInner: ${navController.currentBackStack.value.toList()}")
        drawerState.close()
    }

    ModalNavigationDrawer(drawerState = drawerState, drawerContent = {
        MainDrawerSheet(
            topLevelRoutes = topLevelRoutes,
            itemSelected = { topLevelRoute ->
                currentBackStackDestination?.hierarchy?.any {
                    it.hasRoute(
                        topLevelRoute.route::class
                    )
                } == true
            },
            onItemClick = { topLevelRoute ->
                navController.navigate(topLevelRoute.route) {
                    // Pop up to the start destination of the graph to
                    // avoid building up a large stack of destinations
                    // on the back stack as users select items
                    popUpTo(navController.graph.findStartDestination().id) {
                        saveState = true
                        inclusive = true
                    }
                    // Avoid multiple copies of the same destination when
                    // reselecting the same item
                    launchSingleTop = true
                    // Restore state when reselecting a previously selected item
                    restoreState = true
                }
            },
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

                if (startingDestination::class == MainNavigation.Loading::class) {
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
                    startDestination = startingDestination,
                    modifier = Modifier.padding(contentPadding),
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
                    composable(route = MainNavigation.Groups::class) {
                        GroupsScreen()
                    }

                    dialog(
                        route = MainNavigation.GroupForm::class,
                        dialogProperties = DialogProperties(usePlatformDefaultWidth = false),
                    ) {
                        Surface(Modifier.fillMaxSize()) {
                            Text("skdjf")
                        }
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

                    is MainNavigation.Groups -> navController.navigate(MainNavigation.GroupForm)
                    is MainNavigation.GroupForm -> TODO()
                    is MainNavigation.Loading -> onBack()
                }
            }
        }
    }
}

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
    }

@Composable
private fun MainDrawerSheet(
    modifier: Modifier = Modifier,
    topLevelRoutes: List<MainNavigation>,
    itemSelected: (TopLevelRoute) -> Boolean,
    onItemClick: (TopLevelRoute) -> Unit,
) {
    val textPadding = PaddingValues(horizontal = 28.dp, vertical = 16.dp)
    val itemPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp)

    val mappedTopLevelRoutes = topLevelRoutes.map(::mapNavigationToRoute)

    ModalDrawerSheet(modifier) {
        Text(stringResource(R.string.house_activities), modifier = Modifier.padding(textPadding))

        mappedTopLevelRoutes.forEach { topLevelRoute ->
            NavigationDrawerItem(
                modifier = Modifier.padding(itemPadding),
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
