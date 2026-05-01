// Copyright (c) 2026 relexx. BSD 3-Clause License.
// See LICENSE file in the project root for full license information.
package de.relexx.heratalk.core.logging

/**
 * Severity level of a log entry.
 */
public enum class LogLevel {
    /** Verbose diagnostic information. */
    DEBUG,

    /** Potentially problematic situation that does not prevent operation. */
    WARN,

    /** Error condition that may indicate a failure. */
    ERROR,
}

/**
 * A single immutable log entry stored in the ring buffer.
 *
 * @property level Severity level of this entry.
 * @property tag Log tag that identifies the source component.
 * @property msg Human-readable log message.
 * @property throwable Optional throwable associated with this entry.
 * @property timestamp Epoch milliseconds at which the entry was created.
 */
public data class LogEntry(
    public val level: LogLevel,
    public val tag: String,
    public val msg: String,
    public val throwable: Throwable?,
    public val timestamp: Long,
)
