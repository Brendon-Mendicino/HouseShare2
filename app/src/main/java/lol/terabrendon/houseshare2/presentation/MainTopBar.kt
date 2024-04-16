package lol.terabrendon.houseshare2.presentation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import lol.terabrendon.houseshare2.presentation.navigation.MainDestination

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainTopBar(
    modifier: Modifier = Modifier,
    mainDestination: MainDestination,
    onNavigationClick: () -> Unit,
    actions: @Composable (MainDestination) -> Unit,
) {
    CenterAlignedTopAppBar(
        title = { Text(stringResource(mainDestination.asResource())) },
        navigationIcon = {
            IconButton(onClick = onNavigationClick) {
                Icon(Icons.Filled.Menu, contentDescription = null)
            }
        },
        actions = { actions(mainDestination) },
        modifier = modifier
    )
}