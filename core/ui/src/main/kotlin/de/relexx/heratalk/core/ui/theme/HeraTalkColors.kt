// Copyright (c) 2026 relexx. BSD 3-Clause License.
// See LICENSE file in the project root for full license information.
package de.relexx.heratalk.core.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * HeraTalk colour tokens for the traffic-light palette.
 *
 * The palette is grounded in the UX revision from 2026-04-25:
 * - **Green** (primary): "active / available" — Go/Safety semantic.
 * - **Blue** (secondary): "private direct call" — sets a private call apart from broadcast.
 * - **Yellow** (warning): "degraded" — surfaces non-blocking network issues.
 * - **Red** (error): "fault / hang up" — used for terminal failures and hang-up actions.
 *
 * These tokens fill non-Material slots (e.g. [HeraTalkExtraColors]) and are the source
 * of truth for any UI surface that needs a traffic-light hue outside of Material 3's
 * ColorScheme.
 */
public object HeraTalkColors {
    /** Primary accent in light mode (active / available). */
    public val GreenPrimaryLight: Color = Color(0xFF1B7F3A)

    /** Container behind primary actions in light mode. */
    public val GreenContainerLight: Color = Color(0xFFB7EFC1)

    /** Primary accent in dark mode. */
    public val GreenPrimaryDark: Color = Color(0xFF7BD894)

    /** Container behind primary actions in dark mode. */
    public val GreenContainerDark: Color = Color(0xFF0E4220)

    /** Secondary accent in light mode (direct call / private). */
    public val BlueSecondaryLight: Color = Color(0xFF1A5FB4)

    /** Container behind secondary actions in light mode. */
    public val BlueContainerLight: Color = Color(0xFFCDDCFB)

    /** Secondary accent in dark mode. */
    public val BlueSecondaryDark: Color = Color(0xFF8DB4F0)

    /** Container behind secondary actions in dark mode. */
    public val BlueContainerDark: Color = Color(0xFF0D3260)

    /** Warning hue in light mode (degraded network quality). */
    public val YellowWarningLight: Color = Color(0xFFB58900)

    /** Warning hue in dark mode. */
    public val YellowWarningDark: Color = Color(0xFFE5C45A)

    /** Error hue in light mode (faults / poor network / hang up). */
    public val RedErrorLight: Color = Color(0xFFB3261E)

    /** Container behind errors in light mode. */
    public val RedContainerLight: Color = Color(0xFFF9DEDC)

    /** Error hue in dark mode. */
    public val RedErrorDark: Color = Color(0xFFF2B8B5)

    /** Container behind errors in dark mode. */
    public val RedContainerDark: Color = Color(0xFF601410)

    /** Surface in light mode. */
    public val SurfaceLight: Color = Color(0xFFFDFDFC)

    /** On-surface text colour in light mode. */
    public val OnSurfaceLight: Color = Color(0xFF1C1B1F)

    /** Surface in dark mode. */
    public val SurfaceDark: Color = Color(0xFF121212)

    /** On-surface text colour in dark mode. */
    public val OnSurfaceDark: Color = Color(0xFFE6E1E5)

    /** Neutral grey used for "offline" peer indicator — same value in both modes. */
    public val OfflineGrey: Color = Color(0xFF9E9E9E)

    /** On-primary-container text/icon colour in light mode. */
    public val GreenOnPrimaryContainerLight: Color = Color(0xFF002106)

    /** On-secondary-container text/icon colour in light mode. */
    public val BlueOnSecondaryContainerLight: Color = Color(0xFF001A41)

    /** On-error-container text/icon colour in light mode. */
    public val RedOnErrorContainerLight: Color = Color(0xFF410002)

    /** On-primary text/icon colour in dark mode. */
    public val GreenOnPrimaryDark: Color = Color(0xFF003912)

    /** On-secondary text/icon colour in dark mode. */
    public val BlueOnSecondaryDark: Color = Color(0xFF002F65)
}
