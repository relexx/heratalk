// Copyright (c) 2026 relexx. BSD 3-Clause License.
// See LICENSE file in the project root for full license information.
package de.relexx.heratalk.core.model

/**
 * Represents a discovered peer in the HeraTalk mesh.
 *
 * This is a pure domain value with no network or Android dependencies.
 * The network quality is updated reactively by `:service:discovery` as probes complete.
 *
 * @property id Unique identifier for this peer within the channel.
 * @property displayName Human-readable name chosen by the peer's user.
 * @property networkQuality Latest assessed quality of the link to this peer.
 */
public data class Peer(
    public val id: PeerId,
    public val displayName: DisplayName,
    public val networkQuality: NetworkQuality,
)
