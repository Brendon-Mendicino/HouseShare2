package lol.terabrendon.houseshare2.presentation.home

import androidx.compose.animation.animateContentSize
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector

@Composable
fun MainFab(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    text: String,
    onClick: () -> Unit
) {
    ExtendedFloatingActionButton(
        text = { Text(text) },
        icon = {
            Icon(
                imageVector = icon,
                contentDescription = text,
            )
        },
        onClick = { onClick() },
        modifier = modifier.animateContentSize()
    )
}