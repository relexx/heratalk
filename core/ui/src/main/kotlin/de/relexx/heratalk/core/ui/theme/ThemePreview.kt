// Copyright (c) 2026 relexx. BSD 3-Clause License.
package de.relexx.heratalk.core.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

/**
 * Internal helper that renders the project's color tokens as labelled swatches so the
 * theme can be reviewed at a glance in Android Studio's preview pane.
 *
 * Not part of the public API — declared `private` and only consumed by `@Preview`
 * functions in this file.
 */
@Suppress("HardcodedText") // Preview-only labels — not user-visible at runtime.
@Composable
private fun ColorSwatch(label: String, color: Color, onColor: Color) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(color),
            contentAlignment = Alignment.Center,
        ) {
            Text(text = "Aa", color = onColor, style = MaterialTheme.typography.labelSmall)
        }
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

@Suppress("HardcodedText") // Preview-only labels — not user-visible at runtime.
@Composable
private fun ThemePalettePreview() {
    val cs = MaterialTheme.colorScheme
    val extra = LocalHeraTalkExtraColors.current
    Surface(color = cs.background) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "HeraTalk theme",
                style = MaterialTheme.typography.titleLarge,
                color = cs.onBackground,
            )
            Box(modifier = Modifier.size(0.dp, 8.dp))
            ColorSwatch("primary (active)", cs.primary, cs.onPrimary)
            ColorSwatch("primaryContainer", cs.primaryContainer, cs.onPrimaryContainer)
            ColorSwatch("secondary (direct call)", cs.secondary, cs.onSecondary)
            ColorSwatch("secondaryContainer", cs.secondaryContainer, cs.onSecondaryContainer)
            ColorSwatch("error (hang up / poor)", cs.error, cs.onError)
            ColorSwatch("errorContainer", cs.errorContainer, cs.onErrorContainer)
            ColorSwatch("warning (degraded)", extra.warning, cs.onSurface)
            ColorSwatch("offline grey", extra.offline, cs.onSurface)
            ColorSwatch("surface", cs.surface, cs.onSurface)
        }
    }
}

@Preview(name = "Theme — Light", showBackground = true, heightDp = 520)
@Composable
private fun HeraTalkThemePreviewLight() {
    HeraTalkTheme(darkTheme = false) {
        ThemePalettePreview()
    }
}

@Preview(name = "Theme — Dark", showBackground = true, heightDp = 520)
@Composable
private fun HeraTalkThemePreviewDark() {
    HeraTalkTheme(darkTheme = true) {
        ThemePalettePreview()
    }
}
