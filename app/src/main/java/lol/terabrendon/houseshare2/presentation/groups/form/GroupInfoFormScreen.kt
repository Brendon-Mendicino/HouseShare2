package lol.terabrendon.houseshare2.presentation.groups.form

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import lol.terabrendon.houseshare2.R
import lol.terabrendon.houseshare2.domain.form.GroupFormState
import lol.terabrendon.houseshare2.domain.form.GroupFormStateValidator
import lol.terabrendon.houseshare2.domain.form.toValidator
import lol.terabrendon.houseshare2.presentation.components.FormOutlinedTextField
import lol.terabrendon.houseshare2.presentation.components.RegisterBackNavIcon
import lol.terabrendon.houseshare2.presentation.navigation.HomepageNavigation
import lol.terabrendon.houseshare2.presentation.provider.FabConfig
import lol.terabrendon.houseshare2.presentation.provider.RegisterFabConfig
import lol.terabrendon.houseshare2.presentation.vm.GroupFormViewModel
import lol.terabrendon.houseshare2.util.ObserveAsEvent
import timber.log.Timber

@Composable
fun GroupInfoFormScreen(
    viewModel: GroupFormViewModel = hiltViewModel(),
    onBack: () -> Unit,
    onSubmit: () -> Unit,
) {
    val formState by viewModel.groupFormState.collectAsState()

    RegisterFabConfig<HomepageNavigation.GroupInfoForm>(
        config = FabConfig.Fab(
            onClick = {
                Timber.d("GroupInfoFormScreen: fab has been clicked")
                viewModel.onEvent(GroupFormEvent.Submit)
            },
        )
    )

    ObserveAsEvent(viewModel.uiEvent) { event ->
        when (event) {
            GroupFormUiEvent.SubmitSuccess -> onSubmit()
        }
    }

    RegisterBackNavIcon<HomepageNavigation.GroupInfoForm>(onClick = onBack)

    GroupInfoFormScreenInner(
        groupFormState = formState,
        onEvent = viewModel::onEvent,
    )
}

@Composable
private fun GroupInfoFormScreenInner(
    groupFormState: GroupFormStateValidator,
    onEvent: (GroupFormEvent) -> Unit = {},
) {

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp)
    ) {
        FormOutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            param = groupFormState.name,
            onValueChange = { onEvent(GroupFormEvent.NameChanged(it)) },
            labelText = stringResource(R.string.group_name),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
        )

        FormOutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            param = groupFormState.description,
            onValueChange = { onEvent(GroupFormEvent.DescriptionChanged(it)) },
            labelText = stringResource(R.string.group_description),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = {
                onEvent(GroupFormEvent.Submit)
            }),
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
