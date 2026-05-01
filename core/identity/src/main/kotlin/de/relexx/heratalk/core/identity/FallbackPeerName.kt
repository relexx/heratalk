// Copyright (c) 2026 relexx. BSD 3-Clause License.
// See LICENSE file in the project root for full license information.
package de.relexx.heratalk.core.identity

/**
 * Returns a deterministic fallback peer name derived from [pk].
 *
 * The name has the form `"Peer-xxxxxxxx"` where the suffix is the first 8 hex
 * characters of [pk]. For an empty array the suffix is an empty string, yielding
 * `"Peer-"`.
 *
 * This function is pure (no I/O, no Android imports) and may be called from any
 * thread or coroutine context.
 *
 * @param pk The raw public key bytes of the peer.
 * @return A name string suitable for display as a fallback label.
 */
public fun fallbackPeerName(pk: ByteArray): String {
    val hex = pk.joinToString("") { byte -> "%02x".format(byte) }
    val prefix = hex.take(8)
    return "Peer-$prefix"
}
