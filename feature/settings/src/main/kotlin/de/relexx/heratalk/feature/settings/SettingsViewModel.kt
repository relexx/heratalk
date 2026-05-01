// Copyright (c) 2026 relexx. BSD 3-Clause License.
// See LICENSE file in the project root for full license information.
package de.relexx.heratalk.feature.settings

import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import de.relexx.heratalk.core.identity.IdentityRepository
import de.relexx.heratalk.core.model.DisplayName
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * View-model for the settings screen.
 *
 * Aggregates display-name state from `:core:identity` with the module-local
 * settings persisted via [SettingsPreferences]. The view-model is the only
 * component that touches `AppCompatDelegate.setApplicationLocales(...)` so the
 * locale-switch side effect lives in one place.
 *
 * Persistence and side-effect details live in companion methods: setting
 * a language both writes to DataStore *and* applies the locale; the locale
 * application uses `LocaleListCompat` so AndroidX picks the correct path on
 * Android 13+ (per-app locales) and ≤ 12 (recreate flow).
 *
 * @param preferences Module-local settings persistence.
 * @param identityRepository Display-name repository from `:core:identity`.
 */
public class SettingsViewModel(
    private val preferences: SettingsPreferences,
    private val identityRepository: IdentityRepository,
) : ViewModel() {
    /**
     * Aggregated, conflated state for the settings screen.
     *
     * The flow reads from DataStore and the identity repository, so the
     * initial value is [SettingsScreenState.Initial] and real values arrive
     * once the persistence layer reads its files.
     */
    public val state: StateFlow<SettingsScreenState> =
        combine(
            preferences.language,
            preferences.theme,
            preferences.updateCheckEnabled,
            preferences.autoResumeEnabled,
            identityRepository.displayName,
        ) { language, theme, updateCheck, autoResume, displayName ->
            SettingsScreenState(
                language = language,
                theme = theme,
                updateCheckEnabled = updateCheck,
                autoResumeEnabled = autoResume,
                displayName = displayName,
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
            initialValue = SettingsScreenState.Initial,
        )

    /**
     * Persists the language choice and immediately applies it via AndroidX.
     *
     * The application of [LocaleListCompat] is what makes the UI recompose with
     * translated strings — the persistence is for surviving process death.
     */
    public fun onLanguageSelected(value: LanguagePreference) {
        viewModelScope.launch {
            preferences.setLanguage(value)
            AppCompatDelegate.setApplicationLocales(
                if (value == LanguagePreference.System) {
                    LocaleListCompat.getEmptyLocaleList()
                } else {
                    LocaleListCompat.forLanguageTags(value.tag)
                },
            )
        }
    }

    /** Persists the theme choice (no immediate UI effect; v1.0 will apply it). */
    public fun onThemeSelected(value: ThemePreference) {
        viewModelScope.launch {
            preferences.setTheme(value)
        }
    }

    /** Persists the update-check toggle (no network call in v0.1.0). */
    public fun onUpdateCheckToggled(enabled: Boolean) {
        viewModelScope.launch {
            preferences.setUpdateCheckEnabled(enabled)
        }
    }

    /** Persists the auto-resume toggle (effective from v0.5.0). */
    public fun onAutoResumeToggled(enabled: Boolean) {
        viewModelScope.launch {
            preferences.setAutoResumeEnabled(enabled)
        }
    }

    private companion object {
        // Five seconds matches the standard Compose StateFlow recipe — long
        // enough to survive a configuration change without resubscribing.
        const val STOP_TIMEOUT_MILLIS: Long = 5_000L
    }
}

/**
 * Snapshot of the settings screen state.
 *
 * @property language Current language preference.
 * @property theme Current theme preference.
 * @property updateCheckEnabled Whether the opt-in update-check is enabled.
 * @property autoResumeEnabled Whether the app should rejoin the last channel
 *   on start.
 * @property displayName Current display name from `:core:identity`, or `null`
 *   if none has been set yet.
 */
public data class SettingsScreenState(
    public val language: LanguagePreference,
    public val theme: ThemePreference,
    public val updateCheckEnabled: Boolean,
    public val autoResumeEnabled: Boolean,
    public val displayName: DisplayName?,
) {
    public companion object {
        /** Initial state used before DataStore has produced its first read. */
        public val Initial: SettingsScreenState =
            SettingsScreenState(
                language = LanguagePreference.System,
                theme = ThemePreference.HeraTalkDarkDefault,
                updateCheckEnabled = false,
                autoResumeEnabled = false,
                displayName = null,
            )
    }
}
