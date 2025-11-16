package lol.terabrendon.houseshare2.presentation.shopping

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.ButtonGroup
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch
import lol.terabrendon.houseshare2.domain.model.CheckoffStateModel
import lol.terabrendon.houseshare2.domain.model.ShoppingItemInfoModel
import lol.terabrendon.houseshare2.domain.model.ShoppingItemModel
import lol.terabrendon.houseshare2.domain.model.UserModel
import lol.terabrendon.houseshare2.domain.model.toMoney
import lol.terabrendon.houseshare2.presentation.components.AvatarIcon
import lol.terabrendon.houseshare2.presentation.components.LoadingOverlayScreen
import lol.terabrendon.houseshare2.presentation.util.SnackbarController
import lol.terabrendon.houseshare2.presentation.util.SnackbarEvent
import lol.terabrendon.houseshare2.presentation.util.errorText
import lol.terabrendon.houseshare2.presentation.vm.ShoppingSingleViewModel
import lol.terabrendon.houseshare2.util.ObserveAsEvent
import lol.terabrendon.houseshare2.util.inlineFormat

@Composable
fun ShoppingItemScreen(
    modifier: Modifier = Modifier,
    viewModel: ShoppingSingleViewModel = hiltViewModel(),
) {
    val shoppingItem by viewModel.shoppingItem.collectAsStateWithLifecycle()
    val state by viewModel.state.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    ObserveAsEvent(viewModel.uiEvent) { event ->
        when (event) {
            is ShoppingSingleViewModel.UiEvent.Error -> scope.launch {
                SnackbarController.sendEvent(
                    SnackbarEvent(
                        message = event.error.errorText(
                            "",
                            context
                        )
                    )
                )
            }
        }
    }

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
            .padding(8.dp)
            .animateContentSize(),
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

        ButtonGroup(
            overflowIndicator = {},
            modifier = Modifier.align(Alignment.CenterHorizontally),
        ) {
            toggleableItem(
                checked = check != null,
                onCheckedChange = { if (it) onEvent(ShoppingItemEvent.Toggled) },
                label = "Checked",
                icon = if (check != null) {
                    { Icon(Icons.Filled.Check, null) }
                } else null,
            )

            toggleableItem(
                checked = check == null,
                onCheckedChange = { if (it) onEvent(ShoppingItemEvent.Toggled) },
                label = "Unchecked",
                icon = if (check == null) {
                    { Icon(Icons.Filled.Check, null) }
                } else null,
            )
        }

        if (check != null) {
            ItemField(text = check.checkoffUser.username, label = "Checked by", leadingIcon = {
                AvatarIcon(
                    modifier = Modifier.padding(horizontal = 8.dp),
                    user = check.checkoffUser
                )
            })

            ItemField(text = check.checkoffTime.inlineFormat(), label = "Checked at")
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
