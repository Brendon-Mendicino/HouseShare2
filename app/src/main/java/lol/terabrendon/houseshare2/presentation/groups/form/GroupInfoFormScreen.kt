package lol.terabrendon.houseshare2.presentation.groups.form

import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import lol.terabrendon.houseshare2.R
import lol.terabrendon.houseshare2.domain.model.GroupFormState
import lol.terabrendon.houseshare2.domain.model.GroupFormStateValidator
import lol.terabrendon.houseshare2.domain.model.toValidator
import lol.terabrendon.houseshare2.presentation.components.FormOutlinedTextField
import lol.terabrendon.houseshare2.presentation.fab.RegisterFabAction
import lol.terabrendon.houseshare2.presentation.navigation.GroupFormNavigation
import lol.terabrendon.houseshare2.presentation.util.SnackbarController
import lol.terabrendon.houseshare2.presentation.util.SnackbarEvent
import lol.terabrendon.houseshare2.presentation.vm.GroupFormViewModel
import lol.terabrendon.houseshare2.util.ObserveAsEvent

private const val TAG: String = "GroupInfoFormScreen"

@Composable
fun GroupInfoFormScreen(
    viewModel: GroupFormViewModel = hiltViewModel(),
    navController: NavController,
) {
    val formState by viewModel.groupFormState.collectAsState()
    val scope = rememberCoroutineScope()

    RegisterFabAction {
        Log.d(TAG, "GroupInfoFormScreen: fab has been clicked")
        viewModel.onEvent(GroupFormEvent.Submit)
    }

    ObserveAsEvent(viewModel.uiEvent) {
        when (it) {
            is GroupFormUiEvent.SubmitFailure -> scope.launch {
                SnackbarController.sendEvent(SnackbarEvent(message = "erroreeee"))
            }

            GroupFormUiEvent.SubmitSuccess -> navController.popBackStack<GroupFormNavigation.SelectUsers>(
                inclusive = true
            )
        }
    }

    GroupInfoFormScreenInner(
        groupFormState = formState,
        onEvent = viewModel::onEvent,
    )
}

@Composable
private fun GroupInfoFormScreenInner(
    groupFormState: GroupFormStateValidator,
    onEvent: (GroupFormEvent) -> Unit = {}
) {

    Column(modifier = Modifier.padding(8.dp)) {
        FormOutlinedTextField(
            param = groupFormState.name,
            labelText = stringResource(R.string.group_name),
            onValueChange = { onEvent(GroupFormEvent.NameChanged(it)) },
            modifier = Modifier.fillMaxWidth(),
        )

        FormOutlinedTextField(
            param = groupFormState.description,
            labelText = stringResource(R.string.group_description),
            onValueChange = { onEvent(GroupFormEvent.DescriptionChanged(it)) },
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Preview(showBackground = true)
@Composable
fun GroupInfoFormScreenPreview() {
    GroupInfoFormScreenInner(
        groupFormState = GroupFormState(name = "Sium").toValidator(),
    )
}
