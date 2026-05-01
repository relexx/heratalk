// Copyright (c) 2026 relexx. BSD 3-Clause License.
package de.relexx.heratalk.core.identity

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.emptyPreferences
import de.relexx.heratalk.core.model.DisplayName
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.filter
import io.kotest.property.arbitrary.map
import io.kotest.property.arbitrary.string
import io.kotest.property.checkAll
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test

/**
 * Property-based tests for [DataStoreIdentityRepository].
 *
 * Uses Kotest property-based generators together with the JUnit 5 runner so that
 * no additional Kotest test runner dependency is needed (see `libs.versions.toml`).
 *
 * Verifies that for every valid [DisplayName] input the round-trip through
 * [DataStoreIdentityRepository.setDisplayName] and [DataStoreIdentityRepository.displayName]
 * is lossless.
 */
class IdentityRepositoryPropertyTest {

    @Test
    fun `round-trip through DataStore is lossless for every valid DisplayName`() = runTest {
        // Arb for valid DisplayName values:
        // - length 1..16 characters (stays well within the 32-code-point limit)
        // - at least one non-whitespace character
        // - no Bidi-override code points
        val validNameArb: Arb<DisplayName> = Arb
            .string(minSize = 1, maxSize = 16)
            .filter { s ->
                s.isNotBlank() &&
                    s.codePointCount(0, s.length) <= 32 &&
                    !containsBidiOverride(s)
            }
            .map { s -> DisplayName(s) }

        checkAll(100, validNameArb) { name ->
            val fakeStore = PropertyFakeDataStore()
            val repo = DataStoreIdentityRepository(fakeStore)

            repo.setDisplayName(name)

            val retrieved = repo.displayName.first()
            retrieved shouldBe name
        }
    }
}

// ---------------------------------------------------------------------------
// Helpers
// ---------------------------------------------------------------------------

private val BIDI_OVERRIDE_CODE_POINTS: Set<Int> = buildSet {
    addAll(0x202A..0x202E)
    addAll(0x2066..0x2069)
    add(0x200E)
    add(0x200F)
}

private fun containsBidiOverride(s: String): Boolean {
    var i = 0
    while (i < s.length) {
        val cp = s.codePointAt(i)
        if (cp in BIDI_OVERRIDE_CODE_POINTS) return true
        i += Character.charCount(cp)
    }
    return false
}

/** Minimal in-memory fake for [DataStore]<[Preferences]> used in property tests. */
private class PropertyFakeDataStore : DataStore<Preferences> {

    private val state = MutableStateFlow(emptyPreferences())

    override val data: Flow<Preferences> = state

    override suspend fun updateData(transform: suspend (t: Preferences) -> Preferences): Preferences {
        val updated = transform(state.value)
        state.value = updated
        return updated
    }
}
