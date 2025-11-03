package lol.terabrendon.houseshare2.presentation.shopping

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import lol.terabrendon.houseshare2.domain.model.ShoppingItemModel
import lol.terabrendon.houseshare2.presentation.components.LoadingOverlayScreen
import lol.terabrendon.houseshare2.presentation.vm.ShoppingSingleViewModel

@Composable
fun ShoppingItemScreen(
    modifier: Modifier = Modifier,
    viewModel: ShoppingSingleViewModel = hiltViewModel(),
) {
    val shoppingItem by viewModel.shoppingItem.collectAsStateWithLifecycle()

    if (shoppingItem == null) {
        LoadingOverlayScreen()
        return
    }

    ShoppingItemInner(modifier = modifier, shoppingItemModel = shoppingItem!!)
}

@Composable
private fun ShoppingItemInner(modifier: Modifier = Modifier, shoppingItemModel: ShoppingItemModel) {
    Text(shoppingItemModel.toString())
}