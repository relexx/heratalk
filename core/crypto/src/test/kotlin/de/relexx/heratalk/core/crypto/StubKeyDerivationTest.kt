// Copyright (c) 2026 relexx. BSD 3-Clause License.
package de.relexx.heratalk.core.crypto

import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test

/**
 * Smoketest: the v0.1.0 skeleton must throw [NotImplementedError] on every
 * call so that any accidental wiring before v0.5.0 fails loudly.
 */
class StubKeyDerivationTest {

    private val sharedSecret = ByteArray(32)

    @Test
    fun `deriveSrtpKey throws NotImplementedError for broadcast send`() {
        val subject = StubKeyDerivation()
        assertThrows(NotImplementedError::class.java) {
            subject.deriveSrtpKey(sharedSecret, StreamType.BROADCAST, KeyDirection.SEND)
        }
    }

    @Test
    fun `deriveSrtpKey throws NotImplementedError for direct receive`() {
        val subject = StubKeyDerivation()
        assertThrows(NotImplementedError::class.java) {
            subject.deriveSrtpKey(sharedSecret, StreamType.DIRECT, KeyDirection.RECEIVE)
        }
    }
}
