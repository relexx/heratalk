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
- `:core:ui`: Material-3 `HeraTalkTheme` (light + dark color schemes), `HeraTalkColors` traffic-light palette tokens (green/blue/yellow/red), `HeraTalkExtraColors` (warning/directCall/offline) via `CompositionLocal`, `HeraTalkTypography`, common composables (`HeraTalkScaffold`, `NetworkQualityBadge`, `SectionHeader`); EN+DE strings under `common_*` and `network_quality_*`; light/dark Compose previews. (`feat(core:ui)`)
- `:service:lifecycle`: `HeraTalkService` foreground service with localised notification, `FeatureState` data class driving the `connectedDevice`/`microphone` foreground-service-type switch (per architecture.md §11.3); module-owned EN+DE notification strings. Microphone path parked behind `TODO("v0.7.0")`. (`feat(service:lifecycle)`)
- `:service:discovery`: `PeerDiscovery` interface plus `PeerDiscoveryStub` no-op implementation. Real `NsdManager`-backed adapter and broadcast beacon land in v0.2.0. (`feat(service:discovery)`)
- `:service:transport`: `Transport` interface + `TransportStub`. Real UDP unicast/broadcast and TCP relay land in v0.2.0 / v0.4.0 / v0.10.0. (`feat(service:transport)`)
- `:service:signaling`: `ControlPlane` interface + sealed `ControlPlaneState` hierarchy + `ControlPlaneStub`. Noise handshake lands in v0.5.0. (`feat(service:signaling)`)
- `:service:media`: `MediaEngine` interface + `DecodedFrame` data class + `MediaEngineStub`. Unencrypted RTP path lands v0.4.0; SRTP wrapping v0.6.0. (`feat(service:media)`)
- `:service:audio`: `AudioEngine` interface + `AudioEngineStub`. AudioRecord + libopus JNI bridge land in v0.3.0. (`feat(service:audio)`)
- `:service:ptt`: `FloorController` interface + sealed `FloorState` hierarchy + `FloorControllerStub`. Real arbitration plus VOX hangover land in v0.7.0. (`feat(service:ptt)`)
- `:service:relay`: `RelayService` interface + `RelayOffer` data class + `RelayServiceStub`. Real relay routing lands in v0.10.0. (`feat(service:relay)`)
- `:feature:pairing`: `ChannelChoiceScreen`, `DisplayNameScreen` (validating input with live code-point counter and submit gate), `QrScanScreen` placeholder; `PairingViewModel` persists validated names through `:core:identity`. EN+DE strings under `pairing_*`. (`feat(feature:pairing)`)
- `:feature:channel`: `ChannelScreen` skeleton with `HeraTalkScaffold` header, empty-roster placeholder and a disabled 110 dp PTT anchor labelled "Available in v0.4.0". EN+DE strings under `channel_*`. (`feat(feature:channel)`)
- `:feature:settings`: `SettingsScreen` ordered per the 2026-04-25 UX revision; live language radio (System/DE/EN) immediately applies `AppCompatDelegate.setApplicationLocales(...)` and persists to a module-local DataStore; theme radio + update-check + auto-resume toggles persist for later releases; display-name section reads from `:core:identity` and exposes an Edit action that the host activity routes to the shared `DisplayNameScreen` (no `:feature:settings` → `:feature:pairing` Gradle dependency, navigation hoisted via `:app`). EN+DE strings under `settings_*`. (`feat(feature:settings)`)
- `:feature:direct`: empty placeholder so the Gradle module topology mirrors `architecture.md` §4. Real direct-call UI lands in v0.8.0. (`feat(feature:direct)`)
- `androidx.appcompat` 1.7.0 added to `gradle/libs.versions.toml` exclusively for `:feature:settings`'s locale switch. (`build`)
- ADR-0004: documents the accepted exception to Rule 17 for Android adapter layers inside `:core:logging` and `:core:identity`, including acceptance criteria and module whitelist. (`docs(adr)`)
- F-16: Display-name input during pairing flow — mandatory field (≥1 visible character, ≤32 Unicode codepoints), persisted in DataStore via `:core:identity`. Corruption fallback `Peer-{first8hex(pk)}` — never `Build.MODEL`. Inbound peer names sanitized in `:service:discovery` (NFC normalization, Bidi-override stripping, combining-mark limit, codepoint truncation); see security finding F-PRIV-04.
