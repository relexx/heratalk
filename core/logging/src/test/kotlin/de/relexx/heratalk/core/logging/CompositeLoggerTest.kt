// Copyright (c) 2026 relexx. BSD 3-Clause License.
package de.relexx.heratalk.core.logging

import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test

class CompositeLoggerTest {

    private val delegate1 = mockk<Logger>(relaxed = true)
    private val delegate2 = mockk<Logger>(relaxed = true)
    private val composite = CompositeLogger(listOf(delegate1, delegate2))

    @Test
    fun `d forwards call to all delegates`() {
        composite.d("TAG", "hello")

        verify(exactly = 1) { delegate1.d("TAG", "hello") }
        verify(exactly = 1) { delegate2.d("TAG", "hello") }
    }

    @Test
    fun `w forwards call with throwable to all delegates`() {
        val ex = RuntimeException("oops")
        composite.w("TAG", "warning", ex)

        verify(exactly = 1) { delegate1.w("TAG", "warning", ex) }
        verify(exactly = 1) { delegate2.w("TAG", "warning", ex) }
    }

    @Test
    fun `w forwards call without throwable to all delegates`() {
        composite.w("TAG", "warning")

        verify(exactly = 1) { delegate1.w("TAG", "warning", null) }
        verify(exactly = 1) { delegate2.w("TAG", "warning", null) }
    }

    @Test
    fun `e forwards call with throwable to all delegates`() {
        val ex = IllegalStateException("error")
        composite.e("TAG", "error msg", ex)

        verify(exactly = 1) { delegate1.e("TAG", "error msg", ex) }
        verify(exactly = 1) { delegate2.e("TAG", "error msg", ex) }
    }

    @Test
    fun `e forwards call without throwable to all delegates`() {
        composite.e("TAG", "bare error")

        verify(exactly = 1) { delegate1.e("TAG", "bare error", null) }
        verify(exactly = 1) { delegate2.e("TAG", "bare error", null) }
    }

    @Test
    fun `empty delegate list does not throw`() {
        val empty = CompositeLogger(emptyList())
        empty.d("T", "msg")
        empty.w("T", "msg")
        empty.e("T", "msg")
        // no exception expected
    }

    @Test
    fun `delegates are called in list order`() {
        val callOrder = mutableListOf<Int>()
        val first = object : Logger {
            override fun d(tag: String, msg: String) { callOrder += 1 }
            override fun w(tag: String, msg: String, throwable: Throwable?) { callOrder += 1 }
            override fun e(tag: String, msg: String, throwable: Throwable?) { callOrder += 1 }
        }
        val second = object : Logger {
            override fun d(tag: String, msg: String) { callOrder += 2 }
            override fun w(tag: String, msg: String, throwable: Throwable?) { callOrder += 2 }
            override fun e(tag: String, msg: String, throwable: Throwable?) { callOrder += 2 }
        }
        val ordered = CompositeLogger(listOf(first, second))
        ordered.d("T", "msg")

        assert(callOrder == listOf(1, 2)) { "Expected [1, 2] but got $callOrder" }
    }
}
