// Copyright (c) 2026 relexx. BSD 3-Clause License.
package de.relexx.heratalk.core.crypto

/**
 * Pure domain API for key derivation in HeraTalk's security stack.
 *
 * Two distinct derivation flows live behind this interface (see ADR-0002 and
 * `docs/architecture.md` §9):
 *
 * 1. **Phase-1 PSK derivation** ([derivePhase1Psk]) — runs once per channel
 *    when the QR-code-supplied `channel_secret` is converted into the PSK fed
 *    into `Noise_XXpsk2_25519_ChaChaPoly_SHA256`.
 * 2. **SRTP key derivation** ([deriveSrtpKey]) — runs per session, after a
 *    completed Noise handshake, to produce send/receive keys for broadcast and
 *    direct media streams.
 *
 * Both flows use HKDF-SHA256 with **distinct** `info`-strings so that key
 * material from one context cannot collide with another. Implementations MUST
 * preserve those labels verbatim — they are part of the protocol contract and
 * any drift breaks interoperability between peers.
 *
 * Implementations are wired in from v0.5.0 onwards once the Noise handshake
 * (`:service:signaling`) starts producing real shared secrets.
 *
 * This interface has no Android or platform imports (per ADR-0004) and is
 * intended to remain pure JVM so that a future Kotlin-Multiplatform port stays
 * open.
 */
public interface KeyDerivation {

    /**
     * Derives the 32-byte Noise PSK from the QR-code channel secret.
     *
     * Per ADR-0002 the derivation is HKDF-SHA256 with:
     * - `IKM` = [channelSecret] (32 bytes)
     * - `salt` = empty
     * - `info` = `"HeraTalk Phase 1 Noise PSK"` (UTF-8)
     * - `L`    = 32 bytes
     *
     * The result is fed unchanged into the `XXpsk2` handshake. The same
     * derivation also seeds the Phase-2 PSK for `KKpsk0` sessions.
     *
     * @param channelSecret The 32-byte channel secret obtained out-of-band
     *   (QR-code).
     * @return The 32-byte Noise PSK.
     */
    public fun derivePhase1Psk(channelSecret: ByteArray): ByteArray

    /**
     * Derives a 32-byte SRTP key for a single stream from the Noise shared
     * secret.
     *
     * The returned key material feeds an [Aead] that encrypts SRTP payloads
     * with `ChaCha20-Poly1305` (32-byte key, normative per ADR-0002). Distinct
     * ([streamType], [direction]) pairs MUST yield cryptographically
     * independent keys; implementations therefore pass stable, distinct labels
     * into the HKDF `info` parameter (e.g. `"srtp/broadcast/send"`,
     * `"srtp/direct/recv"` — see `architecture.md` §9).
     *
     * @param sharedSecret The shared secret produced by the Noise handshake
     *   (32 bytes for the ChaChaPoly suite).
     * @param streamType Whether the SRTP key is used for a [StreamType.BROADCAST]
     *   or [StreamType.DIRECT] media stream.
     * @param direction Whether this side will [KeyDirection.SEND] or
     *   [KeyDirection.RECEIVE] on the stream.
     * @return The derived 32-byte key, suitable as input to [Aead].
     */
    public fun deriveSrtpKey(
        sharedSecret: ByteArray,
        streamType: StreamType,
        direction: KeyDirection,
    ): ByteArray
}

/**
 * SRTP stream type. Broadcast and direct streams use different keys so that
 * a captured broadcast packet cannot be replayed into a direct context (and
 * vice versa).
 */
public enum class StreamType {
    /** Broadcast (channel) audio reachable to all peers in the channel. */
    BROADCAST,

    /** Direct (1:1) audio reachable only to the call partner. */
    DIRECT,
}

/**
 * Direction of an SRTP key relative to the local peer.
 *
 * Each end of a session derives one [SEND] and one [RECEIVE] key; the remote
 * peer's [SEND] key matches the local [RECEIVE] key. Mixing send and receive
 * keys into the same AEAD context is a misuse and MUST fail.
 */
public enum class KeyDirection {
    /** Key used by the local peer to encrypt outgoing media. */
    SEND,

    /** Key used by the local peer to decrypt incoming media. */
    RECEIVE,
}

/**
 * Skeleton implementation that throws [NotImplementedError] on every call.
 *
 * Real key derivation arrives with the control plane / Noise handshake in
 * **v0.5.0** (see `docs/releases.md`). Until then this stub exists so that
 * downstream modules (`:service:media`, `:service:signaling`) can already
 * declare their dependency on the [KeyDerivation] interface and wire DI.
 */
public class StubKeyDerivation : KeyDerivation {

    override fun derivePhase1Psk(channelSecret: ByteArray): ByteArray {
        throw NotImplementedError("KeyDerivation.derivePhase1Psk is implemented in v0.5.0")
    }

    override fun deriveSrtpKey(
        sharedSecret: ByteArray,
        streamType: StreamType,
        direction: KeyDirection,
    ): ByteArray {
        throw NotImplementedError("KeyDerivation.deriveSrtpKey is implemented in v0.5.0")
    }
}
