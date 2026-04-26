# HeraTalk — Projektstatus

> **Lebendes Dokument**, gepflegt vom Orchestrator-Agent und ergänzt vom Dokumentierer. Enthält den aktuellen Stand, alle getroffenen Entscheidungen und offene Risiken. Bei jedem PR, der Architektur oder Release-Status berührt, wird dieses Dokument mit aktualisiert.

Stand: Projekt-Setup, Release v0.1.0 noch nicht gestartet.

## Aktueller Release

**In Arbeit:** (noch keiner)
**Nächster geplanter Release:** v0.1.0 — Grundgerüst
**Letzter abgeschlossener Release:** (noch keiner)

## Fortschritt pro Komponente

| Komponente | Status | Nächster Schritt |
|------------|--------|------------------|
| Architektur-Entwurf | ✅ v2 dokumentiert | Stabil, wird bei jedem Release challenged |
| DevContainer | ✅ spezifiziert | In v0.1.0 implementieren |
| GitHub-Repository | 📋 spezifiziert | In v0.1.0 initialisieren |
| CI/CD | 📋 spezifiziert | In v0.1.0 aufsetzen und grün bekommen |
| Claude-Code-Agenten | ✅ definiert | Einsatzbereit ab Projektstart |
| `:app`-Modul | 📋 geplant | v0.1.0 |
| `:core:*` | 📋 geplant | v0.1.0 (Skeleton), fachlich ab v0.5.0 |
| `:service:discovery` | 📋 geplant | v0.2.0 |
| `:service:transport` | 📋 geplant | v0.2.0 (Skeleton), adaptiv ab v0.9.0 |
| `:service:signaling` | 📋 geplant | v0.5.0 |
| `:service:media` | 📋 geplant | v0.4.0 (unverschlüsselt), SRTP ab v0.6.0 |
| `:service:audio` | 📋 geplant | v0.3.0 |
| `:service:ptt` | 📋 geplant | v0.7.0 |
| `:service:relay` | 📋 geplant | v0.10.0 |
| `:service:lifecycle` | 📋 geplant | v0.1.0 (Foreground-Service-Skeleton), dynamischer Typ-Wechsel ab v0.7.0 (VOX) |
| `:feature:pairing` | 📋 geplant | v0.5.0 |
| `:feature:channel` | 📋 geplant | v0.1.0 (Skeleton), fachlich ab v0.2.0 |
| `:feature:direct` | 📋 geplant | v0.8.0 |
| `:feature:settings` | 📋 geplant | v0.1.0 (Skeleton), fachlich ab v0.7.0 |

## Entscheidungsprotokoll

Chronologisch. Jeder Eintrag: Datum · Entscheidung · Begründung. Architektur-relevante Entscheidungen werden zusätzlich als ADR in `docs/adrs/` dokumentiert.

### 2026-04-24 · Projektname: HeraTalk
Kurzer, einprägsamer Name mit Anlehnung an die altgriechische Göttin Hera (Kommunikation / Königin der Götter). Kollidiert nicht mit bekannten Marken.

### 2026-04-24 · Lizenz: BSD 3-Clause
Übereinstimmung mit der Opus-Lizenz. Permissive, keine Copyleft-Pflichten, Copyright-Notice muss erhalten bleiben, kein Endorsement ohne Erlaubnis. Konsequenz: Copyright-Header in jeder Source-Datei, per Spotless in CI erzwungen.

### 2026-04-24 · Plattform: nur Android
Kein MAUI, kein Flutter, kein React Native. Begründung: Audio-Echtzeit-Pipeline ist in allen Cross-Platform-Frameworks nativ zu bauen. Der Cross-Platform-Nutzen verpufft, die Zusatz-Komplexität nicht. iOS-Option bleibt über spätere KMP-Migration offen. Siehe ADR 0001.

### 2026-04-24 · Kommunikationsmodell: Broadcast + 1:1
Zwei Modi: Broadcast-Kanal (alle hören alles) und private 1:1-Direktgespräche parallel zum Kanal. Während eines Direktrufs wird Broadcast beim Teilnehmer lokal gemutet.

### 2026-04-24 · PTT- und VOX-Modus, umschaltbar
Beide Modi werden unterstützt, Umschalten in Settings. Floor-Control nur im PTT-Modus.

### 2026-04-24 · Netzwerk: Single WLAN, Single Subnet
Primär-Szenario. Multi-AP-Handling über `NetworkCallback` + aggressives Reconnect. Multi-Subnet wird explizit nicht unterstützt.

### 2026-04-24 · Verschlüsselung: SRTP mit Noise-Handshake
Noise `KKpsk0_25519_ChaChaPoly_SHA256` für Handshake, SRTP mit AEAD für Medien. Kanal-Secret als PSK. Eigene SRTP-Implementierung in `:core:crypto` statt libsrtp-JNI (Scope klein, weniger Abhängigkeiten). Siehe ADR 0002 und 0003.

### 2026-04-24 · Codec: Opus, adaptiv
Opus als einziger Codec (royalty-free, low-latency, WebRTC-Standard). Bitrate adaptiv 8–32 kbps basierend auf `CodecHint`-Feedback.

### 2026-04-24 · Robustheits-Anspruch: Skype-Niveau
Cross-Layer-Robustheit als Querschnittsziel. Transport-Kaskade (UDP Unicast → Broadcast → Relay), adaptive Codec-Parameter, App-Level-Frame-Duplizierung, Connectivity Prober. Relay-Mode gegen AP-Client-Isolation.

### 2026-04-24 · DevContainer als primäre Dev-Umgebung
Ubuntu 24.04 + JDK 21 + Android SDK/NDK + CMake + protoc. ADB-Brücke über Host-Server. Identisch zur CI. Claude Code via nativen Installer (kein Node.js mehr nötig).

### 2026-04-24 · Agentisches Projektteam für Claude Code
Vier Rollen: Orchestrator (Planung und Fortschritt), Architekt (Architektur und Review), Entwickler (Implementierung und Tests), Dokumentierer (Docs-Pflege). Jeder als `.claude/agents/*.md`-Datei konfiguriert.

### 2026-04-25 · Feature-basiertes Permission-Modell
Kritische Permissions (`RECORD_AUDIO`, `CAMERA`, `FOREGROUND_SERVICE_MICROPHONE`-Nutzung, `USE_FULL_SCREEN_INTENT`) werden nicht beim App-Start, sondern beim Aktivieren des zugehörigen Features abgefragt. Ablehnung deaktiviert ausschließlich das betroffene Feature — der Kanal-Betrieb ohne Mikrofon (nur Zuhören) bleibt möglich. Der Foreground-Service wechselt seinen Typ zur Laufzeit zwischen `connectedDevice` (Standard-Empfang), `microphone` (VOX oder Hardware-PTT aktiv) und gestoppt. Konsequenz: neue Anforderung F-11 in `requirements.md`, Feature-zu-Permission-Matrix und `:service:lifecycle`-Modul in `architecture.md §11.2`, Settings-Unterabschnitt "Features und Berechtigungen" in `ui.md §7`.

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

## Offene Fragen

Keine zum aktuellen Zeitpunkt. Der Architekt-Agent pflegt diese Liste während der Implementierung.

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
