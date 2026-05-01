// Copyright (c) 2026 relexx. BSD 3-Clause License.
package de.relexx.heratalk.core.crypto

import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test

/**
 * Smoketest: the v0.1.0 skeleton must throw [NotImplementedError] on every
 * call so that any accidental wiring before v0.6.0 fails loudly.
 */
class StubAeadTest {

    private val key = ByteArray(32)
    private val nonce = ByteArray(12)
    private val payload = byteArrayOf(0x01, 0x02, 0x03)
    private val aad = byteArrayOf(0x10, 0x20)

    @Test
    fun `seal throws NotImplementedError`() {
        val subject = StubAead()
        assertThrows(NotImplementedError::class.java) {
            subject.seal(key, nonce, payload, aad)
        }
    }

    @Test
    fun `open throws NotImplementedError`() {
        val subject = StubAead()
        assertThrows(NotImplementedError::class.java) {
            subject.open(key, nonce, payload, aad)
        }
    }
}
