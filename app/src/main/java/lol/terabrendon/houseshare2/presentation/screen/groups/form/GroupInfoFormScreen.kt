@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package lol.terabrendon.houseshare2.presentation.screen.groups.form

import android.os.Build.VERSION.SDK_INT
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import coil3.ImageLoader
import coil3.compose.SubcomposeAsyncImage
import coil3.gif.AnimatedImageDecoder
import coil3.gif.GifDecoder
import coil3.network.okhttp.OkHttpNetworkFetcherFactory
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
import lol.terabrendon.houseshare2.ui.theme.HouseShare2Theme
import lol.terabrendon.houseshare2.util.ObserveAsEvent
import timber.log.Timber

@Composable
fun GroupInfoFormScreen(
    viewModel: GroupFormViewModel = hiltViewModel(),
    onBack: () -> Unit,
    onSubmit: () -> Unit,
) {
    val formState by viewModel.groupFormState.collectAsState()

    RegisterFabConfig(
        config = FabConfig.Fab(
            onClick = {
                Timber.d("GroupInfoFormScreen: fab has been clicked")
                viewModel.onEvent(GroupFormEvent.Submit)
            },
        ),
        route = HomepageNavigation.GroupInfoForm::class,
    )

    ObserveAsEvent(viewModel.uiEvent) { event ->
        when (event) {
            GroupFormUiEvent.SubmitSuccess -> onSubmit()
        }
    }

    RegisterBackNavIcon(
        onClick = { onBack() },
        route = HomepageNavigation.GroupInfoForm::class,
    )

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
    val context = LocalContext.current
    val imageLoader = remember(context) {
        ImageLoader.Builder(context)
            .components {
                add(OkHttpNetworkFetcherFactory())
                if (SDK_INT >= 28) {
                    add(AnimatedImageDecoder.Factory())
                } else {
                    add(GifDecoder.Factory())
                }
            }
            .build()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        SubcomposeAsyncImage(
            modifier = Modifier
                .size(140.dp)
                .shadow(
                    elevation = 4.dp,
                    shape = CircleShape,
                )
                .background(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = CircleShape,
                ),
            model = groupFormState.imageUrl.value,
            contentDescription = null,
            imageLoader = imageLoader,
            contentScale = ContentScale.Crop,
            error = {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Default.Groups,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            loading = {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center,
                ) {
                    LoadingIndicator(
                        modifier = Modifier.size(48.dp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            },
        )

        Text(
            text = "Group image",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.primary
        )

        FormOutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            param = groupFormState.imageUrl,
            onValueChange = { onEvent(GroupFormEvent.ImageUrlChanged(it)) },
            labelText = stringResource(R.string.image_url),
            placeholder = { Text("https://example.com/image.jpg") },
            keyboardOptions = KeyboardOptions(
                imeAction = ImeAction.Next,
                keyboardType = KeyboardType.Uri,
            ),
        )

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
            minLines = 3,
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
    HouseShare2Theme {
        GroupInfoFormScreenInner(
            groupFormState = GroupFormState(
                name = "Sium",
            ).toValidator(),
        )
    }
}
