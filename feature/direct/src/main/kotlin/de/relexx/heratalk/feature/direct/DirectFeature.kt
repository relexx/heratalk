// Copyright (c) 2026 relexx. BSD 3-Clause License.
// See LICENSE file in the project root for full license information.
package de.relexx.heratalk.feature.direct

/**
 * Direct (1:1) call feature placeholder.
 *
 * The module is intentionally empty in v0.1.0 — only the build setup exists so
 * the Gradle module topology mirrors `docs/architecture.md` §4. The screen is
 * not registered in the v0.1.0 navigation graph; the feature ships fully with
 * **v0.8.0** (`docs/releases.md` v0.8.0).
 *
 * This object exists solely to satisfy ktlint's `no-empty-file` rule until the
 * real composables land. It carries no behaviour and is not referenced from any
 * other module.
 */
public object DirectFeature {
    /**
     * Marker version string for the placeholder module.
     *
     * Replaced by real composables in v0.8.0; kept opaque on purpose so callers
     * cannot rely on its value.
     */
    public const val PLACEHOLDER_RELEASE: String = "v0.8.0"
}
