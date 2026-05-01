// Copyright (c) 2026 relexx. BSD 3-Clause License.
// See LICENSE file in the project root for full license information.
package de.relexx.heratalk.service.relay

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

/**
 * No-op relay implementation used in v0.1.0.
 *
 * Real relay routing lands in **v0.10.0** once the transport cascade is
 * complete and AP-isolation simulation tests are in place.
 */
public class RelayServiceStub : RelayService {
    private val flow: MutableSharedFlow<RelayOffer> = MutableSharedFlow(extraBufferCapacity = 0)

    override val offers: Flow<RelayOffer> = flow.asSharedFlow()

    override fun advertiseRelay() {
        // TODO(developer): v0.10.0 — broadcast RelayOffer on the control plane.
    }

    override fun stopRelay() {
        // TODO(developer): v0.10.0 — withdraw offer, tear down active tunnels.
    }
}
