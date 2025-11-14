package lol.terabrendon.houseshare2.presentation.groups

import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.navigation3.runtime.EntryProviderScope
import lol.terabrendon.houseshare2.presentation.groups.form.GroupInfoFormScreen
import lol.terabrendon.houseshare2.presentation.groups.form.GroupUsersFormScreen
import lol.terabrendon.houseshare2.presentation.navigation.HomepageNavigation
import lol.terabrendon.houseshare2.presentation.navigation.MainNavigation
import lol.terabrendon.houseshare2.presentation.navigation.Navigator
import lol.terabrendon.houseshare2.presentation.util.TOP_LEVEL_TRANSITION
import lol.terabrendon.houseshare2.presentation.vm.GroupFormViewModel

fun EntryProviderScope<MainNavigation>.groupNavigation(
    navigator: Navigator<MainNavigation>,
) {
    var formStore: ViewModelStoreOwner? = null

    entry<HomepageNavigation.Groups>(
        metadata = TOP_LEVEL_TRANSITION,
    ) {
        GroupsScreen(navigate = navigator::navigate)
    }

    entry<HomepageNavigation.GroupUsersForm> {
        formStore = LocalViewModelStoreOwner.current
        GroupUsersFormScreen(
            onBack = { navigator.pop() },
            onNext = { navigator.navigate(HomepageNavigation.GroupInfoForm) },
        )
    }

    entry<HomepageNavigation.GroupInfoForm> {
        if (formStore == null) {
            navigator.pop()
            return@entry
        }

        val viewModel = hiltViewModel<GroupFormViewModel>(formStore!!)
        GroupInfoFormScreen(
            viewModel = viewModel,
            onBack = {
                navigator.pop()
            },
            onSubmit = {
                navigator.pop(elements = 2)
                formStore = null
            })
    }
}