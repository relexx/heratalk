# HeraTalk — DevContainer-Setup

> Stand: April 2026 — aktualisiert auf AGP 9.2, Gradle 9.4, Kotlin 2.3.21, NDK r28+, Android SDK 36 (Android 16), Claude Code nativ.

## Ziel

Reproduzierbare Entwicklungsumgebung für VS Code Dev Containers und Claude Code. Der Container enthält alles, was zum Bauen von HeraTalk nötig ist; Debugging läuft auf einem physischen Android-Gerät über ADB-over-TCP vom Host.

## Scope-Entscheidung

- **Build und statische Analyse im Container:** Gradle, Lint, Unit-Tests, Protobuf, NDK-Build für libopus.
- **Debugging auf physischem Gerät:** Container enthält `adb`, verbindet sich per TCP zum ADB-Server auf dem Host (USB- oder WLAN-Debugging). Kein Emulator im Container (KVM-Passthrough plattformübergreifend unzuverlässig).
- **APK-Signierung:** Debug-Keystore im Container, Release-Keystore extern (GitHub Actions Secret).

## Anforderungen an den Host

| Host-OS | Voraussetzungen |
|---------|-----------------|
| Linux | Docker oder Podman; `adb start-server` auf Host; Gerät per USB oder WLAN |
| macOS | Docker Desktop oder Colima; `adb start-server` auf Host |
| Windows | Docker Desktop + WSL2; ADB-Server in Windows oder WSL |

Container erreicht Host-ADB über `host.docker.internal:5037` (oder Linux-Gateway-IP).

## Versionsmatrix (April 2026)

| Tool | Version | Quelle |
|------|---------|--------|
| Ubuntu | 24.04 LTS | devcontainer-base |
| JDK | 21 (Temurin) | Gradle 9 empfiehlt JDK 21 |
| Kotlin | 2.3.21 | über AGP 9 built-in |
| Gradle | 9.4.1 | gradle-wrapper |
| Android Gradle Plugin | 9.2.0 | built-in Kotlin, keine `kotlin-android`-Apply-Zeile mehr |
| Android SDK Platform | 36 (Android 16) | Google Play ab August 2026 Pflicht |
| Android Build-Tools | 36.0.0 | — |
| Android NDK | 28.2.13676358 | 16-KB-Alignment Pflicht seit Nov 2025 |
| CMake | 3.22.1 (vom NDK) | — |
| Protoc | 28.3 | Control-Plane-Protobuf |
| Claude Code | latest via nativer Installer | keine Node.js-Abhängigkeit mehr |
| Koin | 4.2.0 | — |
| Jetpack Compose | BOM 2026.04.xx | — |

## Dateistruktur

```
.devcontainer/
├── devcontainer.json
├── Dockerfile
├── post-create.sh
└── post-start.sh
```

## `.devcontainer/Dockerfile`

```dockerfile
FROM mcr.microsoft.com/devcontainers/base:ubuntu-24.04

# --- Versionen (bei Update anpassen) ---
ARG JAVA_VERSION=21
ARG ANDROID_CMDLINE_TOOLS_VERSION=13114758
ARG ANDROID_PLATFORM=android-36
ARG ANDROID_BUILD_TOOLS=36.0.0
ARG ANDROID_NDK_VERSION=28.2.13676358
ARG GRADLE_VERSION=9.4.1
ARG PROTOC_VERSION=28.3

ENV DEBIAN_FRONTEND=noninteractive \
    ANDROID_HOME=/opt/android-sdk \
    ANDROID_SDK_ROOT=/opt/android-sdk \
    ANDROID_NDK_HOME=/opt/android-sdk/ndk/${ANDROID_NDK_VERSION} \
    JAVA_HOME=/usr/lib/jvm/java-${JAVA_VERSION}-openjdk-amd64 \
    GRADLE_USER_HOME=/home/vscode/.gradle \
    PATH=/opt/android-sdk/cmdline-tools/latest/bin:/opt/android-sdk/platform-tools:/opt/android-sdk/build-tools/${ANDROID_BUILD_TOOLS}:/opt/gradle/bin:/home/vscode/.local/bin:${PATH}

# System packages
RUN apt-get update && apt-get install -y --no-install-recommends \
        openjdk-${JAVA_VERSION}-jdk-headless \
        ca-certificates curl unzip zip git git-lfs \
        cmake ninja-build build-essential pkg-config \
        libglu1-mesa libpulse0 libxi6 libxrender1 libxtst6 \
        libxcursor1 libxdamage1 libxrandr2 \
        python3 python3-pip jq less man-db tree ripgrep fd-find \
    && rm -rf /var/lib/apt/lists/*

# Gradle (Wrapper nutzt dies selten, aber gut als Fallback)
RUN curl -fsSL "https://services.gradle.org/distributions/gradle-${GRADLE_VERSION}-bin.zip" -o /tmp/gradle.zip \
    && unzip -q /tmp/gradle.zip -d /opt \
    && ln -s /opt/gradle-${GRADLE_VERSION} /opt/gradle \
    && rm /tmp/gradle.zip

# Android SDK command-line tools
RUN mkdir -p ${ANDROID_HOME}/cmdline-tools \
    && curl -fsSL "https://dl.google.com/android/repository/commandlinetools-linux-${ANDROID_CMDLINE_TOOLS_VERSION}_latest.zip" -o /tmp/sdk.zip \
    && unzip -q /tmp/sdk.zip -d ${ANDROID_HOME}/cmdline-tools \
    && mv ${ANDROID_HOME}/cmdline-tools/cmdline-tools ${ANDROID_HOME}/cmdline-tools/latest \
    && rm /tmp/sdk.zip

# SDK-Komponenten
RUN yes | sdkmanager --licenses >/dev/null \
    && sdkmanager --install \
        "platform-tools" \
        "platforms;${ANDROID_PLATFORM}" \
        "build-tools;${ANDROID_BUILD_TOOLS}" \
        "ndk;${ANDROID_NDK_VERSION}" \
        "cmake;3.22.1"

# Protobuf compiler
RUN curl -fsSL "https://github.com/protocolbuffers/protobuf/releases/download/v${PROTOC_VERSION}/protoc-${PROTOC_VERSION}-linux-x86_64.zip" -o /tmp/protoc.zip \
    && unzip -q /tmp/protoc.zip -d /opt/protoc \
    && ln -s /opt/protoc/bin/protoc /usr/local/bin/protoc \
    && rm /tmp/protoc.zip

# Owner-Fix für SDK
RUN chown -R vscode:vscode ${ANDROID_HOME}

USER vscode
WORKDIR /workspaces
```

## `.devcontainer/devcontainer.json`

```json
{
  "name": "HeraTalk",
  "build": { "dockerfile": "Dockerfile" },
  "runArgs": ["--init"],
  "remoteEnv": {
    "ADB_SERVER_SOCKET": "tcp:host.docker.internal:5037"
  },
  "mounts": [
    "source=heratalk-gradle-cache,target=/home/vscode/.gradle,type=volume",
    "source=heratalk-android-config,target=/home/vscode/.android,type=volume",
    "source=heratalk-claude-config,target=/home/vscode/.claude,type=volume"
  ],
  "customizations": {
    "vscode": {
      "extensions": [
        "fwcd.kotlin",
        "vscjava.vscode-gradle",
        "zxh404.vscode-proto3",
        "redhat.vscode-yaml",
        "DavidAnson.vscode-markdownlint",
        "tamasfe.even-better-toml",
        "eamodio.gitlens",
        "streetsidesoftware.code-spell-checker",
        "streetsidesoftware.code-spell-checker-german"
      ],
      "settings": {
        "editor.formatOnSave": true,
        "editor.tabSize": 4,
        "editor.rulers": [120],
        "files.insertFinalNewline": true,
        "files.trimTrailingWhitespace": true,
        "[kotlin]": { "editor.defaultFormatter": "fwcd.kotlin" },
        "kotlin.languageServer.enabled": true,
        "gradle.nestedProjects": true
      }
    }
  },
  "postCreateCommand": "bash .devcontainer/post-create.sh",
  "postStartCommand": "bash .devcontainer/post-start.sh",
  "remoteUser": "vscode",
  "features": {
    "ghcr.io/devcontainers/features/common-utils:2": {
      "installZsh": true,
      "configureZshAsDefaultShell": false
    },
    "ghcr.io/devcontainers/features/github-cli:1": {}
  }
}
```

## `.devcontainer/post-create.sh`

```bash
#!/usr/bin/env bash
# One-time setup after container build. Idempotent.
set -euo pipefail

echo "==> Git safe.directory"
git config --global --add safe.directory /workspaces/heratalk || true

echo "==> Toolchain check"
java -version
gradle --version | head -3 || true
sdkmanager --list_installed | head -20
protoc --version

echo "==> Install Claude Code (native installer)"
# Seit Oktober 2025 ist der native Installer der empfohlene Weg.
# Keine Node.js-Abhängigkeit mehr. Auto-Update im Hintergrund.
if ! command -v claude &>/dev/null; then
    curl -fsSL https://claude.ai/install.sh | bash
fi
claude --version || true

echo "==> Gradle wrapper pre-download"
if [ -x "./gradlew" ]; then
    ./gradlew --version || true
fi

echo "==> Ready."
```

## `.devcontainer/post-start.sh`

```bash
#!/usr/bin/env bash
# Runs on every container start. Reconnects ADB.
set -euo pipefail

echo "==> Starting ADB"
adb kill-server >/dev/null 2>&1 || true

if [ -n "${ADB_SERVER_SOCKET:-}" ]; then
    echo "==> ADB_SERVER_SOCKET=${ADB_SERVER_SOCKET}"
fi

adb devices || true
```

## ADB-Brücke zum Host

### 1. `host.docker.internal` (Docker Desktop / macOS / Windows)

`ADB_SERVER_SOCKET=tcp:host.docker.internal:5037` — funktioniert out-of-the-box, sobald auf dem Host `adb start-server` lief.

### 2. ADB-over-WLAN (Alle Plattformen, Android 11+)

Auf dem Gerät "Wireless Debugging" aktivieren, Pairing-Code einscannen:

```bash
adb pair <phone-ip>:<pair-port>
adb connect <phone-ip>:<connect-port>
```

### 3. Linux-Host mit Socket-Forward

```bash
# auf dem Host
adb -a -P 5037 nodaemon server
```

## CI-Parität

GitHub Actions nutzt denselben Dockerfile. Was im DevContainer baut, baut auch in CI. Siehe `docs/github-repo.md §CI-Parität`.

## Build-Kommandos

```bash
./gradlew assembleDebug testDebug                     # Build + Unit Tests
./gradlew lintDebug detekt spotlessCheck              # Static Analysis
./gradlew installDebug                                # auf Gerät installieren
./gradlew :service:audio:externalNativeBuildDebug     # NDK-Teil einzeln
./gradlew generateProto                                # Protobuf-Sourcen
./gradlew check16kbAlignment                           # Eigener Task (CI-Gate für APK-Alignment)
```

## Claude Code nutzen

Im Container-Terminal:

```bash
claude                  # Claude Code starten
claude --version        # Version prüfen
claude doctor           # Diagnose (PATH, Auth, Config)
claude --continue       # vorherige Session fortsetzen
```

Claude Code liest automatisch `.claude/CLAUDE.md` und die Agenten unter `.claude/agents/` beim Start der Session im aktuellen Verzeichnis.

## Ressourcen-Empfehlung

- 4 CPU-Cores, 8 GB RAM
- Erster Build ~5–10 min, folgende inkremental

## Einschränkungen

- **Kein Emulator im Container.** Für Emulator-Tests: Host-Emulator + `host.docker.internal:5554`-Bridge. Besser: echtes Gerät (Audio-Latenz-Messung).
- **macOS Apple Silicon:** Dockerfile ist `linux/amd64`, läuft auf M1/M2/M3 via Rosetta. Akzeptabel für reine Build-Arbeit, ggf. spürbar langsamer.
- **16-KB-Alignment-Verifikation** auf physisches Gerät mit Android 15+ oder Emulator-Image mit 16-KB-Option — im Container nicht möglich.
