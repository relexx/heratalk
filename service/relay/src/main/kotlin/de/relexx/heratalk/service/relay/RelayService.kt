// Copyright (c) 2026 relexx. BSD 3-Clause License.
// See LICENSE file in the project root for full license information.
package de.relexx.heratalk.service.relay

import de.relexx.heratalk.core.model.Peer
import kotlinx.coroutines.flow.Flow

/**
 * Relay service that overcomes AP client-isolation by routing media through a
 * third peer (see `docs/architecture.md` §7 and `releases.md` v0.10.0).
 *
 * The outer relay frame wraps the SRTP-protected payload so the relay peer
 * cannot decrypt the audio — end-to-end encryption is preserved.
 *
 * Real wiring lands in **v0.10.0**.
 */
public interface RelayService {
    /**
     * Publishes a `RelayOffer` so other peers learn we can serve as a relay.
     */
    public fun advertiseRelay()

    /**
     * Stops advertising and tears down active relay tunnels.
     */
    public fun stopRelay()

    /**
     * Hot stream of relay offers received from other peers.
     */
    public val offers: Flow<RelayOffer>
}

/**
 * Announcement that [from] is willing to relay traffic for the local peer.
 *
 * @property from Peer offering relay capability.
 */
public data class RelayOffer(
    public val from: Peer,
)
