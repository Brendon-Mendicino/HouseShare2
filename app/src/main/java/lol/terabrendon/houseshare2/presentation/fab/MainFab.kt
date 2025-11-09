package lol.terabrendon.houseshare2.presentation.fab

import android.annotation.SuppressLint
import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingToolbarDefaults
import androidx.compose.material3.HorizontalFloatingToolbar
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import lol.terabrendon.houseshare2.R
import lol.terabrendon.houseshare2.presentation.navigation.HomepageNavigation
import lol.terabrendon.houseshare2.presentation.navigation.MainNavigation
import lol.terabrendon.houseshare2.presentation.provider.FabConfig
import lol.terabrendon.houseshare2.presentation.provider.FabManager
import lol.terabrendon.houseshare2.presentation.provider.LocalFabManager

@SuppressLint("UnusedTargetStateInContentKeyLambda")
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun MainFab(
    modifier: Modifier = Modifier,
    lastEntry: MainNavigation,
    onClick: () -> Unit,
) {
    val fabConfig by LocalFabManager.current.fabConfig
    val vibrantColors = FloatingToolbarDefaults.vibrantFloatingToolbarColors()

    val visible = fabConfig?.visible ?: lastEntry.fabVisible()
    val defaultFabExpanded = lastEntry.fabExpanded()
    val defaultText = stringResource(lastEntry.fabText())
    val defaultIcon = @Composable { t: String? ->
        Icon(
            imageVector = lastEntry.fabIcon(),
            contentDescription = t,
        )
    }

    AnimatedVisibility(visible = visible) {
        AnimatedContent(
            fabConfig to lastEntry,
            contentKey = { it.second },
        ) { (fabConfig, _) ->
            when (fabConfig) {
                is FabConfig.Toolbar ->
                    HorizontalFloatingToolbar(
                        expanded = fabConfig.expanded ?: false,
                        colors = vibrantColors,
                        floatingActionButton = {
                            FloatingToolbarDefaults.VibrantFloatingActionButton(
                                onClick = fabConfig.fab.onClick ?: onClick,
                            ) {
                                fabConfig.fab.icon?.invoke() ?: defaultIcon(
                                    fabConfig.fab.text ?: defaultText
                                )
                            }

                        },
                        content = {
                            fabConfig.content?.invoke(this, fabConfig.expanded ?: false)
                        },
                    )

                is FabConfig.Fab ->
                    ExtendedFloatingActionButton(
                        modifier = modifier,
                        text = { Text(fabConfig.text ?: defaultText) },
                        expanded = fabConfig.expanded ?: defaultFabExpanded,
                        icon = {
                            fabConfig.icon?.invoke() ?: defaultIcon(
                                fabConfig.text ?: defaultText
                            )
                        },
                        onClick = fabConfig.onClick ?: onClick,
                    )

                null -> {}
//                    ExtendedFloatingActionButton(
//                        modifier = modifier,
//                        text = { Text(defaultText) },
//                        expanded = defaultFabExpanded,
//                        icon = {
//                            defaultIcon(defaultText)
//                        },
//                        onClick = onClick,
//                    )
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
    is HomepageNavigation.Settings,
    is HomepageNavigation.UserProfile -> Icons.Filled.Add
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
    is HomepageNavigation.Shopping -> true


    is HomepageNavigation.ExpenseForm,
    is HomepageNavigation.GroupInfoForm,
    is HomepageNavigation.GroupUsersForm,
    is HomepageNavigation.ShoppingItem,
    is HomepageNavigation.ShoppingForm,
    is HomepageNavigation.UserProfile,
    is HomepageNavigation.Settings,
    is MainNavigation.Login,
    is MainNavigation.Loading -> false

}

private fun MainNavigation.fabVisible(): Boolean = when (this) {
    is HomepageNavigation.Billing,
    is HomepageNavigation.Cleaning,
    is HomepageNavigation.Shopping,
    is HomepageNavigation.Groups,
    is HomepageNavigation.GroupInfoForm,
    is HomepageNavigation.GroupUsersForm -> true


    is MainNavigation.Login,
    is MainNavigation.Loading,
    is HomepageNavigation.UserProfile,
    is HomepageNavigation.Settings,
    is HomepageNavigation.ExpenseForm,
    is HomepageNavigation.ShoppingForm,
    is HomepageNavigation.ShoppingItem -> false
}

@Preview
@Composable
private fun ExpFabPreview() {
    val m = FabManager().apply {
        putState(
            FabConfig.Fab(
                visible = true,
                expanded = true,
                text = "Drown",
                icon = {
                    Icon(Icons.Filled.Pool, null)
                }
            )
        )
    }

    CompositionLocalProvider(
        LocalFabManager provides m
    ) {
        MainFab(lastEntry = MainNavigation.Loading) { }
    }
}

@Preview
@Composable
private fun FabPreview() {
    val m = FabManager().apply {
        putState(
            FabConfig.Fab(
                visible = true,
                expanded = false,
                icon = { Icon(Icons.Filled.Factory, null) }
            )
        )
    }

    CompositionLocalProvider(
        LocalFabManager provides m
    ) {
        MainFab(lastEntry = MainNavigation.Loading) { }
    }
}

@Preview
@Composable
private fun ExpToolbarPreview() {
    val m = FabManager().apply {
        putState(
            FabConfig.Toolbar(
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
        )
    }

    CompositionLocalProvider(
        LocalFabManager provides m
    ) {
        MainFab(lastEntry = MainNavigation.Loading) { }
    }
}

@Preview
@Composable
private fun ToolbarPreview() {
    val m = FabManager().apply {
        putState(
            FabConfig.Toolbar(
                visible = true,
                expanded = false,
                content = {},
                fab = FabConfig.Fab(
                    visible = true,
                    expanded = false,
                    icon = { Icon(Icons.Filled.Factory, null) }
                )
            )
        )
    }

    CompositionLocalProvider(
        LocalFabManager provides m
    ) {
        MainFab(lastEntry = MainNavigation.Loading) { }
    }
}
