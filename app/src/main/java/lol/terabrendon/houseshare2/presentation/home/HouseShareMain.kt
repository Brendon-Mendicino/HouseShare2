package lol.terabrendon.houseshare2.presentation.home

import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
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
import lol.terabrendon.houseshare2.presentation.fab.MainFab
import lol.terabrendon.houseshare2.presentation.groups.GroupsScreen
import lol.terabrendon.houseshare2.presentation.groups.form.GroupInfoFormScreen
import lol.terabrendon.houseshare2.presentation.groups.form.GroupUsersFormScreen
import lol.terabrendon.houseshare2.presentation.navigation.ExpenseFormNavigation
import lol.terabrendon.houseshare2.presentation.navigation.GroupFormNavigation
import lol.terabrendon.houseshare2.presentation.navigation.MainNavigation
import lol.terabrendon.houseshare2.presentation.navigation.ShoppingFormNavigation
import lol.terabrendon.houseshare2.presentation.provider.LocalFabActionManagerProvider
import lol.terabrendon.houseshare2.presentation.shopping.ShoppingScreen
import lol.terabrendon.houseshare2.presentation.shopping.form.ShoppingItemFormScreen
import lol.terabrendon.houseshare2.presentation.util.SnackbarController
import lol.terabrendon.houseshare2.presentation.util.currentRoute
import lol.terabrendon.houseshare2.presentation.vm.GroupFormViewModel
import lol.terabrendon.houseshare2.presentation.vm.MainViewModel
import lol.terabrendon.houseshare2.presentation.vm.NewExpenseFormViewModel
import lol.terabrendon.houseshare2.util.ObserveAsEvent
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
    val snackbarHostState = remember { SnackbarHostState() }

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

    ObserveAsEvent(SnackbarController.events, snackbarHostState) { event ->
        scope.launch {
            snackbarHostState.currentSnackbarData?.dismiss()

            val result = snackbarHostState.showSnackbar(
                message = event.message,
                actionLabel = event.action?.name,
                duration = event.duration,
            )

            if (result == SnackbarResult.ActionPerformed) {
                event.action?.action?.invoke()
            }
        }
    }

    LocalFabActionManagerProvider { fabActionManager ->
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
                    floatingActionButton = {
                        MainFab(
                            currentEntry = navBackStackEntry,
                            onClick = {
                                when (navBackStackEntry?.currentRoute()) {
                                    null -> {}

                                    is MainNavigation.Shopping -> navController.navigate(
                                        MainNavigation.ShoppingForm
                                    )

                                    is MainNavigation.Groups -> navController.navigate(
                                        GroupFormNavigation.SelectUsers
                                    )

                                    is MainNavigation.Billing -> navController.navigate(
                                        MainNavigation.ExpenseForm
                                    )

                                    is GroupFormNavigation.SelectUsers -> navController.navigate(
                                        GroupFormNavigation.GroupInfo
                                    )

                                    is GroupFormNavigation.GroupInfo -> fabActionManager.fabAction.value?.invoke()

                                    else -> TODO()
                                }
                            },
                        )
                    },
                    snackbarHost = {
                        SnackbarHost(
                            hostState = snackbarHostState,
                        )
                    },
                    modifier = Modifier.fillMaxSize(),
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
                        navigation<MainNavigation.ShoppingForm>(startDestination = ShoppingFormNavigation.ShoppingItem) {
                            composable<ShoppingFormNavigation.ShoppingItem> {
                                ShoppingItemFormScreen(navController = navController)
                            }
                        }

                        composable<MainNavigation.Billing> {
                            BillingScreen()
                        }
                        navigation<MainNavigation.ExpenseForm>(startDestination = ExpenseFormNavigation.Expense) {
                            composable<ExpenseFormNavigation.Expense> { entry ->
                                val parentEntry =
                                    remember(entry) { navController.getBackStackEntry<MainNavigation.ExpenseForm>() }
                                val viewModel = hiltViewModel<NewExpenseFormViewModel>(parentEntry)
                                NewExpenseForm(viewModel = viewModel, onFinish = {
                                    Log.i(
                                        TAG,
                                        "HouseShareMainInner: NewExpense form onFinish called."
                                    )
                                    navController.popBackStack<ExpenseFormNavigation.Expense>(
                                        inclusive = true
                                    )
                                })
                            }
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
                                GroupInfoFormScreen(
                                    viewModel = viewModel,
                                    navController = navController
                                )
                            }
                        }
                    }
                }
            }

//            // TODO: refactor this mess...
//            AnimatedFab(
//                currentDestination = currentNavigation,
//                modifier = Modifier
//                    .align(Alignment.BottomEnd)
//            ) { onBack ->
//                val shoppingViewModel: ShoppingViewModel =
//                    hiltViewModel(LocalView.current.findViewTreeViewModelStoreOwner()!!)
//
//                BackHandler {
//                    onBack()
//                }
//
//                when (currentNavigation) {
//                    is MainNavigation.Shopping -> Card(
//                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background),
//                        modifier = Modifier.fillMaxSize()
//                    ) {
//                        ShoppingItemForm(
//                            onFinish = { item ->
//                                shoppingViewModel.addShoppingItem(item)
//                                onBack()
//                            },
//                            onBack = {
//                                Log.i(
//                                    TAG,
//                                    "HouseShareMainInner: ShoppingItemForm form onFinish called."
//                                )
//                                onBack()
//                            },
//                            modifier = Modifier
//                                .padding(4.dp)
//                                .fillMaxSize()
//                        )
//                    }
//
//                    is MainNavigation.Billing -> Card(
//                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background),
//                        modifier = Modifier.fillMaxSize()
//                    ) {
//                        NewExpenseForm(
//                            onFinish = {
//                                Log.i(TAG, "HouseShareMainInner: NewExpense form onFinish called.")
//                                onBack()
//                            },
//                        )
//                    }
//
//                    is MainNavigation.Cleaning -> {}
//
//                    is MainNavigation.Groups -> navController.navigate(MainNavigation.GroupForm)
//                    is MainNavigation.GroupForm -> TODO()
//                    is MainNavigation.Loading -> onBack()
//                    GroupFormNavigation.GroupInfo -> TODO()
//                    GroupFormNavigation.SelectUsers -> TODO()
//                }
//            }
        }
    }
}

