package lol.terabrendon.houseshare2.presentation.home

import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.findViewTreeViewModelStoreOwner
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.launch
import lol.terabrendon.houseshare2.presentation.MainTopBar
import lol.terabrendon.houseshare2.presentation.billing.BillingScreen
import lol.terabrendon.houseshare2.presentation.billing.NewExpenseForm
import lol.terabrendon.houseshare2.presentation.cleaning.CleaningScreen
import lol.terabrendon.houseshare2.presentation.groups.GroupsScreen
import lol.terabrendon.houseshare2.presentation.groups.form.GroupInfoFormScreen
import lol.terabrendon.houseshare2.presentation.groups.form.GroupUsersFormScreen
import lol.terabrendon.houseshare2.presentation.navigation.GroupFormNavigation
import lol.terabrendon.houseshare2.presentation.navigation.MainNavigation
import lol.terabrendon.houseshare2.presentation.shopping.ShoppingItemForm
import lol.terabrendon.houseshare2.presentation.shopping.ShoppingScreen
import lol.terabrendon.houseshare2.presentation.vm.GroupFormViewModel
import lol.terabrendon.houseshare2.presentation.vm.MainViewModel
import lol.terabrendon.houseshare2.presentation.vm.ShoppingViewModel
import kotlin.reflect.KClass

private const val TAG = "HouseShareMain"

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
        // Update the current navigation in the datastore when the currentBackStack changes
        topLevelRoutes
            .firstOrNull { topLevelRoute ->
                currentBackStackDestination?.hierarchy?.any {
                    it.hasRoute(topLevelRoute::class)
                } == true
            }
            ?.let {
                setCurrentNavigation(it::class)
            }

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
                    // re-selecting the same item
                    launchSingleTop = true
                    // Restore state when re-selecting a previously selected item
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
                    composable<MainNavigation.Cleaning> {
                        CleaningScreen()
                        Text(text = "Cleaning")
                    }
                    composable<MainNavigation.Shopping> {
                        ShoppingScreen()
                    }
                    composable<MainNavigation.Billing> {
                        BillingScreen()
                    }
                    composable<MainNavigation.Groups> {
                        GroupsScreen()
                    }

                    navigation<MainNavigation.GroupForm>(startDestination = GroupFormNavigation.SelectUsers) {
                        composable<GroupFormNavigation.SelectUsers> { entry ->
                            val parentEntry =
                                remember(entry) { navController.getBackStackEntry<MainNavigation.GroupForm>() }
                            val viewModel = hiltViewModel<GroupFormViewModel>(parentEntry)
                            GroupUsersFormScreen(viewModel = viewModel)
                        }
                        composable<GroupFormNavigation.GroupInfo> { entry ->
                            val parentEntry =
                                remember(entry) { navController.getBackStackEntry<MainNavigation.GroupForm>() }
                            val viewModel = hiltViewModel<GroupFormViewModel>(parentEntry)
                            GroupInfoFormScreen(viewModel = viewModel)
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
                    GroupFormNavigation.GroupInfo -> TODO()
                    GroupFormNavigation.SelectUsers -> TODO()
                }
            }
        }
    }
}

