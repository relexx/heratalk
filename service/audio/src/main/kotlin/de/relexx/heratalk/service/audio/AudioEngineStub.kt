// Copyright (c) 2026 relexx. BSD 3-Clause License.
// See LICENSE file in the project root for full license information.
package de.relexx.heratalk.service.audio

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

/**
 * No-op audio engine used in v0.1.0.
 *
 * No recorder, no track, no JNI bridge. The full pipeline lands in **v0.3.0**
 * (audio loopback milestone).
 */
public class AudioEngineStub : AudioEngine {
    private val frames: MutableSharedFlow<ByteArray> = MutableSharedFlow(extraBufferCapacity = 0)

    override val encodedFrames: Flow<ByteArray> = frames.asSharedFlow()

    override fun startCapture() {
        // TODO(developer): v0.3.0 — open AudioRecord, start Opus encoder loop on Dispatchers.IO.
    }

    override fun stopCapture() {
        // TODO(developer): v0.3.0 — stop AudioRecord, release Opus encoder.
    }

    override fun playFrame(pcm: ShortArray) {
        // TODO(developer): v0.3.0 — push PCM into AudioTrack ring buffer.
    }
}
