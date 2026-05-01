# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Added

- Build system: Gradle multi-module skeleton with `build-logic` convention plugins (`heratalk.android.library`, `heratalk.android.application`, `heratalk.kotlin.common`), all 17 module stubs registered in `settings.gradle.kts`, Gradle wrapper 9.4.1. (`build(a1)`)
- Root tooling: `lint.xml` (`MissingTranslation`, `HardcodedText` as error), `detekt.yml`, Spotless with BSD 3-Clause copyright-header enforcement. (`build(a1)`)
- `:core:model`: domain data classes `PeerId`, `ChannelId`, `DisplayName` (value class, 1–32 codepoints, ≥1 visible character), `NetworkQuality` enum, `Peer`, `ChannelInfo`; full KDoc; JUnit-5 tests including edge cases (empty, whitespace-only, 33 codepoints, combining marks, Bidi-override). (`feat(core:model)`, `fix(core:model)`)
- `:core:logging`: `Logger` interface, `LogEntry` data class, `AndroidLogcatLogger` (logcat adapter), `RingBufferLogger` (1000-entry in-memory ring buffer with `SharedFlow<LogEntry>`), `CompositeLogger`; Turbine-based Flow tests. (`feat(core:logging)`, `build(core:logging)`)
- `:core:crypto`: `KeyDerivation` and `Aead` skeleton interfaces with no-op implementations (`NotImplementedError` with v0.5.0/v0.6.0 release markers); skeleton aligned to ADR-0002 Noise phase separation (initial pairing vs. regular session). (`feat(core:crypto)`, `refactor(core:crypto)`)
- `:core:identity`: `IdentityRepository` interface (`Flow<DisplayName?>`, `set`, `fallbackName`), `DataStoreIdentityRepository` (Preferences DataStore impl), `fallbackPeerName` pure function (`"Peer-{first8hex(pk)}"`); JUnit-5 and Kotest property-based tests. (`feat(core:identity)`)
- ADR-0004: documents the accepted exception to Rule 17 for Android adapter layers inside `:core:logging` and `:core:identity`, including acceptance criteria and module whitelist. (`docs(adr)`)
- F-16: Display-name input during pairing flow — mandatory field (≥1 visible character, ≤32 Unicode codepoints), persisted in DataStore via `:core:identity`. Corruption fallback `Peer-{first8hex(pk)}` — never `Build.MODEL`. Inbound peer names sanitized in `:service:discovery` (NFC normalization, Bidi-override stripping, combining-mark limit, codepoint truncation); see security finding F-PRIV-04.
