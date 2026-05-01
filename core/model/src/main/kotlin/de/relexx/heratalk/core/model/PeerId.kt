// Copyright (c) 2026 relexx. BSD 3-Clause License.
// See LICENSE file in the project root for full license information.
package de.relexx.heratalk.core.model

/**
 * Strongly typed identifier for a peer in the HeraTalk mesh.
 *
 * Valid characters: alphanumeric ASCII, hyphen (`-`), and underscore (`_`).
 * Length constraints: 1–64 characters.
 *
 * @property value The raw string representation of the peer identifier.
 * @throws IllegalArgumentException if the value is blank, exceeds 64 characters,
 *   or contains characters outside the allowed set.
 */
@JvmInline
public value class PeerId(
    public val value: String,
) {
    init {
        require(value.isNotEmpty()) { "PeerId must not be empty" }
        require(value.length <= MAX_LENGTH) { "PeerId must not exceed $MAX_LENGTH characters, was ${value.length}" }
        require(ALLOWED_PATTERN.matches(value)) {
            "PeerId contains invalid characters; only [A-Za-z0-9_-] are allowed"
        }
    }

    public companion object {
        /** Maximum number of characters allowed in a [PeerId]. */
        public const val MAX_LENGTH: Int = 64

        private val ALLOWED_PATTERN = Regex("[A-Za-z0-9_-]+")
    }
}
