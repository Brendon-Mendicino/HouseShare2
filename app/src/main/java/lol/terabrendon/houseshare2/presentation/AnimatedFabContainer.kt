package lol.terabrendon.houseshare2.presentation

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.animateColor
import androidx.compose.animation.core.EaseInCubic
import androidx.compose.animation.core.EaseOutCubic
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDp
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
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
    val transition = updateTransition(containerState, label = animationLabel)

    val animatedColor by transition.animateColor(
        label = "$animationLabel/color",
    ) { state ->
        when (state) {
            MainFabContainerState.Fab -> MaterialTheme.colorScheme.primaryContainer
            MainFabContainerState.Fullscreen -> MaterialTheme.colorScheme.surface
        }
    }
    val cornerRadius by transition.animateDp(
        label = "$animatedColor/cornerRadius",
        transitionSpec = {
            when (targetState) {
                MainFabContainerState.Fab -> tween(
                    durationMillis = 400,
                    easing = EaseOutCubic,
                )

                MainFabContainerState.Fullscreen -> tween(
                    durationMillis = 200,
                    easing = EaseInCubic,
                )
            }
        }
    ) { state ->
        when (state) {
            MainFabContainerState.Fab -> 22.dp
            MainFabContainerState.Fullscreen -> 0.dp
        }
    }
    val elevation by transition.animateDp(
        label = "elevation",
        transitionSpec = {
            when (targetState) {
                MainFabContainerState.Fab -> tween(
                    durationMillis = 400,
                    easing = EaseOutCubic,
                )

                MainFabContainerState.Fullscreen -> tween(
                    durationMillis = 200,
                    easing = EaseOutCubic,
                )
            }
        }
    ) { state ->
        when (state) {
            MainFabContainerState.Fab -> 6.dp
            MainFabContainerState.Fullscreen -> 0.dp
        }
    }
    val padding by transition.animateDp(
        label = "padding",
    ) { state ->
        when (state) {
            MainFabContainerState.Fab -> 16.dp
            MainFabContainerState.Fullscreen -> 0.dp
        }
    }

    transition.AnimatedContent(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .padding(end = padding, bottom = padding)
            .shadow(
                elevation = elevation,
                shape = RoundedCornerShape(cornerRadius)
            )
            .drawBehind { drawRect(animatedColor) },
        transitionSpec = {
            (
                    fadeIn(animationSpec = tween(220, delayMillis = 90)) +
                            scaleIn(
                                initialScale = 0.92f,
                                animationSpec = tween(220, delayMillis = 90)
                            )
                    )
                .togetherWith(fadeOut(animationSpec = tween(90)))
                .using(SizeTransform(clip = false, sizeAnimationSpec = { _, _ ->
                    tween(
                        durationMillis = 500,
                        easing = FastOutSlowInEasing
                    )
                }))
        }
    ) { state ->
        when (state) {
            MainFabContainerState.Fab -> MainFab(onClick = {
                containerState = MainFabContainerState.Fullscreen
            })

            MainFabContainerState.Fullscreen -> fullscreen {
                containerState = MainFabContainerState.Fab
            }
        }
    }
}

