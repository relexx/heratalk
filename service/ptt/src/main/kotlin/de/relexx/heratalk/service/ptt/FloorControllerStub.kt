// Copyright (c) 2026 relexx. BSD 3-Clause License.
// See LICENSE file in the project root for full license information.
package de.relexx.heratalk.service.ptt

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * No-op floor controller used in v0.1.0.
 *
 * State stays at [FloorState.Idle], request/release are no-ops. Real PTT/VOX
 * arbitration lands in **v0.7.0**.
 */
public class FloorControllerStub : FloorController {
    private val internalState: MutableStateFlow<FloorState> = MutableStateFlow(FloorState.Idle)

    override val state: StateFlow<FloorState> = internalState.asStateFlow()

    override suspend fun requestFloor() {
        // TODO(developer): v0.7.0 — send FloorRequest on the control plane and await Grant/Busy.
    }

    override suspend fun releaseFloor() {
        // TODO(developer): v0.7.0 — send FloorRelease on the control plane and reset to Idle.
    }
}
