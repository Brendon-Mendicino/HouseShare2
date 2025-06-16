package lol.terabrendon.houseshare2.presentation.groups.form

import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import lol.terabrendon.houseshare2.R
import lol.terabrendon.houseshare2.domain.model.GroupFormState
import lol.terabrendon.houseshare2.domain.model.GroupFormStateValidator
import lol.terabrendon.houseshare2.domain.model.toValidator
import lol.terabrendon.houseshare2.presentation.components.FormOutlinedTextField
import lol.terabrendon.houseshare2.presentation.util.LocalFabActionManager
import lol.terabrendon.houseshare2.presentation.vm.GroupFormViewModel

private const val TAG: String = "GroupInfoFormScreen"

@Composable
fun GroupInfoFormScreen(
    viewModel: GroupFormViewModel = hiltViewModel(),
    navController: NavController,
) {
    val formState by viewModel.groupFormState.collectAsState()
    val fabActionManager = LocalFabActionManager.current

    LaunchedEffect(Unit) {
        Log.i(TAG, "GroupInfoFormScreen: setting fabActionManager")
        fabActionManager.setFabAction {
            viewModel.onEvent(GroupFormEvent.Submit)
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            Log.d(TAG, "GroupInfoFormScreen: resetting fabActionManager")
            fabActionManager.setFabAction(null)
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

    Column {
        FormOutlinedTextField(
            param = groupFormState.name,
            label = { Text(stringResource(R.string.name)) },
            onValueChange = { onEvent(GroupFormEvent.NameChanged(it)) },
        )

        FormOutlinedTextField(
            param = groupFormState.description,
            onValueChange = { onEvent(GroupFormEvent.DescriptionChanged(it)) },
            label = { Text(stringResource(R.string.group_name)) },
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
