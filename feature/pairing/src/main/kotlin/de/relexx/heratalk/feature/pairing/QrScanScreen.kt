// Copyright (c) 2026 relexx. BSD 3-Clause License.
// See LICENSE file in the project root for full license information.
package de.relexx.heratalk.feature.pairing

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import de.relexx.heratalk.core.model.NetworkQuality
import de.relexx.heratalk.core.ui.components.HeraTalkScaffold
import de.relexx.heratalk.core.ui.theme.HeraTalkTheme

/**
 * QR-scanner stub for the pairing flow.
 *
 * v0.1.0 ships only a placeholder screen so the navigation graph already has
 * the slot. The real ML Kit barcode integration plus channel-secret import
 * land with **v0.5.0** (`docs/releases.md` v0.5.0).
 *
 * The screen exposes a single "back" button to keep the user from getting
 * stuck.
 *
 * @param onBack Invoked when the user taps the back button.
 * @param modifier Optional layout modifier.
 */
@Composable
public fun QrScanScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    HeraTalkScaffold(
        title = stringResource(R.string.pairing_qr_title),
        networkQuality = NetworkQuality.OFFLINE,
        modifier = modifier,
    ) { innerPadding ->
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 24.dp, vertical = 32.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp, Alignment.CenterVertically),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = stringResource(R.string.pairing_qr_coming_soon),
                style = MaterialTheme.typography.bodyLarge,
            )
            OutlinedButton(
                onClick = onBack,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(text = stringResource(R.string.pairing_qr_back))
            }
        }
    }
}

@Preview(name = "Light", showBackground = true)
@Composable
private fun QrScanScreenPreviewLight() {
    HeraTalkTheme(darkTheme = false) {
        QrScanScreen(onBack = {})
    }
}

@Preview(name = "Dark", showBackground = true)
@Composable
private fun QrScanScreenPreviewDark() {
    HeraTalkTheme(darkTheme = true) {
        QrScanScreen(onBack = {})
    }
}
