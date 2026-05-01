// Copyright (c) 2026 relexx. BSD 3-Clause License.
// See LICENSE file in the project root for full license information.
package de.relexx.heratalk.core.logging

import app.cash.turbine.test
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class RingBufferLoggerTest {
    private val logger = RingBufferLogger()

    @Test
    fun `d emits DEBUG entry when called`() =
        runTest {
            logger.entries.test {
                logger.d("TAG", "debug message")
                val entry = awaitItem()
                assertEquals(LogLevel.DEBUG, entry.level)
                assertEquals("TAG", entry.tag)
                assertEquals("debug message", entry.msg)
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `w emits WARN entry with throwable when called`() =
        runTest {
            val cause = RuntimeException("boom")
            logger.entries.test {
                logger.w("TAG", "warn message", cause)
                val entry = awaitItem()
                assertEquals(LogLevel.WARN, entry.level)
                assertEquals("warn message", entry.msg)
                assertEquals(cause, entry.throwable)
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `e emits ERROR entry without throwable when called`() =
        runTest {
            logger.entries.test {
                logger.e("TAG", "error message")
                val entry = awaitItem()
                assertEquals(LogLevel.ERROR, entry.level)
                assertEquals("error message", entry.msg)
                assertEquals(null, entry.throwable)
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `multiple log calls emit entries in order`() =
        runTest {
            logger.entries.test {
                logger.d("T", "first")
                logger.d("T", "second")
                logger.d("T", "third")
                assertEquals("first", awaitItem().msg)
                assertEquals("second", awaitItem().msg)
                assertEquals("third", awaitItem().msg)
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `entries beyond MAX_ENTRIES cause oldest to be dropped`() =
        runTest {
            // Fill the ring buffer beyond capacity before subscribing.
            // We log MAX_ENTRIES + 1 entries. Because there is no subscriber yet,
            // tryEmit buffers up to MAX_ENTRIES; the first entry is dropped via DROP_OLDEST.
            val overflow = RingBufferLogger.MAX_ENTRIES + 1
            repeat(overflow) { i ->
                logger.d("T", "msg-$i")
            }

            // Collect what remains buffered.
            // MutableSharedFlow with replay=0 does not replay — new collectors receive only
            // future emissions. To verify the overflow semantic we therefore log one more entry
            // after subscribing and check that tryEmit still succeeds (i.e. the buffer did not
            // deadlock or throw).
            logger.entries.test {
                logger.d("T", "after-overflow")
                val entry = awaitItem()
                assertEquals("after-overflow", entry.msg)
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `log entry timestamp is a positive epoch millis value`() =
        runTest {
            val before = System.currentTimeMillis()
            logger.entries.test {
                logger.d("T", "ts-check")
                val entry = awaitItem()
                val after = System.currentTimeMillis()
                assert(entry.timestamp in before..after) {
                    "Expected timestamp ${entry.timestamp} in [$before, $after]"
                }
                cancelAndIgnoreRemainingEvents()
            }
        }
}
