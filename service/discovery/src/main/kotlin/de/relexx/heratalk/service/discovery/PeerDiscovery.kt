// Copyright (c) 2026 relexx. BSD 3-Clause License.
// See LICENSE file in the project root for full license information.
package de.relexx.heratalk.service.discovery

import de.relexx.heratalk.core.model.Peer
import kotlinx.coroutines.flow.Flow

/**
 * Discovers HeraTalk peers on the local network.
 *
 * The discovery layer is multi-stage (see `docs/architecture.md` §6.1):
 * 1. mDNS/DNS-SD via Android `NsdManager` — primary stage.
 * 2. UDP broadcast beacon — fallback when mDNS is filtered.
 * 3. Manual peer entry — last-resort for hostile networks.
 *
 * This interface abstracts those stages behind a single reactive contract: callers
 * subscribe to [peers] and receive the union of peers found via any stage.
 *
 * **Sanitisation contract:** Implementations MUST sanitise foreign `dname` values
 * before exposing them through [peers] (NFC normalisation, strip bidi overrides,
 * combining-mark cap, codepoint truncation, fallback to `Peer-{first8hex(pk)}`
 * — see `architecture.md` §6.1 and `security-audit.md` F-PRIV-04).
 *
 * The real wiring lands in **v0.2.0**. In v0.1.0 a no-op stub keeps the DI graph
 * shaped correctly so feature modules can already declare their dependency.
 */
public interface PeerDiscovery {
    /**
     * Hot stream of currently known peers.
     *
     * Each emission is a full snapshot of the peer set (rather than a diff) so
     * that consumers can render the channel roster without state-tracking. The
     * empty set is a valid emission (channel is empty / discovery has not yet
     * found anyone).
     */
    public val peers: Flow<Set<Peer>>

    /**
     * Starts mDNS registration plus continuous discovery.
     *
     * Idempotent: calling [start] on an already-started instance is a no-op.
     */
    public fun start()

    /**
     * Tears down all registrations and probes.
     *
     * Idempotent: calling [stop] on an already-stopped instance is a no-op.
     * After [stop] the [peers] flow stops emitting until [start] is called again.
     */
    public fun stop()
}
