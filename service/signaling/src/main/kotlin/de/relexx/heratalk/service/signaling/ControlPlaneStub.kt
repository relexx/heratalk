// Copyright (c) 2026 relexx. BSD 3-Clause License.
// See LICENSE file in the project root for full license information.
package de.relexx.heratalk.service.signaling

import de.relexx.heratalk.core.model.Peer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * No-op control plane used in v0.1.0.
 *
 * [state] stays at [ControlPlaneState.Idle]; [connect] and [disconnect] are
 * no-ops. The real Noise + TCP wiring lands in **v0.5.0**.
 */
public class ControlPlaneStub : ControlPlane {
    private val internalState: MutableStateFlow<ControlPlaneState> = MutableStateFlow(ControlPlaneState.Idle)

    override val state: StateFlow<ControlPlaneState> = internalState.asStateFlow()

    override suspend fun connect(peer: Peer) {
        // TODO(developer): v0.5.0 — open TCP, run Noise handshake, derive SRTP keys via :core:crypto.
    }

    override suspend fun disconnect() {
        // TODO(developer): v0.5.0 — send Bye, tear down TCP, clear pinned keys for the session.
    }
}
