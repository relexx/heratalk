// Copyright (c) 2026 relexx. BSD 3-Clause License.
// See LICENSE file in the project root for full license information.
package de.relexx.heratalk.core.model

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test

class PeerIdTest {
    @Test
    fun `constructor throws IllegalArgumentException when value is empty`() {
        assertThrows(IllegalArgumentException::class.java) {
            PeerId("")
        }
    }

    @Test
    fun `constructor throws IllegalArgumentException when value exceeds 64 characters`() {
        val sixtyFiveChars = "a".repeat(65)
        assertThrows(IllegalArgumentException::class.java) {
            PeerId(sixtyFiveChars)
        }
    }

    @Test
    fun `constructor throws IllegalArgumentException when value contains invalid character @`() {
        assertThrows(IllegalArgumentException::class.java) {
            PeerId("peer@host")
        }
    }

    @Test
    fun `constructor throws IllegalArgumentException when value contains a space`() {
        assertThrows(IllegalArgumentException::class.java) {
            PeerId("peer id")
        }
    }

    @Test
    fun `constructor accepts a valid alphanumeric value`() {
        val id = PeerId("peer42")
        assertEquals("peer42", id.value)
    }

    @Test
    fun `constructor accepts a value with hyphens and underscores`() {
        val id = PeerId("peer-42_test")
        assertEquals("peer-42_test", id.value)
    }

    @Test
    fun `constructor accepts a value with exactly 64 characters`() {
        val sixtyFourChars = "a".repeat(64)
        val id = PeerId(sixtyFourChars)
        assertEquals(sixtyFourChars, id.value)
    }

    @Test
    fun `constructor accepts a single character value`() {
        val id = PeerId("x")
        assertEquals("x", id.value)
    }
}
