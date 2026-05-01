// Copyright (c) 2026 relexx. BSD 3-Clause License.
// See LICENSE file in the project root for full license information.
package de.relexx.heratalk.feature.settings

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Persistence layer for the local-only settings owned by `:feature:settings`.
 *
 * The display-name lives in `:core:identity` (different lifecycle, different
 * consumer). Everything else — language preference, theme preference,
 * update-check toggle, auto-resume toggle — is module-local in v0.1.0.
 *
 * If a future release needs cross-module settings (e.g. theme observed by
 * other features) the implementation will be lifted into a dedicated
 * `:core:settings` module — see `impl-plan-v0.1.0.md` §C5.
 *
 * @param dataStore Preferences DataStore injected via Koin in v0.1.0/D1.
 */
public class SettingsPreferences(
    private val dataStore: DataStore<Preferences>,
) {
    /** Hot stream of the persisted [LanguagePreference]. */
    public val language: Flow<LanguagePreference> =
        dataStore.data.map { prefs ->
            LanguagePreference.fromTag(prefs[KeyLanguageTag])
        }

    /** Hot stream of the persisted [ThemePreference]. */
    public val theme: Flow<ThemePreference> =
        dataStore.data.map { prefs ->
            ThemePreference.fromName(prefs[KeyThemeName])
        }

    /** Hot stream of the update-check toggle. Defaults to `false`. */
    public val updateCheckEnabled: Flow<Boolean> =
        dataStore.data.map { it[KeyUpdateCheck] ?: false }

    /** Hot stream of the auto-resume toggle. Defaults to `false`. */
    public val autoResumeEnabled: Flow<Boolean> =
        dataStore.data.map { it[KeyAutoResume] ?: false }

    /** Persists the language preference. */
    public suspend fun setLanguage(value: LanguagePreference) {
        dataStore.edit { prefs ->
            prefs[KeyLanguageTag] = value.tag
        }
    }

    /** Persists the theme preference. */
    public suspend fun setTheme(value: ThemePreference) {
        dataStore.edit { prefs ->
            prefs[KeyThemeName] = value.name
        }
    }

    /** Persists the update-check toggle. */
    public suspend fun setUpdateCheckEnabled(value: Boolean) {
        dataStore.edit { prefs -> prefs[KeyUpdateCheck] = value }
    }

    /** Persists the auto-resume toggle. */
    public suspend fun setAutoResumeEnabled(value: Boolean) {
        dataStore.edit { prefs -> prefs[KeyAutoResume] = value }
    }

    private companion object {
        // Stable keys — never rename without a data migration.
        val KeyLanguageTag = stringPreferencesKey("language_tag")
        val KeyThemeName = stringPreferencesKey("theme_name")
        val KeyUpdateCheck = booleanPreferencesKey("update_check_enabled")
        val KeyAutoResume = booleanPreferencesKey("auto_resume_enabled")
    }
}

/**
 * User language preference.
 *
 * @property tag BCP-47 tag fed into `AppCompatDelegate.setApplicationLocales`.
 *   Empty for [System].
 */
public enum class LanguagePreference(
    public val tag: String,
) {
    /** Use the OS-provided locale. */
    System(tag = ""),

    /** Force German (`de`). */
    German(tag = "de"),

    /** Force English (`en`). */
    English(tag = "en"),
    ;

    public companion object {
        /**
         * Parses a persisted tag back into a [LanguagePreference].
         *
         * Unknown / `null` tags fall back to [System] so an upgrade path is
         * always safe.
         *
         * @param tag Tag previously written via [LanguagePreference.tag];
         *   `null` is treated as "never set".
         */
        public fun fromTag(tag: String?): LanguagePreference = entries.firstOrNull { it.tag == tag } ?: System
    }
}

/**
 * User theme preference.
 *
 * v0.1.0 only persists the choice — actually switching the theme lands with v1.0
 * (F-14). Until then [HeraTalkDarkDefault] keeps the dark palette in place.
 */
public enum class ThemePreference {
    /** Default for v0.1.0: dark theme regardless of system. */
    HeraTalkDarkDefault,

    /** User wants the light palette. */
    Light,

    /** User wants the dark palette. */
    Dark,

    /** User wants the theme to follow the system. */
    System,
    ;

    public companion object {
        /**
         * Parses a persisted [name] back into a [ThemePreference].
         *
         * Unknown / `null` names fall back to [HeraTalkDarkDefault].
         *
         * @param name Name previously written via [ThemePreference.name];
         *   `null` is treated as "never set".
         */
        public fun fromName(name: String?): ThemePreference =
            entries.firstOrNull { it.name == name } ?: HeraTalkDarkDefault
    }
}
