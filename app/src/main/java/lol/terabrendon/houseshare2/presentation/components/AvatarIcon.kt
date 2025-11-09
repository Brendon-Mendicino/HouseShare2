package lol.terabrendon.houseshare2.presentation.components

import androidx.annotation.ColorInt
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.graphics.ColorUtils
import coil.compose.SubcomposeAsyncImage
import lol.terabrendon.houseshare2.domain.model.UserModel
import kotlin.math.abs
import kotlin.math.pow

@ColorInt
private fun String.toHslColor(saturation: Float = 0.5f, lightness: Float = 0.4f): Int {
    val hue = fold(0) { acc, char -> char.code + acc * 37 } % 360
    return ColorUtils.HSLToColor(floatArrayOf(abs(hue.toFloat()), saturation, lightness))
}

@Composable
fun AvatarIcon(
    modifier: Modifier = Modifier,
    user: UserModel? = null,
    text: String? = null,
    picture: Boolean = user?.picture != null,
    size: Dp = 40.dp,
) {
    val density = LocalDensity.current
    val firstName = user?.firstName ?: user?.username ?: ""
    val lastName = user?.lastName ?: user?.username?.drop(1) ?: ""

    val color = remember(user) {
        val name = listOf(firstName, lastName).joinToString(separator = "").uppercase()
        Color(name.toHslColor())
    }
    val initials = when {
        text != null -> text
        size >= 24.dp -> (firstName.take(1) + lastName.take(1)).uppercase()
        else -> firstName.take(1).uppercase()
    }

    val letterIcon = @Composable {
        Box(modifier.size(size), contentAlignment = Alignment.Center) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawCircle(SolidColor(color))
            }
            Text(
                text = initials,
                fontSize = with(density) { (size * 2 / 3).toSp() },
                color = Color.White,
                maxLines = 1,
            )
        }
    }

    if (user?.picture != null && picture) {
        SubcomposeAsyncImage(
            user.picture,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = modifier
                .size(size)
                .clip(CircleShape),
            loading = {
                letterIcon()
            },
        )
    } else {
        letterIcon()
    }
}

private class Prev : PreviewParameterProvider<Int> {
    override val values: Sequence<Int>
        get() = (3..8).asSequence().map { 2.0.pow(it).toInt() }
}

@Preview
@Composable
private fun IconPreview(
    @PreviewParameter(provider = Prev::class) size: Int,
) {
    AvatarIcon(user = null, text = "AB", size = size.dp)
}

@Preview
@Composable
private fun IconUserPreview(
    @PreviewParameter(provider = Prev::class) size: Int,
) {
    AvatarIcon(
        user = UserModel.default(),
        size = size.dp
    )
}
