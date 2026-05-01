// Copyright (c) 2026 relexx. BSD 3-Clause License.
// See LICENSE file in the project root for full license information.
package de.relexx.heratalk.service.audio

import kotlinx.coroutines.flow.Flow

/**
 * Audio engine that owns the microphone capture and speaker playback paths
 * plus the libopus JNI bridge (see `docs/architecture.md`, ADR-0003).
 *
 * Capture: `AudioRecord` at 48 kHz mono, 20 ms frames, hardware AEC enabled
 * where available, fed into the Opus encoder (CBR-only, see `.claude/rules.md`
 * Rule 15).
 *
 * Playback: decoded PCM frames pushed into `AudioTrack` with a small ring
 * buffer to absorb scheduling jitter.
 *
 * The real wiring lands in **v0.3.0** with the loopback milestone.
 */
public interface AudioEngine {
    /**
     * Starts microphone capture and Opus encoding.
     *
     * Holds the `RECORD_AUDIO` permission — calling without permission is a
     * programmer error. `:service:lifecycle` must transition the foreground
     * service to `microphone` type before [startCapture] is called.
     */
    public fun startCapture()

    /**
     * Stops microphone capture and releases the recorder.
     *
     * Idempotent.
     */
    public fun stopCapture()

    /**
     * Hot stream of encoded Opus frames produced by the capture pipeline.
     *
     * Each emission is one 20 ms Opus payload ready for `:service:media` to
     * packetise into RTP/SRTP.
     */
    public val encodedFrames: Flow<ByteArray>

    /**
     * Hands a decoded PCM frame to the playback path.
     *
     * @param pcm 16-bit signed PCM mono samples at 48 kHz. Caller is the
     *   `:service:media` mixer.
     */
    public fun playFrame(pcm: ShortArray)
}
