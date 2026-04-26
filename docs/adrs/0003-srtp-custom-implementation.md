# ADR 0003: Eigene SRTP-Implementierung statt WebRTC

**Status:** Angenommen  
**Datum:** 2026-04-27

## Kontext

Die Audio-Streams müssen verschlüsselt übertragen werden. Optionen: WebRTC-Bibliothek (mit eingebautem SRTP), eigene SRTP-Implementierung über BouncyCastle, oder unverschlüsseltes RTP.

## Entscheidung

Wir implementieren SRTP selbst über BouncyCastle für AES-GCM. WebRTC wird **nicht** verwendet.

## Begründung

- WebRTC setzt STUN/TURN-Server voraus und bringt umfangreiche Abhängigkeiten mit.
- HeraTalk funktioniert ausschließlich im LAN — WebRTC-Infra ist überflüssiger Overhead.
- Eine schlanke SRTP-Implementierung gibt uns volle Kontrolle über die Krypto-Parameter.
- BouncyCastle ist gut gepflegt und bietet JDK 18+-kompatible APIs.

## Konsequenzen

- Die SRTP-Implementierung liegt in `:service:media` — CODEOWNER-Review und Security-Audit Pflicht.
- Kein Interop mit WebRTC-Clients.
- Property-Based-Tests in `:core:crypto` fuzzen Cipher und SRTP-Header.
