// Copyright (c) 2026 relexx. BSD 3-Clause License.
package de.relexx.heratalk.core.model

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test

class ChannelIdTest {

    @Test
    fun `constructor throws IllegalArgumentException when value is empty`() {
        assertThrows(IllegalArgumentException::class.java) {
            ChannelId("")
        }
    }

    @Test
    fun `constructor throws IllegalArgumentException when value contains uppercase letter`() {
        // Channel IDs must be lowercase-only to stay case-unambiguous in QR codes.
        assertThrows(IllegalArgumentException::class.java) {
            ChannelId("MyChannel")
        }
    }

    @Test
    fun `constructor throws IllegalArgumentException when value contains underscore`() {
        // Underscores are not in the allowed set [a-z0-9-] for ChannelId.
        assertThrows(IllegalArgumentException::class.java) {
            ChannelId("my_channel")
        }
    }

    @Test
    fun `constructor throws IllegalArgumentException when value exceeds 32 characters`() {
        val thirtyThreeChars = "a".repeat(33)
        assertThrows(IllegalArgumentException::class.java) {
            ChannelId(thirtyThreeChars)
        }
    }

    @Test
    fun `constructor accepts a valid lowercase alphanumeric value`() {
        val id = ChannelId("alpha42")
        assertEquals("alpha42", id.value)
    }

    @Test
    fun `constructor accepts a value with hyphens`() {
        val id = ChannelId("my-channel-1")
        assertEquals("my-channel-1", id.value)
    }

    @Test
    fun `constructor accepts a value with exactly 32 characters`() {
        val thirtyTwoChars = "a".repeat(32)
        val id = ChannelId(thirtyTwoChars)
        assertEquals(thirtyTwoChars, id.value)
    }

    @Test
    fun `constructor accepts a single character value`() {
        val id = ChannelId("z")
        assertEquals("z", id.value)
    }
}
