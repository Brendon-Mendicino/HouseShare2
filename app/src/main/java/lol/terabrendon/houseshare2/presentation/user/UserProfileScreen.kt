package lol.terabrendon.houseshare2.presentation.user

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import lol.terabrendon.houseshare2.R
import lol.terabrendon.houseshare2.domain.model.UserModel
import lol.terabrendon.houseshare2.presentation.components.AvatarIcon
import lol.terabrendon.houseshare2.presentation.components.LoadingOverlayScreen
import lol.terabrendon.houseshare2.presentation.vm.UserViewModel
import lol.terabrendon.houseshare2.ui.theme.HouseShare2Theme


@Composable
fun UserProfileScreen(modifier: Modifier = Modifier, viewModel: UserViewModel = hiltViewModel()) {
    val user by viewModel.user.collectAsStateWithLifecycle(null)

    if (user == null) {
        LoadingOverlayScreen()
        return
    }

    UserProfileInner(modifier = modifier, user = user!!)
}

@Composable
private fun UserProfileInner(modifier: Modifier = Modifier, user: UserModel) {
    Column(modifier = modifier.padding(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            AvatarIcon(
                user = user,
                size = 180.dp,
                modifier = Modifier
                    .padding(16.dp)
                    .rotatingGradient()
            )
        }

        UserField(text = user.username, label = stringResource(R.string.username))

        UserField(text = user.email ?: "", label = stringResource(R.string.email))

        UserField(text = user.firstName ?: "", label = stringResource(R.string.first_name))

        UserField(text = user.lastName ?: "", label = stringResource(R.string.last_name))
    }
}

@Composable
private fun Modifier.rotatingGradient(): Modifier = let {
    val scheme = MaterialTheme.colorScheme
    val grad = Brush.sweepGradient(
        listOf(
            scheme.primaryContainer,
            scheme.secondaryContainer,
            scheme.tertiaryContainer,
            scheme.primaryContainer,
        )
    )

    val infiniteTransition = rememberInfiniteTransition()
    val rotatingAnimation = infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(1000, easing = LinearEasing))
    )

    it
        .drawBehind {
            rotate(rotatingAnimation.value) {
                drawCircle(brush = grad, radius = 180.dp.value, style = Stroke(180.dp.value))
            }
        }
}

@Composable
private fun UserField(modifier: Modifier = Modifier, label: String, text: String) {
    OutlinedTextField(
        modifier = modifier.fillMaxWidth(),
        value = text,
        onValueChange = {},
        label = { Text(label) },
        readOnly = true,
    )
}

@Preview(showBackground = true)
@Composable
private fun Preview() {
    HouseShare2Theme {
        UserProfileInner(
            user = UserModel.default()
                .copy(picture = "https://upload.wikimedia.org/wikipedia/commons/b/b6/Image_created_with_a_mobile_phone.png".toUri())
        )
    }
}