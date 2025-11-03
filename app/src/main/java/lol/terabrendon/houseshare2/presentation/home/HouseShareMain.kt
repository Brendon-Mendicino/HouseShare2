package lol.terabrendon.houseshare2.presentation.home

import android.util.Log
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import kotlinx.coroutines.launch
import lol.terabrendon.houseshare2.presentation.billing.billingNavigation
import lol.terabrendon.houseshare2.presentation.cleaning.cleaningNavigation
import lol.terabrendon.houseshare2.presentation.fab.MainFab
import lol.terabrendon.houseshare2.presentation.groups.groupNavigation
import lol.terabrendon.houseshare2.presentation.login.loginNavigation
import lol.terabrendon.houseshare2.presentation.navigation.HomepageNavigation
import lol.terabrendon.houseshare2.presentation.navigation.MainNavigation
import lol.terabrendon.houseshare2.presentation.navigation.Navigator
import lol.terabrendon.houseshare2.presentation.provider.FabActionManager
import lol.terabrendon.houseshare2.presentation.provider.LocalFabActionManagerProvider
import lol.terabrendon.houseshare2.presentation.provider.LocalMenuActionManagerProvider
import lol.terabrendon.houseshare2.presentation.provider.MenuActionManager
import lol.terabrendon.houseshare2.presentation.shopping.shoppingNavigation
import lol.terabrendon.houseshare2.presentation.util.SnackbarController
import lol.terabrendon.houseshare2.presentation.vm.MainViewModel
import lol.terabrendon.houseshare2.util.ObserveAsEvent

private const val TAG = "HouseShareMain"

@Composable
fun HouseShareMain(
    mainViewModel: MainViewModel = hiltViewModel()
) {
    val navigator = mainViewModel.navigator

    HouseShareMainInner(
        navigator = navigator,
        homepageRoutes = MainNavigation.homepageRoutes,
    )
}

@Composable
private fun HouseShareMainInner(
    homepageRoutes: List<MainNavigation>,
    navigator: Navigator<MainNavigation>,
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val snackbarHostState = remember { SnackbarHostState() }
    val backStack by navigator.backStack.collectAsStateWithLifecycle(listOf(MainNavigation.Loading))

    val scope = rememberCoroutineScope()

    LaunchedEffect(backStack) {
        drawerState.close()
    }

    ObserveAsEvent(SnackbarController.events, snackbarHostState) { event ->
        Log.i(TAG, "HouseShareMainInner: snackbar event received")

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

    MainProviders { fabActionManager, _ ->
        ModalNavigationDrawer(drawerState = drawerState, drawerContent = {
            MainDrawerSheet(
                topLevelRoutes = homepageRoutes,
                itemSelected = { topLevelRoute ->
                    backStack.asReversed().firstOrNull { topLevelRoute.route == it } != null
                },
                onItemClick = { topLevelRoute ->
                    navigator.navigate(topLevelRoute.route)
//                    navController.navigate(topLevelRoute.route) {
//                        // Pop up to the start destination of the graph to
//                        // avoid building up a large stack of destinations
//                        // on the back stack as users select items
//                        popUpTo(navController.graph.findStartDestination().id) {
//                            saveState = true
//                            inclusive = true
//                        }
//                        // Avoid multiple copies of the same destination when
//                        // re-selecting the same item
//                        launchSingleTop = true
//                        // Restore state when re-selecting a previously selected item
//                        restoreState = true
//                    }
                },
            )
        }) {
            Scaffold(
                topBar = {
                    MainTopBar(
                        mainNavigation = backStack.last(),
                        onNavigationClick = { scope.launch { drawerState.open() } },
                    )
                },
                floatingActionButton = {
                    MainFab(
                        lastEntry = backStack.last(),
                        onClick = {
                            when (backStack.last()) {
                                is HomepageNavigation.Shopping -> navigator.navigate(
                                    HomepageNavigation.ShoppingForm
                                )

                                is HomepageNavigation.Groups -> navigator.navigate(
                                    HomepageNavigation.GroupUsersForm
                                )

                                is HomepageNavigation.GroupUsersForm -> navigator.navigate(
                                    HomepageNavigation.GroupInfoForm
                                )

                                is HomepageNavigation.Billing -> navigator.navigate(
                                    HomepageNavigation.ExpenseForm
                                )

                                is HomepageNavigation.GroupInfoForm -> fabActionManager.fabAction.value?.invoke()

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
                NavDisplay(
                    modifier = Modifier.padding(contentPadding),
                    backStack = backStack,
                    onBack = { navigator.pop() },
                    entryDecorators = listOf(
                        rememberSaveableStateHolderNavEntryDecorator(),
                        rememberViewModelStoreNavEntryDecorator(),
                    ),
                    entryProvider = entryProvider {
                        entry<MainNavigation.Loading> {
                            Text("Loading...")
                        }

                        loginNavigation(navigator = navigator)

                        cleaningNavigation()

                        shoppingNavigation(navigator = navigator)

                        billingNavigation()

                        groupNavigation(navigator = navigator)
                    }
                )
            }
        }
    }
}

@Composable
private fun MainProviders(content: @Composable (FabActionManager, MenuActionManager) -> Unit) {
    LocalFabActionManagerProvider { fabActionManager ->
        LocalMenuActionManagerProvider { menuActionManager ->
            content(fabActionManager, menuActionManager)
        }
    }
}
