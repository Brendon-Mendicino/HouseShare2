package lol.terabrendon.houseshare2.presentation.util

import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MotionScheme
import androidx.navigation3.ui.NavDisplay

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
private val SPEC: FiniteAnimationSpec<Float>
    get() = MotionScheme.expressive().slowEffectsSpec()

val TOP_LEVEL_TRANSITION = NavDisplay.transitionSpec {
    // Fade in content, keeping the old content in place underneath
    fadeIn(animationSpec = SPEC) togetherWith fadeOut(animationSpec = SPEC)
} + NavDisplay.popTransitionSpec {
    // Fade old content, revealing the new content in place underneath
    fadeIn(animationSpec = SPEC) togetherWith fadeOut(animationSpec = SPEC)
} + NavDisplay.predictivePopTransitionSpec {
    // Slide old content down, revealing the new content in place underneath
    fadeIn(animationSpec = SPEC) togetherWith fadeOut(animationSpec = SPEC)
}