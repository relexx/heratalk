// Copyright (c) 2026 relexx. BSD 3-Clause License.
// See LICENSE file in the project root for full license information.
package de.relexx.heratalk.service.media

import de.relexx.heratalk.core.model.PeerId
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

/**
 * No-op media engine used in v0.1.0.
 *
 * Calls to [sendFrame] are dropped, [decodedAudio] never emits. The unencrypted
 * RTP path arrives in **v0.4.0**, SRTP wrapping in **v0.6.0**.
 */
public class MediaEngineStub : MediaEngine {
    private val flow: MutableSharedFlow<DecodedFrame> = MutableSharedFlow(extraBufferCapacity = 0)

    override val decodedAudio: Flow<DecodedFrame> = flow.asSharedFlow()

    override fun startReceive() {
        // TODO(developer): v0.4.0 — start RTP receive socket, hand frames to jitter buffer.
    }

    override fun stopReceive() {
        // TODO(developer): v0.4.0 — drain jitter buffer, close receive socket.
    }

    override suspend fun sendFrame(
        frame: ByteArray,
        target: PeerId?,
    ) {
        // TODO(developer): v0.4.0 — packetise into RTP, route via :service:transport.
        //  v0.6.0 — wrap into SRTP via :core:crypto using the per-stream send key.
    }
}
