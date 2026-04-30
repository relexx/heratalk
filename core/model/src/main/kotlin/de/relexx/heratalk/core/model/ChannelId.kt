// Copyright (c) 2026 relexx. BSD 3-Clause License.
package de.relexx.heratalk.core.model

/**
 * Strongly typed identifier for a HeraTalk channel.
 *
 * Valid characters: lowercase ASCII letters (`a-z`), digits (`0-9`), and hyphen (`-`).
 * Length constraints: 1–32 characters.
 *
 * The restricted character set (no uppercase, no underscore) keeps channel identifiers
 * URL-safe and case-unambiguous when exchanged via QR codes or manual entry.
 *
 * @property value The raw string representation of the channel identifier.
 * @throws IllegalArgumentException if the value is blank, exceeds 32 characters,
 *   or contains characters outside the allowed set `[a-z0-9-]`.
 */
@JvmInline
public value class ChannelId(public val value: String) {

    init {
        require(value.isNotEmpty()) { "ChannelId must not be empty" }
        require(value.length <= 32) { "ChannelId must not exceed 32 characters, was ${value.length}" }
        require(ALLOWED_PATTERN.matches(value)) {
            "ChannelId contains invalid characters; only [a-z0-9-] are allowed"
        }
    }

    public companion object {
        private val ALLOWED_PATTERN = Regex("[a-z0-9-]+")
    }
}
