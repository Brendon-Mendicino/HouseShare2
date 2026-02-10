package lol.terabrendon.houseshare2.presentation.screen.settings

import android.content.ClipData
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType.Companion.PrimaryNotEditable
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.toClipEntry
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.os.LocaleListCompat
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch
import lol.terabrendon.houseshare2.BuildConfig
import lol.terabrendon.houseshare2.R
import lol.terabrendon.houseshare2.data.local.preferences.UserData
import lol.terabrendon.houseshare2.presentation.util.SnackbarController
import lol.terabrendon.houseshare2.presentation.util.SnackbarEvent
import lol.terabrendon.houseshare2.presentation.util.UiText
import lol.terabrendon.houseshare2.presentation.vm.SettingsEvent
import lol.terabrendon.houseshare2.presentation.vm.SettingsState
import lol.terabrendon.houseshare2.presentation.vm.SettingsViewModel
import lol.terabrendon.houseshare2.ui.theme.HouseShare2Theme
import lol.terabrendon.houseshare2.util.Config

@Composable
fun SettingsScreen(modifier: Modifier = Modifier, viewModel: SettingsViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    SettingsInner(
        modifier = modifier,
        onEvent = viewModel::onEvent,
        state = state,
    )
}

@Composable
private fun SettingsInner(
    modifier: Modifier = Modifier,
    onEvent: (SettingsEvent) -> Unit,
    state: SettingsState,
) {
    val clipboard = LocalClipboard.current
    val uriHandler = LocalUriHandler.current
    val appName = stringResource(R.string.app_name)
    val scope = rememberCoroutineScope()

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        SettingsHeader(stringResource(R.string.general))

        LanguagePicker()
        ThemePicker(theme = state.appTheme, setTheme = { onEvent(SettingsEvent.ThemeChanged(it)) })
        DynamicColorPicker(
            dynamicColors = state.dynamicColors,
            onToggle = { onEvent(SettingsEvent.DynamicToggled) },
        )

        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))


        // Privacy & Safety
        SettingsHeader(stringResource(R.string.privacy_safety))

        // Privacy / Crashlytics Opt-In (GDPR compliant)
        ListItem(
            modifier = Modifier.toggleable(
                value = state.sendAnalytics,
                onValueChange = { onEvent(SettingsEvent.AnalyticsToggled) },
                role = Role.Switch
            ),
            headlineContent = { Text(stringResource(R.string.help_us_improve)) },
            supportingContent = {
                Text(stringResource(R.string.send_analytics))
            },
            trailingContent = {
                Switch(
                    checked = state.sendAnalytics,
                    onCheckedChange = null, // User ListItem toggleable for better UX
                )
            })

        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

        // About & Legal
        SettingsHeader(stringResource(R.string.about))
        ListItem(
            modifier = Modifier.clickable { uriHandler.openUri(Config.PRIVACY_URL) },
            headlineContent = { Text(stringResource(R.string.privacy_policies)) },
            leadingContent = { Icon(Icons.Default.Shield, null) },
            trailingContent = {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                    contentDescription = stringResource(R.string.open_in_browser),
                    modifier = Modifier.size(20.dp), // M3 standard trailing icon size is often 20-24dp
                    tint = MaterialTheme.colorScheme.onSurfaceVariant // Subtle hint
                )
            })

        ListItem(
            modifier = Modifier.clickable { uriHandler.openUri(Config.TERMS_URL) },
            headlineContent = { Text(stringResource(R.string.terms_conditions)) },
            leadingContent = { Icon(Icons.Default.Gavel, null) },
            trailingContent = { Icon(Icons.AutoMirrored.Filled.OpenInNew, null) })

        ListItem(
            headlineContent = { Text("Version") },
            supportingContent = {
                Text("${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})")
            },
            leadingContent = { Icon(Icons.Default.Info, null) },
            modifier = Modifier.combinedClickable(
                onClick = { /* Nothing or show a Toast */ },
                onLongClick = {
                    // Helpful for bug reporting: copy build info to clipboard
                    val buildInfo =
                        "$appName v${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})"
                    val entry = ClipData.newPlainText("Version", buildInfo).toClipEntry()

                    scope.launch {
                        clipboard.setClipEntry(entry)
                        SnackbarController.sendEvent(
                            SnackbarEvent(
                                message = UiText.Res(R.string.version_copied_to_clipboard)
                            )
                        )
                    }
                })
        )

        ListItem(
            headlineContent = { Text(stringResource(R.string.environment)) },
            supportingContent = {
                Text(BuildConfig.BUILD_TYPE)
            },
            leadingContent = { Icon(Icons.Default.Terminal, null) })

    }
}

@Composable
private fun SettingsHeader(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(start = 16.dp, bottom = 8.dp, top = 16.dp)
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LanguagePicker(
    modifier: Modifier = Modifier,
    localeOptions: Map<String, String> = Config.LANG_LOCALES,
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
    ) {
        OutlinedTextField(
            readOnly = true,
            modifier = modifier
                .fillMaxWidth()
                .animateContentSize()
                .menuAnchor(PrimaryNotEditable),
            value = stringResource(R.string.current_language),
            label = { Text(stringResource(R.string.language)) },
            onValueChange = { },
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(
                    expanded = expanded
                )
            })

        ExposedDropdownMenu(
            expanded = expanded, onDismissRequest = {
                expanded = false
            }) {
            localeOptions.keys.forEach { selectionLocale ->
                DropdownMenuItem(
                    onClick = {
                        expanded = false
                        // set app locale given the user's selected locale
                        AppCompatDelegate.setApplicationLocales(
                            LocaleListCompat.forLanguageTags(
                                localeOptions[selectionLocale]
                            )
                        )
                    },
                    text = { Text(selectionLocale) },
                )
            }
        }
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun ThemePicker(
    modifier: Modifier = Modifier,
    theme: UserData.Theme,
    setTheme: (UserData.Theme) -> Unit,
) {
    fun UserData.Theme.strRes() = when (this) {
        UserData.Theme.System -> R.string.system
        UserData.Theme.Dark -> R.string.dark
        UserData.Theme.Light -> R.string.light
    }

    fun UserData.Theme.icon() = when (this) {
        UserData.Theme.System -> Icons.Default.Android
        UserData.Theme.Dark -> Icons.Default.DarkMode
        UserData.Theme.Light -> Icons.Default.LightMode
    }

    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
    ) {
        OutlinedTextField(
            readOnly = true,
            modifier = modifier
                .fillMaxWidth()
                .animateContentSize()
                .menuAnchor(PrimaryNotEditable),
            value = stringResource(theme.strRes()),
            label = { Text(stringResource(R.string.app_theme)) },
            onValueChange = { },
            leadingIcon = { Icon(theme.icon(), null) },
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(
                    expanded = expanded
                )
            })

        ExposedDropdownMenu(
            expanded = expanded, onDismissRequest = {
                expanded = false
            }) {
            UserData.Theme.entries.forEach { theme ->
                DropdownMenuItem(
                    onClick = {
                        expanded = false
                        // set app locale given the user's selected locale
                        setTheme(theme)
                    },
                    leadingIcon = { Icon(theme.icon(), null) },
                    text = { Text(stringResource(theme.strRes())) },
                )
            }
        }
    }
}

@Composable
private fun DynamicColorPicker(
    modifier: Modifier = Modifier,
    dynamicColors: Boolean,
    onToggle: (Boolean) -> Unit,
) {
    ListItem(
        modifier = modifier.toggleable(
            value = dynamicColors, onValueChange = onToggle, role = Role.Switch
        ), headlineContent = {
            Text(text = stringResource(R.string.dynamic_color))
        }, supportingContent = {
            Text(text = stringResource(R.string.apply_system_wallpaper_colors_to_the_app_theme))
        }, leadingContent = {
            Icon(
                imageVector = Icons.Default.Palette, contentDescription = null // Decorative
            )
        }, trailingContent = {
            Switch(
                checked = dynamicColors,
                onCheckedChange = null // Handled by ListItem toggleable for better UX
            )
        })
}

@Preview(showBackground = true, heightDp = 1200)
@Composable
private fun SettingsPreview() {
    HouseShare2Theme {
        SettingsInner(onEvent = {}, state = SettingsState())
    }
}