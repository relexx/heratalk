// Copyright (c) 2026 relexx. BSD 3-Clause License.
// See LICENSE file in the project root for full license information.
package de.relexx.heratalk.core.logging

/**
 * Pure domain API for structured logging.
 *
 * This interface has no Android or platform imports. Adapters live in
 * `AndroidLogcatLogger.kt` (see ADR-0004). Callers depend only on this interface.
 */
public interface Logger {
    /**
     * Logs a debug-level message.
     *
     * @param tag Log tag, typically the simple class name of the caller.
     * @param msg The message to log.
     */
    public fun d(
        tag: String,
        msg: String,
    )

    /**
     * Logs a warning-level message, optionally with an associated throwable.
     *
     * @param tag Log tag, typically the simple class name of the caller.
     * @param msg The message to log.
     * @param throwable Optional throwable to include in the log entry.
     */
    public fun w(
        tag: String,
        msg: String,
        throwable: Throwable? = null,
    )

    /**
     * Logs an error-level message, optionally with an associated throwable.
     *
     * @param tag Log tag, typically the simple class name of the caller.
     * @param msg The message to log.
     * @param throwable Optional throwable to include in the log entry.
     */
    public fun e(
        tag: String,
        msg: String,
        throwable: Throwable? = null,
    )
}
