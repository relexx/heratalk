// Copyright (c) 2026 relexx. BSD 3-Clause License.
package de.relexx.heratalk.core.logging

import org.junit.jupiter.api.Test

/**
 * Smoke tests for [AndroidLogcatLogger].
 *
 * The Android runtime (`android.util.Log`) is not available in the JVM test environment.
 * `mockkStatic(android.util.Log::class)` is excluded here because AGP does not include
 * the Stub version of `android.jar` that would be needed for a successful JVM run;
 * instrumentation tests (src/androidTest) would be required for full platform coverage.
 *
 * What is tested here:
 * - The class can be instantiated without a platform dependency.
 * - The class implements [Logger] (compile-time guarantee, verified at runtime via `is`).
 *
 * A TODO is left for an instrumentation test that exercises real logcat output.
 */
class AndroidLogcatLoggerTest {

    // TODO(developer): add src/androidTest instrumentation test to verify real logcat
    //   output once a device/emulator target is available in CI (Phase F).

    @Test
    fun `AndroidLogcatLogger is a Logger`() {
        val logger: Logger = AndroidLogcatLogger()
        assert(logger is AndroidLogcatLogger)
    }
}
