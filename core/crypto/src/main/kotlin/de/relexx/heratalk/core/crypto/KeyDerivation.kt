// Copyright (c) 2026 relexx. BSD 3-Clause License.
package de.relexx.heratalk.core.crypto

/**
 * Pure domain API for key derivation in HeraTalk's security stack.
 *
 * Derives transport-level secrets from the Noise-handshake shared secret (see
 * `docs/architecture.md` §9). All derivations use HKDF-SHA256 with distinct
 * `info`-strings per stream type and direction, so cross-context replays
 * across broadcast/direct or send/recv are prevented by construction.
 *
 * The interface intentionally exposes only the call sites used elsewhere in
 * the codebase. Implementations are wired in from v0.5.0 onwards once the
 * Noise-handshake (`:service:signaling`) starts producing real shared secrets.
 *
 * This interface has no Android or platform imports (per ADR-0004) and is
 * intended to remain pure JVM so that a future Kotlin-Multiplatform port
 * stays open.
 */
public interface KeyDerivation {

    /**
     * Derives an SRTP key for a single stream from the Noise shared secret.
     *
     * The returned key material is suitable input for an [Aead] that encrypts
     * SRTP payloads. Distinct ([streamType], [direction]) pairs MUST yield
     * cryptographically independent keys; implementations therefore pass
     * stable, distinct labels into the HKDF `info` parameter.
     *
     * @param sharedSecret The 32-byte shared secret produced by the Noise handshake.
     * @param streamType Whether the SRTP key is used for a [StreamType.BROADCAST]
     *   or [StreamType.DIRECT] media stream.
     * @param direction Whether this side will [KeyDirection.SEND] or
     *   [KeyDirection.RECEIVE] on the stream.
     * @return The derived key material. Length is implementation-defined but
     *   matches the AEAD requirements of [Aead] (32 bytes for ChaCha20-Poly1305).
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

    override fun deriveSrtpKey(
        sharedSecret: ByteArray,
        streamType: StreamType,
        direction: KeyDirection,
    ): ByteArray {
        throw NotImplementedError("KeyDerivation.deriveSrtpKey is implemented in v0.5.0")
    }
}
