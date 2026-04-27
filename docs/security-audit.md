# HeraTalk — Sicherheits-Audit

> Systematische Prüfung aller Projektdokumente auf bisher unaufgedeckte Sicherheitsaspekte. Stand: 2026-04-24, Audit-Runde 1.

## 1. Audit-Methodik

Geprüft wurde gegen folgende Kategorien:

- **Kryptografische Korrektheit** (Schlüssel-Erzeugung, Domain-Separation, Nonce-Handling, Replay-Protection)
- **Pairing- und Trust-Establishment** (QR-Code-Verteilung, TOFU-Modell, Re-Pairing)
- **Netzwerk-Angriffsoberfläche** (Discovery, Control-Plane, Data-Plane, Relay)
- **Mobile-Plattform-Spezifika** (Permissions, Inter-Process-Communication, Storage, Backup)
- **Side-Channels** (Logging, Timing, Traffic-Analyse, UI-Leaks)
- **Supply-Chain & Build** (Dependency-Pinning, Reproducible Builds, Signatur)
- **Operative Sicherheit** (Update-Mechanismus, Vulnerability-Reporting, Privilege-Eskalation)
- **Datenschutz** (Datensparsamkeit, Backup-Verhalten, Telemetrie-Vermeidung)

Severity-Skala: **Critical** (sofortige Action), **High** (vor v1.0.0 fixen), **Medium** (in passendem Release adressieren), **Low** (dokumentieren oder akzeptieren).

## 2. Findings — Kryptografische Aspekte

### F-CRYPTO-01 · TOFU-Schutz vor Static-Key-Substitution unzureichend dokumentiert · High

**Befund:** `docs/security.md §4.3` erwähnt Trust-on-First-Use-Pinning für Peer-Static-Keys, aber das ursprüngliche Design beschrieb keine UX für Key-Wechsel. Ein Angreifer mit `channel_secret` könnte einen neuen Static-Key generieren und sich als ein bekannter Peer ausgeben — der Nutzer würde es nur dann bemerken, wenn die UI ihn explizit warnt.

**Mitigation (jetzt eingearbeitet):** `docs/security.md §4.3` zeigt nun den Fingerprint des Static-Keys (z. B. `a7f3:2c91`) in den Peer-Details. Bei Key-Wechsel erscheint Banner: "Anna's Schlüssel hat sich geändert. Vergleicht den Fingerprint außerhalb der App."

**Remediation:** Für v0.5.0 als Akzeptanzkriterium aufnehmen: Peer-Detail-Screen zeigt Fingerprint, Key-Wechsel löst non-dismissible-UI-Warning aus.

**Status:** ⚠ Open — Action für Architect, Aufnahme in `docs/requirements.md` als neue Anforderung F-10.

### F-CRYPTO-02 · Channel-Secret-Wrapping ohne Key-Attestation · Medium

**Befund:** `docs/security.md §8` schreibt: `channel_secret` wird mit AES-256-GCM verschlüsselt persistiert, der Wrapping-Key kommt aus dem `AndroidKeyStore`. Auf Geräten ohne Hardware-Keystore (Emulator, sehr alte Geräte) fällt das Software-backed zurück — und der Software-Keystore kann mit Root-Zugriff extrahiert werden. Ein Angreifer mit Root-Privileg auf dem Gerät kann das `channel_secret` lesen.

**Mitigation:**
1. `KeyInfo.isInsideSecureHardware()` prüfen und Nutzer warnen, wenn der Keystore nur Software-backed ist.
2. App-interne PIN als zusätzlicher Faktor (post-MVP, akzeptiert).
3. Klare Threat-Model-Aussage: "Schutz vor lokalem Malware ist out of scope" — bereits in `security.md §1`.

**Status:** ✓ Akzeptiert für MVP. Warning-UI auf Software-Keystore wird in v0.5.0 ergänzt (Aufgabe an Architect zur ADR-Erstellung).

### F-CRYPTO-03 · QR-Code-Lebensdauer ursprünglich unbegrenzt · High → Fixed

**Befund:** Erste Architektur-Version sah keine Ablaufzeit auf dem QR-Code vor. Wer einen alten QR-Code findet (z. B. Foto in der Galerie), kann beliebig später dem Kanal beitreten.

**Mitigation (eingearbeitet):** QR-Code enthält jetzt `expires`-Feld mit 5 min Gültigkeit (`docs/security.md §3.1`). Beim Scan wird `expires` geprüft, bei Ablauf erscheint Warnung mit explizitem Override.

**Status:** ✓ Fixed in `docs/security.md §3.1` und `docs/ui.md §5`. Aufnahme in `docs/requirements.md` als F-02-Verfeinerung.

### F-CRYPTO-04 · Domain-Separation ohne Versionierung war fragil · Medium → Fixed

**Befund:** Erste HKDF-Info-Strings waren `srtp/broadcast/send` etc. — ohne Versionsmarker. Eine spätere Änderung des KDF-Prozesses hätte zu silent failures bei Mischbetrieb verschiedener Versionen geführt.

**Mitigation (eingearbeitet):** Info-Strings tragen jetzt `heratalk/srtp/v1/...` Präfix (`docs/security.md §5`). Versions-Bump erfordert explizite Negotiation und ADR.

**Status:** ✓ Fixed.

### F-CRYPTO-05 · Replay-Window-Größe vs. App-Level-FEC · Low

**Befund:** Architektur §10.1 erlaubt App-Level-Frame-Duplikation: Frame n-1 reist in Frame n mit. Wenn ein Empfänger das Original-Frame n-1 *und* die Kopie aus n empfängt, sieht er zwei Pakete mit derselben Sequence-Number — das Replay-Window meldet das zweite als Replay und verwirft es. Das ist korrekt, kostet aber CPU für die fehlgeschlagene Authentifizierung.

**Mitigation:** Replay-Window auf 128 erhöht (`docs/security.md §6.4`). App-Level-Duplikate werden erkannt, bevor SRTP-Decode startet (Vergleich der äußeren Sequence-Number-Range).

**Status:** ✓ Akzeptiert, dokumentiert.

## 3. Findings — Pairing und Trust

### F-PAIR-01 · QR-Code-Anzeige ohne FLAG_SECURE · High → Fixed

**Befund:** Ein QR-Code-Anzeige-Screen ohne `WindowManager.LayoutParams.FLAG_SECURE` erlaubt anderen Apps mit Screen-Capture-Rechten oder System-Screenshots, das `channel_secret` zu erfassen. Beispiele: Bildschirmaufnahme-Apps, Backup-Tools, einige Launcher mit Screenshot-Galerie.

**Mitigation (eingearbeitet):** `docs/security.md §3.3` und `docs/ui.md §5` schreiben `FLAG_SECURE` auf dem QR-Anzeige-Screen vor. System-UI zeigt im Recents-Switcher dann nur einen schwarzen Platzhalter.

**Status:** ✓ Fixed. Aufnahme in `docs/requirements.md` als F-02-Verfeinerung.

### F-PAIR-02 · Kein Re-Key-Mechanismus für kompromittierten Kanal · Medium

**Befund:** Wenn das `channel_secret` einmal kompromittiert ist, ist der einzige Weg ein komplett neuer Kanal mit Re-Pairing aller Mitglieder. Für eine produktive Arbeitsgruppe (z. B. Werkstatt mit 8 Geräten) ist das unbequem genug, dass Nutzer den Vorfall ignorieren könnten.

**Mitigation:** MLS-artiges Group-Rekey für v1.x dokumentiert (`docs/security.md §3.4`). Akzeptierte Limitation für MVP.

**Status:** ⚠ Akzeptiert für MVP, geplant für post-v1.0.

### F-PAIR-03 · QR-Code in URL-Schemes verarbeitet — Path Traversal? · Low

**Befund:** Die App registriert ein URL-Schema `heratalk://join?...`. Wenn ein Angreifer den Nutzer auf eine Webseite mit `<a href="heratalk://join?...">` lockt und der Nutzer den Link tippt, könnte er ungewollt einem Kanal beitreten.

**Mitigation:** Der Beitritts-Vorgang **muss** den Nutzer in einem Confirmation-Dialog explizit fragen ("Möchtest du dem Kanal *Foo* beitreten?"). Niemals automatisch beitreten. Plus: keine Filesystem-Operationen mit der URL-Payload.

**Status:** ⚠ Open — Aufnahme in `docs/ui.md` als verpflichtender Confirmation-Dialog vor Beitritt.

## 4. Findings — Netzwerk-Angriffsoberfläche

### F-NET-01 · Discovery-Beacon ist nicht authentifiziert · Medium

**Befund:** Der UDP-Broadcast-Beacon (`docs/architecture.md §6.1 Stufe 2`) enthält Static-PK, aber keine Signatur. Ein Angreifer im LAN könnte gefälschte Beacons mit fremden Static-Keys senden. Folge: Der Empfänger versucht den Handshake, scheitert (Angreifer hat den privaten Schlüssel nicht), und droppt den Beacon. Kein Vertraulichkeitsverlust, aber Resource-Erschöpfung möglich (Handshake-Floods).

**Mitigation:**
1. Rate-Limit: pro Source-IP max. 5 Handshake-Attempts/Minute.
2. Beacon-Filter: nur Beacons mit `channel_id_hash` matching unserem Kanal werden überhaupt verarbeitet.
3. Optional: Beacon-Body mit MAC unter dem `psk` signieren — zusätzliche CPU-Kosten, aber deutlich schwerer zu spammen.

**Status:** ⚠ Open — Mitigation 1 und 2 sind implementierungs-Pflicht, Mitigation 3 als ADR-Entscheidung für v0.9.0 (Robustheit-Release).

### F-NET-02 · Amplifikations-Risiko durch UDP-Broadcast-Antworten · Low

**Befund:** Wenn ein Peer auf jeden empfangenen Broadcast-Beacon mit einem Direct-UDP-`TransportProbe` antwortet, könnte ein Angreifer mit gespooften Source-IPs Broadcast-Beacons senden und die Antworten auf ein Opfer lenken.

**Mitigation:** `TransportProbe` antwortet **nicht** auf Beacons, sondern erst nachdem über TCP+Noise authentifiziert wurde. Das macht Amplifikation unmöglich.

**Status:** ✓ Akzeptiert, durch Architektur-Sequence (Beacon → Resolve → TCP+Noise → erst dann UDP-Probe) bereits ausgeschlossen. Wird in `docs/architecture.md §7.2` zur Klarheit explizit ergänzt.

### F-NET-03 · TCP-Control-Plane ohne SO_LINGER kann Half-Close-DoS auslösen · Low

**Befund:** Ein Angreifer könnte viele halb-offene TCP-Verbindungen aufbauen (TCP-SYN ohne Handshake-Abschluss) und so File-Descriptor-Erschöpfung auf einem Peer verursachen.

**Mitigation:**
1. `SO_LINGER` mit kurzem Timeout setzen.
2. Maximum-concurrent-pending-handshakes pro Peer (z. B. 16) — alles darüber wird gedroppt.
3. Bei Noise-Handshake-Timeout (5 s) Verbindung schließen.

**Status:** ⚠ Open — Mitigationen sind Implementierungs-Detail für v0.5.0 (Control-Plane-Release).

### F-NET-04 · Relay-Loops nicht ausgeschlossen · Medium

**Befund:** Wenn Peer R Pakete für A↔B relayed, und gleichzeitig Peer R' Pakete für R↔C relayed, kann es theoretisch zu Routing-Loops kommen. Beispiel: A schickt an B via R, R erkennt Pfad nicht, sendet via R'... das endet mit Paket-Verstärkung.

**Mitigation:**
1. Outer-Relay-Frame trägt einen TTL-Counter (max. 1: ein Hop, kein doppelter Relay).
2. Ein Peer akzeptiert nur Relay-Pakete für direkt erreichbare Ziele — keine Kettenbildung.

**Status:** ⚠ Open — wichtig für v0.10.0 (Relay-Mode-Release). Aufnahme in `docs/architecture.md §7.5`.

### F-NET-05 · IP-Adress-Leak via Discovery · Low

**Befund:** mDNS und UDP-Broadcast verraten die IP des Geräts an alle im LAN — auch an Geräte, die nicht zum Kanal gehören. Klassische Datenschutz-vs-Funktionalität-Abwägung.

**Mitigation:** Akzeptiert. Im LAN ist die IP ohnehin sichtbar; mDNS ist Standardverhalten von ChromeCasts, Druckern etc.

**Status:** ✓ Akzeptiert, dokumentiert in `docs/security.md §1` ("Metadata-Privacy out of scope").

## 5. Findings — Mobile-Plattform-Spezifika

### F-MOB-01 · `allowBackup="false"` nicht explizit gesetzt · High → Fixed

**Befund:** Default-Wert für `android:allowBackup` ist auf API < 31 `true`. Das würde bedeuten: Android-Auto-Backup könnte das verschlüsselte `channel_secret` und den App-Status in die Google-Cloud sichern. Der Wrapping-Key liegt zwar im KeyStore und wandert nicht mit, aber der Backup würde unnötigen Daten-Footprint erzeugen.

**Mitigation (eingearbeitet):** `android:allowBackup="false"` ist jetzt in `docs/security.md §8` Pflicht. Zusätzlich `android:fullBackupContent` als Defense-in-Depth.

**Status:** ✓ Fixed. Wird Manifest-Anforderung in v0.1.0.

### F-MOB-02 · `FLAG_ACTIVITY_NEW_TASK`-Hijacking durch andere Apps · Low

**Befund:** Wenn HeraTalk Activity ohne `taskAffinity=""` und `excludeFromRecents="true"` definiert ist, könnte eine andere App den Task hijacken und dem Nutzer eine täuschend ähnliche UI vorsetzen.

**Mitigation:** Activity-Konfiguration im Manifest:
```xml
<activity
    android:name=".MainActivity"
    android:taskAffinity=""
    android:excludeFromRecents="false"
    android:launchMode="singleTask"
    android:exported="true">
```

**Status:** ⚠ Open — implementierungs-Pflicht für v0.1.0.

### F-MOB-03 · Implicit Intents könnten von anderen Apps abgefangen werden · Low

**Befund:** Wenn HeraTalk implicit Intents nutzt (z. B. zum Starten der Kamera für QR-Scan), könnten bösartige Apps diese abfangen.

**Mitigation:** ML Kit Barcode hat in-process API, kein Intent nötig. Wir vermeiden implicit Intents grundsätzlich.

**Status:** ✓ Architektur-bedingt nicht relevant.

### F-MOB-04 · Permission-Overreach durch Early-Request · Medium → Fixed

**Befund:** Frühe Version des Manifests listete alle Permissions, die die App *jemals* brauchen könnte — `CAMERA`, `RECORD_AUDIO`, `FOREGROUND_SERVICE_MICROPHONE`, `BLUETOOTH_CONNECT`, `USE_FULL_SCREEN_INTENT` — als gleichwertig. Folge für den Nutzer: Schon beim ersten App-Start oder spätestens beim ersten Kanal-Beitritt Anhäufung von Permission-Dialogen ohne nachvollziehbaren Grund. Das schlägt Vertrauen, und ein sicherheitsbewusster Nutzer lehnt alles ab — danach funktioniert die App nicht mehr wie erwartet. Gleichzeitig verstößt es gegen das Prinzip der minimalen Privilegien: Ein Kanal-Mitglied, das nur passiv zuhört, braucht weder Mikrofon noch Kamera.

**Mitigation (eingearbeitet):** Feature-basiertes Permission-Modell (`docs/requirements.md F-11`, `docs/architecture.md §11.2`, `docs/ui.md §8.2`). Jede Permission ist einem konkreten Feature zugeordnet; die Permission-Abfrage erfolgt ausschließlich beim ersten Aktivieren des Features. Der Foreground-Service wechselt seinen Typ dynamisch zwischen `connectedDevice` (Default, kein Mikrofon) und `microphone` (nur wenn VOX oder Hardware-PTT aktiv ist). Nutzer, die nur zuhören wollen, brauchen gar keine sensitive Runtime-Permission außer `POST_NOTIFICATIONS`.

**Status:** ✓ Fixed. Implementierungs-Pflicht in v0.1.0 (Manifest und Erstnutzer-Flow), v0.3.0 (RECORD_AUDIO beim PTT), v0.7.0 (VOX-Toggle), v1.0.0 (Hardware-PTT-Toggle).

### F-MOB-05 · Foreground-Service-Typ-Lock missbrauchbar bei Service-Restart · Low

**Befund:** Wenn der `microphone`-Foreground-Service im Hintergrund neu gestartet wird (z. B. nach einem Crash), könnte er auf manchen Geräten ohne sichtbare Activity laufen. Android 14+ verbietet das eigentlich, aber Edge-Cases existieren.

**Mitigation:**
1. Service-Restart-Policy: niemals automatisch nach Crash neu starten (`START_NOT_STICKY`).
2. Wenn Audio-Engine ausfällt, App-UI zeigt fehler-State und Nutzer muss manuell "Wiederverbinden" tippen.

**Status:** ⚠ Open — Implementierungs-Detail für v0.1.0/v0.3.0.

## 6. Findings — Side-Channels

### F-SIDE-01 · Logging-Disziplin nur partiell strukturell durchgesetzt · Medium → Fixed

**Befund:** Erste Version der Regeln ("Logs müssen niemals Secrets enthalten") war eine textuelle Regel ohne strukturelle Unterstützung. Ein neuer Entwickler könnte sie ohne böse Absicht verletzen.

**Mitigation (eingearbeitet):** `docs/security.md §9` definiert eine `LogArg`-Sealed-Class, die *strukturell* verhindert, beliebige Strings zu loggen. Plus detekt-Regel gegen verdächtige Variablennamen in String-Templates.

**Status:** ✓ Fixed konzeptionell. Implementierungs-Pflicht in v0.1.0 (`:core:logging`).

### F-SIDE-02 · Timing-Side-Channels in SRTP-Auth-Tag-Vergleich · Medium

**Befund:** Eine naive Implementierung von Tag-Vergleich (`tag1.contentEquals(tag2)` mit Early-Return) leakt durch Timing, wo der Unterschied zuerst auftritt — angreifbar für Tag-Forgery.

**Mitigation:** Konstantzeit-Vergleich (`MessageDigest.isEqual(tag1, tag2)` in JCE — ist konstantzeit-implementiert). Verbindlich in `:core:crypto`. ADR 0003 muss das festhalten.

**Status:** ⚠ Open — verbindliche Regel in `.claude/rules.md` und ADR 0003.

### F-SIDE-03 · Audio-Pegel-Anzeige (RMS) verrät Content · Low

**Befund:** Die Live-RMS-Bar in der UI (siehe `docs/ui.md`) zeigt sehr genau, wie laut der Sender spricht. Wenn ein Angreifer den Bildschirm sieht oder in einer Diagnose-Logging-Variante landet, könnte er Sprach-Aktivität rekonstruieren — aber nicht Inhalt.

**Mitigation:** Akzeptiert. RMS-Daten werden nicht in Logs geschrieben. UI-Anzeige nur on-screen, nicht in Recents-Thumbnails (durch `FLAG_SECURE` auf Sensitive-Screens — aktuell nur QR-Anzeige; sollten wir auf Direct-Call-Screen ausweiten? Nein, RMS verrät keinen Inhalt).

**Status:** ✓ Akzeptiert.

### F-SIDE-04 · Traffic-Analyse durch Variable-Bitrate-Opus · Medium

**Befund:** Opus mit VBR (variable bitrate) erzeugt Pakete unterschiedlicher Größe abhängig vom Audio-Inhalt — Phoneme können theoretisch anhand von Paketgrößen-Patterns rekonstruiert werden. Forschung dazu existiert (Wright et al. 2008 für Skype VBR).

**Mitigation:**
1. **CBR (Constant Bitrate) statt VBR** verbindlich für HeraTalk. Opus unterstützt das, kostet leicht Audio-Qualität, aber schließt diesen Side-Channel.
2. Padding auf eine feste Frame-Größe pro Bitrate-Stufe.

**Status:** ⚠ Open — wichtige Härtung. Aufnahme in `docs/architecture.md §10.1` als Pflicht-Setting für Opus-Encoder. Update in `docs/security.md §1` (war als "akzeptiertes Risiko" für Traffic-Analyse markiert; CBR ist machbar und sollte gemacht werden).

### F-SIDE-05 · DTX verrät Sprech-Pausen perfekt · Low

**Befund:** Discontinuous Transmission sendet während Stille keine Frames — das ist eindeutig im Traffic-Pattern erkennbar. Ein Angreifer kann mit Bitrate-Sekunden-Auflösung sehen, wann jemand spricht.

**Mitigation:** Akzeptiert. Wer Sprech-Aktivität verbergen will, muss DTX abschalten — Battery-Kosten. Als Settings-Option dokumentieren ("Privacy-Modus: konstante Sendung", off-by-default).

**Status:** ⚠ Akzeptiert für MVP, Settings-Option für v1.0.0.

## 7. Findings — Supply-Chain & Build

### F-SUPPLY-01 · Gradle-Dependency-Verification nicht im Default · High → Fixed

**Befund:** Erstes GitHub-Repo-Setup hatte zwar gepinnte Versionen in `libs.versions.toml`, aber keine `verification-metadata.xml`. Folge: Wenn ein Maven-Repo kompromittiert ist und ein Paket mit derselben Versions-Nummer aber anderem Inhalt liefert, würde der Build es ohne Warnung schlucken.

**Mitigation (eingearbeitet):** `docs/security.md §10` schreibt Gradle Dependency-Verification mit committeter `gradle/verification-metadata.xml` vor. Erstellung via `./gradlew --write-verification-metadata sha256` einmalig.

**Status:** ✓ Fixed konzeptionell. Implementierungs-Pflicht in v0.1.0.

### F-SUPPLY-02 · GitHub Actions nicht auf SHA gepinnt · Medium

**Befund:** Workflow-Dateien referenzieren Actions per Tag (`actions/checkout@v4`). Wenn der Tag verschoben wird (vom Maintainer oder kompromittiert), ändert sich das Verhalten ohne Repo-Update.

**Mitigation:**
1. Pin auf vollständige Commit-SHA: `actions/checkout@b4ffde65f46336ab88eb53be808477a3936bae11 # v4.1.1`.
2. Dependabot kann SHA-Pins automatisch aktualisieren (Konfigurations-Option).

**Status:** ⚠ Open — Aufnahme in `docs/github-repo.md` als Anforderung an alle Workflow-Files. Action für Architect.

### F-SUPPLY-03 · libopus-Quelle nicht definiert · Medium

**Befund:** `docs/architecture.md` sagt "libopus via JNI (AAR oder eigener Build)" — ohne Festlegung. Ein AAR aus unklarer Quelle wäre ein Supply-Chain-Risiko.

**Mitigation:**
1. Empfohlen: eigener CMake-Build aus dem offiziellen [opus-Sourcecode](https://gitlab.xiph.org/xiph/opus) (bekannter Maintainer, BSD-3-Clause, dieselbe Lizenz wie HeraTalk).
2. Alternative: gut-verifizierter Maven-AAR mit gepinnter Version und Hash.
3. ADR 0003 muss diese Wahl explizit festhalten (Selber-Build vs. AAR).

**Status:** ⚠ Open — Architect-Entscheidung in ADR 0003.

### F-SUPPLY-04 · Release-Signatur-Verifikation für Endnutzer schwierig · Low

**Befund:** Ein Nutzer, der das APK von GitHub-Releases lädt, kann zwar die SHA-256 prüfen, hat aber keinen Anker für die Signing-Identität jenseits "vom Repo-Owner gehosted".

**Mitigation:** Ab v1.0.0 SLSA-Provenance-Attestation via GitHub-Actions (`actions/attest-build-provenance`). Dadurch kann der Nutzer kryptografisch verifizieren, dass das APK aus einem bestimmten Commit gebaut wurde.

**Status:** ⚠ Akzeptiert für MVP, geplant für v1.0.0.

## 8. Findings — Operative Sicherheit

### F-OPS-01 · Kein Auto-Update-Mechanismus · Low → Fixed

**Befund:** Bei einem Security-Fix muss der Nutzer aktiv das neue APK herunterladen. Ohne F-Droid- oder Play-Store-Distribution gibt es keinen automatischen Update-Pfad.

**Mitigation (eingearbeitet):**
1. Opt-in-Update-Check beim App-Start gegen `https://github.com/relexx/heratalk/releases/latest`, offline-tolerant. Ergebnis wird als dezenter Banner angezeigt, nicht als Popup. Default: **aus**. Nutzer wird beim allerersten App-Start in einem dedizierten Willkommens-Dialog explizit gefragt ("Soll HeraTalk beim Start kurz mit GitHub prüfen, ob Sicherheitsupdates verfügbar sind?"). Entscheidung persistiert, jederzeit in Settings umschaltbar. Dokumentiert als explizite Ausnahme in `docs/requirements.md NF-03`.
2. F-Droid-Submission als Ziel für post-v1.0 (in `docs/releases.md` Post-v1.0-Roadmap).

**Status:** ✓ Fixed für v0.1.0 (Erstnutzer-Flow) und v1.0.0 (F-Droid-Vorbereitung). Siehe Entscheidungs-Eintrag in `docs/project-state.md` vom 2026-04-25.

### F-OPS-02 · Vulnerability-Reporting-Kanal · Low → Fixed

**Befund:** Frühe Version listete `security@relexx.de` als E-Mail-Kontakt. Ohne PGP-Public-Key würden Reporter sensible Daten im Klartext verschicken. Außerdem: eigene Security-Mail-Adresse bedeutet Infrastruktur zu pflegen (Spam-Filter, Archivierung).

**Mitigation (eingearbeitet):** GitHub Private Vulnerability Reporting aktiviert. Das ist der dokumentierte Standard-Kanal (`SECURITY.md` in `docs/github-repo.md`). Keine eigene Mail-Infrastruktur, keine PGP-Verwaltung nötig. Reporter nutzen die Standard-GitHub-UX ("Report a vulnerability"-Button im Repo).

**Status:** ✓ Fixed. Implementierungs-Schritt: Repo-Setting *Security → Private vulnerability reporting* einmalig aktivieren.

## 9. Findings — Datenschutz

### F-PRIV-01 · Display-Name-Persistenz vs. Pseudonymisierung · Low

**Befund:** Der Display-Name wird sowohl im mDNS-TXT-Record als auch im Broadcast-Beacon und in Control-Messages mitgesendet. Im LAN-Logging eines Angreifers stehen damit Klarnamen sichtbar.

**Mitigation:** Akzeptiert. Nutzer wird in der UI angeleitet, einen sinnvollen Namen zu wählen (z. B. Vorname statt vollständiger Name). Settings-Option für Pseudonyme. Im Threat-Model dokumentiert ("Metadata-Privacy out of scope").

**Status:** ✓ Akzeptiert, dokumentiert.

### F-PRIV-02 · Nutzer-Logs könnten ungewollt Klartext-Inhalte enthalten · Medium → Fixed

**Befund:** Ohne strukturelle Logging-Disziplin könnte ein Entwickler unbeabsichtigt Audio-Content, Display-Namen oder IPs loggen — beim manuellen "Logs exportieren" landet das in einer Datei, die der Nutzer dann arglos in einen Bug-Report kopiert.

**Mitigation (eingearbeitet):** Strukturierte Logging-Fassade mit `LogArg`-Typ-Sicherheit (`docs/security.md §9`). Siehe F-SIDE-01.

**Status:** ✓ Fixed konzeptionell.

### F-PRIV-03 · `MulticastLock` gibt App permanenten Multicast-Receive · Low

**Befund:** Während ein `MulticastLock` gehalten wird, empfängt die App **alle** Multicast-Pakete im Netz — auch solche, die nicht für HeraTalk sind. Theoretisch könnte ein Bug oder ein Logging-Pfad fremde Multicast-Daten exponieren.

**Mitigation:** Multicast-Receive ausschließlich in `:service:discovery`. Strikte Filterung nach Service-Typ und Channel-ID-Hash, **bevor** irgendwelche weiteren Verarbeitungs-Pfade aktiviert werden. Keine Logs auf nicht-passende Multicast-Pakete.

**Status:** ⚠ Open — verbindliche Regel im `.claude/rules.md` (Architektur-Regel).

### F-PRIV-04 · Empfangs-Sanitisierung für eingehende `dname`-Werte · Medium

**Befund:** Bösartige Peers könnten in den `dname`-Feldern (mDNS-TXT-Record und UDP-Broadcast-Beacon) Unicode-Tricks unterbringen, die andere HeraTalk-Instanzen in der UI angezeigt bekommen, ohne sanitisiert zu sein:

- **RTL-/Bidi-Override** (`U+202A`–`U+202E`, `U+2066`–`U+2069`): Buchstaben-Reihenfolge in der Anzeige umkehren, Phishing-Namen erzeugen ("alice" als "ecila" rendern oder Dateinamen-Endungen umdrehen).
- **Zalgo-Combining-Marks**: beliebig viele kombinierende Diakritika auf einem Base-Codepoint stapeln, sprengen Zeilenhöhe und überdecken benachbarte UI-Elemente.
- **Lookalike-Codepoints** (Cyrillic `а` vs. Latin `a` etc.): Identitäts-Spoofing eines bekannten Peers.
- **Übergroße UTF-8-Sequenzen**: 4-Byte-Codepoints können das App-seitige Längenfeld umgehen, wenn nur Byte-Länge geprüft wird.

**Mitigation:** Sanitisierungs-Pipeline in `:service:discovery` gemäß `architecture.md §6.1` ("Empfangs-Sanitisierung für `dname`"):
1. NFC-Normalisierung
2. Strip Bidi-Override-Codepoints (`U+202A`–`U+202E`, `U+2066`–`U+2069`)
3. Combining-Marks auf max. 2 pro Base-Codepoint begrenzen
4. Truncation auf 32 Codepoints
5. Wenn nach Sanitisierung leer → `Peer-{first8hex(pk)}`

Sanitisierung ist verpflichtend vor jeder UI-Darstellung; das Original wird verworfen. Lookalike-Detection (Confusables) bleibt out of scope für MVP — Nutzer sollen auf den `pk`-Fingerprint achten, nicht auf den Display-Namen.

**Status:** 📋 Offen — Implementierung vor v0.5.0 erforderlich (parallel zur ersten echten Discovery-Anbindung in v0.2.0; spätestens mit dem Pairing-Roll-out in v0.5.0 aktiv).

## 10. Findings — Architektur-Konsistenz

### F-ARCH-01 · Keine explizite Regel "Inputs validieren bevor Crypto" · Medium → Fixed

**Befund:** `.claude/rules.md` Regel 5 sagt "Alle Netzwerk-Eingaben sind nicht vertrauenswürdig". Aber: Welche Reihenfolge — erst Längen-Check, dann Format-Parse, dann Crypto-Verify? Falsche Reihenfolge öffnet potenziell DoS- oder Pre-Auth-RCE-Risiken in Parser.

**Mitigation (eingearbeitet):** Verbindliche Reihenfolge in `.claude/rules.md` Regel 11 festgehalten:
1. Hard size limits (z. B. max 1500 Byte pro UDP-Paket).
2. Magic-Bytes und Version-Field-Check.
3. Channel-ID-Hash-Filter (verwerfen wenn nicht eigener Kanal).
4. Replay-Window-Check (vor Crypto, weil günstiger).
5. AEAD-Decrypt + Verify.
6. Erst danach Inhalts-Verarbeitung.

Architektur-Querverweis in `docs/architecture.md §9` ergänzt.

**Status:** ✓ Fixed.

### F-ARCH-02 · Keine explizite Regel zur Konstantzeit-Implementierung · Medium → Fixed

**Befund:** Siehe F-SIDE-02. Die `.claude/rules.md`-Regeln zu Crypto waren allgemein gehalten ("nur JCE/BouncyCastle"), erwähnten aber Konstantzeit nicht explizit.

**Mitigation (eingearbeitet):** `.claude/rules.md` Regel 12 verbindlich: "Vergleiche von Auth-Tags, Hash-Werten und sicherheitsrelevanten Bytes immer konstantzeit (`MessageDigest.isEqual` oder eigene Konstantzeit-Funktion). `Arrays.equals`, `contentEquals`, `==` und `equals` sind verboten in solchen Vergleichen."

**Status:** ✓ Fixed.

### F-ARCH-03 · Kein expliziter Audit-Trail für Crypto-Touchpoints · Low

**Befund:** Wer ändert wann was an `:core:crypto`? CODEOWNERS schützt vor unautorisierten Merges, aber es gibt keinen expliziten "Crypto-Change-Log".

**Mitigation:** ADR-Pflicht für jede Änderung an `:core:crypto`-API oder an SRTP/Noise-Profil. Schon implizit über "ADR für Architektur-Änderungen", aber besser explizit zu machen.

**Status:** ⚠ Open — Klärung in `.claude/rules.md` und im Architekt-Agent-Profil.

## 11. Action-Items zusammengefasst

Diese sind ergänzende Tickets für den Orchestrator zur Aufnahme in `releases.md` und `project-state.md`:

### Sofort (für v0.1.0 Grundgerüst)

- [ ] `android:allowBackup="false"` im Manifest (F-MOB-01)
- [ ] Activity-Manifest-Hardening: `taskAffinity=""`, `launchMode="singleTask"` (F-MOB-02)
- [ ] Logging-Fassade mit strukturellen `LogArg`-Typen in `:core:logging` (F-SIDE-01)
- [ ] GitHub Private Vulnerability Reporting im Repo-Setting aktivieren (F-OPS-02 — `SECURITY.md` ist bereits ausformuliert)
- [ ] GitHub-Actions auf SHA-Pin umstellen (F-SUPPLY-02)
- [ ] Gradle-Dependency-Verification mit `verification-metadata.xml` initialisieren (F-SUPPLY-01)

### Vor v0.5.0 (Pairing/Control-Plane)

- [ ] QR-Code mit `expires`-Feld + Override-Dialog (F-CRYPTO-03)
- [ ] `FLAG_SECURE` auf QR-Anzeige-Screen (F-PAIR-01)
- [ ] URL-Schema-Confirmation-Dialog vor Beitritt (F-PAIR-03)
- [ ] Software-Keystore-Warning-UI (F-CRYPTO-02)
- [ ] Static-Key-Fingerprint in Peer-Details + Key-Wechsel-Banner (F-CRYPTO-01)
- [ ] TCP-Half-Close-Mitigationen (F-NET-03)

### Vor v0.6.0 (SRTP)

- [ ] Konstantzeit-Tag-Vergleich verifizieren (F-SIDE-02)
- [ ] CBR-only-Opus statt VBR (F-SIDE-04) — Architektur-Update
- [ ] Versions-präfixierte HKDF-Info-Strings (F-CRYPTO-04 — bereits fixed)
- [ ] ADR 0003 zu SRTP-Implementierung mit Konstantzeit-Anforderung (F-ARCH-02)

### Vor v0.9.0 (Robustheit)

- [ ] Beacon-MAC-Signatur (F-NET-01) — als ADR-Entscheidung
- [ ] Rate-Limit auf Handshake-Attempts (F-NET-01)
- [ ] Multicast-Filter-Strikt-Pflicht (F-PRIV-03)

### Vor v0.10.0 (Relay)

- [ ] Outer-Frame-TTL-Counter, max 1 Hop (F-NET-04)
- [ ] Relay-Authorisierung über Kanal-Mitgliedschaft prüfen (F-NET-04)

### Für v1.0.0

- [ ] SLSA-Provenance-Attestation (F-SUPPLY-04)
- [ ] DTX-Disable-Privacy-Settings-Option (F-SIDE-05)

### Permanent (bereits erledigt, hier zur Dokumentation)

- [x] `.claude/rules.md` um Validierungs-Reihenfolge ergänzt (F-ARCH-01) — Regel 11
- [x] `.claude/rules.md` um Konstantzeit-Vergleich ergänzt (F-ARCH-02) — Regel 12
- [ ] ADR-Pflicht für `:core:crypto`-Änderungen explizit (F-ARCH-03) — fließt ins ADR-Template ein, sobald erste ADRs geschrieben werden

## 12. Severity-Summary

| Severity | Anzahl Findings | Davon Fixed | Davon Open | Davon Akzeptiert |
|----------|-----------------|-------------|------------|--------------------|
| Critical | 0 | — | — | — |
| High | 5 | 4 | 1 | 0 |
| Medium | 14 | 6 | 5 | 3 |
| Low | 16 | 2 | 6 | 8 |
| **Gesamt** | **35** | **12** | **12** | **11** |

Keine Critical-Findings — die Architektur ist im Kern tragfähig. Vier der fünf High-Findings sind adressiert; das verbliebene (F-CRYPTO-01 Static-Key-Fingerprint-UI) ist für v0.5.0 fest eingeplant. Die offenen Medium-Findings sind im Wesentlichen Implementierungs-Details, die in den entsprechenden Releases verbindlich umzusetzen sind. Akzeptierte Findings sind dokumentierte Restrisiken — siehe `docs/security.md §12`.

## 13. Audit-Folgetermine

- **Audit-Runde 2:** Nach Abschluss von v0.5.0 (Control-Plane) — fokussiert auf reale Implementierungs-Korrektheit gegen das hier dokumentierte Modell.
- **Audit-Runde 3:** Vor v1.0.0 — externes Review empfehlenswert, mindestens durch zweiten erfahrenen Crypto-Reviewer.
- **Penetrationstest:** Optional vor öffentlichem v1.0.0-Release. Realistisches Setup mit zwei Geräten in feindlichem WLAN (Kali Linux mit aircrack/wireshark/scapy).
