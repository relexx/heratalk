// Copyright (c) 2026 relexx. BSD 3-Clause License.
// See LICENSE file in the project root for full license information.
package de.relexx.heratalk.service.ptt

import de.relexx.heratalk.core.model.PeerId
import kotlinx.coroutines.flow.StateFlow

/**
 * Coordinates the PTT floor in a broadcast channel and orchestrates VOX
 * activation (see `docs/architecture.md` §8 and `releases.md` v0.7.0).
 *
 * Half-duplex PTT means at most one peer holds the floor at a time. The
 * controller arbitrates simultaneous requests deterministically (tie-break by
 * the lowest peer-id), broadcasts grants, and releases the floor on
 * disconnect. VOX adds a parallel mode where local audio activity grabs the
 * floor automatically with hangover.
 *
 * The full state machine lands in **v0.7.0**. The stub keeps [state] at
 * [FloorState.Idle].
 */
public interface FloorController {
    /**
     * Hot, conflated state of the floor.
     */
    public val state: StateFlow<FloorState>

    /**
     * Requests the floor for the local peer.
     *
     * Suspends until the request is granted (state moves to [FloorState.HeldByLocal])
     * or denied (state moves to [FloorState.HeldByRemote]). The function never
     * throws on protocol errors; failure surfaces through the state.
     */
    public suspend fun requestFloor()

    /**
     * Releases the floor held by the local peer.
     *
     * No-op if the floor is held by a remote or idle.
     */
    public suspend fun releaseFloor()
}

/**
 * High-level state of the broadcast floor.
 */
public sealed interface FloorState {
    /** No peer is currently sending. */
    public data object Idle : FloorState

    /** The local peer holds the floor and is sending. */
    public data object HeldByLocal : FloorState

    /**
     * A remote peer holds the floor; local audio is muted while sending.
     *
     * @property holder Peer that currently owns the broadcast floor.
     */
    public data class HeldByRemote(
        public val holder: PeerId,
    ) : FloorState
}
