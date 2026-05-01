// Copyright (c) 2026 relexx. BSD 3-Clause License.
// See LICENSE file in the project root for full license information.
package de.relexx.heratalk.core.identity

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.emptyPreferences
import app.cash.turbine.test
import de.relexx.heratalk.core.model.DisplayName
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

/**
 * Unit tests for [DataStoreIdentityRepository].
 *
 * A [FakeDataStore] is used instead of Robolectric or a real file-backed DataStore,
 * keeping the test purely on the JVM (see ADR-0004 §"JVM-Tests gegen das Interface").
 */
class DataStoreIdentityRepositoryTest {
    // ---------------------------------------------------------------------------
    // Fake DataStore — no Android context, no file I/O
    // ---------------------------------------------------------------------------

    /** Minimal in-memory fake for [DataStore]<[Preferences]>. */
    private class FakeDataStore : DataStore<Preferences> {
        private val state = MutableStateFlow(emptyPreferences())

        override val data: Flow<Preferences> = state

        override suspend fun updateData(transform: suspend (t: Preferences) -> Preferences): Preferences {
            val updated = transform(state.value)
            state.value = updated
            return updated
        }
    }

    // ---------------------------------------------------------------------------
    // Helpers
    // ---------------------------------------------------------------------------

    private fun createRepository(dataStore: FakeDataStore = FakeDataStore()): DataStoreIdentityRepository =
        DataStoreIdentityRepository(dataStore)

    // ---------------------------------------------------------------------------
    // Tests: displayName flow
    // ---------------------------------------------------------------------------

    @Test
    fun `displayName emits null when nothing has been set`() =
        runTest {
            val repo = createRepository()

            repo.displayName.test {
                assertNull(awaitItem())
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `displayName emits the name after setDisplayName is called`() =
        runTest {
            val repo = createRepository()
            val expected = DisplayName("Alice")

            repo.displayName.test {
                assertNull(awaitItem()) // initial null
                repo.setDisplayName(expected)
                assertEquals(expected, awaitItem())
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `displayName reflects the latest name after multiple setDisplayName calls`() =
        runTest {
            val repo = createRepository()

            repo.displayName.test {
                assertNull(awaitItem())
                repo.setDisplayName(DisplayName("Alice"))
                assertEquals(DisplayName("Alice"), awaitItem())
                repo.setDisplayName(DisplayName("Bob"))
                assertEquals(DisplayName("Bob"), awaitItem())
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `displayName emits null when DataStore contains an empty string`() =
        runTest {
            // Pre-populate the fake store with an invalid (empty) string to simulate
            // a corrupt or down-migrated store entry.
            val fakeStore = FakeDataStore()
            fakeStore.updateData { prefs ->
                val mutable = prefs.toMutablePreferences()
                mutable[DataStoreIdentityRepository.DISPLAY_NAME_KEY] = ""
                mutable
            }
            val repo = createRepository(fakeStore)

            repo.displayName.test {
                assertNull(awaitItem())
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `displayName emits null when DataStore contains a whitespace-only string`() =
        runTest {
            val fakeStore = FakeDataStore()
            fakeStore.updateData { prefs ->
                val mutable = prefs.toMutablePreferences()
                mutable[DataStoreIdentityRepository.DISPLAY_NAME_KEY] = "   "
                mutable
            }
            val repo = createRepository(fakeStore)

            repo.displayName.test {
                assertNull(awaitItem())
                cancelAndIgnoreRemainingEvents()
            }
        }

    // ---------------------------------------------------------------------------
    // Tests: fallbackName
    // ---------------------------------------------------------------------------

    @Test
    fun `fallbackName returns DisplayName with Peer- prefix`() {
        val repo = createRepository()
        val pk = byteArrayOf(0xAB.toByte(), 0xCD.toByte(), 0x12.toByte(), 0x34.toByte())

        val result = repo.fallbackName(pk)

        assertEquals("Peer-abcd1234", result.value)
    }

    @Test
    fun `fallbackName result is a valid DisplayName`() {
        val repo = createRepository()
        val pk = ByteArray(32) { it.toByte() }

        // Must not throw
        val result = repo.fallbackName(pk)
        assertEquals(DisplayName(result.value), result)
    }
}
