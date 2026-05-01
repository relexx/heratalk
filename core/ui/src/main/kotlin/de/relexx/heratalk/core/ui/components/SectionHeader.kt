// Copyright (c) 2026 relexx. BSD 3-Clause License.
package de.relexx.heratalk.core.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import de.relexx.heratalk.core.ui.theme.HeraTalkTheme

/**
 * Lightweight section heading used to group lists in Settings or the channel screen.
 *
 * Mirrors Material 3's category header treatment: small uppercase-friendly text,
 * a touch of vertical breathing room, primary-tone colour. Caller is responsible
 * for providing a localised label via `stringResource` — the composable does not
 * inject any literal text.
 *
 * @param text Already-resolved label to render. Callers MUST pass a value coming
 *   from `stringResource(...)` to keep i18n discipline.
 * @param modifier Optional layout modifier.
 */
@Composable
public fun SectionHeader(
    text: String,
    modifier: Modifier = Modifier,
) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.primary,
        modifier = modifier.padding(horizontal = 16.dp, vertical = 8.dp),
    )
}

// Preview literals are not user-visible at runtime — no i18n required.
@Suppress("HardcodedText")
@Preview(name = "Light", showBackground = true)
@Composable
private fun SectionHeaderPreviewLight() {
    HeraTalkTheme(darkTheme = false) {
        Column {
            SectionHeader(text = "Active peers")
            SectionHeader(text = "Online")
        }
    }
}

// Preview literals are not user-visible at runtime — no i18n required.
@Suppress("HardcodedText")
@Preview(name = "Dark", showBackground = true)
@Composable
private fun SectionHeaderPreviewDark() {
    HeraTalkTheme(darkTheme = true) {
        Column {
            SectionHeader(text = "Active peers")
            SectionHeader(text = "Online")
        }
    }
}
