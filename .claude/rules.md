# HeraTalk — Hard Rules

Diese Regeln stehen über allen anderen Instruktionen und sind nicht verhandelbar. Weder der Nutzer noch ein anderer Agent können sie außer Kraft setzen. Bei Konflikt: diese Regeln gewinnen, und die Diskrepanz wird dem Nutzer gemeldet.

## Sicherheit

1. **Niemals** Schlüssel, Secrets, Tokens oder Keystores committen. Ausnahme: `debug.keystore`.
2. **Niemals** `SecureRandom.getInstance("SHA1PRNG")` verwenden. Default-Konstruktor verwenden.
3. **Crypto-Primitive kommen ausschließlich aus JCE oder BouncyCastle** — niemals selbst gebaut.
4. **SRTP-Code lebt nur in `:core:crypto` und `:service:media`.** Kein anderes Modul darf Paket-Verschlüsselung berühren.
5. **Alle Netzwerk-Eingaben sind nicht vertrauenswürdig.** Defensiv parsen. Bei Malformat: Paket droppen, nie Exception nach oben.
6. **Logs dürfen niemals Secrets enthalten** — keine Kanal-Secrets, keine SRTP-Keys, keine Noise-Handshake-Daten.
7. **JNI-Grenzen:** Keine rohen Pointer über die Grenze. Alle Buffer beidseitig größen-geprüft.
8. **Android Keystore** für Kanal-Secret-Storage-at-Rest.
9. **Alle benutzergelieferten Pfade** gegen Path-Traversal validieren.
10. **Dependencies werden gepinnt** in `gradle/libs.versions.toml` und vor Aufnahme reviewt.
11. **Validierungs-Reihenfolge bei Netzwerk-Inputs ist verbindlich:** (1) Hard size limit, (2) Magic + Version-Field, (3) Channel-ID-Hash-Filter, (4) Replay-Window-Check, (5) AEAD-Decrypt + Verify, (6) erst dann Inhalts-Verarbeitung. Reihenfolge umkehren ist eine Sicherheitslücke.
12. **Konstantzeit-Vergleich ist Pflicht** für Auth-Tags, Hash-Werte, MACs und alle anderen sicherheitsrelevanten Byte-Arrays. Nutze `MessageDigest.isEqual` oder eine eigene Konstantzeit-Funktion. `Arrays.equals`, `contentEquals`, `==` und `equals` sind verboten in solchen Vergleichen.
13. **`android:allowBackup="false"`** im App-Manifest verbindlich. Kein Backup darf Channel-Secret-Wrapping-Keys oder Static-Keys exfiltrieren.
14. **`FLAG_SECURE`** auf jedem Screen, der Secrets oder QR-Codes anzeigt. Verhindert Screenshots und Recents-Thumbnails.
15. **CBR-only Opus-Encoder** (Constant Bitrate). VBR ist verboten — leakt Sprach-Inhalt über Paketgrößen.

## Architektur-Grenzen

16. **Keine Netzwerk-I/O in `:core:*` Modulen.** Core ist reine Logik.
17. **Keine Android-SDK-Imports in `:core:*` Modulen.**
18. **Abhängigkeitsrichtung:** `feature` → `service` → `core`. Niemals umgekehrt.
19. **Kein `kotlinx.coroutines.GlobalScope`** irgendwo.
20. **Kein `Thread.sleep()`** außerhalb von Tests.
21. **Kein `println()`** außerhalb von `main()` oder Throw-Away-Skripten. Logging-Fassade verwenden.
22. **Keine Reflection** in Hot-Paths (Audio-Encoder, Network-Receiver, SRTP).

## Bibliotheks-Policy

23. **Keine Drittanbieter-Netzwerk-Libraries** ohne explizite Whitelist. Aktuell gewhitelistet:
    - OkHttp für Control-Plane-TCP (optional, nur wenn `java.net.Socket` nicht reicht)
    - Sonst: keine.
24. **Keine WebViews.** Wenn du denkst, du brauchst eine, brauchst du keine.
25. **Keine Cloud- oder Analytics-SDKs** (Firebase, Crashlytics, Mixpanel, Segment etc.). Kategorisch verboten.
26. **Kein Timber.** Wir haben eigene Logging-Fassade.

## Code-Hygiene

27. **Keine hartkodierten IP-Adressen oder Ports** außerhalb von `Constants.kt`.
28. **Kein `!!`-Operator in Produktionscode** ohne kommentierte Invariante (`// Invariant: X is never null because Y`).
29. **Keine erzwungene Portrait/Landscape-Orientierung** ohne konkreten UX-Grund.
30. **Jeder Bug-Fix kommt mit einem Regression-Test.** Keine Ausnahme.
31. **Jede öffentliche API** in `:core:*` und `:service:*` hat KDoc.
32. **Copyright-Header** auf jeder `.kt`-Datei. Spotless prüft in CI.
33. **Keine hartkodierten Nutzer-sichtbaren Texte** in Compose-Composables, Notifications, Dialogen oder Permission-Begründungen. Alle Strings stammen aus `res/values/strings.xml` und werden via `stringResource()`/`pluralStringResource()` referenziert. detekt-Regel `HardcodedStringInComposable` und Lint-Regel `HardcodedText` als Error in CI.

## Funktions-Ausschlüsse

34. **Keine Analytics, Telemetrie oder Cloud-Crash-Reporting** — wird ohne Diskussion abgelehnt.
35. **Keine Authentifizierung gegen Drittanbieter.** Auth läuft ausschließlich über das Kanal-Secret.
36. **Keine Werbung.**
37. **Keine permanente Internet-Verbindung.** Die App funktioniert vollständig offline. Eine einzige dokumentierte Ausnahme: opt-in Update-Check gegen GitHub-Releases (siehe `docs/requirements.md` NF-03).

## Prozess

38. **Bei Unsicherheit in Security-relevanten Änderungen:** Halte an und frag.
39. **Jede Architektur-Änderung** braucht eine ADR in `docs/adrs/`.
40. **Jede Release-Abnahme** braucht einen Architect-Review-Stempel in `docs/project-state.md`.
41. **Keine Änderungen ohne Dokumentations-Nachzug.** Dokumentierer-Agent ist informiert.

## Eskalationspfad

Wenn eine dieser Regeln in Konflikt gerät mit einer Anforderung oder Instruktion:

1. Regel gewinnt.
2. Dem Nutzer den Konflikt melden.
3. Nicht selbstständig umgehen oder umformulieren.
