# ADR 0002: Noise Protocol Framework für Peer-Authentifizierung und Schlüsselaustausch

**Status:** Angenommen  
**Datum:** 2026-04-27

## Kontext

Peers müssen sich gegenseitig authentifizieren und einen gemeinsamen Sitzungsschlüssel aushandeln, ohne eine zentrale Infrastruktur. Der Kanal muss gegen Man-in-the-Middle-Angriffe abgesichert sein.

## Entscheidung

Wir verwenden das **Noise Protocol Framework** (Muster: `Noise_XX_25519_AESGCM_SHA256`) für den initialen Handshake bei der Geräte-Kopplung und für die Kanalaufnahme. Die Implementierung basiert auf `noise-java` (rweather) oder einer entsprechenden vendored Variante.

## Begründung

- Noise ist formal analysiert und bietet Forward Secrecy.
- Kein zentrales PKI notwendig — passt zum Server-freien Ansatz.
- Einfacher als TLS für Peer-to-Peer-Szenarien.
- Unterstützt Mutual Authentication ohne vorab registrierte Identitäten.

## Konsequenzen

- Kopplung erfolgt via QR-Code oder direktem Nahfeld-Austausch.
- Kein kommerzielles TLS-Zertifikat nötig.
- Implementierung liegt in `:core:crypto` — CODEOWNER-Review Pflicht.
