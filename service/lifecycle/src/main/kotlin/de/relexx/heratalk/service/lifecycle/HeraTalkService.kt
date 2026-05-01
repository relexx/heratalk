// Copyright (c) 2026 relexx. BSD 3-Clause License.
// See LICENSE file in the project root for full license information.
package de.relexx.heratalk.service.lifecycle

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.content.getSystemService

/**
 * Foreground service that keeps HeraTalk's networking stack alive while the user
 * is in a channel.
 *
 * **Responsibilities (v0.1.0 skeleton):**
 * - Promote itself to a foreground service with a localised notification.
 * - Switch its `foregroundServiceType` between `connectedDevice` and `microphone`
 *   atomically (see `docs/architecture.md` §11.3) when the consuming layer reports
 *   a new [FeatureState].
 * - Stop itself when the feature state collapses to all-off.
 *
 * In v0.1.0 only the `connectedDevice` path is exercised. The `microphone`
 * branch lands with v0.7.0 (VOX / hardware PTT) — the code below already handles
 * the type computation for forward-compatibility but the actual microphone
 * resource is not held until the audio pipeline exists.
 *
 * **Lifecycle contract:**
 * - Started via [start] from a *visible* activity (Android 14+ rule, §11.6).
 * - Restart policy is `START_NOT_STICKY`: after a crash the OS does not bring the
 *   service back; the user must rejoin the channel manually so the feature state
 *   stays consistent.
 *
 * The actual microphone capture, network sockets, and SRTP keys live in their
 * respective service modules (`:service:audio`, `:service:transport`,
 * `:service:media`). This service is intentionally a thin lifecycle host.
 */
public class HeraTalkService : Service() {
    private var currentType: Int = TYPE_UNINITIALISED
    private var currentState: FeatureState = FeatureState.Idle

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        ensureNotificationChannel(this)
    }

    override fun onStartCommand(
        intent: Intent?,
        flags: Int,
        startId: Int,
    ): Int {
        // The first onStartCommand promotes the service to foreground with the
        // default connectedDevice type. Real feature state is pushed in
        // afterwards via setFeatureState(...). Keeping the initial state Idle
        // here would race with Android's 5s "must call startForeground" budget.
        if (currentType == TYPE_UNINITIALISED) {
            applyState(FeatureState(channelActive = true, voxEnabled = false, hardwarePttEnabled = false))
        }
        return START_NOT_STICKY
    }

    /**
     * Updates the foreground service to reflect a new [FeatureState].
     *
     * The implementation follows the pattern from `architecture.md` §11.3:
     * the type transition is done with a single `startForeground(...)` call so
     * Android treats it as an in-place transition and the service is never
     * killable between two `start*` calls.
     *
     * If the resulting type would be `null` (no feature active) the service
     * stops itself.
     *
     * @param state Desired feature state. The service derives the matching
     *   `foregroundServiceType` and notification text.
     */
    public fun setFeatureState(state: FeatureState) {
        applyState(state)
    }

    private fun applyState(state: FeatureState) {
        val newType = computeServiceType(state)
        if (newType == null) {
            stopSelf()
            currentState = state
            currentType = TYPE_UNINITIALISED
            return
        }

        val notification = buildNotification(state)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(NOTIFICATION_ID, notification, newType)
        } else {
            // Pre-Android-10 path is unreachable in production (minSdk = 29) but kept
            // for completeness — the typed overload is API 29+.
            @Suppress("DEPRECATION")
            startForeground(NOTIFICATION_ID, notification)
        }
        currentType = newType
        currentState = state
    }

    private fun computeServiceType(state: FeatureState): Int? =
        when {
            state.voxEnabled || state.hardwarePttEnabled -> {
                // TODO(developer): v0.7.0 — hold microphone resource via :service:audio
                //  before promoting to FOREGROUND_SERVICE_TYPE_MICROPHONE.
                ServiceInfo.FOREGROUND_SERVICE_TYPE_MICROPHONE
            }
            state.channelActive -> ServiceInfo.FOREGROUND_SERVICE_TYPE_CONNECTED_DEVICE
            else -> null
        }

    private fun buildNotification(state: FeatureState): Notification {
        val contentTextRes =
            when {
                state.voxEnabled -> R.string.lifecycle_notification_text_vox
                state.hardwarePttEnabled -> R.string.lifecycle_notification_text_vox
                state.channelActive -> R.string.lifecycle_notification_text_channel
                else -> R.string.lifecycle_notification_text_idle
            }
        return NotificationCompat
            .Builder(this, NOTIFICATION_CHANNEL_ID)
            .setContentTitle(getString(R.string.lifecycle_notification_title))
            .setContentText(getString(contentTextRes))
            .setSmallIcon(android.R.drawable.stat_sys_data_bluetooth)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    public companion object {
        /**
         * Stable notification id reused across all foreground transitions.
         * The value is arbitrary but must be ≠ 0 and unique within the app.
         */
        public const val NOTIFICATION_ID: Int = 0x4845_5241 // ASCII 'H','E','R','A'

        /** Localised notification channel for the foreground notification. */
        public const val NOTIFICATION_CHANNEL_ID: String = "heratalk_lifecycle"

        private const val TYPE_UNINITIALISED: Int = -1

        /**
         * Starts the foreground service from a visible activity.
         *
         * @param context Activity context. Must be a visible activity (Android
         *   14+ requires this for foreground-service starts).
         */
        public fun start(context: Context) {
            val intent = Intent(context, HeraTalkService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        /**
         * Stops the foreground service.
         *
         * @param context Any context.
         */
        public fun stop(context: Context) {
            context.stopService(Intent(context, HeraTalkService::class.java))
        }

        private fun ensureNotificationChannel(context: Context) {
            // API 26+ guarantees NotificationManager.getSystemService is non-null on a
            // properly initialised Service context — the channel API is available since
            // Android Oreo and minSdk is 29, so the SDK_INT branch is purely defensive
            // for future minSdk drops.
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
            val nm = context.getSystemService<NotificationManager>()
            if (nm != null && nm.getNotificationChannel(NOTIFICATION_CHANNEL_ID) == null) {
                val channel =
                    NotificationChannel(
                        NOTIFICATION_CHANNEL_ID,
                        context.getString(R.string.lifecycle_notification_channel_name),
                        NotificationManager.IMPORTANCE_LOW,
                    ).apply {
                        description = context.getString(R.string.lifecycle_notification_channel_description)
                        setShowBadge(false)
                    }
                nm.createNotificationChannel(channel)
            }
        }
    }
}
