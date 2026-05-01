// Copyright (c) 2026 relexx. BSD 3-Clause License.
// See LICENSE file in the project root for full license information.
package de.relexx.heratalk.service.discovery

import de.relexx.heratalk.core.model.Peer
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * No-op discovery implementation used in v0.1.0.
 *
 * The real `NsdManager`-backed adapter and the UDP-broadcast beacon land in
 * **v0.2.0** (see `docs/releases.md`). Until then this stub:
 * - emits an empty [Set] of peers,
 * - logs nothing,
 * - and treats [start]/[stop] as no-ops.
 *
 * The empty flow is intentional: feature modules that observe [peers] should
 * render an empty roster without special-casing for "discovery not yet
 * implemented".
 */
public class PeerDiscoveryStub : PeerDiscovery {
    private val state: MutableStateFlow<Set<Peer>> = MutableStateFlow(emptySet())

    override val peers: Flow<Set<Peer>> = state.asStateFlow()

    override fun start() {
        // TODO(developer): v0.2.0 — register NsdManager service and subscribe to
        //  IdentityRepository.displayName for reactive re-registration.
    }

    override fun stop() {
        // TODO(developer): v0.2.0 — unregister NsdManager service and cancel beacon job.
    }
}
