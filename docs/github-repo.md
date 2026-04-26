# HeraTalk — GitHub-Repository-Setup

> Stand: April 2026 — aktualisiert auf AGP 9.2, Gradle 9.4, JDK 21, API 36, NDK r28+, und aktuelle GitHub-Action-Major-Versionen.

## Repository

- **URL:** https://github.com/relexx/heratalk
- **Publisher:** relexx (relexx.de)
- **Lizenz:** BSD 3-Clause (wie Opus). LICENSE-Datei wird beim Anlegen durch GitHub erzeugt — nicht manuell committen.
- **Default-Branch:** `main`
- **Language-Label:** Kotlin

## Vollständige Repository-Struktur

```
heratalk/
├── .claude/
│   ├── CLAUDE.md
│   ├── rules.md
│   └── agents/
│       ├── orchestrator.md
│       ├── architect.md
│       ├── developer.md
│       └── documenter.md
├── .devcontainer/
│   ├── devcontainer.json
│   ├── Dockerfile
│   ├── post-create.sh
│   └── post-start.sh
├── .github/
│   ├── workflows/
│   │   ├── build.yml
│   │   ├── lint.yml
│   │   ├── release.yml
│   │   ├── codeql.yml
│   │   └── no-internet-check.yml
│   ├── ISSUE_TEMPLATE/
│   │   ├── bug_report.yml
│   │   ├── feature_request.yml
│   │   └── config.yml
│   ├── PULL_REQUEST_TEMPLATE.md
│   ├── CODEOWNERS
│   └── dependabot.yml
├── app/                              # :app Modul
├── core/                             # :core:* Module
├── feature/                          # :feature:* Module
├── service/                          # :service:* Module
├── gradle/
│   ├── libs.versions.toml
│   └── wrapper/
├── docs/
│   ├── architecture.md
│   ├── devcontainer.md
│   ├── requirements.md
│   ├── project-state.md
│   ├── releases.md
│   ├── security.md
│   ├── protocol.md                   # wird mit v0.5.0 gefüllt
│   └── adrs/
│       ├── 0001-kotlin-native.md
│       ├── 0002-noise-protocol.md
│       └── 0003-srtp-custom-implementation.md
├── build.gradle.kts
├── settings.gradle.kts
├── gradle.properties
├── gradlew
├── gradlew.bat
├── README.md
├── CHANGELOG.md
├── CONTRIBUTING.md
├── CODE_OF_CONDUCT.md
├── SECURITY.md
├── LICENSE                           # wird von GitHub erzeugt
├── .gitignore
├── .gitattributes
└── .editorconfig
```

## Wurzel-Dateien

### `README.md`

```markdown
# HeraTalk

LAN-based push-to-talk voice chat for Android. No servers, no accounts, no internet required.

[![Build](https://github.com/relexx/heratalk/actions/workflows/build.yml/badge.svg)](https://github.com/relexx/heratalk/actions/workflows/build.yml)
[![License: BSD-3-Clause](https://img.shields.io/badge/License-BSD_3--Clause-blue.svg)](LICENSE)

## Features

- Peer-to-peer mesh over Wi-Fi, zero infrastructure
- Encrypted audio (SRTP + Noise protocol)
- Broadcast channels and 1:1 direct calls
- Push-to-talk or VOX (voice-activated)
- Works under hostile AP conditions (client isolation, multicast filtering, jitter)

## Requirements

- Android 10 (API 29) or higher
- Wi-Fi network shared by all participants

## Building

See [docs/devcontainer.md](docs/devcontainer.md) for the recommended dev container setup.
For a local build:

    ./gradlew assembleDebug

## Documentation

- [Architecture](docs/architecture.md)
- [Requirements](docs/requirements.md)
- [Security model](docs/security.md)
- [Release plan](docs/releases.md)
- [Current project state](docs/project-state.md)
- [Architecture Decision Records](docs/adrs/)

## Contributing

See [CONTRIBUTING.md](CONTRIBUTING.md).

## License

BSD 3-Clause License. See [LICENSE](LICENSE).

Copyright © 2026 relexx (https://relexx.de)
```

### Copyright-Header für alle Source-Dateien

```kotlin
/*
 * Copyright (c) 2026 relexx. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the conditions of the
 * BSD 3-Clause License are met. See the LICENSE file in the project root.
 */
```

Wird per Gradle-Plugin (`com.diffplug.spotless`) automatisch eingefügt und geprüft.

### `gradle/libs.versions.toml` (Version-Katalog, Stand April 2026)

```toml
[versions]
# Build
agp = "9.2.0"
kotlin = "2.3.21"
ksp = "2.3.21-2.0.3"

# Min / target / compile SDK
minSdk = "29"
targetSdk = "36"
compileSdk = "36"

# AndroidX
composeBom = "2026.04.00"
activity = "1.10.1"
lifecycle = "2.9.0"
core = "1.17.0"
navigation = "2.9.0"
datastore = "1.2.0"

# DI
koin = "4.2.0"

# Serialisation / Proto
protobuf = "4.29.2"
protobufPlugin = "0.9.5"

# Crypto
bouncycastle = "1.80"
noise = "1.2-SNAPSHOT"  # rweather/noise-java; via JitPack oder vendored

# QR
mlkitBarcode = "17.3.0"
zxing = "3.5.3"

# Audio / NDK
# libopus wird als eigenes Git-Submodule eingebunden und per CMake gebaut.

# Networking
okhttp = "5.0.0"

# Tests
junit5 = "5.11.4"
mockk = "1.14.2"
turbine = "1.2.0"
kotest = "5.9.1"
androidxTest = "1.6.1"

# Static analysis
detekt = "1.23.8"
spotless = "7.0.2"

[libraries]
androidx-core = { module = "androidx.core:core-ktx", version.ref = "core" }
androidx-activity-compose = { module = "androidx.activity:activity-compose", version.ref = "activity" }
androidx-lifecycle-runtime = { module = "androidx.lifecycle:lifecycle-runtime-ktx", version.ref = "lifecycle" }
androidx-lifecycle-viewmodel-compose = { module = "androidx.lifecycle:lifecycle-viewmodel-compose", version.ref = "lifecycle" }
androidx-navigation-compose = { module = "androidx.navigation:navigation-compose", version.ref = "navigation" }
androidx-datastore = { module = "androidx.datastore:datastore-preferences", version.ref = "datastore" }
androidx-datastore-proto = { module = "androidx.datastore:datastore", version.ref = "datastore" }

compose-bom = { module = "androidx.compose:compose-bom", version.ref = "composeBom" }
compose-ui = { module = "androidx.compose.ui:ui" }
compose-ui-tooling-preview = { module = "androidx.compose.ui:ui-tooling-preview" }
compose-material3 = { module = "androidx.compose.material3:material3" }
compose-ui-tooling = { module = "androidx.compose.ui:ui-tooling" }

koin-core = { module = "io.insert-koin:koin-core", version.ref = "koin" }
koin-android = { module = "io.insert-koin:koin-android", version.ref = "koin" }
koin-compose = { module = "io.insert-koin:koin-compose", version.ref = "koin" }
koin-compose-viewmodel = { module = "io.insert-koin:koin-compose-viewmodel", version.ref = "koin" }

protobuf-kotlin-lite = { module = "com.google.protobuf:protobuf-kotlin-lite", version.ref = "protobuf" }
protobuf-protoc = { module = "com.google.protobuf:protoc", version.ref = "protobuf" }

bouncycastle = { module = "org.bouncycastle:bcprov-jdk18on", version.ref = "bouncycastle" }

mlkit-barcode = { module = "com.google.mlkit:barcode-scanning", version.ref = "mlkitBarcode" }
zxing-core = { module = "com.google.zxing:core", version.ref = "zxing" }

okhttp = { module = "com.squareup.okhttp3:okhttp", version.ref = "okhttp" }

junit-jupiter-api = { module = "org.junit.jupiter:junit-jupiter-api", version.ref = "junit5" }
junit-jupiter-engine = { module = "org.junit.jupiter:junit-jupiter-engine", version.ref = "junit5" }
mockk = { module = "io.mockk:mockk", version.ref = "mockk" }
turbine = { module = "app.cash.turbine:turbine", version.ref = "turbine" }
kotest-property = { module = "io.kotest:kotest-property", version.ref = "kotest" }

[plugins]
android-application = { id = "com.android.application", version.ref = "agp" }
android-library = { id = "com.android.library", version.ref = "agp" }
# Note: AGP 9.0+ provides built-in Kotlin support — no "kotlin-android" plugin needed.
protobuf = { id = "com.google.protobuf", version.ref = "protobufPlugin" }
ksp = { id = "com.google.devtools.ksp", version.ref = "ksp" }
detekt = { id = "io.gitlab.arturbosch.detekt", version.ref = "detekt" }
spotless = { id = "com.diffplug.spotless", version.ref = "spotless" }
```

> Hinweis: **AGP 9.0+ hat built-in Kotlin-Support.** Der alte `kotlin-android`-Plugin-Apply entfällt. Die Kotlin-Version wird über AGP-Interna oder einen separaten `kotlin`-Plugin (wenn z. B. für `:core:*`-Pure-Kotlin-Module benötigt) gesetzt.

### `CHANGELOG.md`

```markdown
# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]
### Added
- Initial project scaffolding.
```

### `CONTRIBUTING.md`, `CODE_OF_CONDUCT.md`

Wie in der ursprünglichen Fassung — siehe vorherige Version dieses Dokuments. Inhalte bleiben gleich; lediglich Versionshinweise in `CONTRIBUTING.md` auf die aktuelle Toolchain anpassen.

### `SECURITY.md` (aktualisiert 2026-04-25 — GitHub Private Vulnerability Reporting)

```markdown
# Security Policy

## Supported Versions

Only the latest release is supported for security updates.

## Reporting a Vulnerability

Please do **not** open a public GitHub issue for security vulnerabilities.

Use GitHub's **Private Vulnerability Reporting** instead:

https://github.com/relexx/heratalk/security/advisories/new

This keeps your report confidential between you and the maintainers until a fix
is ready. You will receive an acknowledgement within 72 hours, and we aim to
provide a fix or mitigation plan within 30 days for high-severity issues.

Please include:
- A description of the vulnerability
- Steps to reproduce
- Affected versions if known
- Any proof-of-concept code or data

## Scope

In scope:
- Cryptographic weaknesses in pairing or SRTP implementation
- Bypass of channel-secret enforcement
- Relay peer being able to decrypt forwarded traffic
- Buffer overflows in the JNI audio bridge
- Privilege escalation via the foreground service
- Injection via the `heratalk://` URL scheme

Out of scope:
- Denial of service via malformed packets (expected: packets are dropped cleanly)
- Physical access to a paired device
- Attacks requiring root/administrator access on the target device
- Social engineering against channel members
```

**Repo-Einstellungen zur Aktivierung:** Im GitHub-Repo unter *Settings → Security → Private vulnerability reporting* aktivieren. Geschieht einmalig beim Repo-Setup.

### `.gitignore`, `.gitattributes`, `.editorconfig`

Unverändert zur vorherigen Version.

## GitHub Actions Workflows

> **Action-Versionen aktualisiert** auf aktuelle Major-Releases. `actions/setup-java@v5`, `android-actions/setup-android@v3`, `gradle/actions/setup-gradle@v4`.

### `.github/workflows/build.yml`

```yaml
name: Build

on:
  push:
    branches: [main, develop]
  pull_request:
    branches: [main, develop]

jobs:
  build:
    runs-on: ubuntu-24.04
    steps:
      - uses: actions/checkout@v4
        with:
          fetch-depth: 0

      - name: Set up JDK 21
        uses: actions/setup-java@v5
        with:
          distribution: temurin
          java-version: 21

      - name: Set up Android SDK
        uses: android-actions/setup-android@v3
        with:
          cmdline-tools-version: 13114758

      - name: Install Android NDK and Build-Tools
        run: |
          sdkmanager "platforms;android-36" \
                     "build-tools;36.0.0" \
                     "ndk;28.2.13676358" \
                     "cmake;3.22.1"

      - name: Cache Gradle
        uses: gradle/actions/setup-gradle@v4

      - name: Build debug APK
        run: ./gradlew assembleDebug --no-daemon

      - name: Run unit tests
        run: ./gradlew testDebug --no-daemon

      - name: Verify 16 KB page alignment
        run: |
          APK=$(find app/build/outputs/apk/debug -name "*.apk" | head -1)
          "$ANDROID_HOME/build-tools/36.0.0/zipalign" -c -P 16 -v 4 "$APK" \
            && echo "✓ 16 KB aligned" || (echo "✗ alignment failed" && exit 1)

      - name: Upload APK
        uses: actions/upload-artifact@v4
        with:
          name: heratalk-debug
          path: app/build/outputs/apk/debug/*.apk
          retention-days: 14

      - name: Upload test reports on failure
        if: failure()
        uses: actions/upload-artifact@v4
        with:
          name: test-reports
          path: "**/build/reports/tests/"
```

### `.github/workflows/lint.yml`

```yaml
name: Lint

on:
  push:
    branches: [main, develop]
  pull_request:

jobs:
  lint:
    runs-on: ubuntu-24.04
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-java@v5
        with:
          distribution: temurin
          java-version: 21
      - uses: android-actions/setup-android@v3
      - uses: gradle/actions/setup-gradle@v4
      - name: Run detekt
        run: ./gradlew detekt --no-daemon
      - name: Run Android Lint
        run: ./gradlew lintDebug --no-daemon
      - name: Check Spotless (copyright & format)
        run: ./gradlew spotlessCheck --no-daemon
      - name: Upload lint reports
        if: always()
        uses: actions/upload-artifact@v4
        with:
          name: lint-reports
          path: |
            **/build/reports/detekt/
            **/build/reports/lint-results-*.html
```

### `.github/workflows/codeql.yml`

```yaml
name: CodeQL

on:
  push:
    branches: [main]
  schedule:
    - cron: "0 6 * * 1"

jobs:
  analyze:
    runs-on: ubuntu-24.04
    permissions:
      security-events: write
      contents: read
    strategy:
      matrix:
        language: [java-kotlin]
    steps:
      - uses: actions/checkout@v4
      - uses: github/codeql-action/init@v3
        with:
          languages: ${{ matrix.language }}
          queries: security-extended,security-and-quality
      - uses: actions/setup-java@v5
        with:
          distribution: temurin
          java-version: 21
      - uses: android-actions/setup-android@v3
      - name: Build for CodeQL
        run: ./gradlew assembleDebug --no-daemon
      - uses: github/codeql-action/analyze@v3
```

### `.github/workflows/release.yml`

```yaml
name: Release

on:
  push:
    tags:
      - "v*.*.*"

jobs:
  release:
    runs-on: ubuntu-24.04
    permissions:
      contents: write
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-java@v5
        with:
          distribution: temurin
          java-version: 21
      - uses: android-actions/setup-android@v3
      - uses: gradle/actions/setup-gradle@v4

      - name: Decode keystore
        env:
          KEYSTORE_BASE64: ${{ secrets.RELEASE_KEYSTORE_BASE64 }}
        run: |
          echo "$KEYSTORE_BASE64" | base64 -d > release.keystore

      - name: Build release APK
        env:
          KEYSTORE_PATH: ${{ github.workspace }}/release.keystore
          KEYSTORE_PASSWORD: ${{ secrets.RELEASE_KEYSTORE_PASSWORD }}
          KEY_ALIAS: ${{ secrets.RELEASE_KEY_ALIAS }}
          KEY_PASSWORD: ${{ secrets.RELEASE_KEY_PASSWORD }}
        run: ./gradlew assembleRelease --no-daemon

      - name: Verify 16 KB page alignment (release)
        run: |
          APK=$(find app/build/outputs/apk/release -name "*.apk" | head -1)
          "$ANDROID_HOME/build-tools/36.0.0/zipalign" -c -P 16 -v 4 "$APK"

      - name: Generate build provenance attestation
        uses: actions/attest-build-provenance@v2
        with:
          subject-path: app/build/outputs/apk/release/*.apk

      - name: Create GitHub Release
        uses: softprops/action-gh-release@v2
        with:
          files: app/build/outputs/apk/release/*.apk
          generate_release_notes: true
          make_latest: true
```

**Neu:** `actions/attest-build-provenance@v2` — erstellt eine SLSA-Build-Provenance-Attestierung für jede Release-APK. Das gibt Nachnutzern kryptografische Gewissheit, dass die APK aus genau diesem GitHub-Actions-Run aus diesem Repo stammt. Verifizierbar via `gh attestation verify`.

### `.github/workflows/no-internet-check.yml` (neu)

Verifiziert, dass die Debug-APK tatsächlich keine Internet-Verbindungen öffnet (Anforderung NF-03). Läuft wöchentlich und manuell.

```yaml
name: No-Internet Verification

on:
  workflow_dispatch:
  schedule:
    - cron: "0 4 * * 0"   # Sonntags 04:00 UTC

jobs:
  verify:
    runs-on: ubuntu-24.04
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-java@v5
        with: { distribution: temurin, java-version: 21 }
      - uses: android-actions/setup-android@v3
      - uses: gradle/actions/setup-gradle@v4

      - name: Build debug APK
        run: ./gradlew assembleDebug --no-daemon

      - name: Start Android emulator
        uses: reactivecircus/android-emulator-runner@v2
        with:
          api-level: 35
          target: google_apis
          arch: x86_64
          profile: pixel_6
          script: |
            adb install app/build/outputs/apk/debug/*.apk
            adb shell tcpdump -i any -w /sdcard/capture.pcap &
            adb shell monkey -p de.relexx.heratalk 500
            sleep 20
            adb pull /sdcard/capture.pcap capture.pcap

      - name: Analyze capture for external traffic
        run: |
          # Prüft, dass keine Pakete zu öffentlichen IPs gehen
          python3 tools/check_no_internet.py capture.pcap
```

### `.github/dependabot.yml`

```yaml
version: 2
updates:
  - package-ecosystem: gradle
    directory: "/"
    schedule:
      interval: weekly
    open-pull-requests-limit: 5
    groups:
      androidx:
        patterns: ["androidx.*"]
      kotlin:
        patterns: ["org.jetbrains.kotlin*", "org.jetbrains.kotlinx*"]
      koin:
        patterns: ["io.insert-koin:*"]
      test:
        patterns: ["org.junit.jupiter:*", "io.mockk:*", "app.cash.turbine:*", "io.kotest:*"]

  - package-ecosystem: github-actions
    directory: "/"
    schedule:
      interval: weekly

  - package-ecosystem: docker
    directory: "/.devcontainer"
    schedule:
      interval: weekly
```

## Issue-, PR-Templates, CODEOWNERS

Wie in der ursprünglichen Fassung — Inhalte bleiben gleich.

## Branch Protection (UI konfigurieren)

- `main` protected
- Require PR review (min. 1)
- Require status checks: `Build`, `Lint`, `CodeQL`
- Require branches up to date before merging
- **Require signed commits** (Sigstore oder GPG)
- Disallow force-push
- Require linear history
- Restrict who can push to matching branches: nur Maintainer

## Initial-Commit-Strategie

1. **Repo auf GitHub anlegen** mit BSD-3-Clause-Template (erzeugt `LICENSE`).
2. **Commit 1:** `.gitignore`, `.gitattributes`, `.editorconfig`, `README.md`, `CHANGELOG.md`, `CONTRIBUTING.md`, `CODE_OF_CONDUCT.md`, `SECURITY.md`.
3. **Commit 2:** `.devcontainer/*`.
4. **Commit 3:** `.github/*`.
5. **Commit 4:** `.claude/*` (CLAUDE.md, rules.md, agents/).
6. **Commit 5:** `docs/*` (architecture.md, requirements.md, releases.md, project-state.md, devcontainer.md, security.md, adrs/).
7. **Commit 6:** Gradle-Skeleton (root `build.gradle.kts`, `settings.gradle.kts`, `libs.versions.toml`, Wrapper).
8. **Commit 7+:** Modul-Skelette gemäß Release v0.1.0.

Danach läuft CI grün, und das agentische Team kann die Release-Planung abarbeiten.
