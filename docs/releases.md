# HeraTalk — Release-Plan

> **Lebendes Dokument**, gepflegt vom Orchestrator-Agent. Bei jeder Release-Fertigstellung wird der Status aktualisiert und der nächste Release vorbereitet.

## Strategie

- **Tag-basierte Releases** als `v0.x.0` bis `v1.0.0`, danach Semver.
- **Jeder Release ist testbar** und liefert einen vorführbaren Zustand.
- **Kein Release wird geschlossen**, solange CI, Tests, Dokumentation und die in diesem Dokument genannten Akzeptanzkriterien nicht alle grün sind.
- Der Orchestrator-Agent pflegt `docs/project-state.md` beim Release-Wechsel und trägt gelernte Erkenntnisse ein.

## Release-Übersicht

| Release | Titel | Status |
|---------|-------|--------|
| v0.1.0 | Grundgerüst | 📋 geplant |
| v0.2.0 | PoC Paketversand | 📋 geplant |
| v0.3.0 | Audio-Loopback | 📋 geplant |
| v0.4.0 | Broadcast Audio (unverschlüsselt) | 📋 geplant |
| v0.5.0 | Kontrollebene + Handshake | 📋 geplant |
| v0.6.0 | Verschlüsselung (SRTP) | 📋 geplant |
| v0.7.0 | PTT + VOX | 📋 geplant |
| v0.8.0 | Direktgespräche | 📋 geplant |
| v0.9.0 | Robustheit + Adaptivität | 📋 geplant |
| v0.10.0 | Relay-Mode | 📋 geplant |
| v1.0.0 | Polish & öffentliches Release | 📋 geplant |

Legende: 📋 geplant · 🏗 in Arbeit · ✅ abgeschlossen · ⏸ pausiert

---

## v0.1.0 — Grundgerüst

**Ziel:** Kompilierbares App-Projekt mit allen Berechtigungen, UI-Gerüst und vollständiger Projektinfrastruktur. Noch kein Netzwerk, noch kein Audio.

**Scope:**
- Gradle-Multi-Modul-Skeleton gemäß `docs/architecture.md §4`
- Alle Android-Permissions im Manifest (Runtime-Permission-Flow für RECORD_AUDIO, POST_NOTIFICATIONS, CAMERA)
- Foreground-Service-Skeleton vom Typ `microphone` (startet/stoppt, macht aber noch nichts)
- Compose-UI-Gerüst: 3 Screens (Pairing, Channel, Settings) mit Navigation, noch ohne Funktion
- Material-3-Theme mit Light/Dark
- Koin-DI-Graph initial
- Copyright-Header in allen Source-Dateien
- **i18n-Setup gemäß F-15 (siehe `docs/architecture.md §11.7`):**
  - `values/strings.xml` (Englisch, Default) und `values-de/strings.xml` (Deutsch) pro Modul
  - Lint-Regeln `MissingTranslation` und `HardcodedText` als Error in `lint.xml`
  - Custom-detekt-Regel `HardcodedStringInComposable`
  - Sprach-Auswahl in Settings (System / Deutsch / Englisch) via `AppCompatDelegate.setApplicationLocales()`
  - `android:supportsRtl="true"` deklariert (für spätere RTL-Sprachen)
- DevContainer funktional
- GitHub Actions `build.yml`, `lint.yml`, `codeql.yml` grün
- README, CHANGELOG, SECURITY, CONTRIBUTING auf Stand

**Akzeptanzkriterien:**
- [ ] `./gradlew assembleDebug` läuft im DevContainer erfolgreich
- [ ] `./gradlew lintDebug detekt spotlessCheck` läuft grün, einschließlich `MissingTranslation` und `HardcodedText` als Error
- [ ] CI auf `main` ist grün
- [ ] APK installierbar auf Android 10+ Gerät
- [ ] App startet ohne Crash, zeigt Pairing-Screen
- [ ] Alle 3 Screens via Navigation erreichbar
- [ ] Permissions werden beim ersten Relevanz-Event abgefragt
- [ ] Foreground Service kann gestartet und gestoppt werden
- [ ] Notification erscheint bei Service-Start
- [ ] Sprache lässt sich in Settings zwischen Deutsch und Englisch umschalten, wirkt sofort
- [ ] Bei `de`-System-Locale startet die App auf Deutsch
- [ ] Bei nicht-unterstützter System-Locale fällt die App auf Englisch zurück
- [ ] Dokumente `architecture.md`, `requirements.md`, `releases.md`, `project-state.md` im Repo

**Out of scope:** Netzwerk, Audio, Discovery, Handshake.

**Abhängigkeiten:** keine.

---

## v0.2.0 — PoC Paketversand

**Ziel:** Zwei HeraTalk-Instanzen entdecken sich und tauschen Test-Pakete über UDP aus. Keine Audio-Payload, nur Protokoll-Grundlage.

**Scope:**
- `:service:discovery` — mDNS/NSD Register + Discover, Service-Typ `_heratalk._tcp.local.`, TXT-Records
- `:service:transport` — UDP-Socket-Wrapper, Unicast senden/empfangen
- Channel-Screen zeigt Live-Peer-Liste (alle entdeckten Peers, noch ohne Kanal-Filter)
- "Ping"-Button pro Peer: sendet Test-Paket (magic + timestamp), Empfänger zeigt Toast
- Diagnose-Overlay zeigt RTT

**Akzeptanzkriterien:**
- [ ] Zwei Geräte im selben WLAN finden sich binnen ≤ 5 s
- [ ] Peer verschwindet aus Liste binnen ≤ 15 s nach App-Schließen
- [ ] Ping-Paket kommt an, RTT ≤ 50 ms im guten WLAN
- [ ] Unit-Tests für Packet-Format (Encode/Decode)
- [ ] Keine Crashes bei Discovery-Timeouts oder Peer-Verlust

**Out of scope:** Audio, Handshake, Verschlüsselung, Kanal-Trennung.

**Abhängigkeiten:** v0.1.0.

---

## v0.3.0 — Audio-Loopback

**Ziel:** Mikrofon wird aufgezeichnet, Opus-encoded, wieder decoded, abgespielt. Alles lokal. Latenz-Messung als Baseline.

**Scope:**
- `:service:audio` — AudioRecord 48 kHz mono, 20 ms Frames
- Opus-JNI-Integration (libopus AAR oder eigener CMake-Build)
- Lokaler Loopback: Mic → Encoder → Decoder → AudioTrack
- PTT-Button aktiviert Loopback temporär
- Settings: Latenz-Messung-Screen (Mic-Input ↔ Speaker-Output, misst via Impulse)

**Akzeptanzkriterien:**
- [ ] Mic-Aufnahme läuft ohne Dropouts
- [ ] Opus encode + decode funktioniert, Audio hörbar
- [ ] Loopback-Latenz ≤ 80 ms auf Mittelklasse-Gerät
- [ ] Hardware-AEC wird aktiviert wo verfügbar
- [ ] Keine Memory-Leaks bei wiederholtem Start/Stopp

**Out of scope:** Netzwerk-Übertragung, Mehrere Kanäle, Mixer.

**Abhängigkeiten:** v0.2.0.

---

## v0.4.0 — Broadcast Audio (unverschlüsselt)

**Ziel:** Zwei Peers übertragen Audio zwischen sich. Noch ohne Verschlüsselung, noch ohne Handshake.

**Scope:**
- `:service:media` — RTP-Packetizer, Jitter-Buffer (fix 60 ms), Mixer
- Audio-Pipeline: Mic → Opus → RTP → UDP Unicast → RTP → Jitter → Opus → AudioTrack
- PTT-Button startet/stoppt Sendung
- Alle bekannten Peers werden als Empfänger adressiert (N Unicast-Kopien)
- Receiver mixt mehrere gleichzeitige Sender

**Akzeptanzkriterien:**
- [ ] Zwei Peers hören sich gegenseitig
- [ ] Drei Peers: Peer A hört Peer B und C gleichzeitig (Mix)
- [ ] Mund-zu-Ohr-Latenz ≤ 300 ms
- [ ] Audio bleibt verständlich bei 5 % simuliertem Paketverlust
- [ ] Unit-Tests für Jitter-Buffer und Mixer

**Out of scope:** Verschlüsselung, Floor-Control, VOX, Direktgespräche.

**Abhängigkeiten:** v0.3.0.

---

## v0.5.0 — Kontrollebene + Handshake

**Ziel:** TCP-basierte Control-Plane mit Noise-Handshake. Peer-Roster wird stabil.

**Scope:**
- `:service:signaling` — TCP-Server + Client, Noise-KKpsk0-Handshake
- `:core:crypto` — Noise-Integration, HKDF, X25519, SRTP-Key-Ableitung (Keys werden noch nicht verwendet)
- Protobuf-Messages: `Hello`, `Ping`, `Pong`, `Bye` (ohne Floor/Stream noch)
- Kanal-Secret aus QR-Code → PSK für Noise
- Peer-Filter nach `channel_id_hash` im mDNS-TXT
- Reconnect-Logik mit exponentiellem Backoff

**Akzeptanzkriterien:**
- [ ] QR-Code-Pairing erzeugt Kanal und persistiert ihn im Keystore
- [ ] Zwei Peers mit gleichem Kanal-Secret können handshaken
- [ ] Zwei Peers mit unterschiedlichen Kanälen sehen sich nicht
- [ ] Falscher PSK → Handshake schlägt fehl, Peer verbleibt als "authentication failed"
- [ ] Ping/Pong alle 5 s, Reconnect nach Verbindungsabbruch ≤ 15 s
- [ ] Fuzz-Tests für Protobuf-Parser (Kotest Property)

**Out of scope:** Verschlüsselte Medienübertragung, Floor-Control.

**Abhängigkeiten:** v0.4.0.

---

## v0.6.0 — Verschlüsselung (SRTP)

**Ziel:** Alle Audio-Übertragungen sind SRTP-verschlüsselt.

**Scope:**
- `:core:crypto` — SRTP-AEAD-Implementierung (ChaCha20-Poly1305)
- `:service:media` — SRTP-Send/Recv-Pfade, Rekey-Handling
- `SrtpKeys`- und `SrtpRekey`-Messages
- Replay-Window 64 Pakete
- Rekey vor 2³¹ Paketen oder alle 24 h (was früher kommt)

**Akzeptanzkriterien:**
- [ ] Audio ist über Wireshark nur als verschlüsselter RTP-Stream sichtbar
- [ ] Ein manipuliertes Paket wird verworfen (Auth-Tag-Check)
- [ ] Replay-Attacke (wiederholtes Paket mit gleicher Seq) wird erkannt
- [ ] Rekey funktioniert ohne Audio-Aussetzer
- [ ] Unit-Tests für SRTP mit Test-Vektoren
- [ ] Security-Review durch Architekt-Agent dokumentiert

**Out of scope:** PTT-Floor-Control, VOX.

**Abhängigkeiten:** v0.5.0.

---

## v0.7.0 — PTT + VOX

**Ziel:** Zwei sendende Modi: klassisch PTT mit Floor-Control und sprachaktiviert VOX.

**Scope:**
- `:service:ptt` — Floor-Control-State-Machine, VAD
- PTT-Button auf Channel-Screen funktional
- VOX-Modus in Settings, Schwellwert-Slider mit Live-RMS-Meter
- Floor-Request/Grant/Release-Messages
- Tie-Break bei simultanen Floor-Requests

**Akzeptanzkriterien:**
- [ ] PTT: nur ein Peer sendet gleichzeitig im Kanal
- [ ] Gleichzeitiger PTT-Druck von zwei Peers: einer gewinnt deterministisch, anderer bekommt Feedback
- [ ] VOX aktiviert Sendung in ≤ 200 ms bei Sprache
- [ ] VOX beendet Sendung nach konfigurierbarem Hangover
- [ ] Modus-Umschaltung wirkt sofort
- [ ] Unit-Tests für Floor-State-Machine

**Out of scope:** 1:1-Direktgespräche.

**Abhängigkeiten:** v0.6.0.

---

## v0.8.0 — Direktgespräche

**Ziel:** Zwei Peers können ein privates 1:1-Gespräch parallel zum Broadcast-Kanal führen.

**Scope:**
- `:feature:direct` — UI für Ringsignal, aktive Direktrufe (gemäß `docs/ui.md §6`)
- Signalisierung: `DirectCallRequest`, `DirectCallAccept`, `DirectCallReject`, `DirectCallEnd`, `DirectCallStatus`
- Separate SSRC-Range und eigene SRTP-Keys für Direct-Streams
- Lokales Broadcast-Muting während aktivem Direktruf
- Roster-Anzeige "in direct call"
- **Busy-Flow gemäß F-12:** Heads-up-UI bei eingehendem Direktruf während eigener Sendung oder anderem Direktruf, mit Aktionen "Später" und "Ablehnen"; konfigurierbares Benachrichtigungs-Verhalten (stumm/Vibration/Klingelton)
- State-Machine-Erweiterung in `:service:ptt` um `pendingCalls`-Queue (siehe `docs/architecture.md §8.2`)

**Akzeptanzkriterien:**
- [ ] Peer A ruft Peer B an → B bekommt Ringsignal mit Ton
- [ ] B kann annehmen, ablehnen, oder Timeout (30 s)
- [ ] Während Direktruf hören nur A und B einander
- [ ] Andere Peers sehen A und B im Roster als "in direct call", hören aber nichts
- [ ] Beenden von beiden Seiten möglich
- [ ] Nach Beenden ist Broadcast wieder hörbar
- [ ] Eingehender Direktruf während eigener Sendung: Heads-up statt Full-Screen, PTT-Sendung läuft störungsfrei weiter
- [ ] "Später annehmen" hält den Ruf bis Sendungs-Ende; "Ablehnen" sendet `DirectCallReject(reason=BUSY)`
- [ ] Verpasster Ruf wird als Notification persistiert, nicht stumm verworfen

**Out of scope:** Conferencing (3+ Teilnehmer).

**Abhängigkeiten:** v0.7.0.

---

## v0.9.0 — Robustheit + Adaptivität

**Ziel:** Die App wird so robust wie "Skype auf Luft und Staub". Adaptive Codec-Parameter, UDP-Broadcast-Beacon, Connectivity Prober.

**Scope:**
- UDP-Broadcast-Beacon als Discovery-Fallback
- `TransportProbe`/`TransportReport`-Mechanismus
- Connectivity Prober mit UI-Integration (grün/gelb/rot)
- Adaptive Opus-Bitrate basierend auf `CodecHint`
- App-Level-Frame-Duplizierung bei hohem Loss
- Adaptiver Jitter-Buffer

**Akzeptanzkriterien:**
- [ ] Funktioniert bei mDNS-Filterung (nur Broadcast-Beacon)
- [ ] Funktioniert bei 30 % simuliertem Paketverlust (noch verständlich)
- [ ] Bitrate passt sich innerhalb von 10 s nach Loss-Anstieg an
- [ ] Jitter-Buffer wächst bei Burst-Loss auf bis zu 200 ms und stabilisiert sich wieder
- [ ] Network-Change-Event löst saubere Rediscovery aus
- [ ] Diagnose-Screen zeigt Transport-Pfad, RTT, Loss, Jitter, Bitrate pro Peer

**Out of scope:** Relay-Mode.

**Abhängigkeiten:** v0.8.0.

---

## v0.10.0 — Relay-Mode

**Ziel:** AP-Client-Isolation wird durch Relay über dritten Peer überwunden, ohne Ende-zu-Ende-Verschlüsselung zu brechen.

**Scope:**
- `:service:relay` — Outer-Frame-Format, Relay-Routing
- `RelayOffer`/`RelayRequest`/`RelayAvailable`-Messages
- Automatische Relay-Wahl bei UDP-Fail
- TCP-Fallback-Transport für Relay

**Akzeptanzkriterien:**
- [ ] Simulation von Client-Isolation (iptables/emulator-Config): Audio wird über dritten Peer geroutet
- [ ] Relay-Peer kann Audio-Inhalt nicht entschlüsseln (Test: Relay-Keys werden nie abgeleitet)
- [ ] Audio bleibt verständlich, Latenz erhöht sich um ≤ 100 ms
- [ ] Automatische Relay-Wahl nach niedrigster Latenz
- [ ] Fallback auf TCP-Relay bei UDP-Block

**Out of scope:** Komfort-Features, Polish.

**Abhängigkeiten:** v0.9.0.

---

## v1.0.0 — Polish & öffentliches Release

**Ziel:** Release-ready. UX poliert, Battery optimiert, alle Docs vollständig.

**Scope:**
- Bluetooth-Headset-Support (manueller Toggle)
- Hardware-PTT-Button-Unterstützung (Settings-Feature-Toggle aus F-11/F-13)
- **Theme-Auswahl** (Dunkel/Hell/System folgen) gemäß F-14
- **Peer-Avatare mit Initialen** — automatisch aus Display-Name abgeleitet
- **Kanal-spezifische Farbkodierung** — Vorbereitung für späteres Multi-Channel; im MVP nur subtile Akzentfarbe pro Kanal (Vordergrund-Trim), keine Funktionalität
- **DTX-Disable-Privacy-Option** in Settings (aus Audit F-SIDE-05)
- Settings-Feinschliff
- Diagnose-Export (Log-Bundle, lokaler Share-Intent)
- App-Icon, Splash-Screen, Screenshots für GitHub-Readme
- Battery-Optimierungen (Doze-Tests, Background-Verhalten)
- README-Finalisierung mit Screenshots
- Release-Signatur
- SLSA-Provenance-Attestation via GitHub-Actions (aus Audit F-SUPPLY-04)
- F-Droid-kompatibles Manifest (reproducible build, keine Google-Dependencies für Core)

**Akzeptanzkriterien:**
- [ ] Battery-Idle ≤ 3 %/h im Kanal
- [ ] Alle Anforderungen aus `docs/requirements.md` erfüllt und verifiziert
- [ ] Architect-Agent hat Final-Review + Approval dokumentiert
- [ ] Release-APK signiert, GitHub-Release mit Release Notes und Provenance-Attestation erstellt
- [ ] Peer-Avatare erscheinen in Hauptscreen und Direct-Call-Screen
- [ ] Jeder Kanal zeigt seine charakteristische Farbe im Header-Trim
- [ ] Audit-Runde 3 durch externen Reviewer abgeschlossen

**Out of scope:** Neue Features jenseits dieser Liste.

**Abhängigkeiten:** v0.10.0.

---

## Post-v1.0 Roadmap

Nicht fest terminiert, aber in Planung:

- **F-Droid-Submission.** Reproducible build und Submission-PR an `fdroiddata`. Erfordert SLSA-Provenance und F-Droid-spezifische Metadaten.
- **App-interne PIN** als zusätzlicher Faktor gegen physischen Zugriff (aus Audit Restrisiken).
- **MLS-artiges Group-Rekey** statt Re-Pairing bei Kanal-Secret-Kompromittierung (aus Audit F-PAIR-02) — TBD.
- **Kotlin-Multiplatform-Refactor** für iOS-Portierung — TBD.
- **Post-Quantum-Crypto** (X25519 → X-Wing-Hybrid) — TBD.

---

## Release-Workflow (Orchestrator)

Für jeden Release:

1. **Planning:** Orchestrator liest den kommenden Release, klärt offene Fragen mit Nutzer.
2. **Branching:** Architekt legt `release/vX.Y.0` Branch an, schlägt Teilaufgaben vor.
3. **Implementation:** Entwickler arbeitet Teilaufgaben ab, Dokumentierer aktualisiert Docs begleitend.
4. **Pre-Review:** Architekt führt kritisches Review durch. Entscheidet ggf. über Refactoring.
5. **CI:** Alle Workflows müssen grün sein.
6. **GitHub-Copilot-Kommentare:** Architekt sichtet und entscheidet.
7. **Tagging:** Orchestrator pusht Tag `vX.Y.0`, Release-Workflow erzeugt GitHub-Release.
8. **State-Update:** Orchestrator aktualisiert `docs/project-state.md` und diesen Plan (Status-Marker).
9. **Retrospektive:** Erkenntnisse werden in `docs/project-state.md` festgehalten und ggf. als ADR dokumentiert.
