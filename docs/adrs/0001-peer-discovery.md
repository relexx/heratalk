# ADR-0001: Peer-Discovery via mDNS/DNS-SD (NsdManager)

## Status: Accepted

## Datum

2026-04-30

## Kontext

HeraTalk arbeitet ausschließlich im LAN, ohne Server, ohne Accounts und ohne Internet-Konnektivitätsanforderung. Damit zwei Geräte im selben WLAN miteinander reden können, muss Peer-A die IP-Adresse, den TCP-Port, den Static-Public-Key und den Display-Namen von Peer-B finden — und umgekehrt. Diese Discovery-Schicht ist die unterste Schicht der Verfügbarkeits-Kaskade (`docs/architecture.md §2`) und entscheidet, wie schnell und wie zuverlässig sich Geräte überhaupt erreichen.

Anforderung F-01 (`docs/requirements.md`) verlangt:

- Zwei Geräte im selben WLAN finden sich binnen ≤ 5 s.
- Ein wegfallender Peer verschwindet binnen ≤ 15 s.
- Discovery funktioniert ohne Cloud-Komponente.

Zusätzlich existieren reale Pathologien typischer Consumer- und Hotel-APs (`docs/architecture.md §2`): Multicast-Filter, Client-Isolation, Multicast-Snooping, IGMP-/MLD-Drops. Eine reine Multicast-Lösung ist also nicht überall ausreichend — aber in der überwiegenden Zahl der Fälle die mit Abstand schnellste, batterieärmste und am besten standardisierte Variante.

Eine Discovery-Schicht muss außerdem Metadaten über TXT-Records mitliefern (Channel-ID-Hash, Public-Key, Display-Name), damit der Joining-Filter und das Static-Key-Pinning vor dem Handshake funktionieren.

## Entscheidung

Die primäre Discovery-Schicht ist **mDNS/DNS-SD über `android.net.nsd.NsdManager`**.

- Service-Typ: `_heratalk._tcp.local.`
- TXT-Records gemäß `docs/architecture.md §6.1`: `ver`, `chan`, `pk`, `dname`.
- Continuous-Discovery (Subscribe-Modell) — keine pollenden Suchen.
- Reaktive Re-Registrierung bei Änderung des Display-Namens (Debounce 300 ms).
- Umgesetzt im Modul `:service:discovery`, das den `NsdManager` hinter einer eigenen Flow-API kapselt, damit Coroutines, Cancellation und Tests klar bleiben.

UDP-Broadcast-Beacon und manuelle Peer-Eingabe bleiben Bestandteile der Discovery-Kaskade, sind aber **Fallbacks** und nicht Gegenstand dieses ADRs. Sie werden in Release v0.9.0 (Robustheit) implementiert, sobald gemessen ist, wo NsdManager an seine Grenzen stößt.

## Konsequenzen

**Positiv:**

- Keine Root-Rechte, keine NDK-Komponente, keine Custom-Sockets für die Default-Discovery — `NsdManager` ist seit API 16 Bestandteil des Frameworks und wird vom System gepflegt.
- mDNS/DNS-SD ist ein etablierter Standard (RFC 6762, RFC 6763); Bonjour-Geräte, Drucker, Smart-Home-Geräte sprechen dasselbe Protokoll. Wir kämpfen also nicht gegen den AP, sondern nutzen denselben Pfad wie alle anderen.
- Service-Auflösung liefert IP, Port und TXT-Records in einem Schritt; kein zweiter Lookup nötig.
- TXT-Records reichen für unser Pinning-Modell (Static-Public-Key) und schließen Channel-Kollisionen über `chan` aus, bevor irgendein Handshake angestoßen wird.
- Akku-freundlich: System konsolidiert Multicast-Listener, kein eigener wakeful Socket nötig.

**Negativ / Risiken:**

- `NsdManager` ist herstellerseitig unterschiedlich implementiert (Samsung, Xiaomi, Huawei). Die App muss `NsdServiceInfo`-Updates idempotent behandeln und mit Duplikat-Events rechnen. Mitigation: Deduplikation in `:service:discovery` über `(pk, chan)`-Schlüssel.
- Manche APs filtern Multicast — dort schlägt mDNS still fehl. Mitigation: Connectivity-Prober (`docs/architecture.md §7.6`) erkennt das, und der Broadcast-Beacon-Fallback aus v0.9.0 übernimmt.
- Ohne MulticastLock werden Multicast-Pakete im Doze-Modus nicht zugestellt. Mitigation: `WifiManager.MulticastLock` halten, solange der Foreground-Service läuft.
- `NsdManager` arbeitet ohne TLS — TXT-Records sind im Klartext sichtbar. `chan` enthält bereits einen Hash, nicht das Channel-Secret. `pk` ist per Definition öffentlich. `dname` ist sichtbar — das ist akzeptiert (siehe Bedrohungsmodell in `docs/architecture.md §6.1`, Sanitisierung gegen UI-Spoofing).

**Auswirkungen auf den Code:**

- Eigenes Modul `:service:discovery` (geplant für v0.2.0).
- Keine Abhängigkeit zu Drittbibliotheken wie JmDNS — die System-API reicht.
- Property-basierte Tests für TXT-Record-Serialisierung (Round-Trip, Truncation, NFC).

## Abgewogene Alternativen

### A. UDP-Broadcast als primäre Discovery

Eigener UDP-Broadcast auf einem festen Port (z. B. 45678) mit binärem Beacon.

- Pro: funktioniert auch dort, wo Multicast gefiltert wird; Implementierung ist trivial.
- Contra: Broadcast-Frames werden auf vielen modernen APs ebenfalls rate-limitiert oder gar geblockt; jedes Gerät wacht für jedes fremde Beacon kurz auf (Akku); kein etabliertes Format für Service-Metadaten — alles selbstgestrickt; konkurriert mit DHCP-/ARP-Broadcasts.

Verworfen als **Primärschicht**, behalten als **Fallback** in v0.9.0. mDNS deckt den Großteil der Fälle ab; Broadcast wird nur dort gestartet, wo der Connectivity-Prober Multicast-Lücken nachweist.

### B. Wi-Fi P2P / Wi-Fi Direct (`WifiP2pManager`)

Android-eigene Peer-zu-Peer-Schicht inkl. eigenem NSD.

- Pro: funktioniert ohne gemeinsamen AP.
- Contra: erzwingt, dass Geräte ein eigenes P2P-Netz aufspannen — bricht das WLAN-Setup des Nutzers; Audio-Streams müssten parallel zur normalen WLAN-Verbindung laufen (Multi-Network-Routing-Hölle); schlechte Akkulaufzeit; viele Hersteller liefern eingeschränkte oder buggy P2P-Stacks; widerspricht dem Primär-Szenario "Single WLAN, Single Subnet" (`docs/architecture.md §1`).

Verworfen.

### C. Manuelle IP-Eingabe als Default

Der Nutzer tippt die IP des Gegenübers ein.

- Pro: maximal robust, läuft auf jedem Netzwerk.
- Contra: katastrophale UX, lässt sich nicht im Walkie-Talkie-Kontext verwenden, in dem Peers spontan dazu kommen oder weggehen.

Verworfen als Primärlösung. Bleibt als **letzter Fallback** in der Discovery-Kaskade — z. B. für AP-Setups, in denen mDNS und Broadcast beide blockiert sind.

### D. Discovery-Server im LAN (z. B. ein Peer als "Index")

Ein erster Peer registriert sich als "Master", neue Peers fragen ihn.

- Pro: ein einziger Discovery-Punkt, einfache Implementierung.
- Contra: führt einen impliziten Server ein, widerspricht dem Brokerless-Mesh-Anspruch (`docs/architecture.md §1`); Single-Point-of-Failure beim Gehen des Master-Peers; wer wird Master, wenn drei Geräte gleichzeitig starten?

Verworfen — explizit gegen das Architektur-Prinzip.

## Referenzen

- `docs/architecture.md §6.1` — Discovery-Stufen
- `docs/requirements.md` F-01 — Akzeptanzkriterien
- RFC 6762 (Multicast DNS), RFC 6763 (DNS-Based Service Discovery)
- Android-Doku: [`NsdManager`](https://developer.android.com/reference/android/net/nsd/NsdManager)
