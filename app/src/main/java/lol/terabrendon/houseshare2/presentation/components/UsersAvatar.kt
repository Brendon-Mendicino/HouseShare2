package lol.terabrendon.houseshare2.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.datasource.CollectionPreviewParameterProvider
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import lol.terabrendon.houseshare2.domain.model.UserModel
import lol.terabrendon.houseshare2.ui.theme.HouseShare2Theme

@Composable
fun UsersAvatar(
    users: List<UserModel>,
    modifier: Modifier = Modifier,
    avatarSize: Dp = 40.dp,
    maxVisible: Int = 3,
    // Match this to the background of your screen/list to create the cutout effect
    borderColor: Color = MaterialTheme.colorScheme.surface,
    borderWidth: Dp = 2.dp,
    overlapFactor: Float = 0.3f,
) {
    val displayUsers = users.take(maxVisible)
    val extraCount = (users.size - maxVisible).coerceAtLeast(0)

    // Negative spacing pulls the items together so they overlap
    val spacing = -(avatarSize * overlapFactor)

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(spacing),
        verticalAlignment = Alignment.CenterVertically
    ) {
        displayUsers.forEachIndexed { index, user ->
            Box(
                modifier = Modifier
                    // Higher zIndex for earlier items keeps them on top
                    .zIndex((displayUsers.size - index).toFloat())
                    .size(avatarSize)
                    .clip(CircleShape)
                    .border(borderWidth, borderColor, CircleShape)
            ) {
                // Reusing your exact AvatarIcon component
                AvatarIcon(
                    user = user,
                    size = avatarSize
                )
            }
        }

        // If there are more users than maxVisible, show the +X indicator
        if (extraCount > 0) {
            Box(
                modifier = Modifier
                    .zIndex(0f)
                    .size(avatarSize)
                    .clip(CircleShape)
                    .border(borderWidth, borderColor, CircleShape)
                    .background(MaterialTheme.colorScheme.secondaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "+$extraCount",
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    fontWeight = FontWeight.Medium,
                    fontSize = with(LocalDensity.current) { (avatarSize * 0.4f).toSp() },
                    maxLines = 1
                )
            }
        }
    }
}

private class MulUsers : CollectionPreviewParameterProvider<Int>((0..5).toList())

@Preview(showBackground = true)
@Composable
private fun GroupAvatarPreview(
    @PreviewParameter(provider = MulUsers::class)
    users: Int,
) {
    HouseShare2Theme {
        UsersAvatar(
            List(users) { UserModel.random() },
        )
    }
}
