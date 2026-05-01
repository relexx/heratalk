// Copyright (c) 2026 relexx. BSD 3-Clause License.
// See LICENSE file in the project root for full license information.
package de.relexx.heratalk.core.identity

import de.relexx.heratalk.core.model.DisplayName
import kotlinx.coroutines.flow.Flow

/**
 * Pure domain API for reading and writing the local peer's identity.
 *
 * This interface has no Android or platform imports. Adapters live in
 * `DataStoreIdentityRepository.kt` (see ADR-0004). Callers depend only on this interface.
 *
 * The local identity currently consists of a single [DisplayName]. When no name has
 * been set yet the flow emits `null`. Callers that need a non-null display name for
 * display purposes should fall back to [fallbackName].
 */
public interface IdentityRepository {
    /**
     * A stream of the current [DisplayName] for the local peer.
     *
     * Emits `null` if no display name has been set yet or if the persisted value
     * is no longer valid (e.g. empty string after an unexpected data migration).
     * Emits a new value whenever the name is updated via [setDisplayName].
     */
    public val displayName: Flow<DisplayName?>

    /**
     * Persists [name] as the local peer's display name.
     *
     * @param name A valid [DisplayName]. The caller is responsible for validation;
     *   [DisplayName]'s own `init` block guarantees the value is always well-formed.
     */
    public suspend fun setDisplayName(name: DisplayName)

    /**
     * Returns a deterministic fallback display name derived from the peer's public key.
     *
     * The fallback is computed locally with no I/O. It is suitable as a placeholder
     * label when a peer's self-chosen name is not yet known.
     *
     * @param pk The peer's raw public key bytes.
     * @return A [DisplayName] of the form `"Peer-xxxxxxxx"` where the suffix is the
     *   first 8 hex characters of [pk].
     */
    public fun fallbackName(pk: ByteArray): DisplayName
}
