// Copyright (c) 2026 relexx. BSD 3-Clause License.
package de.relexx.heratalk.core.model

/**
 * Assessed quality of a peer's network connection.
 *
 * UI components (e.g. `NetworkQualityBadge`) map these to colour-coded indicators
 * (green / yellow / red / grey). String resources use keys `network_quality_good`,
 * `network_quality_degraded`, `network_quality_poor`, `network_quality_offline`.
 */
public enum class NetworkQuality {
    /** Low latency and no measurable packet loss. */
    GOOD,

    /** Moderate latency or occasional packet loss; audio may be slightly impaired. */
    DEGRADED,

    /** High latency or significant packet loss; audio quality is severely degraded. */
    POOR,

    /** Peer is unreachable or the connection has been lost. */
    OFFLINE,
}
