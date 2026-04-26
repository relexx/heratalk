# HeraTalk — Sicherheitskonzept

> Detaillierte Beschreibung des Sicherheitsmodells, der Crypto-Protokolle und der Bedrohungslage. Ergänzt `docs/architecture.md §9`. Änderungen an diesem Dokument sind per CODEOWNERS-Regel reviewpflichtig.

## 1. Schutzziele

In absteigender Priorität:

1. **Vertraulichkeit aller Audio-Übertragungen.** Niemand außerhalb des Kanals kann Audio mithören — weder passive Angreifer im WLAN noch bösartige Peers in anderen Kanälen noch bösartige Relay-Peers.
2. **Integrität aller Übertragungen.** Pakete können nicht unbemerkt modifiziert werden; manipulierte Pakete werden verworfen.
3. **Authentizität der Peers.** Ein Peer, der im Kanal auftaucht, kennt das `channel_secret` — sonst ist kein Handshake möglich.
4. **Forward Secrecy (zeitlich begrenzt).** Kompromittierung des `channel_secret` zu einem späteren Zeitpunkt ermöglicht es nicht, zuvor aufgezeichnete Audio-Streams zu entschlüsseln, sofern die Session-Keys rotiert wurden.
5. **Datenschutz-by-Design.** Keine Telemetrie, keine Cloud-Komponente, keine persistent gespeicherten Audio-Inhalte.

Bewusst **nicht** abgebildet:

- **Schutz vor physischem Zugriff auf ein gepairtes Gerät.** Wer das entsperrte Gerät hat, hat den Kanal. Mitigation wäre Geräte-PIN und ggf. später eine App-interne PIN (out of scope MVP).
- **Deniable Authentication / Metadata-Privacy.** Ein Netzwerk-Beobachter sieht, *dass* HeraTalk läuft und zwischen welchen IPs. Nur *was* gesagt wird, bleibt geheim.
- **Schutz vor einem böswilligen Peer im selben Kanal.** Wer das `channel_secret` kennt, ist Mitglied — und kann alles hören und alles senden.
- **Schutz vor Traffic-Analyse.** Paketgrößen und -timing verraten, ob gerade gesprochen wird.

## 2. Bedrohungsmodell

Angreifer-Klassen:

| Klasse | Fähigkeiten | Bedrohung |
|--------|-------------|-----------|
| **Passiv-WLAN** | Liest allen WLAN-Traffic mit (z. B. Monitor-Mode) | Audio mithören → durch Verschlüsselung abgewehrt |
| **Aktiv-WLAN** | Kann Pakete injizieren, droppen, modifizieren | MITM, Replay → durch AEAD + Replay-Window abgewehrt |
| **Bösartiger AP** | Voller Netzwerk-Proxy | Wie aktiv-WLAN, plus Client-Isolation steuern → E2E-SRTP schützt Inhalte, Availability nicht garantiert |
| **Bösartiger Relay-Peer** | Nimmt als Relay teil, obwohl er nicht zum Kanal gehört — geht nicht, weil Relay-Teilnahme bereits Kanal-Mitgliedschaft erfordert | Kein zusätzliches Risiko |
| **Mitgliedschafts-Kompromittierung** | Angreifer erhält QR-Code oder extrahiert `channel_secret` | Voller Kanalzugang → Mitigation: Kanal neu erstellen, alle Nutzer neu pairen |
| **Lokales Malware auf einem Peer** | Malware auf einem HeraTalk-Gerät | Voller Zugriff → Out of scope, Android-Plattform-Schutz ist Voraussetzung |
| **Quantum-Computer-Angreifer** | Bricht ECDH zukünftig | PQ-Transition ist Forschungsthema für spätere Releases |

## 3. Pairing und Kanal-Erzeugung

### 3.1 Kanal-Erzeuger

Der erste Nutzer erzeugt einen Kanal:

1. 32 zufällige Bytes als `channel_secret` (via `SecureRandom` aus `AndroidKeyStore`).
2. 32 Bytes statischer X25519-Key (`static_privkey` + `static_pubkey`), generiert beim ersten App-Start, persistent im `AndroidKeyStore` (`KeyProperties.PURPOSE_AGREE_KEY` erforderlich ab Android 12; Fallback via BouncyCastle).
3. Display-Name wählen.
4. QR-Code generieren aus URI:
   ```
   heratalk://join?v=1&name=<urlencoded>&secret=<base64url(32 bytes)>&expires=<unix-ts>
   ```

Das `channel_secret` selbst ist der einzige Geheimnisträger im Pairing-Vorgang. Kein Server, keine Zwischeninstanz.

### 3.2 Kanal-Beitritt

1. Neuer Nutzer scannt QR-Code.
2. App parst URI, validiert `expires`, extrahiert `channel_secret`.
3. App verschlüsselt `channel_secret` mit einem aus dem `AndroidKeyStore` abgeleiteten Wrapping-Key (AES-256-GCM, hardware-backed wo verfügbar) und persistiert nur den verschlüsselten Wert.
4. App generiert eigenen `static_privkey`/`static_pubkey` oder nutzt den bereits vorhandenen.

### 3.3 Pairing-Sicherheit

**QR-Code-Übergabe:** Der QR-Code ist der kritischste Punkt. Wer ihn sieht oder fotografiert, kann dem Kanal beitreten.

- **Out-of-band-Übergabe empfehlen:** Face-to-face, kurz anzeigen, nicht als Screenshot teilen.
- **Zeitliche Begrenzung:** QR-Code enthält ein `expires`-Feld (Default 5 min Gültigkeit). Nach Ablauf wird beim Beitritt eine Warnung angezeigt, der Nutzer muss explizit bestätigen.
- **Rate-Limit:** Ein Peer akzeptiert nur einen Handshake pro Quelle pro 10 s, um Brute-Force-Versuche zu drosseln — relevant nur theoretisch, da 32 Bytes Entropie unangreifbar sind. Wichtiger: Rate-Limit gegen DoS.
- **FLAG_SECURE auf QR-Anzeige-Screen:** Verhindert Screenshots und Screen-Recording des QR-Codes durch andere Apps.

### 3.4 Wenn das `channel_secret` kompromittiert ist

Mitigation: Der Kanal-Gründer **muss einen neuen Kanal anlegen** und alle Mitglieder neu pairen. HeraTalk kennt im MVP keinen Kanal-Secret-Rotation-Mechanismus ohne Re-Pairing (das wäre eine Epoche-Management-Schicht à la MLS, Out of scope für MVP).

Für v1.x als Feature geplant: **Re-Key-Broadcast-Message** vom Kanal-Gründer, signiert mit seinem Static-Key, die ein neues `channel_secret` mitteilt, verschlüsselt pro aktiven Peer.

## 4. Handshake: Noise KKpsk0

### 4.1 Warum Noise KKpsk0

- **Authentifizierte Peers von beiden Seiten** (KK) — sowohl Initiator als auch Responder haben einen bekannten statischen Key, der vorab ausgetauscht wurde (über mDNS/Broadcast-Beacon).
- **PSK in der ersten Runde (psk0)** — das `channel_secret` als Pre-Shared-Key sichert zusätzlich ab, dass ausschließlich Kanal-Mitglieder handshaken können, selbst wenn Static-Keys geleakt wären.
- **Forward Secrecy** durch zusätzliche ephemere Schlüssel pro Session.
- **Keine Zertifikatshierarchie, keine CA, kein PKI-Overhead.**

Konkrete Profil-Wahl:

```
Noise_KKpsk0_25519_ChaChaPoly_SHA256
```

Bestandteile:
- `KK`: beide Seiten kennen den statischen Key des anderen vorab.
- `psk0`: Pre-Shared-Key wird vor der ersten Message eingemischt.
- `25519`: X25519 für Diffie-Hellman.
- `ChaChaPoly`: ChaCha20-Poly1305 als AEAD (passt gut auf mobile CPUs, schneller als AES-GCM ohne Hardware-AES).
- `SHA256`: für HKDF und Hashing.

### 4.2 Handshake-Ablauf

Zwei Messages (Minimum für KK):

```
Initiator → Responder: e, es, ss, [payload]
Responder → Initiator: e, ee, se, [payload]
```

- `e`: ephemerer Public Key des Senders.
- `es`, `se`, `ee`, `ss`: Diffie-Hellman-Operationen über bestimmte Key-Kombinationen.
- Der PSK wird vor der ersten Message in den Symmetric-State gemischt.

Am Ende haben beide Peers:
- `shared_secret_AB` (geheim, 32 Byte).
- `transcript_hash` (öffentlich, für Binding weiterer Operationen).

### 4.3 Identity-Pinning

- Der erste gesehene `static_pubkey` eines Peers wird gespeichert (**Trust on First Use**).
- Ändert sich der `static_pubkey` später (z. B. durch App-Neuinstallation auf demselben Gerät), wird der Peer als "neu" behandelt; der Nutzer bekommt eine UI-Warnung.
- Der `static_pubkey`-Fingerprint (erste 8 hex-Zeichen, z. B. `a7f3:2c91`) ist in den Peer-Details sichtbar, sodass Nutzer ihn bei Verdacht out-of-band vergleichen können.

Dies ist kein vollständiger Schutz gegen Angreifer, die das `channel_secret` haben — die könnten immer einen neuen Peer mit neuem Static-Key erzeugen. Aber es schützt gegen stille Austausch-Angriffe eines bereits bekannten Peers.

## 5. Schlüssel-Hierarchie

```
channel_secret (32 byte, QR-Code)
├─► channel_id_hash = SHA-256(channel_secret || "heratalk/id/v1")[:16]
│       └─► mDNS TXT "chan", Broadcast-Beacon-Filter
└─► psk = HKDF(channel_secret, salt=0, info="heratalk/psk/v1", len=32)
        └─► Noise KKpsk0

Noise-Handshake(A, B) → shared_secret_AB (32 byte)
├─► srtp_master_A→B_broadcast = HKDF(shared_secret_AB, info="heratalk/srtp/v1/broadcast/send", 32)
├─► srtp_master_A→B_direct    = HKDF(shared_secret_AB, info="heratalk/srtp/v1/direct/send",    32)
├─► srtp_master_B→A_broadcast = HKDF(shared_secret_AB, info="heratalk/srtp/v1/broadcast/recv", 32)
└─► srtp_master_B→A_direct    = HKDF(shared_secret_AB, info="heratalk/srtp/v1/direct/recv",    32)

Per master key (je Richtung und Stream-Typ):
├─► srtp_encryption_key = HKDF(master, info="srtp/enc", 32)
└─► srtp_salt           = HKDF(master, info="srtp/salt", 12)
```

**Domain-Separation durch HKDF-Info-Labels.** Broadcast- und Direct-Keys unterscheiden sich zwingend, selbst wenn derselbe Noise-Shared-Secret zugrunde liegt. Folge: Ein Replay oder Cross-Context-Missbrauch eines Broadcast-Pakets in einen Direct-Kontext schlägt bei der Entschlüsselung fehl.

**Versionierung in den Info-Strings** (`v1`). Sollte der KDF-Prozess jemals geändert werden müssen, bleibt Abwärtskompatibilität über bewusste Version-Negotiation möglich.

## 6. Medien-Verschlüsselung: SRTP mit AEAD

### 6.1 Abweichungen von RFC 3711

Wir implementieren eine **reduzierte SRTP-Variante** in `:core:crypto`, nicht die volle RFC-Spezifikation. Begründung: libsrtp-JNI bringt Komplexität und Abhängigkeiten, die wir nicht brauchen.

Unser Profil:

- **Kein Key-Derivation-Function per SRTP (RFC 3711 §4.3):** Wir leiten Keys über HKDF aus dem Noise-Shared-Secret ab. RFC-KDF ist obsolet.
- **Kein SRTCP:** Wir nutzen RTP nicht mit Sender Reports etc. Eigene RTCP-ähnliche Messages laufen im Control-Plane (TCP + Noise).
- **Keine MKI (Master Key Identifier):** Der SSRC identifiziert implizit den Key (über den Stream-Typ).
- **AEAD (ChaCha20-Poly1305 oder AES-128-GCM)** statt AES-CTR + HMAC. Referenz: RFC 7714 für AES-GCM-SRTP, analog für ChaChaPoly.
- **Replay-Window: 128 Pakete** (RFC 3711 Default ist 64 — wir sind etwas großzügiger wegen App-Level-Frame-Duplikation).

### 6.2 Paket-Format

```
RTP Header (12 byte, in AAD)
├─ Version = 2
├─ Payload Type = 111 (Opus, dynamisch)
├─ Sequence Number (16 bit, rolling)
├─ Timestamp (32 bit, 48 kHz clock)
└─ SSRC (32 bit, Stream-Type | Short-Peer-ID)

Encrypted Payload (Opus-Frame, N bytes)

Auth Tag (16 byte, Poly1305)
```

Der 12-Byte-RTP-Header wird **nicht** verschlüsselt, aber in die AEAD-AAD einbezogen. Manipulation am Header (z. B. SSRC-Spoof, Sequence-Rollback) schlägt deshalb beim Tag-Check fehl.

### 6.3 Nonce-Konstruktion

Nonce = `salt XOR (SSRC || rollover_counter || sequence_number)` (12 byte, nach RFC 7714).

- `rollover_counter` erhöht sich, wenn die 16-Bit-Sequence-Number umklappt.
- Nonce-Reuse ist katastrophal für AEAD-Sicherheit. Folge: Rekey **zwingend** bevor `(rollover_counter, sequence_number)` wrapt. Wir rekeyen konservativ bei 2³¹ Paketen oder nach 24 Stunden.

### 6.4 Replay-Protection

Sliding Window von 128 Paketen pro (SSRC, Sender). Ein Paket mit Sequence-Number unterhalb des Windows wird verworfen. Ein Paket oberhalb wird akzeptiert und markiert. Ein Paket im Window wird nur akzeptiert, wenn das Bit noch frei ist.

Wichtig: Window wird **pro Stream-Type** und **pro Sender-Peer** separat geführt. Ein Broadcast-Replay kann nicht ein Direct-Paket überdecken.

### 6.5 Rekey-Prozess

- Trigger: 24 h Session-Dauer oder 2³¹ Pakete (was früher kommt).
- Neuer Noise-Handshake für volle Forward Secrecy.
- Übergang per `SrtpRekey`-Message: enthält neue Generation-Nummer und ab welcher Sequence-Number die neuen Keys gelten.
- Alte Keys werden 5 Sekunden Grace-Period weiter akzeptiert, um in-flight-Pakete nicht zu verlieren.

## 7. Relay-Mode: E2E-Garantie trotz drittem Peer

### 7.1 Was der Relay-Peer R sieht

- Den vollständigen **Outer-Relay-Frame**: Magic, SrcPeerId, DstPeerId, InnerType, InnerLen.
- Die **verschlüsselte Inner-Payload** (SRTP-Frame von A für B).
- Die Paketgröße und das Timing (→ Traffic-Analyse möglich).

### 7.2 Was der Relay-Peer R nicht sehen kann

- Die entschlüsselte Audio-Payload. Seine Noise-Handshakes mit A und B ergeben *andere* Shared-Secrets als die zwischen A und B.
- Die SRTP-Keys von A↔B. Diese werden aus dem direkten Noise-Handshake zwischen A und B abgeleitet und niemals an R übertragen.

### 7.3 Angriffsvektoren und Antworten

- **R droppt Pakete:** Availability leidet, aber keine Vertraulichkeitsverletzung. A erkennt das über Sequence-Lücken und fehlende RTCP-ähnliche Reports im Control-Plane → wählt ggf. anderen Relay.
- **R ändert Payload:** SRTP-Auth-Tag schlägt bei B fehl → Paket wird verworfen.
- **R injiziert eigene Pakete:** Ohne gültigen SRTP-Key kann R keine valid authentifizierten Pakete erzeugen → Tag-Check bei B schlägt fehl.
- **R ändert Outer-Frame:** Der Outer-Header ist nicht authentifiziert. R könnte `DstPeerId` ändern und das Paket an C statt an B weiterleiten. Konsequenz: C versucht zu entschlüsseln, scheitert am Auth-Tag (C hat nicht die A↔B-Keys), verwirft das Paket. Keine Verletzung.
- **R korreliert Absender-/Empfänger-Peer-IDs:** Metadaten-Leak, aber Inhalte bleiben geheim.

### 7.4 Relay-Authorisierung

Damit nicht jeder Peer sich als Relay aufspielen und Metadaten sammeln kann:

- Ein Peer akzeptiert einen Relay-Angebot nur von Peers, mit denen er erfolgreich im selben Kanal handgeshaked hat.
- Ein Relay-Peer R muss selbst Mitglied des Kanals sein. Ein Angreifer ohne `channel_secret` kann nicht als Relay teilnehmen.

## 8. Speicherung sensibler Daten

| Daten | Speicherort | Schutz |
|-------|-------------|--------|
| `channel_secret` | `DataStore` verschlüsselt mit `AndroidKeyStore`-wrapped AES-256-GCM | Hardware-backed (falls verfügbar, sonst Software) |
| Eigener `static_privkey` | `AndroidKeyStore` direkt (`KeyProperties.PURPOSE_AGREE_KEY`) | Nie aus Keystore exportierbar auf modernen Geräten |
| Peer-`static_pubkey`s (für TOFU) | `DataStore` | Öffentliche Daten — keine Geheimhaltung nötig, aber Integrität durch Datei-MAC |
| Session-Keys (SRTP) | Nur im Prozess-RAM | Werden beim Service-Stop mit 0x00 überschrieben |
| Audio-Buffer | Nur im RAM, Ring-Buffer | Werden nie persistiert |

**Backup-Ausschluss:** `android:allowBackup="false"` im Manifest, zusätzlich `android:fullBackupContent` mit Ausschlussliste. Verhindert, dass `channel_secret`-Wrapping-Keys via Android Backup in die Google-Cloud wandern.

## 9. Logging-Disziplin

In `:core:logging` wird die Logging-Fassade so definiert, dass sicherheitskritische Werte **systematisch unsichtbar** sind:

```kotlin
public fun logger(tag: String): Logger
public class Logger {
    public fun d(msg: String, vararg args: LogArg)
    public fun w(msg: String, vararg args: LogArg)
    public fun e(msg: String, throwable: Throwable?, vararg args: LogArg)
}

// LogArg erlaubt nur typsichere, safe-by-construction Werte:
public sealed class LogArg {
    public data class Peer(val shortId: Int) : LogArg()       // erlaubt
    public data class Count(val value: Int) : LogArg()        // erlaubt
    public data class Redacted(val label: String) : LogArg()  // zeigt "[redacted:label]"
    // KEIN LogArg.Raw(String). Keine "Key"- oder "Secret"-Variante.
}
```

Folge: Niemand kann auf direktem Weg einen Key loggen. detekt ergänzt das mit einer Custom-Regel, die String-Templates mit Variablennamen `*key*`, `*secret*`, `*psk*`, `*nonce*` in `logger.d()`-Aufrufen markiert.

Zusätzlich: **Kein Logging-Backend, das nach außen exportiert.** Logs gehen nur an `logcat` und einen In-App-Ring-Buffer. Es gibt keinen automatischen Upload, kein Crash-Reporter mit Log-Anhang, keinen "Send feedback"-Button, der Logs verschickt. Der Nutzer kann manuell Logs als Datei über den Android-Share-Sheet ausgeben — dann mit expliziter Zustimmung.

## 10. Dependencies und Supply Chain

- **Alle Dependencies gepinnt** in `gradle/libs.versions.toml` — keine offenen Ranges.
- **Dependency-Verification** aktiviert: Gradle überprüft beim Download SHA-256-Hashes gegen eine committete `gradle/verification-metadata.xml`.
- **Dependabot** läuft wöchentlich und erzeugt Update-PRs, die nur nach Architect-Review gemerged werden.
- **CodeQL** scannt wöchentlich auf Security-Issues in eigenem Code und in deklarierten Dependencies.
- **Geschlossene Crypto-Lib-Liste:** Nur `java.security` (JCE) und BouncyCastle für Primitives, Noise-Java für den Handshake. Keine anderen Crypto-Libraries ohne ADR.

## 11. Build-Integrität

- **Debug-Keystore:** versioniert als `debug.keystore` im Repo. Bewusst kein Geheimnis.
- **Release-Keystore:** nur in GitHub Actions Secrets. Base64-kodiert. Niemals im Repo.
- **Reproducible Builds:** Ziel, aber nicht hart durchgesetzt bis v1.0.0. Voraussetzung: gepinnte SDK/NDK-Versionen (bereits erfüllt), reproducible Kotlin-Compilation.
- **APK-Signatur-Verifikation:** GitHub-Release-Artefakt trägt SHA-256-Summe in den Release-Notes; ab v1.0.0 auch SLSA-Provenance-Attestation via GitHub-Actions.

## 12. Restrisiken und offene Punkte

| Risiko | Status | Plan |
|--------|--------|------|
| Kein Schutz vor physischem Zugriff auf entsperrtes Gerät | akzeptiert | App-interne PIN post-v1.0 überlegen |
| Traffic-Analyse verrät Sprech-Aktivität | akzeptiert | Konstantes-Rate-Dummy-Traffic wäre Option, aber Battery-Impact zu hoch |
| `channel_secret`-Kompromittierung erfordert Re-Pairing | akzeptiert für MVP | MLS-artiges Group-Rekey in v1.x evaluieren |
| Keine Post-Quantum-Sicherheit | akzeptiert | X25519 → X-Wing-Hybrid in v2.x |
| QR-Code kann fotografiert werden | mitigiert durch `FLAG_SECURE` und `expires` | — |
| Bluetooth-Headset-Audio-Pfad ist geräteabhängig | Plattform-Problem | Klare UI-Kommunikation |

## 13. Audit-Ergebnisse

Das vollständige Sicherheits-Audit dieses und aller anderen Projektdokumente liegt in `docs/security-audit.md`. Dort werden Findings mit Severity und Remediation-Status geführt.
