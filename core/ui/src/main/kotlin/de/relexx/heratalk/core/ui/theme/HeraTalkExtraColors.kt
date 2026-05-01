// Copyright (c) 2026 relexx. BSD 3-Clause License.
// See LICENSE file in the project root for full license information.
package de.relexx.heratalk.core.ui.theme

import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * Extra colour slots that complement Material 3's [androidx.compose.material3.ColorScheme].
 *
 * Holds traffic-light hues (warning / direct-call accent / offline grey) that cannot be
 * mapped 1:1 to Material 3 slots and must be available in both light and dark themes.
 */
public data class HeraTalkExtraColors(
    /** Warning hue used by [NetworkQualityBadge] for degraded network quality. */
    public val warning: Color,
    /** Accent hue used to mark UI affordances tied to direct (1:1) calls. */
    public val directCall: Color,
    /** Neutral grey used by [NetworkQualityBadge] for offline state. */
    public val offline: Color,
)

internal val LightExtraColors =
    HeraTalkExtraColors(
        warning = HeraTalkColors.YellowWarningLight,
        directCall = HeraTalkColors.BlueSecondaryLight,
        offline = HeraTalkColors.OfflineGrey,
    )

internal val DarkExtraColors =
    HeraTalkExtraColors(
        warning = HeraTalkColors.YellowWarningDark,
        directCall = HeraTalkColors.BlueSecondaryDark,
        offline = HeraTalkColors.OfflineGrey,
    )

/**
 * CompositionLocal for [HeraTalkExtraColors].
 *
 * Default value is the light palette. The actual instance is provided by [HeraTalkTheme]
 * for the current colour mode.
 */
public val LocalHeraTalkExtraColors: ProvidableCompositionLocal<HeraTalkExtraColors> =
    staticCompositionLocalOf { LightExtraColors }
