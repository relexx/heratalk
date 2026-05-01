// Copyright (c) 2026 relexx. BSD 3-Clause License.
// See LICENSE file in the project root for full license information.
package de.relexx.heratalk.service.transport

import de.relexx.heratalk.core.model.PeerId
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

/**
 * No-op transport implementation used in v0.1.0.
 *
 * Calls to [send] are silently dropped, [incoming] never emits. Real UDP and
 * relay transports follow in v0.2.0 / v0.10.0.
 */
public class TransportStub : Transport {
    private val flow: MutableSharedFlow<TransportPacket> = MutableSharedFlow(extraBufferCapacity = 0)

    override val incoming: Flow<TransportPacket> = flow.asSharedFlow()

    override suspend fun send(
        peer: PeerId,
        payload: ByteArray,
    ) {
        // TODO(developer): v0.2.0 — pick transport (UDP unicast first), enqueue to socket writer.
    }
}
