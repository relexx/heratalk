# ADR-0003: Opus als Audio-Codec mit JNI-Brücke zu libopus

## Status: Accepted

## Datum

2026-04-30

## Kontext

HeraTalk überträgt Sprache in Echtzeit über UDP im LAN. Die Codec-Wahl bestimmt drei kritische Eigenschaften:

- **Latenz** — Anforderung NF-02 (`docs/requirements.md`): Mund-zu-Ohr ≤ 250 ms unter guten, ≤ 400 ms unter schlechten Bedingungen. Die Codec-Roundtrip-Latenz (Encode + Decode + Algorithmic Delay) muss deutlich darunter liegen — Zielwert ≤ 60 ms.
- **Robustheit unter Paketverlust** — der Codec muss In-Band-FEC und PLC mitbringen, weil das Netzwerk in der Praxis Loss zeigt (`docs/architecture.md §10.1`).
- **Bitrate-Flexibilität** — die App skaliert die Bitrate adaptiv von 8 bis 32 kbps abhängig vom gemessenen Loss. Der Codec muss das ohne Re-Init unterstützen.

Lizenzbedingungen: HeraTalk steht unter BSD 3-Clause (Konsistenz mit Opus). Der Codec darf keine Patentklauseln tragen, die mit der App-Lizenz kollidieren oder eine Royalty-Pflicht auslösen.

Plattform: Android, min SDK 29, target SDK 36, ARM/ARM64. Kein Cloud-Transcoding. Der Codec läuft in `:service:audio` und bekommt PCM aus `AudioRecord` (48 kHz mono, `VOICE_COMMUNICATION`-Source) zugeführt.

## Entscheidung

HeraTalk verwendet **Opus** als einzigen Audio-Codec.

- **Integration:** über JNI-Brücke zur Referenz-Implementierung [libopus](https://opus-codec.org/).
- **Frame-Größe:** 20 ms Default. 10-ms- und 40-ms-Frames sind optional aktivierbar, falls Adaptivität es verlangt.
- **Sample-Rate:** 48 kHz bei guter Bandbreite, fällt bei extremem Loss (> 30 %) auf Narrowband (8 kHz) zurück. Opus' interne Modus-Umschaltung (SILK/Hybrid/CELT) wird ihm überlassen.
- **Bitrate:** strikt **CBR** (Constant Bitrate), 8–32 kbps adaptiv, gesteuert durch `CodecHint`-Feedback (`docs/architecture.md §6.2`, §10.1). VBR ist explizit ausgeschlossen — siehe Konsequenzen.
- **FEC:** In-Band-FEC ab Loss > 2 % aktiv.
- **DTX:** an, für Stille-Erkennung und Akku-Schonung.
- **PLC:** standardmäßig aktiv im Decoder.

**Lieferweg von libopus:**

- **Präferenz:** vorgefertigtes Android-AAR (z. B. `theeasiestway/android-opus-codec` oder ein vergleichbarer Build) mit gepinntem SHA-256-Hash und einer dokumentierten Bezugsquelle. Spart eigene CMake-/NDK-Build-Pipeline und reduziert Supply-Chain-Aufwand auf eine einzige Artefakt-Verifikation.
- **Fallback:** eigener CMake-Build aus dem offiziellen Xiph-Quellcode, integriert über Gradle-NDK-Build mit reproduzierbaren Flags. Wird nur dann gezogen, wenn keine vertrauenswürdige AAR-Quelle existiert oder der AAR die NDK-r28-Anforderung an 16-KB-Page-Alignment nicht erfüllt (Pflicht für Play Store seit November 2025).
- **JNI-Layer:** dünner C-Wrapper in `:service:audio/src/main/cpp/`, der nur die nötigen `opus_encoder_*`- und `opus_decoder_*`-Funktionen exportiert. Kein eigenes Buffer-Management auf C-Seite — Buffer kommen aus Kotlin.

Die letzte Wahl AAR vs. Selbstbau wird vor dem ersten Audio-Release (v0.3.0) getroffen, sobald der Architekt die AAR-Quelle auf Provenance, Signaturen und 16-KB-Alignment geprüft hat. Beide Pfade sind hier mit dem ADR vereinbar.

## Konsequenzen

**Positiv:**

- Opus ist **royalty-free** (IETF RFC 6716), 3-Clause-BSD-lizensiert — keine Lizenzkollision mit HeraTalk.
- WebRTC-Standard-Codec; Industrie-Best-Practice für Echtzeit-Sprache.
- Algorithmic Delay 5–22.5 ms bei 20 ms Frames — fügt sich problemlos in das 250-ms-Latenz-Budget.
- Bitrate-Adaption ohne Re-Init: ein einzelner `opus_encoder_ctl(OPUS_SET_BITRATE)` reicht.
- In-Band-FEC ist bestens dokumentiert und mit dem App-Level-Frame-Duplikat (`docs/architecture.md §10.1`) komplementär.
- Komplexität sehr gut von Mobile-CPUs unterstützt; CPU-Last < 5 % auf Mittelklasse-Geräten.
- Eine einzige Codec-Implementierung — kein Codec-Negotiation-Protokoll nötig, keine Kompatibilitätsmatrix.

**Negativ / Risiken:**

- **NDK-Pflicht.** Eigener CMake-Build (Fallback) erfordert NDK-r28-Toolchain und 16-KB-Page-Alignment-Compliance. Das ist seit dem CI-Bring-up sowieso geplant; ADR macht es explizit.
- **VBR-Verbot.** Opus' VBR-Modus korreliert Paketgröße mit phonetischem Inhalt — bekannter Side-Channel (Wright et al. 2008 für Skype). Wir laufen daher zwingend in CBR mit 8-Byte-Padding. Kostet leicht Audio-Qualität bei gleicher Bitrate, schließt aber den Inhalts-Leak. Diese Regel ist auch in `docs/architecture.md §10.1` festgeschrieben und in `.claude/rules.md` als harte Regel verankert.
- **Encoder-Lookahead** beim ersten Frame ist nicht null — die `:service:audio`-Pipeline muss die ersten 5 ms verwerfen oder mit einer Pre-Roll umgehen. Wird in der Audio-Pipeline-Implementierung berücksichtigt.
- **AAR-Lieferantenrisiko.** Verwendet die Präferenzlösung einen Drittanbieter-Build, hängt das Vertrauen an dessen Signing- und Build-Disziplin. Mitigation: Hash-Pin in `libs.versions.toml`, regelmäßige Re-Verification durch den Architekten, Ein-Klick-Möglichkeit zum Self-Build.
- **Keine Interop mit Nicht-Opus-Clients.** Bewusst akzeptiert (`docs/architecture.md §1` — kein WebRTC-, kein SIP-Interop).

**Auswirkungen auf den Code:**

- Modul `:service:audio` ist security-relevant (CODEOWNER-Review), weil Buffer-Handling über JNI fließt. Property-Tests fuzzen die JNI-Grenzen mit korrupten Frame-Längen.
- `gradle/libs.versions.toml` führt entweder den AAR-Pin oder den NDK-Build-Hash.
- `:service:audio` exportiert eine reine Kotlin-API; libopus-Aufrufe sind hinter `private external fun`-Deklarationen versteckt und werden über `System.loadLibrary("heratalk_opus")` geladen.

## Abgewogene Alternativen

### A. AMR-WB

3GPP-Codec, weit verbreitet in Mobilfunk-Sprache.

- Pro: in vielen Android-Stacks hardware-decodiert.
- Contra: **Lizenzpflichtig** (VoiceAge / 3G Licensing). Reicht allein, um den Codec auszuschließen — HeraTalk darf keine Royalty-Last tragen.
- Schmalere Bitrate-Range (6.6–23.85 kbps) und schlechtere Qualität bei niedriger Bitrate als Opus.

Verworfen.

### B. AAC-LC / HE-AAC

Industrie-Standard für Audio-Streaming.

- Pro: Hardware-Beschleunigung auf praktisch jedem Android-Gerät.
- Contra: Lizenzgebühren über Via Licensing pflichtig. Algorithmic Delay (≈ 60 ms bei AAC-LC, > 100 ms bei HE-AAC) zu hoch für Real-Time-Sprache.

Verworfen.

### C. Speex

Vorgänger von Opus.

- Pro: BSD-lizensiert, gut etabliert.
- Contra: durch Opus offiziell als deprecated eingestuft, Maintenance praktisch eingestellt seit 2012. Schlechtere Qualität pro kbps. Kein In-Band-FEC.

Verworfen.

### D. G.711 (μ-law / A-law)

Klassischer Telefonie-Codec.

- Pro: lizenzfrei, trivial zu implementieren, null Algorithmic Delay.
- Contra: 64 kbps fix — sprengt das Bitrate-Budget bei schlechtem Loss-Verhalten und ist nicht adaptiv. Schmalbandig (8 kHz). Keine FEC, kein PLC.
- Brauchbar als Fallback-Codec für absolute Notfälle, aber nicht als Primärwahl.

Verworfen als Primärlösung. Wird auch nicht als Fallback gezogen — Opus' Narrowband-Modus deckt diesen Use-Case bereits ab.

### E. SILK (Skype-Codec, Vorgänger von Opus' SILK-Modus)

- Pro: für Sprache optimiert.
- Contra: Opus enthält SILK bereits als internen Modus und schaltet automatisch in den Hybrid-/CELT-Modus, wenn die Bandbreite es zulässt. Standalone-SILK ist eine Untermenge von Opus.

Verworfen — durch Opus subsumiert.

### F. Concentus (Pure-Java-Opus-Port)

- Pro: kein NDK nötig.
- Contra: signifikant höhere CPU-Last als die libopus-C-Implementierung; auf Low-End-Geräten ein Risiko für die Latenz-Anforderung. Maintenance unklar. Keine Audio-Qualitäts-Regression dokumentiert, aber das Risiko ist real.
- Bleibt als Notfall-Plan B in `docs/project-state.md`-Risiko-Tabelle vermerkt, falls libopus-JNI sich als unhaltbar erweist — kein Default.

Verworfen als Default.

## Referenzen

- RFC 6716 — Definition of the Opus Audio Codec
- RFC 7587 — RTP Payload Format for the Opus Speech and Audio Codec
- `docs/architecture.md §10` — Audio-Pipeline mit Adaptivität, CBR-Pflicht
- `docs/requirements.md` NF-02 — Latenz-Budget
- `docs/project-state.md` — Entscheidungsprotokoll 2026-04-25 zur libopus-AAR-Präferenz
- Wright, C. V. et al. (2008): "Spot me if you can: Uncovering spoken phrases in encrypted VoIP conversations" — Begründung für CBR-Pflicht
