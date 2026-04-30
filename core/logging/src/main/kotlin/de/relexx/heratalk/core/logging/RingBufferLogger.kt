// Copyright (c) 2026 relexx. BSD 3-Clause License.
package de.relexx.heratalk.core.logging

import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

/**
 * In-memory ring-buffer logger that retains the last [MAX_ENTRIES] log entries.
 *
 * Entries are emitted as a [Flow] via [entries]. Collectors receive only entries
 * produced after they subscribe — there is no replay of historical entries beyond
 * what is buffered inside the [MutableSharedFlow].
 *
 * Thread safety is provided entirely by [MutableSharedFlow] which serialises
 * emissions internally. No `synchronized` blocks or locks are used.
 *
 * This logger is scope-independent: it does not own a [kotlinx.coroutines.CoroutineScope]
 * and does not launch coroutines. The [MutableSharedFlow.tryEmit] call is non-suspending
 * and safe to call from any thread without a coroutine context.
 *
 * Note: `GlobalScope` is explicitly not used here (see `.claude/rules.md` Rule 19).
 */
public class RingBufferLogger : Logger {

    public companion object {
        /** Maximum number of log entries retained in the ring buffer. */
        public const val MAX_ENTRIES: Int = 1000
    }

    // MutableSharedFlow with extraBufferCapacity = MAX_ENTRIES and DROP_OLDEST overflow
    // models the ring-buffer semantic: when the buffer is full, the oldest entry is
    // silently dropped and the newest entry is accepted.
    private val _entries: MutableSharedFlow<LogEntry> = MutableSharedFlow(
        replay = 0,
        extraBufferCapacity = MAX_ENTRIES,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )

    /** Emits each [LogEntry] as it is produced. No replay of historical entries. */
    public val entries: Flow<LogEntry> = _entries.asSharedFlow()

    override fun d(tag: String, msg: String) {
        emit(LogLevel.DEBUG, tag, msg, null)
    }

    override fun w(tag: String, msg: String, throwable: Throwable?) {
        emit(LogLevel.WARN, tag, msg, throwable)
    }

    override fun e(tag: String, msg: String, throwable: Throwable?) {
        emit(LogLevel.ERROR, tag, msg, throwable)
    }

    private fun emit(level: LogLevel, tag: String, msg: String, throwable: Throwable?) {
        val entry = LogEntry(
            level = level,
            tag = tag,
            msg = msg,
            throwable = throwable,
            timestamp = System.currentTimeMillis(),
        )
        // tryEmit never fails here: BufferOverflow.DROP_OLDEST ensures there is always space.
        _entries.tryEmit(entry)
    }
}
