// Copyright (c) 2026 relexx. BSD 3-Clause License.
package de.relexx.heratalk.core.crypto

/**
 * Pure domain API for authenticated encryption with associated data (AEAD).
 *
 * HeraTalk uses **ChaCha20-Poly1305** (see ADR-0002 and `docs/architecture.md`
 * §9) for both Noise transport messages (from v0.5.0) and SRTP media (from
 * v0.6.0). This interface abstracts the AEAD primitive so that callers
 * (`:service:signaling`, `:service:media`) depend on a stable contract rather
 * than a concrete provider — BouncyCastle is wired in v0.5.0 alongside the
 * Noise handshake.
 *
 * Invariants every implementation MUST uphold:
 *
 * 1. **Nonce uniqueness per key.** Reusing a (`key`, `nonce`) pair is a
 *    catastrophic key-stream reuse. Callers manage nonces; implementations
 *    MUST NOT silently rotate them.
 * 2. **Constant-time tag verification.** [open] MUST compare tags in constant
 *    time (`.claude/rules.md` Rule 12). `Arrays.equals` and friends are
 *    forbidden in the tag path.
 * 3. **No partial output on failure.** [open] MUST NOT return any plaintext
 *    bytes if authentication fails — return `null` before any plaintext is
 *    observable to the caller.
 *
 * **Why [open] returns `ByteArray?` instead of `Result<ByteArray>`:** AEAD
 * failure is an expected hot-path event — every dropped/spoofed RTP packet
 * runs through it. `null` keeps the failure path allocation-free; `Result`
 * would wrap each call in an extra object and a `Throwable` in the failure
 * branch. The caller's reaction is invariably "drop packet", so the loss in
 * type expressiveness is negligible. This is a deliberate exception to the
 * project-wide `Result<T>` convention for expected errors.
 *
 * This interface has no Android or platform imports (per ADR-0004) and is
 * pure JVM.
 */
public interface Aead {

    /**
     * Encrypts [plaintext] under [key] and authenticates [associatedData].
     *
     * The returned byte array carries the AEAD ciphertext followed by the
     * 16-byte Poly1305 authentication tag. The exact layout is fixed by the
     * implementation but MUST round-trip with [open].
     *
     * @param key 32-byte symmetric key (ChaCha20-Poly1305).
     * @param nonce 12-byte per-message nonce (ChaCha20-Poly1305). MUST be
     *   unique for the lifetime of [key].
     * @param plaintext The data to encrypt. May be empty.
     * @param associatedData Authenticated but unencrypted data (e.g. SRTP
     *   header). May be empty.
     * @return Ciphertext concatenated with authentication tag.
     */
    public fun seal(
        key: ByteArray,
        nonce: ByteArray,
        plaintext: ByteArray,
        associatedData: ByteArray,
    ): ByteArray

    /**
     * Decrypts and authenticates [ciphertext] under [key].
     *
     * Returns the decrypted plaintext on success or `null` if authentication
     * fails. The implementation MUST perform a constant-time tag comparison
     * (invariant 2) and MUST NOT leak any plaintext bytes when the tag is
     * invalid (invariant 3).
     *
     * @param key 32-byte symmetric key (ChaCha20-Poly1305).
     * @param nonce 12-byte per-message nonce (ChaCha20-Poly1305). MUST match
     *   the nonce used by the sender's [seal] call.
     * @param ciphertext Ciphertext concatenated with authentication tag, as
     *   produced by [seal].
     * @param associatedData Authenticated but unencrypted data. MUST match
     *   the sender's input bit-for-bit.
     * @return Plaintext bytes on success, `null` if the tag is invalid or the
     *   input is malformed (see invariants 2 and 3).
     */
    public fun open(
        key: ByteArray,
        nonce: ByteArray,
        ciphertext: ByteArray,
        associatedData: ByteArray,
    ): ByteArray?
}

/**
 * Skeleton implementation that throws [NotImplementedError] on every call.
 *
 * The real ChaCha20-Poly1305 implementation arrives in **v0.5.0** for the
 * Noise transport AEAD and is reused by SRTP from **v0.6.0** onwards (see
 * `docs/releases.md`). Until then this stub exists so that downstream modules
 * can already declare their dependency on the [Aead] interface and wire DI.
 *
 * The stub deliberately throws — even from [open], whose contract returns
 * `null` on failure. Throwing is louder than silently dropping packets and
 * surfaces accidental wiring before the real implementation lands.
 */
public class StubAead : Aead {

    override fun seal(
        key: ByteArray,
        nonce: ByteArray,
        plaintext: ByteArray,
        associatedData: ByteArray,
    ): ByteArray {
        throw NotImplementedError("Aead.seal is implemented in v0.5.0")
    }

    override fun open(
        key: ByteArray,
        nonce: ByteArray,
        ciphertext: ByteArray,
        associatedData: ByteArray,
    ): ByteArray? {
        throw NotImplementedError("Aead.open is implemented in v0.5.0")
    }
}
