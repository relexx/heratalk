// Copyright (c) 2026 relexx. BSD 3-Clause License.
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
public value class PeerId(public val value: String) {

    init {
        require(value.isNotEmpty()) { "PeerId must not be empty" }
        require(value.length <= 64) { "PeerId must not exceed 64 characters, was ${value.length}" }
        require(ALLOWED_PATTERN.matches(value)) {
            "PeerId contains invalid characters; only [A-Za-z0-9_-] are allowed"
        }
    }

    public companion object {
        private val ALLOWED_PATTERN = Regex("[A-Za-z0-9_-]+")
    }
}
