# ADR-0004: Adapter-Schichten für Android-SDK-Symbole in `:core:*`-Modulen

## Status: Accepted

## Datum

2026-04-30

## Kontext

`.claude/rules.md` Rule 17 verbietet kategorisch Android-SDK-Imports in `:core:*`-Modulen, und `docs/architecture.md §4` schreibt fest, dass `:core:*` reine Domain-Logik enthält. Diese Regel ist gut begründet:

- Sie hält Domain-Logik plattformunabhängig und damit ohne Robolectric/Instrumentation testbar.
- Sie schützt vor unbeabsichtigtem Eindringen von Android-Framework-Lebenszyklus, `Context`-Leaks und I/O in einer Schicht, die bewusst rein gehalten ist.
- Sie hält die Abhängigkeitsrichtung `feature → service → core` einseitig und damit refaktorierbar.

In Phase A1 (Release v0.1.0) ist beim Bring-up zweier Module ein wiederkehrendes Muster aufgetaucht, das nicht durch reine Konfiguration auflösbar ist:

- **`:core:logging`** liefert die Logging-Fassade (`Logger.kt`) und enthält eine konkrete Implementierung `AndroidLogcatLogger`, die auf `android.util.Log` zugreift. Der Sinn der Fassade ist, dass Aufrufer in `:service:*` und `:feature:*` nicht direkt gegen `Log` schreiben — die Implementierung muss aber irgendwo wohnen.
- **`:core:identity`** persistiert den Display-Namen über `androidx.datastore.preferences`. Die DataStore-Bibliothek ist eine `androidx.*`-Komponente und benötigt einen Android-`Context`. Eine reine JVM-Persistenz wäre Plattform-Doppel­arbeit ohne Mehrwert.

Beide Fälle haben dieselbe Struktur: eine plattformunabhängige Domain-API und eine Android-spezifische Implementierung, die im selben Modul leben **müssen**, weil das Modul aus Sicht der Aufrufer "den Vertrag plus die Default-Implementierung liefert". Eine Aufspaltung in zwei Module je `:core:*`-Modul mit Adapter würde die Modul-Anzahl ohne Architektur-Gewinn nahezu verdoppeln.

Zugleich darf Rule 17 nicht aufgeweicht werden, weil sie für Module wie `:core:crypto`, `:core:model` und `:core:ui` weiterhin uneingeschränkt gilt: dort gibt es keinerlei Rechtfertigung für Android-Symbole.

Die Entscheidung legt daher die **Ausnahme-Bedingungen** fest, unter denen ein `:core:*`-Modul als Android-Library konfiguriert werden darf, und schreibt die strukturelle Trennung von Interface und Adapter so eng vor, dass die Schutzwirkung von Rule 17 erhalten bleibt.

## Entscheidung

`:core:*`-Module dürfen Android-SDK- und `androidx.*`-Symbole referenzieren, **wenn alle folgenden Bedingungen gleichzeitig erfüllt sind**:

### Akzeptanzkriterien für die Adapter-Trennung

1. **Reines Interface in eigener Datei.** Die Domain-API (Interface, Sealed Class, Data Classes) liegt in einer separaten Kotlin-Datei ohne `import android.*` oder `import androidx.*`. Beispiele: `Logger.kt`, `IdentityRepository.kt`. Diese Datei ist gegen die JVM kompilierbar und enthält keine Plattform-Spuren.

2. **Adapter-Klassen explizit benannt.** Nur Klassen mit dem Suffix `Adapter` oder `Impl` (z. B. `AndroidLogcatLogger`, `DataStoreIdentityRepository`) dürfen `android.*`/`androidx.*` importieren. Der Name macht im Code-Review sofort sichtbar, dass es sich um eine Adapter-Schicht handelt.

3. **Sichtbarkeit minimieren.** Adapter-Klassen sind `internal`, sofern sie nicht zwingend vom DI-Graph in `:app` instanziiert werden müssen. Nur die Factory-Methode oder das Koin-Modul ist `public`. Aufrufer aus `:service:*`/`:feature:*` sehen ausschließlich das Interface.

4. **JVM-Tests gegen das Interface.** Unit-Tests in `src/test/kotlin` testen die Domain-Logik gegen das Interface mit Fakes/MockK — niemals gegen den Adapter direkt. Tests, die den Adapter selbst gegen die Plattform prüfen, gehören nach `src/androidTest/kotlin` und sind sparsam einzusetzen. **Robolectric ist nicht erforderlich, um die Domain-Logik zu testen** — wenn ein Test Robolectric braucht, ist die Trennung verletzt.

5. **Keine Android-Symbole in der API-Signatur.** Methoden des Interfaces dürfen keine `Context`, `SharedPreferences`, `View`, `Bundle` o. Ä. annehmen oder zurückgeben. Der `Context` wird ausschließlich beim Konstruieren des Adapters im DI-Graph injiziert und verlässt das Modul nie wieder.

6. **Modul-Konfiguration als Android-Library.** Solche `:core:*`-Module werden als `com.android.library` konfiguriert (statt `org.jetbrains.kotlin.jvm`). Die Build-Konfiguration deklariert die Android-Abhängigkeit explizit; sie ist nicht implizit.

7. **Whitelist gepflegt.** Welche `:core:*`-Module als Android-Library laufen dürfen, wird in `docs/architecture.md §4` namentlich aufgelistet. Aktuell: `:core:logging`, `:core:identity`. Jede Erweiterung dieser Liste ist eine Architektur-Änderung und braucht ein neues ADR oder ein Update dieses ADRs.

### Module, die uneingeschränkt unter Rule 17 fallen

Folgende Module bleiben **strikt JVM-only** und dürfen unter keinen Umständen Android-Symbole referenzieren:

- `:core:crypto` — security-kritisch, muss auf reiner JVM auditierbar bleiben.
- `:core:model` — reine Domain-Modelle.
- `:core:ui` — Compose-Theme und -Composables; nutzt zwar `androidx.compose.*`, aber **keine** `android.*`-SDK-Symbole außerhalb der Compose-API.

Rule 17 in `.claude/rules.md` wird durch dieses ADR **nicht entschärft**. Die Regel formuliert das Default-Verhalten; dieses ADR formalisiert die einzig zulässige Ausnahme und macht ihre Bedingungen explizit prüfbar.

## Konsequenzen

**Positiv:**

- Domain-Logik in `:core:logging` und `:core:identity` bleibt klar von der Plattform getrennt — die Tests laufen weiterhin als reine JUnit-5-Tests ohne Robolectric.
- Modul-Anzahl bleibt überschaubar; keine künstliche Aufspaltung in `:core:logging-api` + `:core:logging-android`-Paare.
- Code-Review hat eine klare, lokal prüfbare Heuristik: "Wenn die Datei `Adapter` oder `Impl` heißt, darf sie Android importieren — sonst nicht."
- Die Whitelist in `architecture.md §4` ist ein eindeutiger Anker für CI-Linter oder detekt-Custom-Rules, falls die Regel später automatisch durchgesetzt werden soll.

**Negativ / Risiken:**

- Die Regel ist nicht durch den Compiler erzwingbar — sie hängt am Code-Review. Mitigation: detekt-Custom-Rule auf `:core:*` (geplant für v0.2.0), die Android-Imports außerhalb von Dateien mit `Adapter`/`Impl`-Suffix verbietet.
- Es besteht das Risiko, dass spätere Beiträge das Muster falsch verstehen und Android-Symbole direkt in das Interface eindringen. Mitigation: KDoc-Hinweis am Interface ("Pure domain API. Adapters live in `*Adapter.kt` / `*Impl.kt`."), CODEOWNER-Review für `:core:*`.
- Bei DI-Setup muss `:app` den Adapter mit `Context` konstruieren — geringer Boilerplate-Mehraufwand im Koin-Modul.

**Auswirkungen auf den Code:**

- `:core:logging/build.gradle.kts` und `:core:identity/build.gradle.kts` sind als `com.android.library` konfiguriert. Andere `:core:*`-Module bleiben JVM-Module.
- Interface-Dateien in betroffenen Modulen tragen einen Header-Kommentar, der die Adapter-Konvention verlinkt.
- DI-Graph in `:app` instanziiert konkret `AndroidLogcatLogger` und `DataStoreIdentityRepository`; Aufrufer halten nur die Interface-Referenz.

## Abgewogene Alternativen

### A. Aufspaltung jedes betroffenen Moduls in zwei Module (`-api` + `-android`)

Statt einer Adapter-Datei im selben Modul: ein reines JVM-Modul `:core:logging-api` mit dem Interface und ein zweites Modul `:core:logging-android` mit der Adapter-Implementierung. Aufrufer hängen vom `-api`-Modul ab, der DI-Graph in `:app` zieht zusätzlich das `-android`-Modul.

- Pro: Compiler-erzwungene Trennung — `:core:logging-api` kann gar keine Android-Symbole sehen, weil es ein JVM-Modul ist.
- Contra: verdoppelt die Modul-Anzahl je betroffenem `:core:*`-Modul. Aktuell wären das aus 2 Modulen 4, mit absehbarer weiterer Vermehrung. Zusätzliche Build-Konfiguration, zusätzliche `libs.versions.toml`-Einträge, zusätzliche README-Verweise. Der Mehrwert gegenüber einer per Konvention erzwungenen Trennung im selben Modul ist gering, weil der Adapter ohnehin nur im DI-Graph instanziiert wird.
- Contra: erhöht die kognitive Last für neue Entwickler ("Warum gibt es zwei Module für Logging?") ohne klaren Architektur-Gewinn.

Verworfen — der Aufwand wiegt den Compiler-Vorteil nicht auf, solange detekt-Custom-Rules die Konvention im selben Modul automatisiert prüfen können.

### B. Status quo ohne Regel-Präzisierung

Rule 17 unverändert lassen, die Tatsache, dass `:core:logging` und `:core:identity` faktisch Android-Libraries sind, undokumentiert lassen.

- Pro: minimaler Doku-Aufwand.
- Contra: schafft eine versteckte Diskrepanz zwischen `.claude/rules.md` (verbietet Android in `:core:*`) und der tatsächlichen Modul-Konfiguration. Jede Code-Review-Runde stolpert darüber. Ein neuer Beitrag — sei es menschlich oder Agent — kann nicht erkennen, ob die aktuelle Konfiguration ein Bug oder gewollt ist. Erste Anzeichen sind bereits in PR-Review-Threads aufgetaucht (Phase A1, Bring-up).
- Contra: macht Rule 17 unzuverlässig — wenn die Regel an zwei Modulen explizit nicht gilt, ohne dass das niedergeschrieben ist, verliert sie auch ihre Schutzwirkung in den Modulen, in denen sie weiter gelten soll.

Verworfen — undokumentierte Ausnahmen sind das Gegenteil dessen, was Rule 17 leisten soll.

### C. Rule 17 selbst aufweichen

Rule 17 in `.claude/rules.md` umformulieren auf "keine Android-SDK-Imports außerhalb von Adapter-Dateien" und damit die Ausnahme direkt in die Hard Rule ziehen.

- Pro: ein einziger Ort, an dem die Regel und ihre Ausnahme stehen.
- Contra: verwischt die Klarheit der Hard Rules. `.claude/rules.md` ist ein Prinzipien-Dokument — kurz, kategorisch, unverhandelbar. ADRs sind der richtige Ort für Detail-Entscheidungen mit Kontext, Begründung und Alternativen. Wer Rule 17 zitiert, soll in einem Satz wissen, was gilt; Details gehören in das ADR-Verzeichnis.
- Contra: die Hard Rule würde durch eine Ausnahme länger und damit psychologisch weicher. Genau das Gegenteil dessen, was eine Hard Rule leisten soll.

Verworfen — Rule 17 bleibt als Grundregel kurz und kategorisch; dieses ADR liefert die formale Ausnahme-Spezifikation.

## Referenzen

- `.claude/rules.md` — Rule 17 (keine Android-SDK-Imports in `:core:*`)
- `docs/architecture.md §4` — Modul-Topologie, Whitelist der Adapter-Module
- `docs/project-state.md` — Phase-A1-Bring-up, Befund zu `:core:logging` und `:core:identity`
- ADR-0002 — verweist auf `:core:identity` als Heimat der gepinnten Static-Keys (Android-Keystore-Adapter folgt demselben Muster)
