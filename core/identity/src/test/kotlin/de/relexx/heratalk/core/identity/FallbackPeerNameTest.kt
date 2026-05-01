// Copyright (c) 2026 relexx. BSD 3-Clause License.
// See LICENSE file in the project root for full license information.
package de.relexx.heratalk.core.identity

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class FallbackPeerNameTest {
    @Test
    fun `fallbackPeerName returns Peer- prefix followed by first 8 hex chars of pk`() {
        // 0xAB = 171, 0xCD = 205, 0x12 = 18, 0x34 = 52 → "abcd1234"
        val pk = byteArrayOf(0xAB.toByte(), 0xCD.toByte(), 0x12.toByte(), 0x34.toByte())
        assertEquals("Peer-abcd1234", fallbackPeerName(pk))
    }

    @Test
    fun `fallbackPeerName uses only first 8 hex chars when pk is longer`() {
        // 8 bytes → 16 hex chars; only first 8 should appear in the result
        val pk =
            byteArrayOf(
                0xDE.toByte(),
                0xAD.toByte(),
                0xBE.toByte(),
                0xEF.toByte(),
                0x00.toByte(),
                0x11.toByte(),
                0x22.toByte(),
                0x33.toByte(),
            )
        // full hex: "deadbeef00112233" → first 8: "deadbeef"
        assertEquals("Peer-deadbeef", fallbackPeerName(pk))
    }

    @Test
    fun `fallbackPeerName uses all available hex chars when pk has fewer than 4 bytes`() {
        // 1 byte → 2 hex chars — shorter than 8, so the whole hex string is used
        val pk = byteArrayOf(0xFF.toByte())
        assertEquals("Peer-ff", fallbackPeerName(pk))
    }

    @Test
    fun `fallbackPeerName returns Peer- with empty suffix for empty pk`() {
        assertEquals("Peer-", fallbackPeerName(byteArrayOf()))
    }

    @Test
    fun `fallbackPeerName pads single-digit byte values with leading zero`() {
        // 0x01 must render as "01", not "1"
        val pk = byteArrayOf(0x01.toByte(), 0x02.toByte(), 0x03.toByte(), 0x04.toByte())
        assertEquals("Peer-01020304", fallbackPeerName(pk))
    }

    @Test
    fun `fallbackPeerName uses lowercase hex digits`() {
        val pk = byteArrayOf(0xAB.toByte(), 0xCD.toByte(), 0xEF.toByte(), 0x00.toByte())
        val result = fallbackPeerName(pk)
        assertEquals("Peer-abcdef00", result)
        // The assertEquals above already proves the hex suffix is lowercase; the "Peer-" prefix is intentionally mixed-case.
        val suffix = result.removePrefix("Peer-")
        assertEquals(suffix, suffix.lowercase()) { "hex digits must be lowercase" }
    }
}
