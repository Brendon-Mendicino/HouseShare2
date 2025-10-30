package lol.terabrendon.houseshare2.presentation.home

//@Composable
//fun NavGraphBuilder.homepageNavigation(
//    modifier: Modifier = Modifier,
//    navController: NavHostController
//) {
//    navigation<MainNavigation.Homepage>(startDestination = ) {
//        composable {
//
//        }
//        ModalNavigationDrawer(drawerState = drawerState, drawerContent = {
//            MainDrawerSheet(
//                topLevelRoutes = topLevelRoutes,
//                itemSelected = { topLevelRoute ->
//                    currentBackStackDestination?.hierarchy?.any {
//                        it.hasRoute(
//                            topLevelRoute.route::class
//                        )
//                    } == true
//                },
//                onItemClick = { topLevelRoute ->
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
//                },
//            )
//        }) {
//            Box {
//                Scaffold(
//                    topBar = {
//                        MainTopBar(
//                            mainNavigation = currentNavigation,
//                            onNavigationClick = { scope.launch { drawerState.open() } },
//                        )
//                    },
//                    floatingActionButton = {
//                        MainFab(
//                            currentEntry = navBackStackEntry,
//                            onClick = {
//                                when (navBackStackEntry?.currentRoute()) {
//                                    null -> {}
//
//                                    is MainNavigation.Shopping -> navController.navigate(
//                                        MainNavigation.ShoppingForm
//                                    )
//
//                                    is MainNavigation.Groups -> navController.navigate(
//                                        GroupFormNavigation.SelectUsers
//                                    )
//
//                                    is MainNavigation.Billing -> navController.navigate(
//                                        MainNavigation.ExpenseForm
//                                    )
//
//                                    is GroupFormNavigation.SelectUsers -> navController.navigate(
//                                        GroupFormNavigation.GroupInfo
//                                    )
//
//                                    is GroupFormNavigation.GroupInfo -> fabActionManager.fabAction.value?.invoke()
//
//                                    else -> TODO()
//                                }
//                            },
//                        )
//                    },
//                    snackbarHost = {
//                        SnackbarHost(
//                            hostState = snackbarHostState,
//                        )
//                    },
//                    modifier = Modifier.fillMaxSize(),
//                ) { contentPadding ->
//
//                    if (startingDestination::class == MainNavigation.Loading::class) {
//                        Log.d(TAG, "HouseShareMain: starting loading screen")
//                        // TODO: extract into splash screen
//                        Box(
//                            modifier = Modifier
//                                .padding(contentPadding)
//                                .fillMaxSize()
//                        ) {
//                            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
//                        }
//
//                        return@Scaffold
//                    }
//
//                    cleaningNavigation()
//
//                    shoppingNavigation(navController)
//
//                    billingNavigation(navController)
//
//                    groupNavigation(navController)
//                }
//            }
//        }
//    }
//}