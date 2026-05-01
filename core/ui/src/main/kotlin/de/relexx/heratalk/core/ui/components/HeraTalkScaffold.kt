// Copyright (c) 2026 relexx. BSD 3-Clause License.
package de.relexx.heratalk.core.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import de.relexx.heratalk.core.model.NetworkQuality
import de.relexx.heratalk.core.ui.theme.HeraTalkTheme

/**
 * Project-standard scaffold for full-screen surfaces.
 *
 * Couples a Material 3 [Scaffold] with the project's required header anatomy:
 * - title on the leading side
 * - [NetworkQualityBadge] on the trailing side ("network indicator always visible",
 *   per UX revision 2026-04-25)
 *
 * Callers supply the screen content via [content], which receives the inner padding
 * coming from the scaffold so list-like content can edge-align under the bar.
 *
 * @param title Already-resolved screen title. Callers MUST pass a value coming
 *   from `stringResource(...)`.
 * @param networkQuality Current quality bucket displayed in the trailing badge.
 * @param modifier Optional layout modifier.
 * @param content Body composable. Receives padding to honour the top app bar.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
public fun HeraTalkScaffold(
    title: String,
    networkQuality: NetworkQuality,
    modifier: Modifier = Modifier,
    content: @Composable (PaddingValues) -> Unit,
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleLarge,
                    )
                },
                actions = {
                    NetworkQualityBadge(
                        quality = networkQuality,
                        modifier = Modifier.padding(end = 12.dp),
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                ),
            )
        },
        content = { innerPadding -> content(innerPadding) },
    )
}

// Preview literals are not user-visible at runtime — no i18n required.
@Suppress("HardcodedText")
@Preview(name = "Light", showBackground = true)
@Composable
private fun HeraTalkScaffoldPreviewLight() {
    HeraTalkTheme(darkTheme = false) {
        HeraTalkScaffold(
            title = "Werkstatt Nord",
            networkQuality = NetworkQuality.GOOD,
        ) { innerPadding ->
            Box(modifier = Modifier.padding(innerPadding)) {
                Text(text = "Body content")
            }
        }
    }
}

// Preview literals are not user-visible at runtime — no i18n required.
@Suppress("HardcodedText")
@Preview(name = "Dark", showBackground = true)
@Composable
private fun HeraTalkScaffoldPreviewDark() {
    HeraTalkTheme(darkTheme = true) {
        HeraTalkScaffold(
            title = "Werkstatt Nord",
            networkQuality = NetworkQuality.DEGRADED,
        ) { innerPadding ->
            Box(modifier = Modifier.padding(innerPadding)) {
                Text(text = "Body content")
            }
        }
    }
}
