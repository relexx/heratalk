// Copyright (c) 2026 relexx. BSD 3-Clause License.
// See LICENSE file in the project root for full license information.
package de.relexx.heratalk.service.media

import de.relexx.heratalk.core.model.PeerId
import kotlinx.coroutines.flow.Flow

/**
 * Media engine for SRTP send/receive, jitter buffering and mixer logic
 * (see `docs/architecture.md` §6 and §11).
 *
 * The engine wraps the security-critical SRTP path: it MUST be the only place
 * (beside `:core:crypto`) that touches raw SRTP keys (see `.claude/rules.md`
 * Rule 4). Until the SRTP code lands the [MediaEngineStub] keeps the contract
 * shaped without touching keys.
 *
 * Implementation phases:
 * - **v0.4.0** — RTP packetiser, jitter buffer, mixer. Unencrypted.
 * - **v0.6.0** — SRTP wrapping with rekey handling.
 */
public interface MediaEngine {
    /**
     * Starts the receive pipeline so that [decodedAudio] begins emitting frames.
     *
     * Idempotent.
     */
    public fun startReceive()

    /**
     * Stops the receive pipeline.
     *
     * Idempotent.
     */
    public fun stopReceive()

    /**
     * Sends one Opus frame to all connected peers (broadcast) or a specific
     * peer (direct call) — implementations resolve the routing.
     *
     * @param frame Encoded Opus payload (already produced by `:service:audio`).
     * @param target Optional direct-call recipient; `null` means broadcast.
     */
    public suspend fun sendFrame(
        frame: ByteArray,
        target: PeerId?,
    )

    /**
     * Hot stream of decoded PCM frames ready for `AudioTrack` playback.
     */
    public val decodedAudio: Flow<DecodedFrame>
}

/**
 * One decoded audio frame from a peer.
 *
 * @property source Peer that produced the frame (for mixer accounting).
 * @property pcm 16-bit signed PCM mono samples at 48 kHz.
 */
public data class DecodedFrame(
    public val source: PeerId,
    public val pcm: ShortArray,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is DecodedFrame) return false
        return source == other.source && pcm.contentEquals(other.pcm)
    }

    override fun hashCode(): Int = 31 * source.hashCode() + pcm.contentHashCode()
}
