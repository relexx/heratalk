// Copyright (c) 2026 relexx. BSD 3-Clause License.
// See LICENSE file in the project root for full license information.
package de.relexx.heratalk.service.signaling

import de.relexx.heratalk.core.model.Peer
import kotlinx.coroutines.flow.StateFlow

/**
 * TCP-based control plane that performs the Noise handshake and exchanges
 * non-media protocol messages (see `docs/architecture.md` §6.2).
 *
 * Two phase-distinct Noise patterns live behind the same engine (see ADR-0002):
 * - `Noise_XXpsk2_25519_ChaChaPoly_SHA256` — initial pairing.
 * - `Noise_KKpsk0_25519_ChaChaPoly_SHA256` — regular sessions with pinned
 *   static keys.
 *
 * The real wiring lands in **v0.5.0**. In v0.1.0 the [ControlPlaneStub] keeps
 * [state] at [ControlPlaneState.Idle] forever.
 */
public interface ControlPlane {
    /**
     * Hot, conflated state of the control-plane session.
     *
     * Consumers (UI, transport) react to transitions to drive
     * "connecting…" / "authenticated" / "disconnected" indicators.
     */
    public val state: StateFlow<ControlPlaneState>

    /**
     * Initiates a Noise handshake with [peer].
     *
     * Suspends until the handshake completes (success → state moves to
     * [ControlPlaneState.Connected]) or fails (state moves to
     * [ControlPlaneState.Failed]). Either outcome is reflected through
     * [state]; the function itself does not throw on protocol errors —
     * exceptions are reserved for programmer mistakes.
     *
     * @param peer Target peer; must already be discovered via
     *   `:service:discovery`.
     */
    public suspend fun connect(peer: Peer)

    /**
     * Tears down the control-plane connection.
     *
     * Idempotent.
     */
    public suspend fun disconnect()
}

/**
 * High-level state of the control-plane session.
 */
public sealed interface ControlPlaneState {
    /** No connection attempt in flight. */
    public data object Idle : ControlPlaneState

    /**
     * Noise handshake in progress with [peer].
     *
     * @property peer Remote peer the handshake is being established with.
     */
    public data class Connecting(
        public val peer: Peer,
    ) : ControlPlaneState

    /**
     * Handshake completed; SRTP keys derived.
     *
     * @property peer Authenticated peer on the other end of this session.
     */
    public data class Connected(
        public val peer: Peer,
    ) : ControlPlaneState

    /**
     * Connection torn down or never established.
     *
     * @property reason Human-readable failure cause, used for logging only —
     *   never surfaced verbatim in user-facing UI.
     */
    public data class Failed(
        public val reason: String,
    ) : ControlPlaneState
}
