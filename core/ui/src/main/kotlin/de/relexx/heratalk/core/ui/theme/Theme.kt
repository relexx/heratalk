// Copyright (c) 2026 relexx. BSD 3-Clause License.
package de.relexx.heratalk.core.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * Extra colour slots that complement Material 3's [androidx.compose.material3.ColorScheme].
 *
 * Holds traffic-light hues (warning / direct-call accent / offline grey) that cannot be
 * mapped 1:1 to Material 3 slots and must be available in both light and dark themes.
 */
public data class HeraTalkExtraColors(
    /** Warning hue used by `NetworkQualityBadge` in the [DEGRADED][de.relexx.heratalk.core.model.NetworkQuality.DEGRADED] state. */
    public val warning: Color,

    /** Accent hue used to mark UI affordances tied to direct (1:1) calls. */
    public val directCall: Color,

    /** Neutral grey used by `NetworkQualityBadge` in the [OFFLINE][de.relexx.heratalk.core.model.NetworkQuality.OFFLINE] state. */
    public val offline: Color,
)

private val LightExtraColors = HeraTalkExtraColors(
    warning = HeraTalkColors.YellowWarningLight,
    directCall = HeraTalkColors.BlueSecondaryLight,
    offline = HeraTalkColors.OfflineGrey,
)

private val DarkExtraColors = HeraTalkExtraColors(
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
public val LocalHeraTalkExtraColors: androidx.compose.runtime.ProvidableCompositionLocal<HeraTalkExtraColors> =
    staticCompositionLocalOf { LightExtraColors }

private val HeraTalkLightColorScheme = lightColorScheme(
    primary = HeraTalkColors.GreenPrimaryLight,
    onPrimary = Color.White,
    primaryContainer = HeraTalkColors.GreenContainerLight,
    onPrimaryContainer = Color(0xFF002106),
    secondary = HeraTalkColors.BlueSecondaryLight,
    onSecondary = Color.White,
    secondaryContainer = HeraTalkColors.BlueContainerLight,
    onSecondaryContainer = Color(0xFF001A41),
    error = HeraTalkColors.RedErrorLight,
    onError = Color.White,
    errorContainer = HeraTalkColors.RedContainerLight,
    onErrorContainer = Color(0xFF410002),
    background = HeraTalkColors.SurfaceLight,
    onBackground = HeraTalkColors.OnSurfaceLight,
    surface = HeraTalkColors.SurfaceLight,
    onSurface = HeraTalkColors.OnSurfaceLight,
)

private val HeraTalkDarkColorScheme = darkColorScheme(
    primary = HeraTalkColors.GreenPrimaryDark,
    onPrimary = Color(0xFF003912),
    primaryContainer = HeraTalkColors.GreenContainerDark,
    onPrimaryContainer = HeraTalkColors.GreenContainerLight,
    secondary = HeraTalkColors.BlueSecondaryDark,
    onSecondary = Color(0xFF002F65),
    secondaryContainer = HeraTalkColors.BlueContainerDark,
    onSecondaryContainer = HeraTalkColors.BlueContainerLight,
    error = HeraTalkColors.RedErrorDark,
    onError = Color(0xFF601410),
    errorContainer = HeraTalkColors.RedContainerDark,
    onErrorContainer = HeraTalkColors.RedContainerLight,
    background = HeraTalkColors.SurfaceDark,
    onBackground = HeraTalkColors.OnSurfaceDark,
    surface = HeraTalkColors.SurfaceDark,
    onSurface = HeraTalkColors.OnSurfaceDark,
)

/**
 * Top-level theme wrapper for HeraTalk.
 *
 * Applies the Material 3 ColorScheme matching [darkTheme], the project typography,
 * and exposes [HeraTalkExtraColors] via [LocalHeraTalkExtraColors] so non-Material
 * slots (warning / direct-call accent / offline grey) are reachable from any composable.
 *
 * @param darkTheme `true` to force the dark palette, `false` for light. Defaults to the
 *   system setting.
 * @param content The UI to render inside the themed surface.
 */
@Composable
public fun HeraTalkTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) HeraTalkDarkColorScheme else HeraTalkLightColorScheme
    val extraColors = if (darkTheme) DarkExtraColors else LightExtraColors

    CompositionLocalProvider(LocalHeraTalkExtraColors provides extraColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = HeraTalkTypography,
            content = content,
        )
    }
}
