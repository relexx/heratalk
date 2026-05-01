// Copyright (c) 2026 relexx. BSD 3-Clause License.
// See LICENSE file in the project root for full license information.
package de.relexx.heratalk.service.lifecycle

/**
 * Snapshot of the user-facing feature configuration that drives the foreground
 * service type (`connectedDevice` vs. `microphone`) and the notification text.
 *
 * The lifecycle service consumes [FeatureState] via [HeraTalkService.setFeatureState]
 * and decides whether the service must run with `FOREGROUND_SERVICE_TYPE_MICROPHONE`
 * (continuous mic capture, see VOX or hardware-PTT in `architecture.md` §11.3),
 * `FOREGROUND_SERVICE_TYPE_CONNECTED_DEVICE` (channel active, no mic), or whether
 * it should stop entirely.
 *
 * In v0.1.0 only the [channelActive] flag is acted upon. [voxEnabled] and
 * [hardwarePttEnabled] are accepted but the microphone path is parked behind a
 * `TODO("v0.7.0")` until the audio pipeline lands.
 *
 * @property channelActive Whether the local peer is currently part of a channel.
 *   Drives `connectedDevice` foreground type.
 * @property voxEnabled Whether VOX (voice-activated transmission) is enabled.
 *   Drives `microphone` foreground type from v0.7.0 onwards.
 * @property hardwarePttEnabled Whether a hardware PTT button (Bluetooth media
 *   button or volume key) is bound. Drives `microphone` foreground type from
 *   v0.7.0 onwards.
 */
public data class FeatureState(
    public val channelActive: Boolean,
    public val voxEnabled: Boolean,
    public val hardwarePttEnabled: Boolean,
) {
    public companion object {
        /** All-off state used as the lifecycle service's initial state. */
        public val Idle: FeatureState =
            FeatureState(
                channelActive = false,
                voxEnabled = false,
                hardwarePttEnabled = false,
            )
    }
}
