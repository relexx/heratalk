// Copyright (c) 2026 relexx. BSD 3-Clause License.
// See LICENSE file in the project root for full license information.
package de.relexx.heratalk.core.logging

import android.util.Log

/**
 * Android-platform adapter that forwards log calls to [android.util.Log].
 *
 * Per ADR-0004: this file is the only place in `:core:logging` that imports Android SDK symbols.
 * It MUST NOT be referenced directly by callers outside of DI setup — use [Logger] instead.
 *
 * Visibility is `internal` to prevent direct instantiation outside of this module.
 * The Koin module in Phase D will expose it via `Logger` binding.
 *
 * @see Logger
 */
public class AndroidLogcatLogger : Logger {
    override fun d(
        tag: String,
        msg: String,
    ) {
        Log.d(tag, msg)
    }

    override fun w(
        tag: String,
        msg: String,
        throwable: Throwable?,
    ) {
        if (throwable != null) {
            Log.w(tag, msg, throwable)
        } else {
            Log.w(tag, msg)
        }
    }

    override fun e(
        tag: String,
        msg: String,
        throwable: Throwable?,
    ) {
        if (throwable != null) {
            Log.e(tag, msg, throwable)
        } else {
            Log.e(tag, msg)
        }
    }
}
