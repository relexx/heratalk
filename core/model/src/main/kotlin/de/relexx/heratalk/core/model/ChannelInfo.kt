// Copyright (c) 2026 relexx. BSD 3-Clause License.
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
 */
public data class ChannelInfo(
    public val id: ChannelId,
    public val displayName: DisplayName,
    public val memberCount: Int,
)
