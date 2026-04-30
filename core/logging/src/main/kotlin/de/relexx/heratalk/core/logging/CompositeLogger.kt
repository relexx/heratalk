// Copyright (c) 2026 relexx. BSD 3-Clause License.
package de.relexx.heratalk.core.logging

/**
 * A [Logger] that forwards every call to all delegates in [loggers].
 *
 * Delegates are called in list order. Exceptions thrown by a delegate
 * propagate to the caller — no silent swallowing.
 *
 * @property loggers Ordered list of loggers to receive each log call.
 */
public class CompositeLogger(
    private val loggers: List<Logger>,
) : Logger {

    override fun d(tag: String, msg: String) {
        loggers.forEach { it.d(tag, msg) }
    }

    override fun w(tag: String, msg: String, throwable: Throwable?) {
        loggers.forEach { it.w(tag, msg, throwable) }
    }

    override fun e(tag: String, msg: String, throwable: Throwable?) {
        loggers.forEach { it.e(tag, msg, throwable) }
    }
}
