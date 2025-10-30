package lol.terabrendon.houseshare2.presentation.groups

import androidx.compose.runtime.remember
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import lol.terabrendon.houseshare2.presentation.groups.form.GroupInfoFormScreen
import lol.terabrendon.houseshare2.presentation.groups.form.GroupUsersFormScreen
import lol.terabrendon.houseshare2.presentation.navigation.GroupFormNavigation
import lol.terabrendon.houseshare2.presentation.navigation.HomepageNavigation
import lol.terabrendon.houseshare2.presentation.vm.GroupFormViewModel

fun NavGraphBuilder.groupNavigation(navController: NavHostController) {
    composable<HomepageNavigation.Groups> {
        GroupsScreen()
    }

    navigation<HomepageNavigation.GroupForm>(startDestination = GroupFormNavigation.SelectUsers) {
        composable<GroupFormNavigation.SelectUsers> { entry ->
            val parentEntry =
                remember(entry) { navController.getBackStackEntry<HomepageNavigation.GroupForm>() }
            val viewModel = hiltViewModel<GroupFormViewModel>(parentEntry)
            GroupUsersFormScreen(viewModel = viewModel)
        }
        composable<GroupFormNavigation.GroupInfo> { entry ->
            val parentEntry =
                remember(entry) { navController.getBackStackEntry<HomepageNavigation.GroupForm>() }
            val viewModel = hiltViewModel<GroupFormViewModel>(parentEntry)
            GroupInfoFormScreen(
                viewModel = viewModel,
                navController = navController
            )
        }
    }
}