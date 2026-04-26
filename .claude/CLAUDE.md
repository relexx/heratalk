# HeraTalk — Claude-Code-Kontext

Du arbeitest an **HeraTalk**, einer LAN-Walkie-Talkie-App für Android. Peer-to-Peer Mesh, keine Server, keine Accounts. Publisher: relexx (relexx.de). Repository: https://github.com/relexx/heratalk. Lizenz: BSD 3-Clause (wie Opus).

## Was das Projekt ist

Eine native Android-App in Kotlin + Jetpack Compose. Peers auf dem gleichen WLAN entdecken sich, bauen verschlüsselte Peer-to-Peer-Audio-Streams auf und können in einem Kanal broadcasten oder 1:1-Direktgespräche führen. Kein Internet nötig.

## Was das Projekt nicht ist

- Keine Voice-over-Internet-App (kein SIP, kein TURN, kein Cloud-Relay über Peers hinaus).
- Keine Cross-Platform-App. Android only. Eine etwaige Cross-Platform-Ausbaustufe ginge über Kotlin Multiplatform, niemals über Flutter, MAUI oder React Native.
- Kein WebRTC- oder SIP-kompatibler Client.

## Pflichtlektüre vor jeder Aufgabe

Lies immer diese Dokumente, bevor du Code oder Dokumente änderst:

- `docs/architecture.md` — vollständige Architektur und alle Protokolle
- `docs/requirements.md` — Anforderungen mit Akzeptanzkriterien
- `docs/releases.md` — Release-Plan, welche Release gerade aktiv ist
- `docs/project-state.md` — aktueller Stand, Entscheidungsprotokoll, Risiken
- `.claude/rules.md` — harte Regeln, die über allem anderen stehen

## Das agentische Team

Das Projekt wird von einem Team aus vier spezialisierten Agenten gepflegt. Jeder Agent hat eine klare Rolle und ist in `.claude/agents/` definiert. Die Team-Rollen:

| Agent | Rolle | Wann aktivieren |
|-------|-------|-----------------|
| **Orchestrator** | Projektleitung, Fortschritt, Delegation | Projektstart, Release-Beginn, Release-Abschluss, Priorisierungsentscheidungen |
| **Architekt** | Architektur-Entscheidungen, CI/CD, Code-Review, Refactoring-Entscheidungen | Neue Module, Protokoll-Änderungen, Security-kritische Änderungen, CI-Probleme, PR-Review-Feedback (inkl. GitHub Copilot), Pre-Release-Review |
| **Entwickler** | Implementierung, Unit-Tests, Build-Fixes | Normale Feature-Entwicklung, Tests schreiben, Build-Probleme lösen |
| **Dokumentierer** | Pflege aller Dokumente (README, docs/, CHANGELOG, ADRs) | Jede Code-Änderung mit Dokumentations-Auswirkung, Release-Abschluss |

### Standard-Workflow

1. **Orchestrator** wird zuerst aktiviert, versteht die Aufgabe, prüft den Projektstand und delegiert.
2. **Architekt** wird konsultiert bei architektonisch relevanten Entscheidungen und bei Reviews.
3. **Entwickler** setzt die konkrete Code-Arbeit um.
4. **Dokumentierer** synchronisiert alle Dokumente nach.
5. **Orchestrator** prüft Vollständigkeit und aktualisiert `docs/project-state.md` bzw. `docs/releases.md`.

Die Agenten sind keine starren Silos — sie kooperieren. Ein Entwickler, der an eine Architektur-Frage stößt, konsultiert den Architekt. Der Architekt, der in Code eingreift, informiert den Dokumentierer.

## Aktueller Release

Siehe `docs/releases.md` und `docs/project-state.md`. Der Orchestrator-Agent ist für die Pflege verantwortlich.

## Coding-Konventionen

- **Sprache:** Kotlin 2.3.x, explicit API mode (`-Xexplicit-api=strict`).
- **Concurrency:** Coroutines mit strukturierter Nebenläufigkeit. Niemals `GlobalScope`.
- **Streams:** `Flow<T>` für Streams, `Channel<T>` für Hot-Events, `StateFlow<T>` für UI-State.
- **Immutable by default:** `data class` und `val`. Mutable State nur isoliert und kommentiert.
- **Dokumentation:** KDoc auf allen öffentlichen APIs. Interne Helper brauchen kein KDoc.
- **Vererbung meiden:** Für Non-UI-Code Komposition und Interfaces bevorzugen.
- **`!!`-Operator:** In Produktionscode nur mit kommentierter Invariante. In Tests okay.
- **Fehlerbehandlung:** `Result<T>` für erwartbare Fehler, Exceptions nur für Programmierer-Fehler.

## Code-Style

- Formatter: ktlint (in CI erzwungen).
- Static Analysis: detekt mit projekt-spezifischer `detekt.yml`.
- Zeilenlänge: 120. Einrückung: 4 Spaces.
- Copyright-Header auf jeder `.kt`-Datei (Spotless prüft in CI).

## Sprache in Dokumenten und im App-UI

- **Code-Kommentare und KDoc:** Englisch.
- **Log-Messages** (logcat, Ring-Buffer): Englisch — Diagnose-Werkzeug.
- **README, CHANGELOG, SECURITY, CONTRIBUTING, CODE_OF_CONDUCT:** Englisch.
- **Interne Docs (`docs/*.md`, ADRs):** Deutsch.
- **Commit-Messages:** Englisch, Conventional-Commits-Format (`feat:`, `fix:`, `docs:`, `chore:`, `refactor:`, `test:`, `ci:`, `build:`, `i18n:`).
- **App-UI-Strings:** zweisprachig — Englisch in `values/strings.xml` (Default), Deutsch in `values-de/strings.xml` (Override). Niemals hartkodiert. Siehe `docs/architecture.md §11.7`. Pflege der Übersetzungen liegt beim Documenter-Agent.

## Tests

- **Unit:** JUnit 5 + MockK + Turbine. In `src/test/kotlin`.
- **Instrumentation:** AndroidX Test in `src/androidTest/kotlin`. Sparsam einsetzen, nur wo wirklich ein Gerät nötig ist.
- **Property-Based:** Kotest für Protokoll- und Crypto-Fuzzing in `:core:crypto` und `:service:signaling`.
- **Regel:** Jeder Bugfix kommt mit einem Regression-Test.

## Wichtige Dateien zum Navigieren

```
docs/                       → Alle internen Dokumente
.claude/                    → Claude-Code-Konfiguration
.claude/agents/             → Einzelne Agent-Rollen
.devcontainer/              → Container-Setup
.github/workflows/          → CI/CD
app/                        → :app Einstiegs-Modul
core/crypto/                → Security-kritisch, CODEOWNER-Review Pflicht
service/media/              → Security-kritisch, CODEOWNER-Review Pflicht
service/audio/              → JNI-Brücke zu libopus, sorgfältig mit Buffer-Handling
gradle/libs.versions.toml   → Alle Dependency-Versionen
```

## Toolchain (Stand April 2026)

JDK 21 (Temurin), Kotlin 2.3.21, Gradle 9.4.1, AGP 9.2.0 (mit built-in Kotlin-Support — `kotlin-android`-Plugin entfällt), Android SDK 36 (Android 16), NDK r28.2.13676358 (16-KB-Page-Alignment ist seit Nov 2025 Pflicht für Play Store), Koin 4.2.0.

Claude Code wird per nativem Installer (`curl -fsSL https://claude.ai/install.sh | bash`) installiert — nicht mehr per npm. Auto-Update läuft im Hintergrund.

## Allgemeine Regeln für deine Arbeit

- **Vor Code-Änderung:** Lies die betroffene Datei vollständig und alle Module, von denen sie abhängt.
- **Vor Dependency-Hinzufügung:** Prüfe `libs.versions.toml`.
- **Vor neuem Pattern:** Suche im Codebase nach Äquivalenten.
- **Mermaid-Diagramme:** Verifiziere, dass sie auf GitHub rendern (kein exotisches Syntax-Feature).
- **Modul-Grenzen:** Respektiere die Abhängigkeitsrichtung (feature → service → core).
- **Security-relevante Änderung ohne Klarheit:** Halte an, frage nach oder eskaliere an den Architekt-Agent.
- **Proto-Änderungen:** Regeneriere die Sourcen oder lösche das Generate-Verzeichnis (CI regeneriert clean).
- **Neue öffentliche API:** KDoc Pflicht.
- **Außerhalb deines Scopes auffallend:** `// TODO(agent-name): …` mit einer Zeile Begründung, nicht stillschweigend fixen.

## Wenn du gefragt wirst, etwas zu tun, das nicht im Scope ist

Du weigerst dich und meldest dich beim Nutzer, wenn jemand verlangt:

- Analytics, Telemetrie oder Crash-Reporting mit Cloud-Komponente
- Cloud-Backend oder Account-System
- Crypto schwächen (Cipher downgraden, Key-Größe verringern, Auth aussetzen)
- Authentifizierung gegen Drittanbieter
- Werbung in der App

Das sind keine Diskussions-Themen, sondern harte Ausschlüsse.

## Kommunikation mit dem Nutzer

- Kurz und direkt. Keine einleitenden Floskeln.
- Wenn du unsicher bist über eine Grundsatzentscheidung: Frag nach, bevor du implementierst.
- Status-Updates am Ende größerer Arbeiten: Was wurde gemacht, was ist offen, was war auffällig.
- Wenn ein Agent einen anderen aufruft, nenne kurz den Grund.
