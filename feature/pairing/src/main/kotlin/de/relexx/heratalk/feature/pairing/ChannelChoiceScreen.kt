// Copyright (c) 2026 relexx. BSD 3-Clause License.
// See LICENSE file in the project root for full license information.
package de.relexx.heratalk.feature.pairing

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
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
 * First screen of the pairing flow.
 *
 * Asks the user whether they want to join an existing channel or create a new
 * one. Both buttons advance to the same next step ([DisplayNameScreen]); the
 * branch only matters once the QR-scanner / QR-generator lands in v0.5.0.
 *
 * No side effects, no view-model — the screen is a pure UI dispatcher.
 *
 * @param onJoinExisting Invoked when the user picks "Join an existing channel".
 * @param onCreateNew Invoked when the user picks "Create a new channel".
 * @param modifier Optional layout modifier for placement inside a NavHost slot.
 */
@Composable
public fun ChannelChoiceScreen(
    onJoinExisting: () -> Unit,
    onCreateNew: () -> Unit,
    modifier: Modifier = Modifier,
) {
    HeraTalkScaffold(
        title = stringResource(R.string.pairing_choice_title),
        networkQuality = NetworkQuality.OFFLINE,
        modifier = modifier,
    ) { innerPadding ->
        ChannelChoiceContent(
            innerPadding = innerPadding,
            onJoinExisting = onJoinExisting,
            onCreateNew = onCreateNew,
        )
    }
}

@Composable
private fun ChannelChoiceContent(
    innerPadding: PaddingValues,
    onJoinExisting: () -> Unit,
    onCreateNew: () -> Unit,
) {
    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp, vertical = 32.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = stringResource(R.string.pairing_choice_intro),
            style = MaterialTheme.typography.bodyLarge,
        )
        Button(
            onClick = onJoinExisting,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(text = stringResource(R.string.pairing_choice_join))
        }
        OutlinedButton(
            onClick = onCreateNew,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(text = stringResource(R.string.pairing_choice_create))
        }
    }
}

@Preview(name = "Light", showBackground = true)
@Composable
private fun ChannelChoiceScreenPreviewLight() {
    HeraTalkTheme(darkTheme = false) {
        ChannelChoiceScreen(onJoinExisting = {}, onCreateNew = {})
    }
}

@Preview(name = "Dark", showBackground = true)
@Composable
private fun ChannelChoiceScreenPreviewDark() {
    HeraTalkTheme(darkTheme = true) {
        ChannelChoiceScreen(onJoinExisting = {}, onCreateNew = {})
    }
}
