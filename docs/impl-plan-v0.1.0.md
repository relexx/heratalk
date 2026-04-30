# Implementierungsplan — Release v0.1.0 "Grundgerüst"

> Erstellt vom Architekt-Agent am 2026-04-30 auf Branch `release/v0.1.0`.
> Zielgruppe: Entwickler-Agent. Gepflegt vom Orchestrator beim Fortschreiten.

## 0. Ziel und Leitplanken

v0.1.0 liefert ein **kompilierbares, lauffähiges App-Skelett** ohne Netzwerk und ohne Audio. Jede in `docs/architecture.md §4` benannte Modul-Box existiert als Gradle-Modul mit Stub-Interface, leerer Implementierung und Koin-Binding. Drei Compose-Screens sind via Navigation erreichbar. Der Foreground-Service kann gestartet/gestoppt werden. i18n ist von Beginn an scharf geschaltet.

**Was nicht in diesen Release gehört:** Discovery, Transport, Crypto-Logik, Audio-Pipeline, Pairing-QR-Logik, echte VOX/PTT-Funktion. Diese Module werden als **Skelett mit Interface + leerer Stub-Impl** angelegt — ihr fachlicher Inhalt kommt in den nachfolgenden Releases (siehe Modul-Tabelle unten).

**Drei Leitprinzipien für den Entwickler:**

1. **Jeder Schritt baut.** Nach jedem Schritt aus diesem Plan muss `./gradlew assembleDebug` durchlaufen. Niemals 10 Module gleichzeitig hinzufügen, dann kompilieren — immer eines nach dem anderen.
2. **Interfaces zuerst, Impls leer.** Ein Skelett-Modul exportiert ein Interface und eine no-op-Stub-Implementierung mit `TODO("v0.X.0")`-Markierungen. Das Interface ist der Vertrag, an dem Folge-Releases hängen.
3. **i18n von der ersten Zeile an.** Kein Compose-Screen darf einen einzigen hartkodierten String enthalten (siehe `.claude/rules.md` Regel 33). Sobald ein Text auftaucht, kommt er in `values/strings.xml` und `values-de/strings.xml`.

## 1. Modul-Inventar und Reihenfolge

Reihenfolge folgt der Abhängigkeitsregel `feature` → `service` → `core`. Module werden **von innen nach außen** angelegt.

| # | Modul | Typ | Skelett-Inhalt v0.1.0 | Aktiv ab |
|---|-------|-----|------------------------|----------|
| 1 | `:core:model` | Library | Domain-Datenklassen: `PeerId`, `ChannelId`, `Peer`, `ChannelInfo`, `DisplayName` (value class), `NetworkQuality` (enum) | v0.1.0 |
| 2 | `:core:logging` | Library | `Logger`-Interface, `AndroidLogcatLogger`-Impl, `RingBufferLogger`-Impl mit `Flow<LogEntry>` für späteres Diagnose-Overlay | v0.1.0 |
| 3 | `:core:crypto` | Library | Nur Type-Stubs: `interface KeyDerivation`, `interface Aead`, no-op-Impls mit `TODO("v0.5.0/v0.6.0")` | v0.1.0 (Skeleton) |
| 4 | `:core:identity` | Library | `IdentityRepository` (Interface) + `DataStoreIdentityRepository` (Impl). DataStore-Key `display_name`, `Flow<DisplayName>` als API. Validierung: 1-32 Codepoints, sichtbares Zeichen verlangt. Fallback `Peer-{first8hex(pk)}` als pure Funktion | v0.1.0 |
| 5 | `:core:ui` | Library (Compose) | Material-3-Theme (Light/Dark, Ampel-Farbpalette), gemeinsame Composables (`HeraTalkScaffold`, `NetworkQualityBadge`, `SectionHeader`), Typografie. Strings in `values/strings.xml` und `values-de/strings.xml` für gemeinsame UI | v0.1.0 |
| 6 | `:service:lifecycle` | Library | `HeraTalkService` (Foreground-Service), `FeatureState`-Datenklasse, `setFeatureState(...)`-Methode mit dynamischem Typ-Wechsel-Skelett (siehe `architecture.md §11.3`). Im Skelett nur Typ `connectedDevice` aktiv — `microphone`-Wechsel als TODO | v0.1.0 |
| 7 | `:service:discovery` | Library | `interface PeerDiscovery { fun start(); fun stop(); val peers: Flow<Set<Peer>> }` + leere Stub-Impl. Subscription auf `IdentityRepository.displayName` als TODO mit Kommentar | v0.2.0 (fachlich) |
| 8 | `:service:transport` | Library | `interface Transport { suspend fun send(...); val incoming: Flow<...> }` + Stub | v0.2.0 |
| 9 | `:service:signaling` | Library | `interface ControlPlane { suspend fun connect(peer: Peer); val state: StateFlow<...> }` + Stub | v0.5.0 |
| 10 | `:service:media` | Library | `interface MediaEngine { ... }` + Stub | v0.4.0 |
| 11 | `:service:audio` | Library | `interface AudioEngine { ... }` + Stub. **Keine** JNI-Brücke in v0.1.0 — wird mit v0.3.0 angelegt | v0.3.0 |
| 12 | `:service:ptt` | Library | `interface FloorController { ... }` + Stub | v0.7.0 |
| 13 | `:service:relay` | Library | `interface RelayService { ... }` + Stub | v0.10.0 |
| 14 | `:feature:pairing` | Library (Compose) | Pairing-Screen-Skelett: Kanal-Wahl-Screen + **Display-Name-Eingabe-Screen** (validierend, F-16). QR-Scanner-Screen als Stub mit TODO. Liest und schreibt ausschließlich über `:core:identity`. | v0.1.0 (Display-Name fachlich), v0.5.0 (QR fachlich) |
| 15 | `:feature:channel` | Library (Compose) | Hauptscreen-Skelett: Header (Kanal-Name + Netzwerk-Indikator), Peer-Liste (leer), großer PTT-Button (deaktiviert). Strings vollständig lokalisiert | v0.1.0 (Skelett), v0.2.0 (fachlich) |
| 16 | `:feature:direct` | Library (Compose) | **Nur Modulanlage + leerer Composable**. Kein Screen in Navigation in v0.1.0. Damit ist der Slot belegt, aber unsichtbar | v0.8.0 |
| 17 | `:feature:settings` | Library (Compose) | Settings-Screen-Skelett: Sprach-Auswahl (System/DE/EN, **funktional**), Theme-Toggle (Stub), "Dein Name"-Eintrag (liest/schreibt `:core:identity`), Update-Check-Toggle (Persistent, kein Netzwerk-Aufruf in v0.1.0) | v0.1.0 (fachlich für i18n + Display-Name + Update-Toggle), v0.7.0 (Audio-Settings) |
| 18 | `:app` | Application | Application-Klasse mit Koin-`startKoin`, MainActivity mit NavHost (3 Routes: pairing/channel/settings), AndroidManifest mit allen Permissions aus `architecture.md §11.1`, Service-Deklaration | v0.1.0 |

Hinweis zur **`:feature:direct`-Anlage**: bewusst angelegt, aber nicht in die Navigation eingebunden. So bleibt die Modul-Topologie konsistent zur Architektur, ohne dass eine Geistermenüpunkt entsteht.

## 2. Implementierungs-Sequenz

Jeder Schritt ist ein eigener Commit. Reihenfolge ist verbindlich.

### Phase A — Build-System und Infrastruktur

**A1 — `settings.gradle.kts` und Build-Konventionen.**
- `settings.gradle.kts`: alle Module aus Tabelle oben unkommentieren bzw. ergänzen (`:core:identity`, `:core:logging`, `:core:ui`, `:service:lifecycle`, `:service:discovery`, `:service:transport`, `:service:relay`, `:service:ptt`, `:feature:pairing`, `:feature:direct`).
- `build.gradle.kts` (root): bleibt thin. Plugin-Aliase via `apply false`.
- Optional, aber empfohlen: `buildSrc/` oder `build-logic/` (Convention-Plugin) für `heratalk.android.library`, `heratalk.android.application`, `heratalk.kotlin.common` — vermeidet Dutzende kopierter Build-Skripte. Wenn der Entwickler-Agent das vermeiden will, akzeptabel — dann jedes Modul-Skript einzeln pflegen.
- `kotlinOptions`/`compilerOptions`: explicit API mode `-Xexplicit-api=strict` für Library-Module, `-Xexplicit-api=warning` für `:app`.
- `lint.xml` im Root: `MissingTranslation` und `HardcodedText` als `error`. Wird in jedem Modul aktiv.
- `detekt.yml`: vorhandene Defaults plus Custom-Rule-Stub `HardcodedStringInComposable` (Custom-Rule kommt in A8 separat — hier Platzhalter).
- `spotless`-Config im Root: Kotlin-Formatter ktlint, Copyright-Header-Template gemäß BSD 3-Clause.

**Akzeptanz A1:** `./gradlew help` läuft. `./gradlew projects` zeigt alle Module.

**A2 — `:core:model`.**
- Datei: `core/model/build.gradle.kts` als Java-Library oder Android-Library (Java-Library reicht — kein Android-API nötig).
- Inhalt: `data class Peer`, `value class PeerId`, `value class ChannelId`, `value class DisplayName(val value: String)` mit `init`-Block (1-32 Codepoints, mind. 1 sichtbares Zeichen), `enum class NetworkQuality { GOOD, DEGRADED, POOR, OFFLINE }`. KDoc auf jedem Public Member.
- Tests: `DisplayNameTest` mit JUnit 5 — Validierung Edge-Cases (leer, 33 Codepoints, nur Whitespace, mit Combining Marks, mit Bidi-Override).

**Akzeptanz A2:** `./gradlew :core:model:test` grün.

**A3 — `:core:logging`.**
- `interface Logger { fun d(tag, msg); fun w(...); fun e(...) }`, `data class LogEntry`.
- Impl 1: `AndroidLogcatLogger` (delegiert an `android.util.Log`).
- Impl 2: `RingBufferLogger` (in-memory, 1000 Entries, `MutableSharedFlow<LogEntry>`).
- Composite-Logger: `CompositeLogger(loggers: List<Logger>)`.

**Akzeptanz A3:** `./gradlew :core:logging:test` grün, Ring-Buffer-Test mit Turbine.

**A4 — `:core:crypto` (Skeleton).**
- `interface KeyDerivation { fun deriveSrtpKey(...): ByteArray }` — Impl wirft `NotImplementedError("v0.5.0")`.
- `interface Aead { fun seal(...); fun open(...) }` — Impl wirft `NotImplementedError("v0.6.0")`.
- Keine echten Crypto-Operationen. Keine BouncyCastle-Dependency in v0.1.0 — wird in v0.5.0 ergänzt.

**Akzeptanz A4:** Modul kompiliert.

**A5 — `:core:identity`.**
- DataStore-Setup: Preferences-DataStore (kein Proto in v0.1.0 nötig — DataStore-Preferences reicht für einen einzelnen String-Key).
- `interface IdentityRepository { val displayName: Flow<DisplayName?>; suspend fun set(name: DisplayName); suspend fun fallbackName(pk: ByteArray): DisplayName }`.
- Impl: `DataStoreIdentityRepository(dataStore: DataStore<Preferences>, ...)`.
- Pure Funktion: `fallbackPeerName(pk: ByteArray): String` → `"Peer-${first8hex(pk)}"`.
- Tests: Validierung, Persistenz mit fakem DataStore, Fallback-Generator mit bekannten Test-Vektoren.

**Akzeptanz A5:** `./gradlew :core:identity:test` grün, Property-Test (Kotest) verifiziert: für jede gültige `DisplayName`-Eingabe ist Round-Trip durch DataStore fehlerfrei.

### Phase B — UI-Grundlagen und i18n

**B1 — `:core:ui`.**
- Material-3-Theme mit Color-Schemes für Light und Dark. Primärfarbe = Grün (Ampel "aktiv/verfügbar"), Sekundärfarbe = Blau (Direktruf), Error = Rot, Warning = Gelb. Farb-Tokens als `object HeraTalkColors` für nicht-Material-Slots.
- Typografie-Definition (`Typography` mit Compose-Material-3-Defaults, Anpassungen für `bodyLarge` und `titleLarge`).
- Wiederverwendbare Composables: `HeraTalkScaffold(title, networkQuality, content)`, `NetworkQualityBadge(quality)`, `SectionHeader(text)`.
- `core/ui/src/main/res/values/strings.xml` und `values-de/strings.xml` mit den allgemeinen Keys: `common_back`, `common_continue`, `common_cancel`, `network_quality_good/degraded/poor/offline`.
- `android:supportsRtl="true"` wird im `:app`-Manifest gesetzt (kommt in Phase E) — hier nur als Notiz.

**Akzeptanz B1:** Modul kompiliert. Compose-Preview rendert Theme.

**B2 — Lint-Enforcement testen.**
- Bewusst eine hartkodierte String-Verwendung in einer temporären Datei einbauen, `./gradlew lintDebug` laufen lassen — muss Build brechen. Danach wieder entfernen. Zweck: Smoketest, dass `lint.xml` greift.
- Custom-Detekt-Rule `HardcodedStringInComposable` ist out-of-scope für v0.1.0, falls die Implementierung den Release sprengt — TODO mit Issue. Alternative: vorerst nur Lint und manuelle Disziplin.

**Akzeptanz B2:** Lint-Check schlägt bei hartkodierten Strings fehl. Nach Cleanup grün.

### Phase C — Service- und Feature-Skelette

**C1 — `:service:lifecycle`.**
- `class HeraTalkService : Service()` mit `startForeground`-Aufruf, Notification mit lokalisiertem Channel-Name.
- `data class FeatureState(channelActive: Boolean, voxEnabled: Boolean, hardwarePttEnabled: Boolean)`.
- `fun setFeatureState(state: FeatureState)`-Methode: implementiert die Type-Switch-Logik aus `architecture.md §11.3`. In v0.1.0 ist nur `connectedDevice` aktiv; `microphone`-Pfad enthält `TODO("v0.7.0 — VOX/Hardware-PTT")`.
- Notification-Builder mit Strings aus `:core:ui`'s `strings.xml` (Notification-Channel-Name) bzw. `:app`'s strings (Service-Beschreibung).
- Service-Klasse exportiert `start(context, state)` und `stop(context)` als Companion-Object-API für saubere Aufrufe aus der Activity.

**Akzeptanz C1:** Modul kompiliert. Manueller Test in C5/Phase E: Service kann gestartet, Notification erscheint.

**C2 — Service-Skelette `:service:discovery`, `:service:transport`, `:service:signaling`, `:service:media`, `:service:audio`, `:service:ptt`, `:service:relay`.**

Jedes Modul:
- `build.gradle.kts` minimal (Android-Library, Kotlin, Coroutines).
- Eine `interface XxxEngine`-Datei mit Public API gemäß Architektur.
- Eine `XxxEngineStub`-Klasse, die das Interface implementiert mit `TODO("vX.Y.0")`.
- Keine Tests in v0.1.0 — Stubs testen wäre Zeremonie.

Reihenfolge der Anlage egal, aber alle Module sollen einzeln kompilieren. Niemals echten Code in einem Stub einbauen — nur Interface + No-op + Marker.

**Akzeptanz C2:** `./gradlew assembleDebug` grün mit allen Skelett-Modulen.

**C3 — `:feature:pairing` (Display-Name-Eingabe-Screen funktional).**
- Composables: `ChannelChoiceScreen` (Tap → Display-Name-Screen), `DisplayNameScreen` (TextField mit Validierung; deaktivierter "Weiter"-Button bei leerer/whitespace-Eingabe; Live-Counter Codepoints).
- ViewModel `PairingViewModel(identityRepository: IdentityRepository)` — schreibt nur, wenn Validierung passt.
- QR-Scan-Screen: `QrScanScreen` als Composable-Stub mit "Coming in v0.5.0"-Hinweis (lokalisiert).
- Strings vollständig in `values/strings.xml` und `values-de/strings.xml`.

**Akzeptanz C3:** Display-Name lässt sich eingeben, persistiert in DataStore, taucht in Settings wieder auf (siehe C5).

**C4 — `:feature:channel` (Skelett).**
- `ChannelScreen` Composable mit Scaffold (Header zeigt platzhaltigen Kanal-Namen aus String-Resource, Netzwerk-Indikator zeigt `OFFLINE` als Default), Peer-Liste leer, PTT-Button deaktiviert mit Hinweis-String "Verfügbar in v0.4.0".

**Akzeptanz C4:** Screen rendert.

**C5 — `:feature:settings` (Sprache + Display-Name + Update-Toggle funktional).**
- Sortierung gemäß Entscheidung 2026-04-25: Audio (Stub) → App-Verhalten (Sprache, Theme, Update-Check, Auto-Resume) → Netzwerk (Stub) → Benachrichtigungen (Stub) → Features und Berechtigungen (Stub) → Kanal (Display-Name) → Info.
- Sprach-Auswahl: 3-Optionen-Radio (System / Deutsch / Englisch). Auswahl ruft `AppCompatDelegate.setApplicationLocales(...)`. Persistenz in DataStore.
- Display-Name-Eintrag öffnet `DisplayNameScreen` aus `:feature:pairing` im Edit-Modus (vorbelegt mit aktuellem Wert; ist global, nicht pro Kanal).
- Update-Check-Toggle: persistiert Bool, **kein** tatsächlicher Netzwerkaufruf in v0.1.0 (kommt in späterem Release).
- Theme-Toggle: persistiert Bool/Enum, wirkt aber noch nicht — Theme-Switching kommt in v1.0.

**Akzeptanz C5:** Sprache lässt sich umschalten und wirkt sofort. Display-Name editierbar.

**C6 — `:feature:direct` (Modul-Anlage ohne Inhalt).**
- Modul anlegen, `build.gradle.kts`, leeres Package mit `package-info`-Kommentar "Direct call feature — implemented from v0.8.0".
- Nicht in Navigation einbinden.

**Akzeptanz C6:** Modul kompiliert.

### Phase D — Koin-DI-Graph

**D1 — Koin-Module pro Library-Modul.**
- Jedes Library-Modul, das DI-Bindings braucht, exportiert ein `val xxxModule = module { ... }`.
- Beispiele:
  - `core:identity` → `identityModule { single<IdentityRepository> { DataStoreIdentityRepository(get(), get()) } }`
  - `core:logging` → `loggingModule { single<Logger> { CompositeLogger(listOf(AndroidLogcatLogger(), RingBufferLogger())) } }`
  - `service:discovery` → `discoveryModule { single<PeerDiscovery> { PeerDiscoveryStub() } }`
  - `feature:pairing` → `pairingModule { viewModel { PairingViewModel(get()) } }` (mit `koin-compose-viewmodel`).
- Application-Modul `:app/AppModule.kt` aggregiert alle Module in einer Liste.

**D2 — Koin-Init in `HeraTalkApplication`.**
- `class HeraTalkApplication : Application()`.
- `onCreate { startKoin { androidContext(this@HeraTalkApplication); modules(allModules) } }`.

**Akzeptanz D:** App startet ohne Koin-Initialization-Error. `koin.checkModules()` als Test in `:app/src/test`.

### Phase E — `:app` Einstiegspunkt

**E1 — AndroidManifest.**
- Alle Permissions aus `architecture.md §11.1` deklariert. Kommentare zu Runtime-vs.-Always-on.
- Service-Deklaration für `HeraTalkService` mit `foregroundServiceType="connectedDevice|microphone"`.
- `android:supportsRtl="true"`.
- `android:icon`, `android:label` (lokalisiert via `@string/app_name` aus `app/src/main/res/values/strings.xml` und `values-de/strings.xml`).

**E2 — MainActivity.**
- Single-Activity, `ComponentActivity` mit `setContent { HeraTalkTheme { ... } }`.
- NavHost mit 3 Routes:
  - `pairing` (Start) → `ChannelChoiceScreen` → `DisplayNameScreen` → `QrScanScreen`-Stub.
  - `channel` → `ChannelScreen`.
  - `settings` → `SettingsScreen`.
- Navigation-Gating: Wenn DisplayName + Channel-Secret persistiert → starte direkt in `channel`. Sonst in `pairing`. (In v0.1.0 ohne Channel-Secret ist `pairing` fast immer der Start.)
- BottomBar oder TopBar für Navigation zwischen `channel` und `settings` nach Pairing.

**E3 — Permissions-Bootstrap.**
- Beim Tap auf "Kanal beitreten" wird `CAMERA`-Permission angefragt. Bei Ablehnung freundlicher Hinweis.
- Beim ersten Service-Start wird `POST_NOTIFICATIONS` angefragt (Android 13+).
- `RECORD_AUDIO`: nicht in v0.1.0 — kein Sende-Pfad. TODO-Kommentar "wird in v0.4.0 abgefragt".
- Permission-Helpers in `:core:ui` als Composable-`rememberLauncherForActivityResult`-Wrapper.

**E4 — Service-Start/-Stop-Knopf in Settings.**
- Temporärer Debug-Eintrag in Settings ("Foreground-Service starten/stoppen") — nur in `debug`-BuildType. Erlaubt manuellen Test der Akzeptanz "Foreground Service kann gestartet und gestoppt werden".

**Akzeptanz E:** App startet, alle 3 Screens via Navigation erreichbar, Permission-Dialoge erscheinen am richtigen Punkt, Service start/stop funktioniert.

### Phase F — CI und Dokumentation

**F1 — CI-Workflows prüfen und ggf. anpassen.**
- `build.yml`: `./gradlew assembleDebug lintDebug detekt spotlessCheck` — auf Release-Branch laufen lassen.
- `lint.yml`: prüft `MissingTranslation` und `HardcodedText` als Error.
- `codeql.yml`: Kotlin-Coverage prüfen.
- `no-internet-check.yml`: bestehender Check bleibt.
- Falls ein neuer Workflow nötig wäre, hier ergänzen — aktuell vermutlich nichts.

**F2 — Dokumenten-Sync (an Documenter delegiert nach Code-Abschluss).**
- README.md: Quick-Start für DevContainer + Build-Befehle aktualisieren.
- CHANGELOG.md: neuer Eintrag `## [v0.1.0] - 2026-XX-XX` mit den im Plan benannten Bullets.
- `docs/project-state.md` und `docs/releases.md`: Status durch Orchestrator beim Release-Abschluss.

**F3 — Manueller Test auf Gerät.**
- Pascal installiert das APK auf Android-10+-Gerät und prüft die Akzeptanzkriterien aus `releases.md §v0.1.0` Punkt für Punkt durch.

## 3. Konkrete Akzeptanz-Checkpoints für den Entwickler

Die folgenden Checks führt der Entwickler-Agent **lokal** vor PR-Erstellung aus. Erst wenn alle grün sind, kommt der Architekt-Review.

```bash
./gradlew assembleDebug
./gradlew lintDebug
./gradlew detekt
./gradlew spotlessCheck
./gradlew test  # Unit-Tests aller Module
```

Auf einem echten Android-10+-Gerät:

- [ ] APK installiert sich, Icon und App-Name erscheinen lokalisiert (DE/EN je nach System-Locale).
- [ ] App startet auf Pairing-Screen ohne Crash.
- [ ] Display-Name lässt sich eingeben (Validierung greift bei leer und > 32 Codepoints).
- [ ] Display-Name persistiert über App-Neustart.
- [ ] Settings-Screen zeigt Display-Name und erlaubt Editieren.
- [ ] Sprach-Umschaltung System/DE/EN wirkt sofort, persistiert über Neustart.
- [ ] Bei `de_DE`-Locale startet die App auf Deutsch; bei nicht-unterstützter Locale (z. B. `fr_FR`) auf Englisch.
- [ ] Foreground-Service start/stop via Debug-Menü funktioniert; Notification erscheint mit lokalisiertem Text und Channel-Name.
- [ ] Channel-Screen öffnet, zeigt leere Peer-Liste und deaktivierten PTT-Button mit "Verfügbar in v0.4.0".
- [ ] Navigation zwischen den 3 Screens funktioniert.
- [ ] Permission-Dialog für Camera erscheint beim ersten "QR scannen"-Tap; Ablehnung zeigt Hinweis ohne Crash.
- [ ] Permission-Dialog für POST_NOTIFICATIONS erscheint beim ersten Service-Start auf Android 13+.

## 4. Out-of-Scope-Liste (gegen Scope-Creep)

Der Entwickler-Agent baut **nicht** in v0.1.0:

- Discovery-Logik (mDNS, Beacon, manuelle Eingabe) — v0.2.0/v0.9.0.
- Transport-Sockets, Packetizer — v0.2.0.
- Audio-Aufnahme, Opus-JNI — v0.3.0.
- SRTP, Noise-Handshake — v0.5.0/v0.6.0.
- QR-Scan-Logik, Channel-Secret-Persistenz — v0.5.0.
- Custom-Detekt-Rule `HardcodedStringInComposable` — kann in v0.1.0 oder als Folge-PR. Wenn Aufwand > 1 Tag, vertagen.
- Update-Check-Netzwerkaufruf — späterer Release.
- Theme-Switch-Wirkung (nur Persistenz in v0.1.0) — v1.0.
- Hardware-PTT-Logik — v0.7.0/v1.0.
- Avatare, Kanal-Farben — v1.0.

## 5. Risiken und Mitigations für diesen Release

| Risiko | Wahrscheinlichkeit | Mitigation |
|--------|--------------------|------------|
| `MissingTranslation`-Lint blockiert CI auf jedem PR | hoch (per Design) | Documenter-Agent ergänzt Übersetzungen synchron; Entwickler-PRs mit Label "needs translation" markieren |
| Modul-Explosion macht Build langsam | mittel | Convention-Plugin (`build-logic`) aufsetzen; Gradle-Configuration-Cache aktivieren |
| AGP 9.2 + Kotlin 2.3.21 + Compose-BOM 2026.04 — kombinatorische Inkompatibilitäten | niedrig (alles spezifiziert) | Bei Build-Bruch: Architekt eskalieren, ggf. `libs.versions.toml` anpassen |
| `AppCompatDelegate.setApplicationLocales` benötigt `appcompat`-Dependency, die wir noch nicht haben | mittel | Dependency in `app`-Modul ergänzen; alternativ AndroidX `core-ktx` reicht für 13+, mit Recreate-Fallback für ältere |
| Foreground-Service-Typ `connectedDevice` braucht Android 14+ und sichtbare Activity beim Start | mittel | Service nur aus sichtbarer Activity starten; Fallback-Pfad mit `try/catch` für ältere Geräte; Pascal testet auf realem Gerät |
| Display-Name-Validierung lässt Bidi-Override-Codepoints durch | niedrig in v0.1.0 (eigener Name, kein Empfang) | Sanitisierung der **Eingabe** in v0.1.0 ist optional; Sanitisierung **fremder** Namen kommt erst mit v0.2.0 (Discovery). Empfehlung: schon in `:core:identity` minimal sanitisieren (nur Bidi strippen) — wenig Aufwand, schliesst F-PRIV-04 für eigenen Namen vorab |

## 6. Übergabe und Folgeschritte

Nach Abschluss dieses Plans:

1. **Architekt** macht Pre-Release-Review (Code, Modul-Grenzen, DI-Graph, Permission-Logik).
2. **Documenter** synchronisiert README, CHANGELOG, prüft alle Strings auf Doppel-Pflege.
3. **Orchestrator** prüft Akzeptanzkriterien aus `releases.md §v0.1.0`, taggt `v0.1.0`, schiebt Status auf ✅.
4. **Lessons Learned** wandern nach `project-state.md`.

Der erste konkrete Entwickler-Schritt ist **Phase A1**: `settings.gradle.kts` aktualisieren und Convention-Plugin entscheiden.
