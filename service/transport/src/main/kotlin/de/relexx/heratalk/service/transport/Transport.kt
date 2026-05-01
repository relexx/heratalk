// Copyright (c) 2026 relexx. BSD 3-Clause License.
// See LICENSE file in the project root for full license information.
package de.relexx.heratalk.service.transport

import de.relexx.heratalk.core.model.PeerId
import kotlinx.coroutines.flow.Flow

/**
 * Multi-transport engine that hides UDP unicast, UDP broadcast and TCP relay
 * behind a single send/receive contract (see `docs/architecture.md` §7).
 *
 * Callers (`:service:media`, `:service:signaling`) hand opaque byte buffers to
 * [send] addressed to a [PeerId]; the implementation picks the best available
 * path according to the transport cascade and reports back through [incoming]
 * with the source peer attached.
 *
 * The real implementation lands incrementally:
 * - **v0.2.0** — UDP unicast send/receive.
 * - **v0.4.0** — UDP broadcast for channel media.
 * - **v0.10.0** — TCP relay fallback when UDP fails.
 */
public interface Transport {
    /**
     * Sends an opaque payload to [peer].
     *
     * The implementation chooses the transport (UDP unicast → broadcast →
     * relay) and may queue the payload while a path is being probed.
     *
     * @param peer Target peer.
     * @param payload Opaque bytes — typically an SRTP-protected RTP frame or
     *   a control-plane message. The transport never inspects the payload.
     */
    public suspend fun send(
        peer: PeerId,
        payload: ByteArray,
    )

    /**
     * Hot stream of inbound payloads with their source peer attached.
     *
     * The flow does not buffer beyond one element per peer to keep memory flat
     * for fast senders; consumers are expected to keep up.
     */
    public val incoming: Flow<TransportPacket>
}

/**
 * One inbound payload from [source] together with the bytes received.
 *
 * @property source Peer that produced the payload.
 * @property payload Opaque bytes as received from the wire. The transport does
 *   not validate or decrypt them.
 */
public data class TransportPacket(
    public val source: PeerId,
    public val payload: ByteArray,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is TransportPacket) return false
        return source == other.source && payload.contentEquals(other.payload)
    }

    override fun hashCode(): Int = 31 * source.hashCode() + payload.contentHashCode()
}
