// Copyright (c) 2026 relexx. BSD 3-Clause License.
// See LICENSE file in the project root for full license information.
package de.relexx.heratalk.core.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color

private val HeraTalkLightColorScheme =
    lightColorScheme(
        primary = HeraTalkColors.GreenPrimaryLight,
        onPrimary = Color.White,
        primaryContainer = HeraTalkColors.GreenContainerLight,
        onPrimaryContainer = HeraTalkColors.GreenOnPrimaryContainerLight,
        secondary = HeraTalkColors.BlueSecondaryLight,
        onSecondary = Color.White,
        secondaryContainer = HeraTalkColors.BlueContainerLight,
        onSecondaryContainer = HeraTalkColors.BlueOnSecondaryContainerLight,
        error = HeraTalkColors.RedErrorLight,
        onError = Color.White,
        errorContainer = HeraTalkColors.RedContainerLight,
        onErrorContainer = HeraTalkColors.RedOnErrorContainerLight,
        background = HeraTalkColors.SurfaceLight,
        onBackground = HeraTalkColors.OnSurfaceLight,
        surface = HeraTalkColors.SurfaceLight,
        onSurface = HeraTalkColors.OnSurfaceLight,
    )

private val HeraTalkDarkColorScheme =
    darkColorScheme(
        primary = HeraTalkColors.GreenPrimaryDark,
        onPrimary = HeraTalkColors.GreenOnPrimaryDark,
        primaryContainer = HeraTalkColors.GreenContainerDark,
        onPrimaryContainer = HeraTalkColors.GreenContainerLight,
        secondary = HeraTalkColors.BlueSecondaryDark,
        onSecondary = HeraTalkColors.BlueOnSecondaryDark,
        secondaryContainer = HeraTalkColors.BlueContainerDark,
        onSecondaryContainer = HeraTalkColors.BlueContainerLight,
        error = HeraTalkColors.RedErrorDark,
        onError = HeraTalkColors.RedContainerDark,
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
