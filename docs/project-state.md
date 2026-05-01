# HeraTalk — Projektstatus

> **Lebendes Dokument**, gepflegt vom Orchestrator-Agent und ergänzt vom Dokumentierer. Enthält den aktuellen Stand, alle getroffenen Entscheidungen und offene Risiken. Bei jedem PR, der Architektur oder Release-Status berührt, wird dieses Dokument mit aktualisiert.

Stand: Release v0.1.0 (Grundgerüst) — Branch `release/v0.1.0`, Phase A (A1–A5), Phase B (B1, B2a) und **Phase C (C1–C6) vollständig abgeschlossen (2026-05-01)**. B2b bewusst zurückgestellt — Lint-HardcodedText-Pflicht (B2a) deckt den Schutz ab. Nächster Schritt: **Phase D — Koin-DI-Graph**.

## Aktueller Release

**In Arbeit:** v0.1.0 — Grundgerüst (Kick-off 2026-04-30, Phase A vollständig abgeschlossen 2026-05-01, Phase B vollständig abgeschlossen 2026-05-01, Phase C vollständig abgeschlossen 2026-05-01)
**Nächster geplanter Release:** v0.2.0 — PoC Paketversand
**Letzter abgeschlossener Release:** (noch keiner)
**Aktiver Branch:** `release/v0.1.0`
**Nächster Schritt:** Phase D — Koin-DI-Graph (D1: Modul-Bindings pro Library-Modul; D2: Koin-Init in `HeraTalkApplication`).

## Fortschritt v0.1.0 — Phasen-Tracking

| Phase | Inhalt | Status |
|-------|--------|--------|
| A1 | Build-System-Grundgerüst (`settings.gradle.kts`, Convention-Plugins, `lint.xml`, `detekt.yml`, Spotless) | ✅ abgeschlossen, vom Architekten freigegeben |
| A2 | `:core:model` (Domain-Datenklassen, `DisplayName`-Validierung, JUnit-5-Tests) | ✅ abgeschlossen, alle Tests grün, Copilot-Feedback eingearbeitet |
| A3 | `:core:logging` (Logger-Interface, AndroidLogcatLogger, RingBufferLogger, CompositeLogger) | ✅ abgeschlossen |
| A4 | `:core:crypto` (Skeleton, Type-Stubs mit `TODO("v0.5.0/v0.6.0")`) | ✅ abgeschlossen |
| A5 | `:core:identity` (DataStore, `IdentityRepository`, Validierung) | ✅ abgeschlossen |
| B1 | `:core:ui` (Material-3-Theme, gemeinsame Composables, Strings) | ✅ abgeschlossen, Architekt-Review-Fixes eingearbeitet |
| B2a | Lint-Enforcement-Smoketest (HardcodedText als Error) | ✅ abgeschlossen |
| B2b | Custom-detekt-Rule `HardcodedStringInComposable` | ⏸ bewusst zurückgestellt — Lint-HardcodedText-Pflicht reicht; TODO in `detekt.yml` |
| C1 | `:service:lifecycle` (Foreground-Service, FeatureState, eigene Strings) | ✅ abgeschlossen |
| C2 | `:service:discovery`/`transport`/`signaling`/`media`/`audio`/`ptt`/`relay` Skelette | ✅ abgeschlossen |
| C3 | `:feature:pairing` (Channel-Choice, Display-Name, QR-Stub, ViewModel) | ✅ abgeschlossen, vom Architekten freigegeben |
| C4 | `:feature:channel` (Skelett mit fixem PTT-Anker, leerer Roster) | ✅ abgeschlossen, vom Architekten freigegeben |
| C5 | `:feature:settings` (Sektionen, Sprache funktional, Display-Name-Edit) | ✅ abgeschlossen, vom Architekten freigegeben |
| C6 | `:feature:direct` (leerer Slot mit `DirectFeature`-Marker) | ✅ abgeschlossen |
| D1–D2 | Koin-DI-Graph | 📋 geplant |
| E1–E4 | `:app`-Einstiegspunkt | 📋 geplant |
| F1–F3 | CI, Doku-Sync, Geräte-Test | 📋 geplant |

## Fortschritt pro Komponente

| Komponente | Status | Nächster Schritt |
|------------|--------|------------------|
| Architektur-Entwurf | ✅ v2 dokumentiert | Stabil, wird bei jedem Release challenged |
| ADRs 0001–0003 | ✅ verfasst (Peer-Discovery, Noise, Audio-Codec) | Werden bei jeder Architektur-Änderung neu geprüft |
| DevContainer | ✅ spezifiziert | In v0.1.0 implementieren |
| GitHub-Repository | 📋 spezifiziert | In v0.1.0 initialisieren |
| CI/CD | 📋 spezifiziert | In v0.1.0 aufsetzen und grün bekommen |
| Claude-Code-Agenten | ✅ definiert | Einsatzbereit ab Projektstart |
| `:app`-Modul | 📋 geplant | v0.1.0 |
| `:core:model` | ✅ implementiert (v0.1.0/A2) | Stabil; Domain-Datenklassen werden bei Bedarf in Folge-Releases erweitert |
| `:core:logging` | ✅ implementiert (v0.1.0/A3) | Logger-Interface, AndroidLogcatLogger, RingBufferLogger, CompositeLogger — alle Tests grün |
| `:core:crypto` | ✅ implementiert (v0.1.0/A4 Skeleton) | KeyDerivation + Aead Interfaces mit TODO("v0.5.0/v0.6.0"); Skelett an ADR-0002-Phasen ausgerichtet |
| `:core:ui` | ✅ implementiert (v0.1.0/B1) | HeraTalkTheme light/dark, HeraTalkColors, HeraTalkExtraColors, HeraTalkScaffold, NetworkQualityBadge, SectionHeader; EN+DE Strings; Compose-Previews; Architekt-Review-Fixes eingearbeitet |
| `:core:identity` | ✅ implementiert (v0.1.0/A5) | IdentityRepository, DataStoreIdentityRepository, fallbackPeerName; alle Tests grün |
| `:service:discovery` | ✅ Skelett (v0.1.0/C2) | Fachlich ab v0.2.0 — `NsdManager` und Broadcast-Beacon |
| `:service:transport` | ✅ Skelett (v0.1.0/C2) | UDP unicast v0.2.0, broadcast v0.4.0, TCP-Relay v0.10.0 |
| `:service:signaling` | ✅ Skelett (v0.1.0/C2) | Noise-Handshake + TCP-Control-Plane ab v0.5.0 |
| `:service:media` | ✅ Skelett (v0.1.0/C2) | Unverschlüsselter RTP v0.4.0, SRTP-Wrapping v0.6.0 |
| `:service:audio` | ✅ Skelett (v0.1.0/C2) | AudioRecord + libopus-JNI ab v0.3.0 |
| `:service:ptt` | ✅ Skelett (v0.1.0/C2) | Floor-Arbitrierung + VOX-Hangover ab v0.7.0 |
| `:service:relay` | ✅ Skelett (v0.1.0/C2) | Routing über dritten Peer ab v0.10.0 |
| `:service:lifecycle` | ✅ Skelett (v0.1.0/C1) | Foreground-Service mit `connectedDevice`/`microphone`-Switch; Mic-Pfad ab v0.7.0 |
| `:feature:pairing` | ✅ Skelett (v0.1.0/C3) | Display-Name funktional, QR-Scan-Logik ab v0.5.0 |
| `:feature:channel` | ✅ Skelett (v0.1.0/C4) | Fachlich ab v0.2.0 (Roster), v0.4.0 (PTT-Audio) |
| `:feature:direct` | ✅ Slot belegt (v0.1.0/C6) | Direct-Call-UI komplett ab v0.8.0 |
| `:feature:settings` | ✅ Skelett (v0.1.0/C5) | Sprache + Display-Name funktional; Audio-/Netzwerk-/Notification-Sektionen ab v0.7.0/v0.8.0/v0.9.0 |

## Entscheidungsprotokoll

Chronologisch. Jeder Eintrag: Datum · Entscheidung · Begründung. Architektur-relevante Entscheidungen werden zusätzlich als ADR in `docs/adrs/` dokumentiert.

### 2026-04-24 · Projektname: HeraTalk
Kurzer, einprägsamer Name mit Anlehnung an die altgriechische Göttin Hera (Kommunikation / Königin der Götter). Kollidiert nicht mit bekannten Marken.

### 2026-04-24 · Lizenz: BSD 3-Clause
Übereinstimmung mit der Opus-Lizenz. Permissive, keine Copyleft-Pflichten, Copyright-Notice muss erhalten bleiben, kein Endorsement ohne Erlaubnis. Konsequenz: Copyright-Header in jeder Source-Datei, per Spotless in CI erzwungen.

### 2026-04-24 · Plattform: nur Android
Kein MAUI, kein Flutter, kein React Native. Begründung: Audio-Echtzeit-Pipeline ist in allen Cross-Platform-Frameworks nativ zu bauen. Der Cross-Platform-Nutzen verpufft, die Zusatz-Komplexität nicht. iOS-Option bleibt über spätere KMP-Migration offen. (Frühere Version dieses Themas existierte als ADR-0001 "kotlin-native"; mit der ADR-Sanierung 2026-04-30 wurde der ADR-Slot 0001 für Peer-Discovery wiederverwendet — die Plattform-Entscheidung lebt nun ausschließlich in diesem Entscheidungsprotokoll und in `docs/architecture.md`.)

### 2026-04-24 · Kommunikationsmodell: Broadcast + 1:1
Zwei Modi: Broadcast-Kanal (alle hören alles) und private 1:1-Direktgespräche parallel zum Kanal. Während eines Direktrufs wird Broadcast beim Teilnehmer lokal gemutet.

### 2026-04-24 · PTT- und VOX-Modus, umschaltbar
Beide Modi werden unterstützt, Umschalten in Settings. Floor-Control nur im PTT-Modus.

### 2026-04-24 · Netzwerk: Single WLAN, Single Subnet
Primär-Szenario. Multi-AP-Handling über `NetworkCallback` + aggressives Reconnect. Multi-Subnet wird explizit nicht unterstützt.

### 2026-04-24 · Verschlüsselung: SRTP mit Noise-Handshake
Noise für Handshake (Pattern-Auswahl je nach Phase, siehe ADR-0002), SRTP mit AEAD (ChaCha20-Poly1305) für Medien. Kanal-Secret als PSK. Eigene SRTP-Implementierung in `:core:crypto` statt libsrtp-JNI (Scope klein, weniger Abhängigkeiten). Die SRTP-Entscheidung ist in `docs/architecture.md §9` festgehalten und nicht (mehr) als eigener ADR ausgelagert — der ADR-Slot 0003 wurde mit der Sanierung 2026-04-30 für die Audio-Codec-Entscheidung neu belegt.

### 2026-04-24 · Codec: Opus, adaptiv
Opus als einziger Codec (royalty-free, low-latency, WebRTC-Standard). Bitrate adaptiv 8–32 kbps basierend auf `CodecHint`-Feedback.

### 2026-04-24 · Robustheits-Anspruch: Skype-Niveau
Cross-Layer-Robustheit als Querschnittsziel. Transport-Kaskade (UDP Unicast → Broadcast → Relay), adaptive Codec-Parameter, App-Level-Frame-Duplizierung, Connectivity Prober. Relay-Mode gegen AP-Client-Isolation.

### 2026-04-24 · DevContainer als primäre Dev-Umgebung
Ubuntu 24.04 + JDK 21 + Android SDK/NDK + CMake + protoc. ADB-Brücke über Host-Server. Identisch zur CI. Claude Code via nativen Installer (kein Node.js mehr nötig).

### 2026-04-24 · Agentisches Projektteam für Claude Code
Vier Rollen: Orchestrator (Planung und Fortschritt), Architekt (Architektur und Review), Entwickler (Implementierung und Tests), Dokumentierer (Docs-Pflege). Jeder als `.claude/agents/*.md`-Datei konfiguriert.

### 2026-04-25 · Feature-basiertes Permission-Modell
Kritische Permissions (`RECORD_AUDIO`, `CAMERA`, `FOREGROUND_SERVICE_MICROPHONE`-Nutzung, `USE_FULL_SCREEN_INTENT`) werden nicht beim App-Start, sondern beim Aktivieren des zugehörigen Features abgefragt. Ablehnung deaktiviert ausschließlich das betroffene Feature — der Kanal-Betrieb ohne Mikrofon (nur Zuhören) bleibt möglich. Der Foreground-Service wechselt seinen Typ zur Laufzeit zwischen `connectedDevice` (Standard-Empfang), `microphone` (VOX oder Hardware-PTT aktiv) und gestoppt. Konsequenz: neue Anforderung F-11 in `requirements.md`, Feature-zu-Permission-Matrix und `:service:lifecycle`-Modul in `architecture.md §11.2`, Settings-Unterabschnitt "Features und Berechtigungen" in `ui.md §8`.

### 2026-04-25 · Vulnerability-Reporting via GitHub Private Vulnerability Reporting
Keine eigene Security-Mailadresse, kein PGP-Key. Stattdessen GitHub Private Vulnerability Reporting (PVR) im Repo aktiviert. Vorteile: eingebauter Workflow, keine Mail-Infrastruktur zu pflegen, für Reporter bekannter UX-Flow. `SECURITY.md` verweist darauf. Schließt Audit-Finding F-OPS-02.

### 2026-04-25 · Update-Check als Opt-in mit Erstnutzer-Frage
Die App darf optional beim Start gegen `https://github.com/relexx/heratalk/releases/latest` prüfen, ob neue Versionen existieren, und ein "Update verfügbar"-Banner anzeigen. Default: **aus**. Beim ersten App-Start wird der Nutzer explizit gefragt: "Soll HeraTalk automatisch nach Sicherheitsupdates suchen? Dazu verbindet sich die App beim Start kurz mit GitHub — sonst läuft sie komplett offline." Nutzer entscheidet, Entscheidung wird persistiert und ist in Settings änderbar. Schließt Audit-Finding F-OPS-01. Wird als explizite Ausnahme in `requirements.md NF-03` (Offline-Fähigkeit) dokumentiert.

### 2026-04-25 · libopus-Integration: Präferenz AAR
Architekt-Agent wird die finale Wahl später treffen (ADR 0003). Präferenz: vorgefertigter AAR mit gepinntem Hash, sofern eine vertrauenswürdige Quelle existiert. Fallback: eigener CMake-Build aus Xiph-Source. Spart Build-Infrastruktur-Aufwand, reduziert Supply-Chain-Prüfung auf ein einziges Artefakt.

### 2026-04-25 · Nice-to-have-Features für v1.0 und später
Durchentschieden im UX-Review: Peer-Avatare mit Initialen (v1.0), Kanal-spezifische Farbkodierung (v1.0), F-Droid-Submission (post-v1.0). Aufgenommen in `docs/releases.md`.

### 2026-04-25 · Offen-für-später-Entscheidungen (TBD)
Bewusst nicht entschieden, Architekt-Agent darf bei Gelegenheit Diskussion anstoßen: Kotlin-Multiplatform-Refactor für iOS-Portierung; MLS-artiges Group-Rekey statt Re-Pairing bei Kanal-Secret-Kompromittierung; Post-Quantum-Crypto (X25519 → X-Wing-Hybrid).

### 2026-04-25 · UX-Revision — PTT-Anker, Ampel-Farben, Peer-Gruppierung, Busy-Direktruf
Zweite UX-Durchsicht nach erstem Mockup. Kernentscheidungen:
- **PTT als fixer Anker:** Der PTT-Button hat in jedem Screen mit Audio-Kontext dieselbe absolute Position und dieselbe Größe (110 dp), inklusive Direktruf-Screen. Muscle-Memory-Garantie.
- **Ampel-Farblogik:** Grün = aktiv/verfügbar, Blau = Direktruf/privat, Gelb = Warnung/degradiert, Rot = Fehler/Auflegen. Orange als frühere Primary-Wahl verworfen — grün signalisiert "Go/Safety" konsistenter.
- **Netzwerk-Indikator immer sichtbar**, oben rechts in jedem Screen.
- **Peer-Liste gruppiert** in "Aktiv" und "Online", mit sticky "Aktiv"-Gruppe beim Scrollen. Reduziert kognitive Last.
- **VOX-Schwellwert-Slider mit Live-RMS-Pegel** direkt darunter — Nutzer sieht sofort, ob die Einstellung passt.
- **Eingehender Direktruf während eigener Sendung** (neue Anforderung F-12): Heads-up statt Full-Screen-Ringing, konfigurierbares Benachrichtigungs-Verhalten (stumm/Vibration/Klingel, default Vibration). State-Machine-Erweiterung um Busy-Übergänge.
- **Hardware-PTT erweitert** (neue Anforderung F-13): Bluetooth-Media-Button und Lautstärke-Tasten (einzeln aktivierbar), alles default-aus. USB-Fernbedienung post-v1.0.
- **Theme-Einstellung für v1.0** (neue Anforderung F-14): Dunkel (default), Hell, System folgen.

Konsequenz: Updates in `requirements.md` (neue F-12/F-13/F-14, F-11 präzisiert), `ui.md` (komplett überarbeitet), `architecture.md §8.2` (State-Machine + Busy-Verhalten), `architecture.md §11.2` (Hardware-PTT-Details). Mockup neu gebaut.

### 2026-04-25 · Settings-Sortierung und Auto-Resume statt Auto-Boot
Nach UX-Review die Settings-Gruppen-Reihenfolge umgestellt auf: Audio → App-Verhalten → Netzwerk → Benachrichtigungen → Features und Berechtigungen → Kanal → Info. Begründung: oben das Verhalten, das man laufend justiert; in der Mitte die "wenn-was-nicht-stimmt"-Knöpfe; unten die "einmal-eingestellt"-Sektionen. Sidetone wandert von "Audio" in "Features und Berechtigungen", weil es Audio-Routing-Verhalten ändert. Außerdem: Toggle "Auto-Start beim Gerätestart" wurde fehlinterpretiert — gemeint war "Letzten Kanal bei App-Start fortsetzen" (überspringt den Kanal-Auswahl-Screen). Kein `RECEIVE_BOOT_COMPLETED`-Permission, kein Auto-Boot. App läuft weiterhin nur, wenn der Nutzer sie aktiv aufruft.

### 2026-04-25 · Internationalisierung von Beginn an
HeraTalk wird ab v0.1.0 mehrsprachig aufgesetzt. MVP-Sprachen: **Englisch** (Default) und **Deutsch** (Override). Begründung: hartkodierte Strings sind später schwer zu finden und nachzurüsten; ein i18n-Setup von Anfang an ist die billige Variante. Konsequenz: neue Anforderung F-15, neuer Architektur-Abschnitt §11.7 mit Resource-Layout und Locale-Handling, neue Regel 33 in `.claude/rules.md` (keine hartkodierten UI-Strings), Erweiterung des Documenter-Agent-Profils um Übersetzungs-Pflege inklusive Glossar mit kanonischen Übersetzungen, Erweiterung des Developer-Agent-Profils um i18n-Disziplin. Sprach-Auswahl in Settings (System folgen / Deutsch / Englisch). Übersetzungs-Pflege liegt beim Documenter-Agent. Code-Kommentare, KDoc und Log-Messages bleiben Englisch.

### 2026-04-27 · Display-Name-Eingabe im Pairing-Flow (Lücke im Erstnutzer-Flow geschlossen)
Befund: Der dokumentierte Erstnutzer-Flow in `ui.md §8.3` deckte nur die Update-Check-Entscheidung ab, enthielt aber keinen Schritt für die Eingabe des Display-Namens. Gleichzeitig verlangt `architecture.md §6.1` `dname` als Pflicht-TXT-Record im Discovery, und der Settings-Eintrag "Dein Name" (`ui.md §8`) ist erst nach erfolgreichem Kanal-Beitritt erreichbar — eine zirkuläre Lücke.

**Entscheidung:** Der Display-Name wird **als verpflichtender Schritt im Pairing-Flow** abgefragt, direkt nach Tap auf "Kanal beitreten" oder "Neuen Kanal erstellen" (Screen 1) und **vor** dem QR-Scanner bzw. der QR-Code-Generierung. Begründung:

- **Natürlicher Identitätsmoment.** Der Nutzer wählt gerade seine Identität in einer Gruppe — der einzige Moment im Flow, in dem "Wer bin ich für die anderen?" inhaltlich passt.
- **`dname` ist Pflicht-Feld** im Discovery-TXT-Record. Ohne Wert müsste die App leer broadcasten (Peers sehen "(unbekannt)") oder den Geräte-Hostnamen ableiten (auf Android herstellerabhängig instabil, oft mit echtem Namen vorbelegt → Datenschutz-Risiko).
- **Onboarding-Dialog bleibt fokussiert.** Der Willkommens-Screen (§8.3) trifft weiterhin genau eine sicherheitsrelevante Entscheidung (Update-Check). Identität gehört in den Pairing-Kontext, nicht in den Update-Check-Kontext.
- **Konsistent für Re-Pairing.** Beim späteren Kanal-Wechsel über Settings wird derselbe Flow durchlaufen; der bisherige Name ist als Default vorbelegt, lässt sich aber pro Kanal überschreiben.

**Fallback-Verhalten für leeren `dname`:** Der Pairing-Flow lehnt leere oder rein aus Whitespace bestehende Namen ab (Eingabefeld erlaubt keinen Weiter-Tap, bis ein nicht-leerer Wert mit ≥ 1 sichtbarem Zeichen gesetzt ist). Maximal 32 Unicode-Codepoints, damit der Wert in den TXT-Record passt und in der UI nicht überläuft. Falls `dname` trotzdem (etwa durch Datenkorruption oder Migration) leer wäre, fällt `:service:discovery` auf `"Peer-{first8hex(pk)}"` zurück (z. B. `Peer-a7f3:2c91`) — **nie** auf den Geräte-Hostnamen.

**Konsequenzen / Folge-Aufgaben für den Documenter:**
- `requirements.md`: Neue Anforderung **F-16 — Display-Name-Eingabe im Pairing-Flow** mit Akzeptanzkriterien (Pflicht-Eingabe vor QR-Schritt, Mindestens 1 sichtbares Zeichen, Maximal 32 Codepoints, Default-Vorbelegung beim Re-Pairing aus letztem Wert, Persistenz in DataStore, in Settings unter "Kanal" → "Dein Name" jederzeit änderbar). F-02 (Kanal-Auswahl beim Start) erhält Querverweis auf F-16.
- `ui.md §8.3`: Erstnutzer-Flow um den Pairing-Schritt mit Namens-Eingabe erweitern. Klarstellen, dass die Reihenfolge ist: Willkommens-Dialog (Update-Check) → Screen 1 (Kanal-Wahl) → **Namens-Eingabe-Screen (§4)** → QR-Scanner / QR-Anzeige → Hauptscreen.
- `ui.md §3` bzw. neuer Screen-Abschnitt: ASCII-Mockup für den Namens-Eingabe-Screen ergänzen. Layout: Eingabefeld (groß, zentriert, Touch-target ≥ 48 dp), Hilfetext "Dieser Name erscheint bei anderen Peers im Kanal", primärer "Weiter"-Button (deaktiviert bei leerer Eingabe), sekundärer "Zurück"-Pfeil. Beim Re-Pairing-Kontext zusätzlicher Hinweis: "Du kannst den Namen pro Kanal anpassen."
- `architecture.md §6.1`: Klarstellen, dass `dname` aus Pairing-Flow stammt und niemals leer broadcastet wird. Fallback-Regel `Peer-{first8hex(pk)}` für Korruptions-/Migrations-Edge-Case dokumentieren. Hinweis ergänzen: kein Geräte-Hostname als Fallback (Datenschutz).
- Dokumentierer aktualisiert ggf. die i18n-Glossar-Einträge in `.claude/agents/documenter.md` für die neuen UI-Strings (Eingabefeld-Hinweis, Validierungs-Fehlertexte).

Implementierung: Frühestmöglich in **Release v0.1.0** als Skeleton (UI-Screen mit Validierung, persistiert in DataStore, vorerst ohne tatsächliche Discovery-Anbindung — die kommt mit v0.2.0). Damit ist der Flow von Anfang an korrekt geformt und es gibt keine spätere Reorganisation des Onboardings.

**Nachtrag 2026-04-27 (nach Architekt-Review):** Eingabefeld ist leer (keine Vorbelegung). Placeholder-Text statt Default-Wert. Pflichtfeld (≥ 1 sichtbares Zeichen). 32 Codepoints max. Fallback bei Korruption: `Peer-{first8hex(pk)}`. Re-Pairing zeigt Screen mit letztem gespeichertem Wert. Name ist global, nicht pro Kanal. Persistenz und Validierungslogik leben in einem neuen Modul `:core:identity` (`IdentityRepository`); `:feature:pairing`, `:feature:settings` und `:service:discovery` greifen ausschließlich darüber zu. Eingehende `dname`-Werte fremder Peers werden in `:service:discovery` sanitisiert (NFC-Normalisierung, Strip Bidi-Override, Combining-Marks-Begrenzung, Truncation auf 32 Codepoints) — siehe neues Audit-Finding F-PRIV-04.

### 2026-04-30 · ADR-Sanierung 0001–0003

Die drei bestehenden ADRs wurden als inkonsistent und teilweise duplikativ zur Architektur-Doku identifiziert (Architekt-Review):

- **ADR-0001 "kotlin-native"** dupliziert lediglich die Plattform-Entscheidung aus `architecture.md §1` und dem Eintrag vom 2026-04-24 in diesem Protokoll — keine zusätzliche Information, kein abgewogenes Alternativen-Set.
- **ADR-0002 "noise-protocol"** nannte `Noise_XX_25519_AESGCM_SHA256`, während `architecture.md §9` `Noise_KKpsk0_25519_ChaChaPoly_SHA256` festlegt. Widerspruch in der Cipher-Suite und im Handshake-Pattern.
- **ADR-0003 "srtp-custom-implementation"** beschreibt eine SRTP-Eigenimplementation, deren Entscheidung bereits im Entscheidungsprotokoll vom 2026-04-24 dokumentiert ist und in `architecture.md §9` ausgeführt wird — kein eigenständiges ADR-würdiges Thema mehr.

**Entscheidung (Nutzer wählte Option C — Überschreiben):** Die ADR-Slots 0001–0003 werden mit drei neuen, inhaltlich tragenden ADRs belegt:

- **ADR-0001 — Peer-Discovery:** mDNS/DNS-SD via `NsdManager` als Primärschicht; UDP-Broadcast-Beacon und manuelle IP-Eingabe als Fallback-Stufen (verworfen als Primärlösungen). Datei: `docs/adrs/0001-peer-discovery.md`.
- **ADR-0002 — Noise Protocol Framework:** Auflösung des Widerspruchs zwischen XX und KKpsk0 durch klare Phasen-Trennung. `Noise_XXpsk2_25519_ChaChaPoly_SHA256` für initiales Pairing (Public-Keys werden im Handshake selbst übertragen; PSK-Bindung erfolgt aus dem QR-Channel-Secret via HKDF-SHA256; gepinnt nach erstem Erfolg). `Noise_KKpsk0_25519_ChaChaPoly_SHA256` für reguläre Sessions (Static-Keys gepinnt, Channel-Secret als PSK). ChaCha20-Poly1305 als AEAD durchgängig. Datei: `docs/adrs/0002-noise-protocol.md`.
- **ADR-0003 — Audio-Codec:** Opus als einziger Codec, Integration via JNI zu libopus, AAR-Präferenz mit Selbstbau-Fallback, strikt CBR (Side-Channel-Schutz nach Wright et al. 2008), 8–32 kbps adaptiv. Datei: `docs/adrs/0003-audio-codec.md`.

**Konsequenz:** Die alten Dateien `0001-kotlin-native.md` und `0003-srtp-custom-implementation.md` wurden gelöscht; `0002-noise-protocol.md` wurde überschrieben. Die Plattform- und SRTP-Entscheidungen leben weiter in diesem Entscheidungsprotokoll und in `architecture.md` — keine inhaltliche Information ist verloren gegangen.

**Wichtig — Querverweis-Hinweis:** ADR-0003 ist ab jetzt ausschließlich die Audio-Codec-Entscheidung und darf nicht mehr als Referenz für SRTP-/Security-Themen gelesen werden. Die normativen SRTP-/Transport-Sicherheitsdetails werden nun in `architecture.md` festgehalten. Bestehende Querverweise auf "ADR 0003" in älteren Dokumenten (insbesondere `docs/security-audit.md`) im SRTP-Kontext sind als veraltet zu betrachten; die jeweilige normative Quelle ist `architecture.md §9`.

### 2026-05-01 · Phase B abgeschlossen: `:core:ui`, Lint-Smoketest, CI-Fixes

**B1 — `:core:ui` implementiert (commits `8f81f72`, `3299cf4`, `f00cf68`):**

- `HeraTalkTheme` (light + dark Color-Schemes), `HeraTalkColors` (Ampel-Farbtokens: grün/blau/gelb/rot), `HeraTalkExtraColors` (warning, directCall, offline via `CompositionLocal`), `HeraTalkTypography`.
- Composables: `HeraTalkScaffold`, `NetworkQualityBadge`, `SectionHeader` — alle mit Light/Dark `@Preview`.
- `ThemePreview.kt` mit dedizierten Theme-Palette-Previews (Architekt-B1-Review-Kriterium).
- EN-Strings in `core/ui/src/main/res/values/strings.xml`, DE-Strings in `values-de/strings.xml` (`common_*`, `network_quality_*`).
- Compose-BOM auf `2026.04.01` angehoben (`2026.04.00` existiert nicht auf Google Maven).

**Architekt-Befund D-1 (`:core:ui` ist Compose-Android-Library, nicht JVM-only):** `architecture.md §4.1` korrigiert — `:core:ui` aus der JVM-only-Aussage ausgenommen und korrekt als Compose-Android-Library beschrieben.

**CI-Fixes (commit `f00cf68`):**

- `detekt.yml`: ungültiger `formatting`-Block und `AvoidReferenceToMutableStateFlow` entfernt; `FunctionNaming.ignoreAnnotated` für `@Composable`/`@Preview`, `UnusedPrivateMember.ignoreAnnotated`, `MaxLineLength` mit Test-Ausnahmen, `MagicNumber.ignorePropertyDeclaration`, `UndocumentedPublicClass.searchInInnerObject=false` ergänzt.
- `.editorconfig`: `ktlint_function_naming_ignore_when_annotated_with` für `@Composable`/`@Preview` (PascalCase-Preview-Funktionen zulässig).
- Datei-Umbenennung: `Color.kt → HeraTalkColors.kt`, `Theme.kt → HeraTalkTheme.kt` (ktlint-filename-Rule).
- `HeraTalkExtraColors.kt` separiert, alle On-Color-Hex-Literale in `HeraTalkColors` als benannte Konstanten.
- `PeerId.MAX_LENGTH=64` als Konstante extrahiert (`MagicNumber`-Finding).
- Spotless: zweizeiliger Copyright-Header auf allen `.kt`-Dateien.

**B2a — Lint-Smoketest bestanden:** `HardcodedText` und `MissingTranslation` als Error in `lint.xml` aktiv und in CI erzwungen.

**B2b — Custom-detekt-Rule zurückgestellt:** Lint-`HardcodedText`-Pflicht übernimmt den Schutz. TODO in `detekt.yml` dokumentiert.

### Architect Review for Phase B (v0.1.0/B1+B2a)

Date: 2026-05-01
Result: APPROVED

Findings (alle nicht-blockierend):

- `HeraTalkTheme` Dark-Scheme: `onError` ist korrekt dunkel (#601410), weil `error` selbst hell ist (#F2B8B5). User-Verdacht (in Review-Aufgabe) trifft nicht zu — Material-3-konform.
- `HeraTalkTheme` Dark-Scheme: `errorContainer` und `onError` haben beide den Wert `RedContainerDark` (#601410). Material-3-Baseline würde `errorContainer` mittlerweise dunkler (z. B. #8C1D18) anlegen. Optisch flach in `Snackbar`-Backgrounds — aber rein kosmetisch. **Vertagt nach v1.0** (Theme-Polish, Folge der F-14-Theme-Auswahl).
- `HeraTalkTheme` Dark-Scheme nutzt Light-Konstanten als On-Container-Werte (`onPrimaryContainer = GreenContainerLight`). Material-3-konform und spart Konstanten, mentales Modell beim Lesen ist aber holprig. Optional in v1.0: `Green/Blue/Red OnContainerDark`-Aliase einführen, die intern auf die Light-Container-Konstanten zeigen.
- `@Suppress("HardcodedText")`-Verteilung: pro Funktion (statt file-level) — eng wie gewünscht. **OK.**
- `:core:ui` referenziert nur `:core:model`. Dep-Regel eingehalten. **OK.**
- String-Naming-Konvention (`common_*`, `network_quality_*`) eingehalten. **OK.**
- KDoc auf allen `public` Membern in `:core:ui`. **OK.**
- `detekt.yml` Suppressors angemessen (Compose-Annotated-Functions, Magic-Number-dp-Wertepool); `HardcodedStringInComposable` als TODO mit Issue-Referenz transparent. **OK.**
- `.editorconfig` Compose-Function-Naming-Override korrekt. **OK.**
- Detekt + Spotless auf Phase-B-Scope: clean. (2 Findings auf untracked Phase-C-Stubs sind nicht Phase-B.)

Conditions: keine.

Phase B ist freigegeben. Phase C kann gestartet werden — siehe aktualisierten `docs/impl-plan-v0.1.0.md §C` mit Architekt-Korrekturen (String-Quelle für `:service:lifecycle`, KDoc-Pflicht, appcompat-Dependency-Hinweis, feature → feature-Dependency-Akzeptanz für Display-Name-Wiederverwendung).

### 2026-05-01 · Phase C abgeschlossen: Service- und Feature-Skelette

**C1 — `:service:lifecycle`** (commit `9d089e6`):

- `HeraTalkService : Service()` mit `start(context)`/`stop(context)`-Companion, `setFeatureState(state)`-API, `applyState(...)`-Logik für den `connectedDevice`/`microphone`-Foreground-Type-Switch (architecture.md §11.3).
- `data class FeatureState(channelActive, voxEnabled, hardwarePttEnabled)` mit `Idle`-Companion.
- `NotificationCompat`-Builder mit lokalisierter Channel-/Title-/Text-Struktur. Eigene `values/values-de`-Strings (`lifecycle_notification_*`) — bewusst keine `:core:ui`-Abhängigkeit, damit die `feature → service → core`-Richtung sauber bleibt.
- `<manifest />` bleibt leer mit Kommentar; Service-Deklaration kommt in `:app` (Phase E1).
- TODO(developer)-Markierung im VOX-Pfad für v0.7.0.

**C2 — Service-Skelette `:service:discovery`/`transport`/`signaling`/`media`/`audio`/`ptt`/`relay`** (commits `29b85b4`, `685b610`, `8c5695d`, `26bb50f`, `3a15e24`, `11bccc0`, `4c22edf`):

- Pro Modul: ein `interface`-File mit Public API + KDoc auf jedem `public` Member, ein `XxxStub`-No-op mit `TODO(developer): vX.Y.0`-Markern, leeres `<manifest />`. Keine Tests in v0.1.0 (Stubs testen wäre Zeremonie).
- Sealed-Hierarchien für Zustandsmaschinen: `ControlPlaneState` (Idle/Connecting/Connected/Failed), `FloorState` (Idle/HeldByLocal/HeldByRemote).
- Datenklassen mit Array-Inhalt (`TransportPacket`, `DecodedFrame`) überschreiben `equals/hashCode` für inhaltliche Gleichheit.
- KDoc-Lücken aus dem Plan-§C2-Hinweis (`Connected.peer`, `HeldByRemote.holder`) wurden geschlossen.
- Pro Modul ein eigener Commit gemäß Plan-§C2-Granularitätsforderung.

**C3 — `:feature:pairing`** (commit `d4b5bf8`):

- `ChannelChoiceScreen`, `DisplayNameScreen` (validierender Pflicht-Eingabe-Screen mit Live-Codepoint-Counter und deaktiviertem "Weiter"-Button bei leerer/zu-langer Eingabe), `QrScanScreen` (Stub mit "verfügbar in v0.5.0"-Hinweis).
- `PairingViewModel(identityRepository)`: `DisplayNameInputState` mit `canSubmit`-Property, `onDisplayNameChanged(...)` re-evaluiert Validation, `onSubmit()` schreibt validen `DisplayName` über `IdentityRepository.setDisplayName(...)`. Bidi-Override-Validierung erfolgt im `DisplayName.init` aus `:core:model`; etwaige `IllegalArgumentException` flowt als `PersistResult.Error` zurück.
- Strings vollständig unter `pairing_*` in EN/DE; Compose-Previews für Light/Dark.
- KDoc auf allen `public` Composables, ViewModel, sealed `PersistResult`, `enum DisplayNameValidationError`.

**C4 — `:feature:channel`** (commit `3798c69`):

- `ChannelScreen` mit `HeraTalkScaffold`-Header, Section-Header "Peers", leerem Roster-Placeholder ("erscheinen hier sobald Discovery in v0.2.0…"), und einem zentral unten platzierten 110-dp-Kreis-PTT-Button (deaktiviert, mit Label "Drücken zum Sprechen" + Hilfstext "Verfügbar in v0.4.0").
- Strings unter `channel_*` in EN/DE; Compose-Previews für Light/Dark.

**C5 — `:feature:settings`** (commits `43ae446` für Build, `f3494df` für Code):

- Sektionen-Reihenfolge gemäß UX-Revision 2026-04-25: Audio (Stub) → App-Verhalten (live) → Netzwerk (Stub) → Benachrichtigungen (Stub) → Features+Berechtigungen (Stub) → Kanal (live) → Info.
- App-Verhalten: Sprach-Radio (System/DE/EN) ruft `AppCompatDelegate.setApplicationLocales(LocaleListCompat...)` auf und persistiert in modul-eigenem DataStore. Theme-Radio + Update-Check-Switch + Auto-Resume-Switch persistieren ihre Werte für spätere Releases.
- Kanal: Display-Name-Eintrag liest `:core:identity.IdentityRepository.displayName` und exponiert einen Edit-Button — `:feature:settings` hat **keine** Gradle-Abhängigkeit auf `:feature:pairing` (Empfehlung des Vorgänger-Orchestrators); Navigation zu `DisplayNameScreen` wird in `:app` (Phase E2) gehoist. Damit ist die Plan-§C5-Notiz über akzeptierte feature → feature-Abhängigkeit obsolet.
- `androidx.appcompat:appcompat:1.7.0` neu in `libs.versions.toml`, ausschließlich `:feature:settings` referenziert.
- Strings unter `settings_*` in EN/DE; `HorizontalDivider` (nicht das deprecate `Divider`) zwischen Sektionen.

**C6 — `:feature:direct`** (commit `23950f8`):

- Modul anlegen ohne UI; einziger Inhalt ist `object DirectFeature { const val PLACEHOLDER_RELEASE = "v0.8.0" }` als Marker, damit ktlint's `no-empty-file`-Regel nicht greift. Modul wird in v0.8.0 mit der echten Direct-Call-UI ersetzt.

**Build-/CI-Befunde:**

- `./gradlew assembleDebug`, `./gradlew detekt`, `./gradlew spotlessCheck` jeweils einzeln grün.
- `./gradlew assembleDebug detekt spotlessCheck` zusammen schlägt mit Configuration-Cache-Implicit-Dependency-Warnung fehl (Gradle-9.4.1, `:detekt` ↔ `:core:identity:checkDebugAarMetadata`). Sequenziell läuft alles. Tracked als OQ-07.
- Insgesamt 12 Phase-C-Commits + 1 Build-Setup-Commit + 1 Tooling-Commit (architect.md YAML-Quoting). Branch ist gepusht.

### Architect Review for Phase C (v0.1.0/C1–C6)

Date: 2026-05-01
Result: APPROVED

Findings (alle nicht-blockierend):

- Modul-Grenzen: `:service:lifecycle` zieht `:core:model` + `:core:logging` (kein `:core:ui`); andere Service-Module nur `:core:model` + `:core:logging` + `kotlinx.coroutines.android`. **OK.**
- Feature-Module: `:feature:pairing`/`channel`/`direct` ziehen `:core:model` + `:core:ui` (+ `:core:identity` bei pairing). `:feature:settings` zusätzlich `androidx.appcompat`, **kein** `:feature:pairing` (Plan-§C5-Empfehlung "feature → feature akzeptiert" wurde durch saubere Navigation-Lift in `:app` obsolet). **OK.**
- Stub-Qualität: KDoc dokumentiert die Ziel-Releases präzise, alle TODO(developer)-Marker referenzieren konkrete `vX.Y.0`-Releases. **OK.**
- Sealed-Hierarchien (`ControlPlaneState`, `FloorState`, `PersistResult`, `DisplayNameValidationError`) sind sauber modelliert; alle Konstruktor-Parameter haben KDoc (`UndocumentedPublicProperty` greift sonst). **OK.**
- Sicherheits-Stellen: `MediaEngine` und `AudioEngine` verweisen explizit auf `.claude/rules.md` Rule 4 / Rule 15. `PairingViewModel.onSubmit()` wrapped die `DisplayName(...)`-Konstruktion in `runCatching`, damit Bidi-Override-Validierungs-Exceptions die App nicht crashen. **OK.**
- `HeraTalkService.applyState(...)`: Race-Condition-Kommentar erklärt die "5-Sekunden-Frist"-Erst-`startForeground` korrekt. **OK.**
- KDoc auf allen `public` Membern aller neuen Module. **OK.**
- Compose-Previews Light + Dark in jedem Feature-Screen. **OK.**

Conditions: keine.

Open-Questions für Folge-Releases (in den OQ-Tabelle ergänzt):

- **OQ-04** (`:service:lifecycle`): Notification-Icon ist Platzhalter (`stat_sys_data_bluetooth`). Eigenes Asset mit Phase E1 / v1.0.
- **OQ-05** (Repo-Hygiene): `.gitignore` um `**/bin/` ergänzen — Eclipse/IDE-Output liegt aktuell untracked herum.
- **OQ-06** (`:feature:direct`): `DirectFeature`-Marker mit der ersten echten UI in v0.8.0 entfernen.
- **OQ-07** (CI): Gradle-9.4.1-Configuration-Cache-Implicit-Dependency zwischen `:detekt` und `:core:identity:checkDebugAarMetadata`. Sequenziell unkritisch; in CI getrennte Jobs oder explizites `mustRunAfter` setzen.

Phase C ist freigegeben. Phase D (Koin-DI-Graph) kann starten.

## Offene Fragen

Die folgenden Punkte wurden im Rahmen des Architekt-Reviews zu Phase A identifiziert und sind für spätere Releases vorgemerkt:

| Modul (ID) | Befund | Geplant für |
|------------|--------|-------------|
| `:core:crypto` (OQ-01) | Noise-Snapshot-Artefakt: Version und Hash für `libs.versions.toml` noch nicht festgelegt | v0.5.0 |
| `:core:logging` (OQ-02) | Replay-Semantik `RingBufferLogger`: bei neuem Subscriber werden alle 1000 gepufferten Einträge gesendet — gewünschtes Verhalten für Diagnose-Overlay muss spezifiziert werden | v0.2.0 |
| `:core:identity` (OQ-03) | Combining-Marks-Begrenzung für `DisplayName` (fremde Eingaben) nicht umgesetzt; Sanitisierung fremder Peer-Namen folgt mit `:service:discovery` | v0.2.0 |
| `:service:lifecycle` (OQ-04) | Notification-Icon ist Platzhalter (`android.R.drawable.stat_sys_data_bluetooth`); eigenes App-Asset entwerfen | v0.1.0/E1 oder v1.0 |
| Repo-Hygiene (OQ-05) | `.gitignore` deckt `**/bin/` (Eclipse/IDE-Output) nicht ab; Dirs liegen untracked herum | v0.1.0 (vor Tagging) |
| `:feature:direct` (OQ-06) | `DirectFeature`-Marker-Object existiert nur, weil ktlint `no-empty-file` greift; entfällt mit echter UI | v0.8.0 |
| CI/Gradle (OQ-07) | Configuration-Cache-Implicit-Dependency zwischen `:detekt` und `:core:identity:checkDebugAarMetadata` (Gradle 9.4.1); aufrufbar nur sequenziell | v0.2.0 |

## Risiken

| Risiko | Wahrscheinlichkeit | Impact | Mitigation |
|--------|--------------------|--------|------------|
| libopus-JNI-Build-Probleme | mittel | mittel | AAR verwenden statt eigener Build; Fallback Concentus (Pure-Java) |
| Android-Bluetooth-SCO-Inkonsistenzen | hoch | niedrig | MVP mit manuellem Toggle; Auto-Switching erst nach v1.0 |
| Hersteller-spezifisches Power-Saving killt Service | mittel | hoch | Foreground-Service-Typ `microphone`, Tests auf Samsung, Xiaomi; UI-Hinweis an Nutzer |
| AP-Client-Isolation blockiert auch Broadcast-Beacon | niedrig | hoch | Relay-Mode als Fallback (v0.10.0) |
| Android-15-API-Änderungen bei Foreground-Services | mittel | mittel | Architekt-Agent verfolgt Android-Release-Notes; Target-SDK frühzeitig auf 35 |
| Test auf echten Geräten erfordert Dev-Zugang zu Android-Hardware | sicher | niedrig | Pascal nutzt eigenes Gerät; CI läuft Unit-Tests, keine Instrumentation |

## Qualitäts-Kennzahlen

Werden gepflegt, sobald die ersten Tests existieren. Ziele aus `docs/requirements.md`:

- Unit-Test-Coverage `:core:*` + `:service:*` ≥ 70 %
- CI grün auf `main`
- Keine detekt/lint-Warnings auf PRs
- Mund-zu-Ohr-Latenz ≤ 250 ms im guten WLAN
- Battery-Idle ≤ 3 %/h im Kanal

## Lessons Learned

Wird nach jedem Release vom Orchestrator ergänzt. Format: Datum · Release · Erkenntnis · Konsequenz.

(Bisher keine Einträge.)
