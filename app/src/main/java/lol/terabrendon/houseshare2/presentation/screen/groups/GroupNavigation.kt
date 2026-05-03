package lol.terabrendon.houseshare2.presentation.screen.groups

import androidx.navigation3.runtime.EntryProviderScope
import lol.terabrendon.houseshare2.presentation.navigation.HomepageNavigation
import lol.terabrendon.houseshare2.presentation.navigation.MainNavigation
import lol.terabrendon.houseshare2.presentation.navigation.Navigator
import lol.terabrendon.houseshare2.presentation.screen.groups.form.GroupInfoFormScreen
import lol.terabrendon.houseshare2.presentation.screen.groups.form.GroupUsersFormScreen
import lol.terabrendon.houseshare2.presentation.util.TOP_LEVEL_TRANSITION
import lol.terabrendon.houseshare2.presentation.util.contentKey
import lol.terabrendon.houseshare2.presentation.util.parent
import lol.terabrendon.houseshare2.presentation.vm.GroupFormViewModel
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
        val vm = GroupInfoViewModel.create(key)

        GroupInfoScreen(viewModel = vm, navigator = navigator)
    }

    entry<HomepageNavigation.GroupUsersForm>(
        clazzContentKey = { key -> key.contentKey() }
    ) {
        val vm = GroupFormViewModel.create(HomepageNavigation.GroupInfoForm(groupId = null))

        GroupUsersFormScreen(
            viewModel = vm,
            onBack = { navigator.pop() },
            onNext = { navigator.navigate(HomepageNavigation.GroupInfoForm(groupId = null)) },
        )
    }

    entry<HomepageNavigation.GroupInfoForm>(
        metadata = HomepageNavigation.GroupUsersForm.parent(),
    ) { key ->
        val vm = GroupFormViewModel.create(key)
        val backSteps = if (key.groupId == null) 2 else 1

        GroupInfoFormScreen(
            viewModel = vm,
            onBack = { navigator.pop() },
            onSubmit = { navigator.pop(elements = backSteps) },
        )
    }
}