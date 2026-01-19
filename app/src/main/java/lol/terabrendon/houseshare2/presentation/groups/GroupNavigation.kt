package lol.terabrendon.houseshare2.presentation.groups

import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation3.runtime.EntryProviderScope
import lol.terabrendon.houseshare2.presentation.groups.form.GroupInfoFormScreen
import lol.terabrendon.houseshare2.presentation.groups.form.GroupUsersFormScreen
import lol.terabrendon.houseshare2.presentation.navigation.HomepageNavigation
import lol.terabrendon.houseshare2.presentation.navigation.MainNavigation
import lol.terabrendon.houseshare2.presentation.navigation.Navigator
import lol.terabrendon.houseshare2.presentation.util.TOP_LEVEL_TRANSITION
import lol.terabrendon.houseshare2.presentation.util.contentKey
import lol.terabrendon.houseshare2.presentation.util.parent
import lol.terabrendon.houseshare2.presentation.vm.GroupInfoViewModel

fun EntryProviderScope<MainNavigation>.groupNavigation(
    navigator: Navigator<MainNavigation>,
) {
    entry<HomepageNavigation.Groups>(
        metadata = TOP_LEVEL_TRANSITION,
    ) {
        GroupsScreen(navigate = navigator::navigate)
    }

    entry<HomepageNavigation.GroupInfo> { key ->
        val vm =
            hiltViewModel<GroupInfoViewModel, GroupInfoViewModel.Factory>(creationCallback = { factory ->
                factory.create(key)
            })

        GroupInfoScreen(viewModel = vm, navigator = navigator)
    }

    entry<HomepageNavigation.GroupUsersForm>(
        clazzContentKey = { key -> key.contentKey() }
    ) {
        GroupUsersFormScreen(
            onBack = { navigator.pop() },
            onNext = { navigator.navigate(HomepageNavigation.GroupInfoForm) },
        )
    }

    entry<HomepageNavigation.GroupInfoForm>(
        metadata = HomepageNavigation.GroupUsersForm.parent(),
    ) {
        GroupInfoFormScreen(
            onBack = { navigator.pop() },
            onSubmit = { navigator.pop(elements = 2) },
        )
    }
}