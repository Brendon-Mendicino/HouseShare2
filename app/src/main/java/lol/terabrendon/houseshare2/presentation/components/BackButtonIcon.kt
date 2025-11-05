package lol.terabrendon.houseshare2.presentation.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import lol.terabrendon.houseshare2.R

@Composable
fun BackButtonIcon(onClick: () -> Unit, modifier: Modifier = Modifier) {
    IconButton(modifier = modifier, onClick = onClick) {
        Icon(
            Icons.AutoMirrored.Filled.ArrowBack,
            contentDescription = stringResource(R.string.back_action)
        )
    }
}