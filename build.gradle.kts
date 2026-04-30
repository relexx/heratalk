// Root build file — module-specific config in each submodule's build.gradle.kts
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.protobuf) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.detekt)
    alias(libs.plugins.spotless)
}

detekt {
    config.setFrom(files("$rootDir/detekt.yml"))
    buildUponDefaultConfig = true
    allRules = false
    source.setFrom(
        files(
            "$rootDir/app/src",
            "$rootDir/core",
            "$rootDir/service",
            "$rootDir/feature",
        )
    )
}

spotless {
    kotlin {
        target("**/*.kt")
        targetExclude("**/build/**/*.kt")
        ktlint()
        licenseHeader(
            """
            // Copyright (c) 2026 relexx. BSD 3-Clause License.
            // See LICENSE file in the project root for full license information.
            """.trimIndent()
        )
    }
    kotlinGradle {
        target("**/*.gradle.kts")
        ktlint()
    }
}
