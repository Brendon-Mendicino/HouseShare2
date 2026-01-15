package lol.terabrendon.houseshare2.presentation.fab

import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddShoppingCart
import androidx.compose.material.icons.filled.BikeScooter
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Factory
import androidx.compose.material.icons.filled.Man
import androidx.compose.material.icons.filled.Pool
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FloatingToolbarDefaults
import androidx.compose.material3.HorizontalFloatingToolbar
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MediumExtendedFloatingActionButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import lol.terabrendon.houseshare2.R
import lol.terabrendon.houseshare2.presentation.navigation.HomepageNavigation
import lol.terabrendon.houseshare2.presentation.navigation.MainNavigation
import lol.terabrendon.houseshare2.presentation.provider.FabConfig
import lol.terabrendon.houseshare2.presentation.provider.LocalFabManager
import kotlin.time.Duration.Companion.milliseconds


@OptIn(ExperimentalMaterial3ExpressiveApi::class, FlowPreview::class)
@Composable
fun MainFab(
    modifier: Modifier = Modifier,
    lastEntry: MainNavigation,
) {
    // Debounce the stateFlow
    val baseFabFlow = LocalFabManager.current.fabConfig
    var fabConfig by rememberSaveable { mutableStateOf<FabConfig?>(null) }

    LaunchedEffect(baseFabFlow) {
        baseFabFlow.debounce(50.milliseconds).collectLatest {
            fabConfig = it
        }
    }

    MainFabInner(modifier, lastEntry, fabConfig)
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class, FlowPreview::class)
@Composable
private fun MainFabInner(
    modifier: Modifier = Modifier,
    lastEntry: MainNavigation,
    fabConfig: FabConfig?,
) {
    val haptic = LocalHapticFeedback.current

    val vibrantColors = FloatingToolbarDefaults.vibrantFloatingToolbarColors()
    val motion = MaterialTheme.motionScheme

    // Logic for default fallback values
    val defaultText = stringResource(lastEntry.fabText())
    val defaultIcon = @Composable {
        Icon(imageVector = lastEntry.fabIcon(), contentDescription = null)
    }

    AnimatedContent(
        fabConfig,
        modifier = modifier.windowInsetsPadding(WindowInsets.ime.only(WindowInsetsSides.Bottom)),
        contentKey = { conf -> conf?.route ?: "none" },
        transitionSpec = {
            // Material 3 recommendation: Scale + Fade for FABs
            (fadeIn(motion.defaultSpatialSpec()) + scaleIn(initialScale = 0.8f))
                .togetherWith(fadeOut(motion.defaultSpatialSpec()) + scaleOut(targetScale = 0.8f))
        },
        label = "FabTransform"
    ) { fabConfig ->
        if (fabConfig == null || !(fabConfig.visible ?: lastEntry.fabVisible())) {
            return@AnimatedContent
        }

        // This fires every time the toolbar opens or closes.
        LaunchedEffect(fabConfig.expanded) {
            haptic.performHapticFeedback(HapticFeedbackType.GestureThresholdActivate)
        }

        when (fabConfig) {
            is FabConfig.Toolbar -> {
                HorizontalFloatingToolbar(
                    expanded = fabConfig.expanded ?: false,
                    colors = vibrantColors,
                    floatingActionButton = {
                        FloatingToolbarDefaults.VibrantFloatingActionButton(
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                fabConfig.fab.onClick?.invoke()
                            },
                        ) {
                            fabConfig.fab.icon?.invoke() ?: defaultIcon()
                        }
                    },
                    content = {
                        fabConfig.content?.invoke(this, fabConfig.expanded ?: false)
                    },
                )
            }

            is FabConfig.Fab -> {
                MediumExtendedFloatingActionButton(
                    text = { Text(fabConfig.text ?: defaultText) },
                    expanded = fabConfig.expanded ?: lastEntry.fabExpanded(),
                    icon = { fabConfig.icon?.invoke() ?: defaultIcon() },
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        fabConfig.onClick?.invoke()
                    },
                )
            }
        }
    }
}

private fun MainNavigation.fabIcon(): ImageVector = when (this) {
    is HomepageNavigation.Shopping -> Icons.Filled.AddShoppingCart
    is HomepageNavigation.Billing -> Icons.Filled.Receipt
    is HomepageNavigation.Groups -> Icons.Filled.Add
    is HomepageNavigation.GroupUsersForm -> Icons.AutoMirrored.Filled.ArrowForward
    is HomepageNavigation.GroupInfoForm -> Icons.Filled.Check
    is HomepageNavigation.ExpenseForm -> Icons.Filled.Check

    is HomepageNavigation.ShoppingForm,
    is HomepageNavigation.ShoppingItem,
    is MainNavigation.Login,
    is HomepageNavigation.Cleaning,
    is MainNavigation.Loading,
    is HomepageNavigation.GroupInfo,
    is HomepageNavigation.Settings,
    is HomepageNavigation.UserProfile,
        -> Icons.Filled.Add

}

@StringRes
private fun MainNavigation.fabText(): Int = when (this) {
    // TODO: change this
    else -> R.string.create
}

private fun MainNavigation.fabExpanded(): Boolean = when (this) {
    is HomepageNavigation.Billing,
    is HomepageNavigation.Cleaning,
    is HomepageNavigation.Groups,
    is HomepageNavigation.Shopping,
        -> true


    is HomepageNavigation.GroupInfo,
    is HomepageNavigation.ExpenseForm,
    is HomepageNavigation.GroupInfoForm,
    is HomepageNavigation.GroupUsersForm,
    is HomepageNavigation.ShoppingItem,
    is HomepageNavigation.ShoppingForm,
    is HomepageNavigation.UserProfile,
    is HomepageNavigation.Settings,
    is MainNavigation.Login,
    is MainNavigation.Loading,
        -> false
}

private fun MainNavigation.fabVisible(): Boolean = when (this) {
    is HomepageNavigation.Billing,
    is HomepageNavigation.Cleaning,
    is HomepageNavigation.Shopping,
    is HomepageNavigation.Groups,
    is HomepageNavigation.GroupInfoForm,
    is HomepageNavigation.GroupUsersForm,
        -> true


    is MainNavigation.Login,
    is MainNavigation.Loading,
    is HomepageNavigation.UserProfile,
    is HomepageNavigation.Settings,
    is HomepageNavigation.GroupInfo,
    is HomepageNavigation.ExpenseForm,
    is HomepageNavigation.ShoppingForm,
    is HomepageNavigation.ShoppingItem,
        -> false
}

@Preview
@Composable
private fun ExpFabPreview() {
    val config = FabConfig.Fab(
        visible = true,
        expanded = true,
        text = "Drown",
        icon = {
            Icon(Icons.Filled.Pool, null)
        }
    )

    MainFabInner(lastEntry = MainNavigation.Loading, fabConfig = config)
}

@Preview
@Composable
private fun FabPreview() {
    val config = FabConfig.Fab(
        visible = true,
        expanded = false,
        icon = { Icon(Icons.Filled.Factory, null) }
    )

    MainFabInner(lastEntry = MainNavigation.Loading, fabConfig = config)
}

@Preview
@Composable
private fun ExpToolbarPreview() {
    val config = FabConfig.Toolbar(
        visible = true,
        expanded = true,
        content = {
            IconButton(onClick = {}) {
                Icon(Icons.Filled.Receipt, null)
            }
            IconButton(onClick = {}) {
                Icon(Icons.Filled.Check, null)
            }
            IconButton(onClick = {}) {
                Icon(Icons.Filled.BikeScooter, null)
            }
            IconButton(onClick = {}) {
                Icon(Icons.Filled.Man, null)
            }
        },
        fab = FabConfig.Fab(
            visible = true,
            expanded = false,
            icon = { Icon(Icons.Filled.Factory, null) }
        )
    )

    MainFabInner(lastEntry = MainNavigation.Loading, fabConfig = config)
}

@Preview
@Composable
private fun ToolbarPreview() {
    val config = FabConfig.Toolbar(
        visible = true,
        expanded = false,
        content = {},
        fab = FabConfig.Fab(
            visible = true,
            expanded = false,
            icon = { Icon(Icons.Filled.Factory, null) }
        )
    )

    MainFabInner(lastEntry = MainNavigation.Loading, fabConfig = config)
}
