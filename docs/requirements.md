# HeraTalk — Anforderungen

Kompakte Liste aller Anforderungen mit Akzeptanzkriterien und Umsetzungshinweisen. Jede Anforderung hat eine eindeutige ID (`F-nn` funktional, `NF-nn` nicht-funktional, `T-nn` technisch, `Q-nn` Qualität) und ist in der CI als Tracking-Label verwendbar.

## Funktionale Anforderungen

### F-01 — Peer-Discovery im LAN

**Anforderung:** Die App findet andere HeraTalk-Peers im selben WLAN-Subnetz automatisch.
**Akzeptanz:**
- Neuer Peer erscheint in der Liste binnen ≤ 5 s nach App-Start eines zweiten Geräts.
- Entfernter Peer verschwindet binnen ≤ 15 s nach Schließen.
- Discovery funktioniert ohne jegliche Cloud-/Server-Komponente.
**Umsetzung:** mDNS/DNS-SD (`NsdManager`, Service-Typ `_heratalk._tcp.local.`). UDP-Broadcast-Beacon als Fallback. Manuelle IP-Eingabe als letzter Fallback.

### F-02 — Kanal-Auswahl beim Start

**Anforderung:** Nutzer wählt beim ersten Start einen Kanal (per QR-Code-Scan oder selbst erstellen).
**Akzeptanz:**
- QR-Code enthält `channel_secret` (32 Byte zufällig) und Display-Name.
- Nach Pairing wird `channel_secret` verschlüsselt im Android Keystore gespeichert.
- Kanal-Wechsel ist in den Settings möglich.
- Vor dem QR-Schritt wird der Display-Name eingegeben (siehe F-16).
**Umsetzung:** ML Kit Barcode (Scan), ZXing (Generierung), URI-Schema `heratalk://join?v=1&name=…&secret=…`.

### F-03 — Broadcast im Kanal

**Anforderung:** Nutzer können im Kanal sprechen und werden von allen anderen Kanal-Teilnehmern gehört.
**Akzeptanz:**
- Audio kommt bei allen anderen Peers mit ≤ 250 ms Mund-zu-Ohr-Latenz unter guten Bedingungen an.
- Bei 10 % Paketverlust bleibt Audio verständlich.
- Beim Beenden des Sendens hört das Audio bei allen Empfängern innerhalb ≤ 500 ms auf.
**Umsetzung:** Opus 20 ms Frames, SRTP-verschlüsselt, UDP Unicast (oder Broadcast wenn AP-tauglich).

### F-04 — Push-to-Talk-Modus

**Anforderung:** Klassischer PTT: Nutzer hält Taste gedrückt, um zu sprechen.
**Akzeptanz:**
- PTT-Tasten-Druck startet Aufnahme und Sendung in ≤ 50 ms.
- Während ein Peer sendet, sind andere PTT-Sendungen im selben Kanal blockiert (Floor-Control). Blockierter Sender bekommt optisches + akustisches Feedback.
- PTT-Release stoppt Sendung sofort.
**Umsetzung:** Verteiltes Floor-Control mit Tie-Break via `(timestamp, peer_id)`.

### F-05 — VOX-Modus (sprachaktiviert)

**Anforderung:** Alternativer Modus: Sendung wird automatisch durch Sprache ausgelöst.
**Akzeptanz:**
- Sprache aktiviert Sendung in ≤ 200 ms.
- Schwellwert in Settings konfigurierbar mit Live-RMS-Meter.
- Stille beendet Sendung nach konfigurierbarem Hangover (Default 800 ms).
**Umsetzung:** RMS-basierte VAD mit Hysterese + Hangover-Timer.

### F-06 — Umschalten zwischen PTT und VOX

**Anforderung:** Nutzer kann jederzeit zwischen PTT und VOX umschalten.
**Akzeptanz:**
- Umschalten ist in Settings zugänglich und wirkt sofort.
- Modus wird pro Installation persistiert.
**Umsetzung:** `DataStore`-gespeicherter Enum, state-machine hört auf Änderungen.

### F-07 — 1:1-Direktgespräche

**Anforderung:** Zwei Peers im Kanal können ein privates Gespräch führen, das andere Peers nicht hören.
**Akzeptanz:**
- Initiator wählt Peer → Angerufener bekommt Ringsignal (visuell + Ton).
- Annahme etabliert exklusiven Stream.
- Andere Peers sehen "in direct call" im Roster, hören aber nichts.
- Während Direktruf ist Broadcast-Playback beim Initiator/Angerufenen lokal gemutet.
- Beenden geht von beiden Seiten.
**Umsetzung:** Separate SSRC-Range, eigene SRTP-Keys (HKDF-Info unterscheidet), Signalisierung via `DirectCall*`-Messages.

### F-08 — Verschlüsselung aller Audio-Streams

**Anforderung:** Jede Audio-Übertragung ist verschlüsselt, auch zwischen Peers im selben Kanal.
**Akzeptanz:**
- Ohne gültiges `channel_secret` kann kein Peer einem Kanal beitreten.
- Ein MITM im WLAN kann Audio nicht entschlüsseln.
- Relay-Peer (wenn aktiv) kann Audio nicht entschlüsseln.
**Umsetzung:** Noise `KKpsk0_25519_ChaChaPoly_SHA256` für Handshake, SRTP mit AEAD für Medien. Siehe `docs/security.md`.

### F-09 — Anzeige des Netzwerkzustands

**Anforderung:** Nutzer sieht auf einen Blick, ob die Netzwerkverbindung gut ist.
**Akzeptanz:**
- Indikator (grün/gelb/rot) im Hauptscreen.
- Details in Settings: RTT, Loss, Transport-Pfad pro Peer.
- "Netzwerk neu prüfen"-Button löst Connectivity Prober manuell aus.
**Umsetzung:** Connectivity Prober, siehe `docs/architecture.md §7.6`.

### F-10 — Peer-Identitäts-Verifikation

**Anforderung:** Nutzer können die Identität eines Peers verifizieren, um Substitutions-Angriffe zu erkennen.
**Akzeptanz:**
- Peer-Detail-Screen zeigt Static-Key-Fingerprint (8 hex-Zeichen, z. B. `a7f3:2c91`).
- Bei Wechsel des Static-Keys eines bekannten Peers erscheint ein nicht-wegblendbares Banner mit Warnung und Vergleichs-Hinweis ("Vergleiche den Fingerprint außerhalb der App").
- Nutzer kann einen Peer in den Details als "vertraut" markieren.
**Umsetzung:** Trust-on-First-Use mit persistierten Public Keys, siehe `docs/security.md §4.3`.

### F-11 — Feature-basiertes Permission-Modell

**Anforderung:** Kritische Permissions werden erst angefordert, wenn der Nutzer ein Feature aktiviert, das sie benötigt. Ablehnung einer Permission deaktiviert nur das betroffene Feature, nicht die gesamte App.
**Akzeptanz:**
- Kanal-Betreten funktioniert auch ohne `RECORD_AUDIO` (nur Zuhören im Broadcast-Kanal möglich).
- Der Druck auf den PTT-Button löst beim ersten Mal den `RECORD_AUDIO`-Permission-Dialog aus; bei Ablehnung bleibt der PTT-Button deaktiviert mit Hinweistext und Link zu den App-Einstellungen.
- VOX-Aktivierung in Settings fragt beim ersten Einschalten `RECORD_AUDIO` (falls noch nicht vorhanden) und triggert den Wechsel des Foreground-Service-Typs auf `microphone`.
- Hardware-PTT-Aktivierung verhält sich analog zu VOX (siehe F-13).
- Wake-on-Direktruf (Bildschirm einschalten bei eingehendem Ruf) ist opt-in über Settings-Toggle und fragt `USE_FULL_SCREEN_INTENT` erst beim Einschalten.
- Jeder Permission-gebundene Toggle in Settings zeigt einen Erklärtext, was er aktiviert und welche Auswirkung er hat (Batterie, Privatsphäre).
**Umsetzung:** Siehe `docs/architecture.md §11.2` (Feature-zu-Permission-Matrix) und `docs/ui.md §8` (Settings-Unterabschnitt "Features und Berechtigungen"). Der Foreground-Service kann seinen Typ zur Laufzeit atomar wechseln durch erneuten Aufruf von `startForeground(id, notification, newType)`, gekapselt in `:service:lifecycle` (Details und Code-Snippet in `docs/architecture.md §11.3`).

### F-12 — Verhalten bei eingehendem Direktruf während eigener Sendung

**Anforderung:** Ein eingehender Direktruf darf den Nutzer nicht beim Sprechen stören, aber er muss ihn wahrnehmen können, ohne den Kanal zu verlassen.
**Akzeptanz:**
- Während der Nutzer im Broadcast sendet (PTT gedrückt) oder in einem anderen Direktruf ist, wird ein eingehender Direktruf **nicht** mit Full-Screen-UI angezeigt.
- Stattdessen erscheint ein kompakter Hinweis am oberen Bildschirmrand (Heads-up-Style) mit Caller-Name und zwei Aktionen: "Später annehmen" (Ruf bleibt als Benachrichtigung stehen) und "Ablehnen".
- Das Benachrichtigungs-Verhalten ist in Settings konfigurierbar: (a) **Stumm**, (b) **Vibration**, (c) **Klingelton**. Default: Vibration.
- Wenn der Nutzer "Später annehmen" wählt und die aktuelle Sendung beendet, öffnet sich der gewohnte Direktruf-Annehmen-Screen — vorausgesetzt der Anrufer hält noch.
- Wenn der Anrufer auflegt, bevor der Nutzer reagiert, wird der Hinweis mit "Verpasster Ruf von *Name*" ersetzt.
**Umsetzung:** State-Machine-Erweiterung in `:service:ptt` um die Zustände `BusyTx` und `BusyInDirectCall`. Eingehende `DirectCallRequest`-Messages werden in diesen Zuständen anders gerendert. UI-Layer via `:feature:direct` und `:feature:channel` mit gemeinsam genutztem Heads-up-Composable in `:core:ui`.

### F-13 — Hardware-PTT-Auslöser

**Anforderung:** Nutzer können konfigurieren, welche Hardware-Taste PTT auslöst, um HeraTalk mit Handschuhen, aus der Tasche oder mit einem Headset bedienen zu können.
**Akzeptanz:**
- In Settings > Features > Hardware-PTT wählt der Nutzer zwischen mindestens drei Optionen, jeweils einzeln aktivierbar:
  - **Bluetooth-Media-Button** (z. B. Headset-Play/Pause-Taste)
  - **Lautstärke-Tasten** (wahlweise Volume-Up, Volume-Down oder beide)
  - **USB/BT-Fernbedienung mit frei konfigurierbarem KeyCode** (post-v1.0, nicht im MVP)
- Alle Hardware-PTT-Optionen sind standardmäßig **deaktiviert**.
- Bei Aktivierung der Lautstärke-Tasten-Option sieht der Nutzer einen klaren Hinweis: "Während HeraTalk läuft, steuern die Lautstärke-Tasten nicht mehr die System-Lautstärke." Bestätigung erforderlich.
- Lautstärke-Tasten werden nur abgefangen, solange die App im Vordergrund ist **oder** VOX-/Hardware-PTT-Modus aktiv ist und der Foreground-Service auf `microphone` läuft. In allen anderen Zuständen verhalten sich die Tasten normal.
- Ein Druck auf den Hardware-Auslöser löst PTT-Floor-Request aus, Loslassen endet die Sendung — identisch zum Soft-Button.
**Umsetzung:** Bluetooth-Media-Button via `MediaSession` und `MediaButtonReceiver`. Lautstärke-Tasten via `Activity.onKeyDown`/`onKeyUp` mit `event.repeatCount == 0`-Filter und `return true` zum Konsumieren. Foreground-Service-Variante via `KeyEvent.Callback` in einem dedizierten Receiver im `:service:ptt`-Modul.

### F-14 — Theme-Einstellung (v1.0)

**Anforderung:** Nutzer können das App-Theme manuell wählen oder System-Default folgen lassen.
**Akzeptanz:**
- In Settings unter "App-Verhalten" Auswahl zwischen **Dunkel**, **Hell**, **System folgen**. Default: **Dunkel** (nicht System folgen — typischer Einsatz draußen und abends, OLED-Akku-Vorteil).
- Theme-Wechsel ist sofort wirksam, kein App-Neustart erforderlich.
- Alle Screens respektieren das Theme. Insbesondere die PTT-Button-States und die Farbkodierung (grün/blau/gelb/rot) bleiben in beiden Themes klar erkennbar und erfüllen WCAG-AA-Kontrastanforderungen.
**Umsetzung:** Compose mit `MaterialTheme(colorScheme = ...)`-Switch. Theme-Wahl persistiert in DataStore. Farbpalette separat für Dark und Light definiert in `:core:ui`. Nicht im MVP, Ziel v1.0.

### F-16 — Display-Name-Eingabe beim Onboarding

**Anforderung:** Beim ersten App-Start gibt der Nutzer seinen Display-Namen ein, bevor er einem Kanal beitritt oder einen erstellt. Der Name erscheint bei anderen Peers in der Peer-Liste und im Direktruf-Screen. Der Name ist global (nicht pro Kanal) — Multi-Channel ist im MVP nicht vorgesehen.
**Akzeptanz:**
- Name wird im Pairing-Flow abgefragt, vor QR-Scan oder QR-Anzeige.
- Eingabefeld startet **leer** (keine Vorbelegung, kein `Build.MODEL`, kein Geräte-Hostname). Im Feld erscheint Placeholder-Text (z. B. "z. B. Anna oder Werkstatt-Tablet"), der bei Eingabe verschwindet.
- Pflichtfeld: Name darf nicht leer sein; der "Weiter"-Button ist deaktiviert, bis ≥ 1 sichtbares Zeichen eingegeben wurde (rein aus Whitespace bestehende Eingaben gelten als leer).
- Maximallänge: **32 Unicode-Codepoints** (App-seitige Begrenzung; das UDP-Beacon-Protokoll erlaubt bis zu 255 Byte via 1-Byte-Längenfeld in `docs/architecture.md §6.1`).
- Name wird persistent in DataStore gespeichert und ist über Settings → Kanal → "Dein Name" jederzeit änderbar.
- Änderung in Settings wirkt sofort auf Discovery: mDNS wird neu registriert (Debounce 300 ms), der Broadcast-Beacon übernimmt den neuen Wert beim nächsten Tick (alle 3 s).
- **Re-Pairing:** Beim Kanal-Wechsel über Settings wird der Namens-Eingabe-Screen angezeigt mit dem zuletzt gespeicherten Wert vorbelegt (editierbar). Der Screen wird **nicht** übersprungen.
- **Fallback bei Korruption:** Sollte `dname` durch Datenkorruption oder Migration leer sein, wird `Peer-{first8hex(pk)}` (z. B. `Peer-a7f32c91`) gesendet — **niemals** `Build.MODEL` oder Geräte-Hostname.
- **Empfangs-Sanitisierung:** Eingehende `dname`-Werte von anderen Peers werden in `:service:discovery` sanitisiert (NFC-Normalisierung, Strip Bidi-Override-Codepoints, Combining-Marks-Begrenzung, Truncation auf 32 Codepoints), bevor sie an die UI weitergegeben werden. Details siehe `docs/architecture.md §6.1`.
- Im `dname`-Feld (mDNS TXT) und im UDP-Broadcast-Beacon wird immer der aktuelle Name oder Fallback gesendet.
**Umsetzung:** Persistenz und Validierungslogik liegen in `:core:identity` (`IdentityRepository`, DataStore-Key `display_name`), nicht direkt in `:feature:pairing`. Eingabe-UI in `:feature:pairing`. Anzeige und Änderung in `:feature:settings`. `:service:discovery` subscribt auf `IdentityRepository.displayName` als `Flow` und reagiert reaktiv. Alle drei Module greifen ausschließlich über `:core:identity` zu.

### F-15 — Internationalisierung (i18n)

**Anforderung:** Die App ist von Beginn an mehrsprachig aufgesetzt. Im MVP werden Deutsch und Englisch unterstützt; weitere Sprachen können ohne Code-Änderung später ergänzt werden.
**Akzeptanz:**
- Kein hartkodierter Nutzer-sichtbarer Text in Compose-Composables, Notification-Texten, Permission-Begründungen oder Fehlerdialogen. Alle Strings stammen aus `strings.xml`.
- Default-Locale ist Englisch (`res/values/strings.xml`). Deutsch ist Override (`res/values-de/strings.xml`).
- Beim ersten App-Start nutzt HeraTalk die System-Locale, sofern eine unterstützte Sprache vorhanden ist; sonst Englisch.
- In Settings unter "App-Verhalten" gibt es eine Sprach-Auswahl: **System folgen** (Default), **Deutsch**, **Englisch**. Wechsel wirkt sofort, kein App-Neustart.
- Plurale werden korrekt behandelt (`<plurals>`-Resource): "1 Peer" / "3 Peers" / "0 Peers", inklusive sprachspezifischer Regeln.
- Datums-, Zeit- und Zahlenformate folgen der gewählten Locale (Java `Locale`, `DateTimeFormatter`).
- String-Ressourcen folgen Android-Konventionen: keine `<string>`-Konkatenation im Code, sondern Format-Strings mit benannten Argumenten (`%1$s spricht`).
- Strings sind nach Feature-Modul aufgeteilt (`core/ui/src/main/res/values/strings.xml`, `feature/channel/src/main/res/values/strings.xml`, etc.) — keine zentrale Mega-Datei.
- Code-Kommentare und KDoc bleiben Englisch (siehe `.claude/CLAUDE.md`).
- Log-Messages (an `logcat` oder Ring-Buffer) bleiben Englisch — Diagnose-Werkzeug für Entwickler, nicht Nutzer-facing.
**Umsetzung:** Standard-Android-Resources (`strings.xml` pro Locale-Qualifier). Compose-Strings via `stringResource(R.string.xxx)`. Übersetzungen werden vom Documenter-Agent gepflegt (siehe `.claude/agents/documenter.md`). Lint-Regel `MissingTranslation` muss in CI grün sein. Neue String-Keys in einem PR ohne Übersetzung blockieren den Merge.

## Nicht-funktionale Anforderungen

### NF-01 — Robustheit gegen schlechte APs

**Anforderung:** Die App funktioniert unter den miesesten AP-Bedingungen trotzdem — Ziel ist "Skype-Niveau" an Verbindungsrobustheit.
**Akzeptanz:**
- Funktioniert bei 30 % Paketverlust (Audio noch verständlich, Adaptive Codec greift).
- Funktioniert bei Multicast-Filterung durch AP (Broadcast-Beacon-Fallback aktiv).
- Funktioniert bei AP-Client-Isolation (Relay-Mode durch dritten Peer).
- Recovery nach AP-Reboot ≤ 10 s ohne Nutzer-Interaktion.
- IP-Wechsel (AP-Roaming) wird in ≤ 5 s ohne Abbruch gehandhabt.
**Umsetzung:** Mehrschicht-Transport-Kaskade, adaptive Bitrate, FEC, Frame-Duplizierung, `NetworkCallback`-Monitor.

### NF-02 — Niedrige Latenz

**Anforderung:** Hörbare Latenz so gering wie technisch sinnvoll auf Android.
**Akzeptanz:**
- Mund-zu-Ohr-Latenz ≤ 250 ms unter guten Bedingungen (mittleres Smartphone, gesundes WLAN).
- Latenz ≤ 400 ms unter schlechten Bedingungen.
**Umsetzung:** `AudioSource.VOICE_COMMUNICATION`, 20 ms-Frames, 40 ms-Jitter-Target, kein Trans-Codec.

### NF-03 — Offline-Fähigkeit

**Anforderung:** Die App arbeitet ohne Internet, Cloud-Services, Accounts oder Telemetrie.
**Akzeptanz:**
- Nach Installation und erstem Pairing funktioniert die App im Flugmodus-mit-WLAN.
- Keine Verbindungen ins Internet (verifiziert per Traffic-Mitschnitt im CI), mit genau einer dokumentierten Ausnahme (siehe unten).
- Der Audio-, Discovery- und Pairing-Pfad kontaktiert niemals eine externe Adresse.
**Umsetzung:** Keine Analytics, keine Crash-Reporter, keine Cloud-SDKs. Lokale Logs nur.

**Dokumentierte Ausnahme — opt-in Update-Check:** Die App darf optional beim Start einen einzigen HTTPS-Request an `https://github.com/relexx/heratalk/releases/latest` senden, um auf Sicherheitsupdates hinzuweisen. Dieses Verhalten ist standardmäßig **deaktiviert**, wird beim ersten App-Start explizit abgefragt ("Soll HeraTalk beim Start kurz nach Sicherheitsupdates suchen?"), ist jederzeit in Settings umschaltbar und wird bei Deaktivierung garantiert unterdrückt. Keine weiteren Netzwerk-Requests zu externen Adressen erlaubt.

### NF-04 — Batterie-Schonung

**Anforderung:** Dauerhafter Idle-Betrieb im Kanal ist akkuverträglich.
**Akzeptanz:**
- Idle-Stromverbrauch (verbunden, nicht sprechend): ≤ 3 %/Stunde auf einem mittleren Gerät.
**Umsetzung:** DTX im Opus-Encoder, TCP-Ping alle 5 s statt permanenter Heartbeats, kein Full-WakeLock.

### NF-05 — Datenschutz

**Anforderung:** Die App sammelt keine personenbezogenen Daten und überträgt nichts an Dritte.
**Akzeptanz:**
- Keine Telemetrie, keine Crash-Reporter, keine Werbe-IDs.
- Display-Name und Kanal-Secret verbleiben lokal.
- Logs enthalten keine Secrets, keine Namen, keine IPs über DEBUG-Level hinaus.
**Umsetzung:** Strikte Log-Filter, Code-Review-Rule in `.claude/rules.md`.

## Technische Anforderungen

### T-01 — Plattform und Sprache

- Min SDK Android 10 (API 29), Target SDK Android 16 (API 36) — ab August 2026 für Google-Play-Submission Pflicht.
- Kotlin 2.3.x mit explicit API mode (`-Xexplicit-api=strict`).
- Jetpack Compose + Material 3.

### T-02 — Architektur

- MVI-Pattern für UI-State.
- Koin für Dependency Injection.
- Coroutines + Flows für Async.
- Modul-Struktur wie in `docs/architecture.md §4`, Abhängigkeiten nur nach innen.

### T-03 — Persistenz

- `DataStore` (Preferences + Proto) für nicht-sensible Daten.
- Android Keystore für Kanal-Secrets und Peer-Static-Keys.
- Keine SQLite-Datenbank (Overkill für diesen Scope).

### T-04 — Serialisierung

- Protobuf (`protobuf-kotlin-lite`) für Control-Plane-Messages.
- CBOR oder rohe Bytes für Performance-kritische Pfade (Data Plane).

### T-05 — Cryptographie

- Noise-Java (rweather/noise-java) für Handshake.
- Eigene SRTP-Implementierung in `:core:crypto` (bewusster Scope: nur AEAD, kein SRTCP, keine MKI).
- JCE/BouncyCastle für Primitives (ChaCha20-Poly1305, AES-GCM, HKDF, X25519).

### T-06 — Audio

- `AudioRecord` + `AudioTrack` direkt (kein `MediaRecorder`).
- libopus via JNI. Präferenz: vorgefertigter AAR mit Hash-Pin. Fallback: eigener CMake-Build aus Xiph-Source. Finale Entscheidung in ADR 0003 (siehe `docs/architecture.md §13`).
- 48 kHz mono Input, ggf. Downsampling für narrowband Opus-Modes.
- CBR-only Encoder (siehe `.claude/rules.md` Regel 15; Begründung in `docs/security-audit.md` F-SIDE-04).

### T-07 — Build und CI

- Gradle 9.x mit Kotlin DSL.
- Android Gradle Plugin 9.x (mit built-in Kotlin-Support, ohne separates `kotlin-android`-Plugin).
- 16-KB-Page-Size-Alignment im NDK-Build (Pflicht für Google Play seit Nov 2025 für API 35+).
- CI auf GitHub Actions mit gleichen Versionen wie DevContainer.
- Debug-APK als Artifact, Release-APK auf Tag-Push.

## Qualitäts-Anforderungen

### Q-01 — Test-Coverage

**Anforderung:** Alle Logik mit hohem Komplexitätsgrad ist durch Tests abgedeckt.
**Akzeptanz:**
- Unit-Test-Coverage ≥ 70 % für `:core:*` und `:service:*`.
- Property-Based-Tests für Protokoll-Parser (Kotest).
- Jeder Bug-Fix enthält einen Regression-Test.
**Umsetzung:** JUnit 5, MockK, Turbine, Kotest.

### Q-02 — Statische Analyse

**Anforderung:** Code ist stilistisch einheitlich und frei von bekannten Problemen.
**Akzeptanz:**
- `./gradlew detekt lintDebug spotlessCheck` läuft in CI grün.
- Alle `.kt`-Dateien tragen den BSD-3-Clause-Copyright-Header.
**Umsetzung:** detekt mit projekt-spezifischem `detekt.yml`, Android Lint, Spotless mit Copyright-Template.

### Q-03 — Dokumentation

**Anforderung:** Das Projekt ist zu jedem Zeitpunkt verständlich dokumentiert.
**Akzeptanz:**
- `README.md` beschreibt Projekt und Build.
- `docs/architecture.md`, `docs/requirements.md`, `docs/releases.md`, `docs/project-state.md` sind aktuell.
- Jede bedeutende Entscheidung hat eine ADR in `docs/adrs/`.
- KDoc auf allen öffentlichen API-Oberflächen.
**Umsetzung:** Agent "Dokumentierer" pflegt Dokumente bei jeder Code-Änderung.

### Q-04 — Security-Review

**Anforderung:** Sicherheitsrelevante Änderungen werden zusätzlich geprüft.
**Akzeptanz:**
- Änderungen an `:core:crypto` und `:service:media` benötigen CODEOWNER-Review.
- CodeQL läuft wöchentlich und pro Push auf `main`.
- SECURITY.md beschreibt Vulnerability-Reporting-Prozess.
**Umsetzung:** GitHub-Branch-Protection + CODEOWNERS, CodeQL-Workflow.

### Q-05 — Reproducible Builds

**Anforderung:** Jeder Build aus dem gleichen Commit erzeugt dasselbe APK.
**Akzeptanz:**
- DevContainer- und CI-Builds sind bit-identisch bei gleichem Commit.
**Umsetzung:** Gradle-Wrapper gepinnt, SDK-/NDK-Versionen im Dockerfile gepinnt.

## Abgrenzungen (ausdrücklich nicht im Scope)

- Keine Cross-Platform-Version (Android only). KMP-Refactor ist Option, kein MVP-Ziel.
- Keine Cloud-Komponente, keine Account-Systeme.
- Keine Interoperabilität mit SIP, WebRTC, Zello, o. ä.
- Keine Video-Übertragung.
- Keine Sprach-Aufzeichnung oder persistente Wiedergabe (höchstens Ring-Buffer für Replay als spätere Option).
- Keine Werbung, keine Analytics, keine Telemetrie.
