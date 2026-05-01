// Copyright (c) 2026 relexx. BSD 3-Clause License.
// See LICENSE file in the project root for full license information.
package de.relexx.heratalk.core.identity

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import de.relexx.heratalk.core.model.DisplayName
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Android adapter that persists the local peer's [DisplayName] via Jetpack DataStore.
 *
 * This class is the Android-specific implementation of [IdentityRepository] (see ADR-0004).
 * Only this file imports `androidx.*` symbols; the interface in `IdentityRepository.kt`
 * remains platform-free.
 *
 * @param dataStore A [DataStore]<[Preferences]> instance provided by the DI graph.
 *   The DI graph is responsible for constructing the DataStore with the appropriate
 *   [android.content.Context]; that Context never escapes this class boundary.
 */
public class DataStoreIdentityRepository(
    private val dataStore: DataStore<Preferences>,
) : IdentityRepository {
    internal companion object {
        /**
         * Preferences key used to persist the display name.
         *
         * `internal` so the constant is reachable from same-module unit tests but
         * not exposed as part of the module's public API surface — callers depend
         * only on [IdentityRepository], never on the underlying storage layout
         * (see ADR-0004 §3 — minimize adapter visibility).
         */
        internal val DISPLAY_NAME_KEY: Preferences.Key<String> =
            stringPreferencesKey("display_name")
    }

    /**
     * A stream of the current [DisplayName].
     *
     * Emits `null` when no name has been saved yet or when the persisted value is
     * invalid (e.g. an empty string caused by a corrupt or migrated store). Invalid
     * values are silently dropped — the caller is notified via `null` rather than
     * an exception, keeping the UI in a recoverable "no name set" state.
     */
    override val displayName: Flow<DisplayName?> =
        dataStore.data.map { preferences ->
            val raw = preferences[DISPLAY_NAME_KEY]
            if (raw == null) {
                null
            } else {
                try {
                    DisplayName(raw)
                } catch (_: IllegalArgumentException) {
                    // Invalid persisted value — treat as "not set" rather than crashing.
                    null
                }
            }
        }

    /**
     * Persists [name] in DataStore, replacing any previously stored value.
     */
    override suspend fun setDisplayName(name: DisplayName) {
        dataStore.edit { preferences ->
            preferences[DISPLAY_NAME_KEY] = name.value
        }
    }

    /**
     * Returns a deterministic fallback display name derived from [pk].
     *
     * Delegates to the pure top-level function [fallbackPeerName] so that the
     * pure logic remains independently testable without an Android environment.
     */
    override fun fallbackName(pk: ByteArray): DisplayName {
        val nameString = fallbackPeerName(pk)
        return DisplayName(nameString)
    }
}
