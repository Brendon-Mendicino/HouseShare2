package lol.terabrendon.houseshare2.presentation.screen.groups.form

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import lol.terabrendon.houseshare2.R
import lol.terabrendon.houseshare2.domain.form.GroupMemberFormState
import lol.terabrendon.houseshare2.domain.form.GroupMemberFormStateValidator
import lol.terabrendon.houseshare2.domain.form.toValidator
import lol.terabrendon.houseshare2.domain.model.GroupMemberModel
import lol.terabrendon.houseshare2.presentation.components.AvatarIcon
import lol.terabrendon.houseshare2.presentation.components.FormOutlinedTextField
import lol.terabrendon.houseshare2.presentation.components.RegisterBackNavIcon
import lol.terabrendon.houseshare2.presentation.navigation.HomepageNavigation
import lol.terabrendon.houseshare2.presentation.provider.FabConfig
import lol.terabrendon.houseshare2.presentation.provider.RegisterFabConfig
import lol.terabrendon.houseshare2.presentation.vm.GroupMemberFormViewModel
import lol.terabrendon.houseshare2.ui.theme.HouseShare2Theme
import lol.terabrendon.houseshare2.util.ObserveAsEvent
import timber.log.Timber

@Composable
fun GroupMemberFormScreen(
    viewModel: GroupMemberFormViewModel = hiltViewModel(),
    onBack: () -> Unit,
    onSubmit: () -> Unit,
) {
    val formState by viewModel.groupMemberFormState.collectAsState()

    RegisterBackNavIcon(
        onClick = { onBack() },
        route = HomepageNavigation.GroupMemberForm::class,
    )

    RegisterFabConfig(
        config = FabConfig.Fab(
            onClick = {
                Timber.d("GroupMemberFormScreen: fab has been clicked")
                viewModel.onEvent(GroupMemberFormEvent.Submit)
            },
        ),
        route = HomepageNavigation.GroupMemberForm::class,
    )

    ObserveAsEvent(viewModel.uiEvent) { event ->
        when (event) {
            GroupMemberFormUiEvent.SubmitSuccess -> onSubmit()
        }
    }

    GroupMemberFormScreenInner(
        groupMemberFormState = formState,
        onEvent = viewModel::onEvent,
    )
}

@Composable
private fun GroupMemberFormScreenInner(
    groupMemberFormState: GroupMemberFormStateValidator,
    onEvent: (GroupMemberFormEvent) -> Unit = {},
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        val previewMember = GroupMemberModel(
            id = 0,
            firstName = groupMemberFormState.firstName.value,
            lastName = groupMemberFormState.lastName.value,
            picture = groupMemberFormState.picture.value?.toUri(),
            groupId = 0,
            userId = null,
        )

        AvatarIcon(
            user = previewMember,
            size = 96.dp,
        )

        FormOutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            param = groupMemberFormState.picture,
            onValueChange = { onEvent(GroupMemberFormEvent.PictureChanged(it)) },
            labelText = stringResource(R.string.image_url),
            placeholder = { Text("https://example.com/image.jpg") },
            maxLines = 1,
            keyboardOptions = KeyboardOptions(
                imeAction = ImeAction.Next,
                keyboardType = KeyboardType.Uri,
            ),
        )

        FormOutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            param = groupMemberFormState.firstName,
            onValueChange = { onEvent(GroupMemberFormEvent.FirstNameChanged(it)) },
            labelText = stringResource(R.string.first_name),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
        )

        FormOutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            param = groupMemberFormState.lastName,
            onValueChange = { onEvent(GroupMemberFormEvent.LastNameChanged(it)) },
            labelText = stringResource(R.string.last_name),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = {
                onEvent(GroupMemberFormEvent.Submit)
            }),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun GroupMemberFormScreenPreview() {
    HouseShare2Theme {
        GroupMemberFormScreenInner(
            groupMemberFormState = GroupMemberFormState().toValidator(),
        )
    }
}
