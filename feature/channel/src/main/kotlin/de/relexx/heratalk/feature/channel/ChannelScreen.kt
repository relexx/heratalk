// Copyright (c) 2026 relexx. BSD 3-Clause License.
// See LICENSE file in the project root for full license information.
package de.relexx.heratalk.feature.channel

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import de.relexx.heratalk.core.model.NetworkQuality
import de.relexx.heratalk.core.ui.components.HeraTalkScaffold
import de.relexx.heratalk.core.ui.components.SectionHeader
import de.relexx.heratalk.core.ui.theme.HeraTalkTheme

private val PttButtonSize = 110.dp

/**
 * Main channel screen for the broadcast walkie-talkie experience (UX revision
 * 2026-04-25).
 *
 * **v0.1.0 skeleton:**
 * - [HeraTalkScaffold] header with placeholder channel title and the
 *   ever-visible network indicator.
 * - Peer-list placeholder ("no peers yet") — real roster lands with discovery
 *   in v0.2.0.
 * - Disabled PTT button at the fixed UX-anchor size of 110 dp with a localised
 *   "available in v0.4.0" hint.
 *
 * The button is intentionally a fixed circular shape and disabled — when the
 * audio pipeline arrives in v0.4.0 the same anchor stays so muscle memory is
 * preserved (UX revision 2026-04-25).
 *
 * @param modifier Optional layout modifier.
 */
@Composable
public fun ChannelScreen(modifier: Modifier = Modifier) {
    HeraTalkScaffold(
        title = stringResource(R.string.channel_title_placeholder),
        networkQuality = NetworkQuality.OFFLINE,
        modifier = modifier,
    ) { innerPadding ->
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 24.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            SectionHeader(text = stringResource(R.string.channel_peers_section))
            Text(
                text = stringResource(R.string.channel_empty_roster),
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.fillMaxWidth(),
            )

            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.BottomCenter,
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Button(
                        onClick = {},
                        enabled = false,
                        modifier =
                            Modifier
                                .size(PttButtonSize)
                                .clip(CircleShape),
                    ) {
                        Text(text = stringResource(R.string.channel_ptt_label))
                    }
                    Text(
                        text = stringResource(R.string.channel_ptt_hint_disabled),
                        style = MaterialTheme.typography.labelMedium,
                    )
                }
            }
        }
    }
}

@Preview(name = "Light", showBackground = true)
@Composable
private fun ChannelScreenPreviewLight() {
    HeraTalkTheme(darkTheme = false) {
        ChannelScreen()
    }
}

@Preview(name = "Dark", showBackground = true)
@Composable
private fun ChannelScreenPreviewDark() {
    HeraTalkTheme(darkTheme = true) {
        ChannelScreen()
    }
}
