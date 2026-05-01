// Copyright (c) 2026 relexx. BSD 3-Clause License.
// See LICENSE file in the project root for full license information.
package de.relexx.heratalk.core.model

/**
 * A validated display name for a HeraTalk peer.
 *
 * Constraints:
 * - Not empty and not purely whitespace (at least one visible character required).
 * - At most 32 Unicode code points (matches the protocol limit in §6.1).
 * - Must not contain Bidi-override code points that could be used to spoof displayed text
 *   (Unicode categories LRE/RLE/PDF/LRO/RLO, FSI/PDI/LRI/RLI, LRM/RLM).
 *
 * The Bidi-override check guards against spoofing attacks where a peer's name appears
 * benign in the source but renders differently in the UI due to bidirectional overrides.
 *
 * @property value The validated display name string.
 * @throws IllegalArgumentException if any constraint is violated.
 */
@JvmInline
public value class DisplayName(
    public val value: String,
) {
    init {
        require(value.isNotEmpty()) { "DisplayName must not be empty" }
        require(value.codePointCount(0, value.length) <= 32) {
            "DisplayName must not exceed 32 code points, was ${value.codePointCount(0, value.length)}"
        }
        require(value.isNotBlank()) { "DisplayName must contain at least one visible character" }
        require(!containsBidiOverride(value)) {
            "DisplayName must not contain Bidi-override characters"
        }
    }

    public companion object {
        // Unicode Bidi-override code points that can be used to spoof displayed text.
        // Ranges: U+202A–U+202E (LRE, RLE, PDF, LRO, RLO)
        //         U+2066–U+2069 (LRI, RLI, FSI, PDI)
        //         U+200E (LRM), U+200F (RLM)
        private val BIDI_OVERRIDE_CODE_POINTS: Set<Int> =
            buildSet {
                addAll(0x202A..0x202E)
                addAll(0x2066..0x2069)
                add(0x200E)
                add(0x200F)
            }

        private fun containsBidiOverride(s: String): Boolean {
            var i = 0
            while (i < s.length) {
                val cp = s.codePointAt(i)
                if (cp in BIDI_OVERRIDE_CODE_POINTS) return true
                i += Character.charCount(cp)
            }
            return false
        }
    }
}
