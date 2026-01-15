package lol.terabrendon.houseshare2.presentation.home

import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.IntOffset
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import kotlinx.coroutines.launch
import lol.terabrendon.houseshare2.presentation.billing.billingNavigation
import lol.terabrendon.houseshare2.presentation.cleaning.cleaningNavigation
import lol.terabrendon.houseshare2.presentation.components.LoadingOverlayScreen
import lol.terabrendon.houseshare2.presentation.fab.MainFab
import lol.terabrendon.houseshare2.presentation.groups.groupNavigation
import lol.terabrendon.houseshare2.presentation.login.loginNavigation
import lol.terabrendon.houseshare2.presentation.navigation.MainNavigation
import lol.terabrendon.houseshare2.presentation.navigation.Navigator
import lol.terabrendon.houseshare2.presentation.provider.LocalBackStackProvider
import lol.terabrendon.houseshare2.presentation.provider.LocalFabManagerProvider
import lol.terabrendon.houseshare2.presentation.provider.LocalMenuActionManagerProvider
import lol.terabrendon.houseshare2.presentation.provider.LocalTopBarManagerProvider
import lol.terabrendon.houseshare2.presentation.provider.RegisterTopBarConfig
import lol.terabrendon.houseshare2.presentation.provider.TopBarConfig
import lol.terabrendon.houseshare2.presentation.settings.settingsNavigation
import lol.terabrendon.houseshare2.presentation.shopping.shoppingNavigation
import lol.terabrendon.houseshare2.presentation.user.userNavigation
import lol.terabrendon.houseshare2.presentation.util.SnackbarController
import lol.terabrendon.houseshare2.presentation.vm.MainViewModel
import lol.terabrendon.houseshare2.util.ObserveAsEvent
import timber.log.Timber

@Composable
fun HouseShareMain(
    mainViewModel: MainViewModel = hiltViewModel(),
) {
    val navigator = mainViewModel.navigator

    HouseShareMainInner(
        navigator = navigator,
    )
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun HouseShareMainInner(
    navigator: Navigator<MainNavigation>,
) {
    val context = LocalContext.current
    val slideSpec = MaterialTheme.motionScheme.slowSpatialSpec<IntOffset>()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val snackbarHostState = remember { SnackbarHostState() }
    val backStack by navigator.backStack.collectAsStateWithLifecycle(listOf(MainNavigation.Loading))

    val scope = rememberCoroutineScope()

    LaunchedEffect(backStack) {
        drawerState.close()
    }

    ObserveAsEvent(SnackbarController.events, snackbarHostState) { event ->
        Timber.i("HouseShareMainInner: snackbar event received")

        scope.launch {
            snackbarHostState.currentSnackbarData?.dismiss()

            val result = snackbarHostState.showSnackbar(
                message = event.message.text(context),
                actionLabel = event.action?.name?.text(context),
                duration = event.duration,
            )

            if (result == SnackbarResult.ActionPerformed) {
                event.action?.action?.invoke()
            }
        }
    }

    MainProviders(backStack) {
        ModalNavigationDrawer(
            drawerState = drawerState,
            gesturesEnabled = drawerState.isOpen,
            drawerContent = {
                MainDrawerSheet(
                    itemSelected = { topLevelRoute ->
                        backStack.asReversed().firstOrNull { topLevelRoute.route == it } != null
                    },
                    onItemClick = { topLevelRoute ->
                        navigator.navigate(topLevelRoute.route)
                    },
                )
            },
        ) {
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
                            RegisterTopBarConfig<MainNavigation.Loading>(
                                config = TopBarConfig(navigationIcon = {})
                            )

                            LoadingOverlayScreen()
                        }

                        loginNavigation(navigator = navigator)

                        cleaningNavigation()

                        shoppingNavigation(navigator = navigator)

                        billingNavigation(navigator = navigator)

                        groupNavigation(navigator = navigator)

                        userNavigation(navigator = navigator)

                        settingsNavigation(navigator = navigator)
                    },
                    transitionSpec = {
                        // Slide in from right when navigating forward
                        slideInHorizontally(
                            animationSpec = slideSpec,
                            initialOffsetX = { it }) togetherWith
                                slideOutHorizontally(
                                    animationSpec = slideSpec,
                                    targetOffsetX = { -it })
                    },
                    popTransitionSpec = {
                        // Slide in from left when navigating back
                        slideInHorizontally(
                            animationSpec = slideSpec,
                            initialOffsetX = { -it }) togetherWith
                                slideOutHorizontally(
                                    animationSpec = slideSpec,
                                    targetOffsetX = { it })
                    },
                    predictivePopTransitionSpec = {
                        // Slide in from left when navigating back
                        slideInHorizontally(
                            animationSpec = slideSpec,
                            initialOffsetX = { -it }) togetherWith
                                slideOutHorizontally(
                                    animationSpec = slideSpec,
                                    targetOffsetX = { it })
                    },
                )
            }
        }
    }
}

@Composable
private fun MainProviders(
    backStack: List<MainNavigation>,
    content: @Composable () -> Unit,
) {
    LocalFabManagerProvider {
        LocalMenuActionManagerProvider {
            LocalTopBarManagerProvider {
                LocalBackStackProvider(backStack) {
                    content()
                }
            }
        }
    }
}
