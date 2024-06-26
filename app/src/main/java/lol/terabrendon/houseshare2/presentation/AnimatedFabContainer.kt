package lol.terabrendon.houseshare2.presentation

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.unit.dp
import lol.terabrendon.houseshare2.presentation.navigation.MainDestination
import lol.terabrendon.houseshare2.presentation.state.MainFabContainerState

@Composable
fun AnimatedFab(
    modifier: Modifier = Modifier,
    currentDestination: MainDestination,
    fullscreen: @Composable (onBack: () -> Unit) -> Unit
) {
    val animationLabel = "MainFabAnimation"
    var containerState by rememberSaveable { mutableStateOf(MainFabContainerState.Fab) }

    val durationIn = 250
    val delay = 150
    val durationTot = durationIn + delay

    val animatedAlpha = remember { Animatable(0f) }
    val padding = when (containerState) {
        MainFabContainerState.Fab -> 16.dp
        MainFabContainerState.Fullscreen -> 8.dp
    }

    val primaryContainer = MaterialTheme.colorScheme.primaryContainer

    LaunchedEffect(containerState) {
        when (containerState) {
            MainFabContainerState.Fab -> animatedAlpha.animateTo(
                0f, animationSpec = tween(durationTot, 0, FastOutLinearInEasing)
            )

            MainFabContainerState.Fullscreen -> animatedAlpha.animateTo(
                1f, animationSpec = tween(durationTot, 0, FastOutSlowInEasing)
            )
        }
    }

    AnimatedContent(targetState = containerState,
        label = animationLabel,
        modifier = modifier
            .drawBehind {
                drawRoundRect(
                    color = primaryContainer,
                    cornerRadius = CornerRadius(12.dp.toPx()),
                    alpha = animatedAlpha.value
                )
            }
            .padding(padding)
            .windowInsetsPadding(WindowInsets.systemBars)
            .imePadding(),
        transitionSpec = {
            fadeIn(animationSpec = tween(durationIn, delay)).togetherWith(
                fadeOut(animationSpec = tween(delay))
            ).using(SizeTransform(clip = false) { _, _ -> tween(durationTot) })
        }) { state ->
        when (state) {
            MainFabContainerState.Fab -> MainFab(
                onClick = {
                    containerState = MainFabContainerState.Fullscreen
                },
            )

            MainFabContainerState.Fullscreen -> fullscreen {
                containerState = MainFabContainerState.Fab
            }
        }
    }
}

