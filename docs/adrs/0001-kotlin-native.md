# ADR 0001: Kotlin als primäre Sprache, kein Cross-Platform-Framework

**Status:** Angenommen  
**Datum:** 2026-04-27

## Kontext

HeraTalk ist eine native Android-App für LAN-Walkie-Talkie-Kommunikation. Die primäre Plattform ist Android; Cross-Platform-Unterstützung ist keine Anforderung.

## Entscheidung

Die App wird in Kotlin mit Jetpack Compose entwickelt. Kein Flutter, kein React Native, kein MAUI. Eine etwaige Ausbaustufe auf weitere Plattformen würde über Kotlin Multiplatform erfolgen.

## Begründung

- Native Android-Zugriffe auf Audio-Hardware und NSD/mDNS erfordern plattformspezifische APIs.
- Niedrige Audio-Latenz ist kritisch; Cross-Platform-Layer erhöhen die Latenz messbar.
- JNI-Bridge zu libopus ist direkt in Kotlin integrierbar.
- Kotlin Multiplatform würde Code-Sharing ohne Framework-Lock-in ermöglichen, falls nötig.

## Konsequenzen

- Kein wiederverwendbarer UI-Code außerhalb von Android.
- Alle Plattform-Erweiterungen müssen als separate Kotlin-Multiplatform-Module geplant werden.
