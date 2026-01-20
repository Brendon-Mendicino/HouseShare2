package lol.terabrendon.houseshare2.presentation.shopping

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.ToggleButton
import androidx.compose.material3.ToggleButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import lol.terabrendon.houseshare2.R
import lol.terabrendon.houseshare2.domain.model.CheckoffStateModel
import lol.terabrendon.houseshare2.domain.model.ShoppingItemInfoModel
import lol.terabrendon.houseshare2.domain.model.ShoppingItemModel
import lol.terabrendon.houseshare2.domain.model.UserModel
import lol.terabrendon.houseshare2.domain.model.toMoney
import lol.terabrendon.houseshare2.presentation.components.AvatarIcon
import lol.terabrendon.houseshare2.presentation.components.LoadingOverlayScreen
import lol.terabrendon.houseshare2.presentation.vm.ShoppingSingleViewModel
import lol.terabrendon.houseshare2.util.inlineFormat

@Composable
fun ShoppingItemScreen(
    modifier: Modifier = Modifier,
    viewModel: ShoppingSingleViewModel = hiltViewModel(),
) {
    val shoppingItem by viewModel.shoppingItem.collectAsStateWithLifecycle()
    val state by viewModel.state.collectAsStateWithLifecycle()

    if (shoppingItem == null) {
        LoadingOverlayScreen()
        return
    }

    ShoppingItemInner(
        modifier = modifier,
        item = shoppingItem!!,
        state = state,
        onEvent = viewModel::onEvent,
    )
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun ShoppingItemInner(
    modifier: Modifier = Modifier,
    item: ShoppingItemModel,
    onEvent: (ShoppingItemEvent) -> Unit,
    state: ShoppingSingleViewModel.State = ShoppingSingleViewModel.State(),
) {
    val isPending = state.pending > 0
    val info = item.info
    val check = item.checkoffState

    Column(
        modifier = modifier
            .fillMaxSize()
            .animateContentSize()
            .padding(8.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        ModifyItemField(
            text = info.name,
            label = "Item name",
            onChange = { onEvent(ShoppingItemEvent.NameChanged(it)) },
        )

        ModifyItemField(
            text = info.price?.toString() ?: "",
            label = "Price",
            onChange = { onEvent(ShoppingItemEvent.PriceChanged(it)) },
        )

        ModifyItemField(
            text = info.amount.toString(),
            label = "Quantity",
            onChange = { onEvent(ShoppingItemEvent.QuantityChanged(it)) },
        )

        ItemField(text = info.creationTimestamp.inlineFormat(), label = "Created at")

        ItemField(
            text = item.itemOwner.username,
            label = "Created by",
            leadingIcon = {
                AvatarIcon(
                    modifier = Modifier.padding(horizontal = 8.dp),
                    user = item.itemOwner
                )
            },
        )

        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

        Row(
            Modifier.padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(ButtonGroupDefaults.ConnectedSpaceBetween),
        ) {
            ToggleButton(
                checked = check != null,
                onCheckedChange = { if (it) onEvent(ShoppingItemEvent.Toggled) },
                modifier = Modifier
                    .weight(1f)
                    .semantics { role = Role.RadioButton },
                shapes = ButtonGroupDefaults.connectedLeadingButtonShapes(),
            ) {
                AnimatedVisibility(check != null) { Icon(Icons.Filled.Check, null) }
                Spacer(Modifier.size(ToggleButtonDefaults.IconSpacing))
                Text(stringResource(R.string.checked))
            }

            ToggleButton(
                checked = check == null,
                onCheckedChange = { if (it) onEvent(ShoppingItemEvent.Toggled) },
                modifier = Modifier
                    .weight(1f)
                    .semantics { role = Role.RadioButton },
                shapes = ButtonGroupDefaults.connectedTrailingButtonShapes(),
            ) {
                AnimatedVisibility(check == null) { Icon(Icons.Filled.Check, null) }
                Spacer(Modifier.size(ToggleButtonDefaults.IconSpacing))
                Text(stringResource(R.string.unchecked))
            }
        }

        AnimatedVisibility(check != null) {
            val checkInner by remember { mutableStateOf<CheckoffStateModel?>(null) }
                .apply { if (check != null) value = check }

            if (checkInner == null)
                return@AnimatedVisibility

            ItemField(
                text = checkInner!!.checkoffUser.username,
                label = stringResource(R.string.checked_by),
                leadingIcon = {
                    AvatarIcon(
                        modifier = Modifier.padding(horizontal = 8.dp),
                        user = checkInner!!.checkoffUser
                    )
                })
        }

        AnimatedVisibility(check != null) {
            val checkInner by remember { mutableStateOf<CheckoffStateModel?>(null) }
                .apply { if (check != null) value = check }

            if (checkInner == null)
                return@AnimatedVisibility

            ItemField(
                text = checkInner!!.checkoffTime.inlineFormat(),
                label = stringResource(R.string.checked_at)
            )
        }
    }

    if (isPending) {
        LoadingOverlayScreen()
    }
}

@Composable
private fun ItemField(
    modifier: Modifier = Modifier,
    leadingIcon: (@Composable () -> Unit)? = null,
    text: String,
    label: String,
) {
    OutlinedTextField(
        modifier = modifier.fillMaxWidth(),
        value = text,
        enabled = false,
        onValueChange = {},
        readOnly = true,
        label = { Text(label) },
        leadingIcon = leadingIcon,
    )
}

@Composable
private fun ModifyItemField(
    modifier: Modifier = Modifier,
    leadingIcon: (@Composable () -> Unit)? = null,
    onChange: (text: String) -> Unit,
    text: String,
    label: String,
) {
    var tempText by rememberSaveable { mutableStateOf(text) }

    LaunchedEffect(text) {
        tempText = text
    }

    OutlinedTextField(
        modifier = modifier
            .fillMaxWidth()
            .onFocusChanged {
                if (!it.isCaptured && !it.isFocused)
                    onChange(tempText)
            },
        value = tempText,
        onValueChange = { tempText = it },
        label = { Text(label) },
        leadingIcon = leadingIcon,
    )
}

@Preview(showBackground = true)
@Composable
private fun ShoppingItemPreview() {
    ShoppingItemInner(
        item = ShoppingItemModel(
            info = ShoppingItemInfoModel.default()
                .copy(
                    name = "Pasta",
                    amount = 10,
                    price = 3.50.toMoney()
                ),
            itemOwner = UserModel.default(),
            checkoffState = null,
        ),
        onEvent = {},
    )
}

@Preview(showBackground = true)
@Composable
private fun ShoppingItemCheckedPreview() {
    ShoppingItemInner(
        item = ShoppingItemModel(
            info = ShoppingItemInfoModel.default()
                .copy(
                    name = "Pasta",
                    amount = 10,
                    price = 3.50.toMoney()
                ),
            itemOwner = UserModel.default(),
            checkoffState = CheckoffStateModel.default(),
        ),
        onEvent = {},
    )
}
