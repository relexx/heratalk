// Copyright (c) 2026 relexx. BSD 3-Clause License.
package de.relexx.heratalk.core.crypto

/**
 * Pure domain API for authenticated encryption with associated data (AEAD).
 *
 * HeraTalk uses **ChaCha20-Poly1305** (see `docs/architecture.md` §9) for both
 * SRTP media and Noise transport messages. This interface abstracts the AEAD
 * primitive so that callers (`:service:media`, `:service:signaling`) depend on
 * a stable contract rather than a concrete provider (BouncyCastle is added in
 * v0.5.0).
 *
 * Invariants that every implementation MUST uphold:
 *
 * 1. **Nonce uniqueness per key.** Reusing a (`key`, `nonce`) pair is a
 *    catastrophic key-stream reuse. Callers are responsible for nonce
 *    management; implementations MUST NOT rotate nonces silently.
 * 2. **Constant-time tag verification.** [open] MUST compare the tag in
 *    constant time (`.claude/rules.md` Rule 12).
 * 3. **No partial output on failure.** [open] MUST NOT return any plaintext
 *    bytes if authentication fails — return `null` or throw before any
 *    plaintext byte is observable.
 *
 * This interface has no Android or platform imports (per ADR-0004) and is
 * pure JVM.
 */
public interface Aead {

    /**
     * Encrypts [plaintext] under [key] and authenticates [associatedData].
     *
     * The returned byte array carries the AEAD ciphertext followed by the
     * authentication tag (Poly1305: 16 bytes) — the exact layout is defined
     * by the implementation but MUST round-trip with [open].
     *
     * @param key Symmetric key (32 bytes for ChaCha20-Poly1305).
     * @param nonce Per-message nonce (12 bytes for ChaCha20-Poly1305). MUST be
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
     * (see invariant 2) and MUST NOT leak any plaintext bytes when the tag
     * is invalid (invariant 3).
     *
     * @param key Symmetric key (32 bytes for ChaCha20-Poly1305).
     * @param nonce Per-message nonce (12 bytes for ChaCha20-Poly1305). Must
     *   match the nonce used by the sender's [seal] call.
     * @param ciphertext Ciphertext concatenated with authentication tag, as
     *   produced by [seal].
     * @param associatedData Authenticated but unencrypted data. Must match
     *   the sender's input bit-for-bit.
     * @return Plaintext bytes on success, `null` if the tag is invalid or
     *   the input is malformed.
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
 * Real ChaCha20-Poly1305 arrives with SRTP in **v0.6.0** (see `docs/releases.md`).
 * Until then this stub exists so that downstream modules can already declare
 * their dependency on the [Aead] interface and wire DI.
 */
public class StubAead : Aead {

    override fun seal(
        key: ByteArray,
        nonce: ByteArray,
        plaintext: ByteArray,
        associatedData: ByteArray,
    ): ByteArray {
        throw NotImplementedError("Aead.seal is implemented in v0.6.0")
    }

    override fun open(
        key: ByteArray,
        nonce: ByteArray,
        ciphertext: ByteArray,
        associatedData: ByteArray,
    ): ByteArray? {
        throw NotImplementedError("Aead.open is implemented in v0.6.0")
    }
}
