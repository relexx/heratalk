// Copyright (c) 2026 relexx. BSD 3-Clause License.
// See LICENSE file in the project root for full license information.
package de.relexx.heratalk.feature.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import de.relexx.heratalk.core.model.DisplayName
import de.relexx.heratalk.core.model.NetworkQuality
import de.relexx.heratalk.core.ui.components.HeraTalkScaffold
import de.relexx.heratalk.core.ui.components.SectionHeader
import de.relexx.heratalk.core.ui.theme.HeraTalkTheme

/**
 * Settings screen, ordered per the 2026-04-25 UX revision:
 * Audio → App-Verhalten → Netzwerk → Benachrichtigungen
 *  → Features+Berechtigungen → Kanal → Info.
 *
 * Stub sections render their "coming soon" message; the live sections
 * (App-Verhalten and Kanal) call back to the [SettingsViewModel] handlers.
 *
 * @param state Current screen state.
 * @param onLanguageSelected Forwarded to [SettingsViewModel.onLanguageSelected].
 * @param onThemeSelected Forwarded to [SettingsViewModel.onThemeSelected].
 * @param onUpdateCheckToggled Forwarded to [SettingsViewModel.onUpdateCheckToggled].
 * @param onAutoResumeToggled Forwarded to [SettingsViewModel.onAutoResumeToggled].
 * @param onEditDisplayName Invoked when the user taps "Edit" beside the
 *   display-name entry; the host activity navigates to the shared
 *   `DisplayNameScreen`.
 * @param modifier Optional layout modifier.
 */
@Composable
public fun SettingsScreen(
    state: SettingsScreenState,
    onLanguageSelected: (LanguagePreference) -> Unit,
    onThemeSelected: (ThemePreference) -> Unit,
    onUpdateCheckToggled: (Boolean) -> Unit,
    onAutoResumeToggled: (Boolean) -> Unit,
    onEditDisplayName: () -> Unit,
    modifier: Modifier = Modifier,
) {
    HeraTalkScaffold(
        title = stringResource(R.string.settings_screen_title),
        networkQuality = NetworkQuality.OFFLINE,
        modifier = modifier,
    ) { innerPadding ->
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            AudioSection()
            SectionDivider()
            AppBehaviourSection(
                state = state,
                onLanguageSelected = onLanguageSelected,
                onThemeSelected = onThemeSelected,
                onUpdateCheckToggled = onUpdateCheckToggled,
                onAutoResumeToggled = onAutoResumeToggled,
            )
            SectionDivider()
            NetworkSection()
            SectionDivider()
            NotificationsSection()
            SectionDivider()
            FeaturesSection()
            SectionDivider()
            ChannelSection(
                displayName = state.displayName,
                onEditDisplayName = onEditDisplayName,
            )
            SectionDivider()
            InfoSection()
        }
    }
}

@Composable
private fun AudioSection() {
    SectionHeader(text = stringResource(R.string.settings_section_audio))
    Text(
        text = stringResource(R.string.settings_audio_stub),
        style = MaterialTheme.typography.bodyMedium,
    )
}

@Composable
private fun AppBehaviourSection(
    state: SettingsScreenState,
    onLanguageSelected: (LanguagePreference) -> Unit,
    onThemeSelected: (ThemePreference) -> Unit,
    onUpdateCheckToggled: (Boolean) -> Unit,
    onAutoResumeToggled: (Boolean) -> Unit,
) {
    SectionHeader(text = stringResource(R.string.settings_section_app))

    Text(
        text = stringResource(R.string.settings_language_label),
        style = MaterialTheme.typography.titleSmall,
    )
    LanguageOptionRow(
        labelRes = R.string.settings_language_system,
        selected = state.language == LanguagePreference.System,
        onSelect = { onLanguageSelected(LanguagePreference.System) },
    )
    LanguageOptionRow(
        labelRes = R.string.settings_language_de,
        selected = state.language == LanguagePreference.German,
        onSelect = { onLanguageSelected(LanguagePreference.German) },
    )
    LanguageOptionRow(
        labelRes = R.string.settings_language_en,
        selected = state.language == LanguagePreference.English,
        onSelect = { onLanguageSelected(LanguagePreference.English) },
    )

    Text(
        text = stringResource(R.string.settings_theme_label),
        style = MaterialTheme.typography.titleSmall,
        modifier = Modifier.padding(top = 8.dp),
    )
    ThemeOptionRow(
        labelRes = R.string.settings_theme_value_dark,
        selected = state.theme == ThemePreference.HeraTalkDarkDefault || state.theme == ThemePreference.Dark,
        onSelect = { onThemeSelected(ThemePreference.Dark) },
    )
    ThemeOptionRow(
        labelRes = R.string.settings_theme_value_light,
        selected = state.theme == ThemePreference.Light,
        onSelect = { onThemeSelected(ThemePreference.Light) },
    )
    ThemeOptionRow(
        labelRes = R.string.settings_theme_value_system,
        selected = state.theme == ThemePreference.System,
        onSelect = { onThemeSelected(ThemePreference.System) },
    )
    Text(
        text = stringResource(R.string.settings_theme_note),
        style = MaterialTheme.typography.labelSmall,
    )

    SwitchRow(
        labelRes = R.string.settings_update_check_label,
        summaryRes = R.string.settings_update_check_summary,
        checked = state.updateCheckEnabled,
        onCheckedChange = onUpdateCheckToggled,
    )
    SwitchRow(
        labelRes = R.string.settings_auto_resume_label,
        summaryRes = R.string.settings_auto_resume_summary,
        checked = state.autoResumeEnabled,
        onCheckedChange = onAutoResumeToggled,
    )
}

@Composable
private fun NetworkSection() {
    SectionHeader(text = stringResource(R.string.settings_section_network))
    Text(
        text = stringResource(R.string.settings_network_stub),
        style = MaterialTheme.typography.bodyMedium,
    )
}

@Composable
private fun NotificationsSection() {
    SectionHeader(text = stringResource(R.string.settings_section_notifications))
    Text(
        text = stringResource(R.string.settings_notifications_stub),
        style = MaterialTheme.typography.bodyMedium,
    )
}

@Composable
private fun FeaturesSection() {
    SectionHeader(text = stringResource(R.string.settings_section_features))
    Text(
        text = stringResource(R.string.settings_features_stub),
        style = MaterialTheme.typography.bodyMedium,
    )
}

@Composable
private fun ChannelSection(
    displayName: DisplayName?,
    onEditDisplayName: () -> Unit,
) {
    SectionHeader(text = stringResource(R.string.settings_section_channel))
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Column(modifier = Modifier.padding(end = 12.dp)) {
            Text(
                text = stringResource(R.string.settings_display_name_label),
                style = MaterialTheme.typography.titleSmall,
            )
            Text(
                text = displayName?.value ?: stringResource(R.string.settings_display_name_unset),
                style = MaterialTheme.typography.bodyMedium,
            )
        }
        TextButton(onClick = onEditDisplayName) {
            Text(text = stringResource(R.string.settings_display_name_edit))
        }
    }
}

@Composable
private fun InfoSection() {
    SectionHeader(text = stringResource(R.string.settings_section_info))
    Text(
        text = stringResource(R.string.settings_app_name),
        style = MaterialTheme.typography.bodyMedium,
    )
    Text(
        text = stringResource(R.string.settings_app_version_label),
        style = MaterialTheme.typography.labelMedium,
    )
}

@Composable
private fun LanguageOptionRow(
    labelRes: Int,
    selected: Boolean,
    onSelect: () -> Unit,
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .selectable(
                    selected = selected,
                    onClick = onSelect,
                    role = Role.RadioButton,
                ).padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RadioButton(selected = selected, onClick = null)
        Text(
            text = stringResource(labelRes),
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(start = 12.dp),
        )
    }
}

@Composable
private fun ThemeOptionRow(
    labelRes: Int,
    selected: Boolean,
    onSelect: () -> Unit,
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .selectable(
                    selected = selected,
                    onClick = onSelect,
                    role = Role.RadioButton,
                ).padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RadioButton(selected = selected, onClick = null)
        Text(
            text = stringResource(labelRes),
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(start = 12.dp),
        )
    }
}

@Composable
private fun SwitchRow(
    labelRes: Int,
    summaryRes: Int,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
            Text(
                text = stringResource(labelRes),
                style = MaterialTheme.typography.titleSmall,
            )
            Text(
                text = stringResource(summaryRes),
                style = MaterialTheme.typography.bodySmall,
            )
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
private fun SectionDivider() {
    HorizontalDivider()
}

@Preview(name = "Light", showBackground = true, heightDp = 1400)
@Composable
private fun SettingsScreenPreviewLight() {
    HeraTalkTheme(darkTheme = false) {
        SettingsScreen(
            state = SettingsScreenState.Initial,
            onLanguageSelected = {},
            onThemeSelected = {},
            onUpdateCheckToggled = {},
            onAutoResumeToggled = {},
            onEditDisplayName = {},
        )
    }
}

@Preview(name = "Dark", showBackground = true, heightDp = 1400)
@Composable
private fun SettingsScreenPreviewDark() {
    HeraTalkTheme(darkTheme = true) {
        SettingsScreen(
            state = SettingsScreenState.Initial,
            onLanguageSelected = {},
            onThemeSelected = {},
            onUpdateCheckToggled = {},
            onAutoResumeToggled = {},
            onEditDisplayName = {},
        )
    }
}
