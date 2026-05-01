// Copyright (c) 2026 relexx. BSD 3-Clause License.
// See LICENSE file in the project root for full license information.
package de.relexx.heratalk.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import de.relexx.heratalk.core.model.NetworkQuality
import de.relexx.heratalk.core.ui.R
import de.relexx.heratalk.core.ui.theme.HeraTalkTheme
import de.relexx.heratalk.core.ui.theme.LocalHeraTalkExtraColors

/**
 * Compact pill that visualises a peer's [NetworkQuality] using the traffic-light palette.
 *
 * Renders a coloured dot followed by a localised label. Always visible in screen
 * headers per the UX revision (2026-04-25). Colours come from the active
 * [HeraTalkTheme] — green / yellow / red / grey for good / degraded / poor / offline.
 *
 * @param quality The current quality bucket to display.
 * @param modifier Optional layout modifier; default is no extra layout.
 */
@Composable
public fun NetworkQualityBadge(
    quality: NetworkQuality,
    modifier: Modifier = Modifier,
) {
    val extra = LocalHeraTalkExtraColors.current
    val dotColor: Color =
        when (quality) {
            NetworkQuality.GOOD -> MaterialTheme.colorScheme.primary
            NetworkQuality.DEGRADED -> extra.warning
            NetworkQuality.POOR -> MaterialTheme.colorScheme.error
            NetworkQuality.OFFLINE -> extra.offline
        }
    val labelRes =
        when (quality) {
            NetworkQuality.GOOD -> R.string.network_quality_good
            NetworkQuality.DEGRADED -> R.string.network_quality_degraded
            NetworkQuality.POOR -> R.string.network_quality_poor
            NetworkQuality.OFFLINE -> R.string.network_quality_offline
        }

    Row(
        modifier =
            modifier
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .padding(horizontal = 10.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Box(
            modifier =
                Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(dotColor),
        )
        Text(
            text = stringResource(labelRes),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Preview(name = "Light", showBackground = true)
@Composable
private fun NetworkQualityBadgePreviewLight() {
    HeraTalkTheme(darkTheme = false) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            NetworkQualityBadge(NetworkQuality.GOOD)
            NetworkQualityBadge(NetworkQuality.DEGRADED)
            NetworkQualityBadge(NetworkQuality.POOR)
            NetworkQualityBadge(NetworkQuality.OFFLINE)
        }
    }
}

@Preview(name = "Dark", showBackground = true)
@Composable
private fun NetworkQualityBadgePreviewDark() {
    HeraTalkTheme(darkTheme = true) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            NetworkQualityBadge(NetworkQuality.GOOD)
            NetworkQualityBadge(NetworkQuality.DEGRADED)
            NetworkQualityBadge(NetworkQuality.POOR)
            NetworkQualityBadge(NetworkQuality.OFFLINE)
        }
    }
}
