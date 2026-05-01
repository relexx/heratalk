// Copyright (c) 2026 relexx. BSD 3-Clause License.
// See LICENSE file in the project root for full license information.
package de.relexx.heratalk.core.model

/**
 * Metadata about a HeraTalk channel as seen by the local peer.
 *
 * [memberCount] reflects the number of peers currently active in the channel,
 * including the local peer. The value is updated reactively by `:service:discovery`.
 *
 * @property id Unique identifier of the channel.
 * @property displayName Human-readable name for the channel, shown in the channel header.
 * @property memberCount Number of peers (including the local peer) currently in the channel.
 *   Must be ≥ 1 (the local peer itself) and ≤ [MAX_MEMBERS].
 */
public data class ChannelInfo(
    public val id: ChannelId,
    public val displayName: DisplayName,
    public val memberCount: Int,
) {
    init {
        require(memberCount >= 1) { "memberCount must be ≥ 1, was $memberCount" }
        // MAX_MEMBERS is a conservative LAN limit; will be aligned with the protocol spec
        // once a hard mesh-size ceiling is defined (see architecture.md §4).
        require(memberCount <= MAX_MEMBERS) { "memberCount must be ≤ $MAX_MEMBERS, was $memberCount" }
    }

    public companion object {
        /** Conservative upper bound for LAN mesh size; protocol-level ceiling TBD. */
        public const val MAX_MEMBERS: Int = 50
    }
}
