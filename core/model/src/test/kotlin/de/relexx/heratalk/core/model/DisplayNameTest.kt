// Copyright (c) 2026 relexx. BSD 3-Clause License.
package de.relexx.heratalk.core.model

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test

class DisplayNameTest {

    @Test
    fun `constructor throws IllegalArgumentException when value is empty`() {
        assertThrows(IllegalArgumentException::class.java) {
            DisplayName("")
        }
    }

    @Test
    fun `constructor throws IllegalArgumentException when value exceeds 32 code points`() {
        val thirtyThreeChars = "a".repeat(33)
        assertThrows(IllegalArgumentException::class.java) {
            DisplayName(thirtyThreeChars)
        }
    }

    @Test
    fun `constructor throws IllegalArgumentException when value is only whitespace`() {
        assertThrows(IllegalArgumentException::class.java) {
            DisplayName("   ")
        }
    }

    @Test
    fun `constructor throws IllegalArgumentException for value containing U+202E (RIGHT-TO-LEFT OVERRIDE)`() {
        // U+202E is a Bidi-override codepoint that can be used to spoof displayed filenames/names.
        val nameWithBidi = "Alice‮Bob"
        assertThrows(IllegalArgumentException::class.java) {
            DisplayName(nameWithBidi)
        }
    }

    @Test
    fun `constructor throws IllegalArgumentException for value containing U+202A (LEFT-TO-RIGHT EMBEDDING)`() {
        val nameWithBidi = "‪Alice"
        assertThrows(IllegalArgumentException::class.java) {
            DisplayName(nameWithBidi)
        }
    }

    @Test
    fun `constructor throws IllegalArgumentException for value containing U+2066 (LEFT-TO-RIGHT ISOLATE)`() {
        val nameWithBidi = "Alice⁦"
        assertThrows(IllegalArgumentException::class.java) {
            DisplayName(nameWithBidi)
        }
    }

    @Test
    fun `constructor throws IllegalArgumentException for value containing U+200F (RIGHT-TO-LEFT MARK)`() {
        val nameWithBidi = "Alice‏"
        assertThrows(IllegalArgumentException::class.java) {
            DisplayName(nameWithBidi)
        }
    }

    @Test
    fun `constructor accepts a valid ASCII name`() {
        val name = DisplayName("Alice")
        assertEquals("Alice", name.value)
    }

    @Test
    fun `constructor accepts a name with exactly 32 code points`() {
        val thirtyTwoChars = "a".repeat(32)
        val name = DisplayName(thirtyTwoChars)
        assertEquals(thirtyTwoChars, name.value)
    }

    @Test
    fun `constructor accepts a name containing Unicode letters outside ASCII`() {
        val name = DisplayName("Ärger-mit-Björn")
        assertEquals("Ärger-mit-Björn", name.value)
    }

    @Test
    fun `constructor accounts for supplementary code points when checking length`() {
        // Each emoji below is one code point but two UTF-16 chars (surrogate pair).
        // 32 emoji = 32 code points → must be accepted.
        val thirtyTwoEmoji = "😀".repeat(32)
        val name = DisplayName(thirtyTwoEmoji)
        assertEquals(32, name.value.codePointCount(0, name.value.length))
    }

    @Test
    fun `constructor throws when 33 supplementary code points are provided`() {
        val thirtyThreeEmoji = "😀".repeat(33)
        assertThrows(IllegalArgumentException::class.java) {
            DisplayName(thirtyThreeEmoji)
        }
    }
}
