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
import kotlinx.coroutines.launch
import lol.terabrendon.houseshare2.R
import lol.terabrendon.houseshare2.domain.model.GroupFormState
import lol.terabrendon.houseshare2.domain.model.GroupFormStateValidator
import lol.terabrendon.houseshare2.domain.model.toValidator
import lol.terabrendon.houseshare2.presentation.components.FormOutlinedTextField
import lol.terabrendon.houseshare2.presentation.provider.RegisterFabAction
import lol.terabrendon.houseshare2.presentation.util.SnackbarController
import lol.terabrendon.houseshare2.presentation.util.SnackbarEvent
import lol.terabrendon.houseshare2.presentation.vm.GroupFormViewModel
import lol.terabrendon.houseshare2.util.ObserveAsEvent

private const val TAG: String = "GroupInfoFormScreen"

@Composable
fun GroupInfoFormScreen(
    viewModel: GroupFormViewModel = hiltViewModel(),
    onSubmit: () -> Unit,
) {
    val formState by viewModel.groupFormState.collectAsState()
    val scope = rememberCoroutineScope()

    RegisterFabAction {
        Log.d(TAG, "GroupInfoFormScreen: fab has been clicked")
        viewModel.onEvent(GroupFormEvent.Submit)
    }

    ObserveAsEvent(viewModel.uiEvent) { event ->
        when (event) {
            is GroupFormUiEvent.SubmitFailure -> scope.launch {
                // TODO: fix this!
                SnackbarController.sendEvent(SnackbarEvent(message = event.error))
            }

            GroupFormUiEvent.SubmitSuccess -> onSubmit()
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
            modifier = Modifier.fillMaxWidth(),
            param = groupFormState.name,
            onValueChange = { onEvent(GroupFormEvent.NameChanged(it)) },
            labelText = stringResource(R.string.group_name),
        )

        FormOutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            param = groupFormState.description,
            onValueChange = { onEvent(GroupFormEvent.DescriptionChanged(it)) },
            labelText = stringResource(R.string.group_description),
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
