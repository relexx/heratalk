FROM mcr.microsoft.com/devcontainers/java:25-bookworm

# --- Versionen (bei Update anpassen) ---
ARG ANDROID_CMDLINE_TOOLS_VERSION=14742923
ARG ANDROID_PLATFORM=android-36
ARG ANDROID_BUILD_TOOLS=36.0.0
ARG ANDROID_NDK_VERSION=28.2.13676358
ARG GRADLE_VERSION=9.4.1
ARG PROTOC_VERSION=28.3

# ── Android SDK ────────────────────────────────────────────────────────────────
# JAVA_HOME is already set by the base image (mcr.microsoft.com/devcontainers/java:21-bookworm).
# We only define Android/Gradle paths here.
ENV DEBIAN_FRONTEND=noninteractive \
    ANDROID_HOME=/opt/android-sdk \
    ANDROID_SDK_ROOT=/opt/android-sdk \
    ANDROID_NDK_HOME=/opt/android-sdk/ndk/${ANDROID_NDK_VERSION} \
    GRADLE_USER_HOME=/home/vscode/.gradle

ENV PATH="${ANDROID_HOME}/cmdline-tools/latest/bin:${ANDROID_HOME}/platform-tools:${ANDROID_HOME}/build-tools/${ANDROID_BUILD_TOOLS}:/opt/gradle/bin:/home/vscode/.local/bin:${PATH}"

# Additional system packages for Android tooling
RUN apt-get update && apt-get install -y --no-install-recommends \
        wget unzip \
        ca-certificates curl zip git git-lfs \
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

# Bootstrap Android SDK Command-Line Tools
# Build number: verify at https://developer.android.com/studio#command-tools
RUN mkdir -p "${ANDROID_HOME}/cmdline-tools" \
    && wget -q \
        "https://dl.google.com/android/repository/commandlinetools-linux-${ANDROID_CMDLINE_TOOLS_VERSION}_latest.zip" \
        -O /tmp/cmdtools.zip \
    && unzip -q /tmp/cmdtools.zip -d /tmp/cmdtools_extract \
    && mv /tmp/cmdtools_extract/cmdline-tools "${ANDROID_HOME}/cmdline-tools/latest" \
    && rm -rf /tmp/cmdtools.zip /tmp/cmdtools_extract

# Accept licenses and install SDK packages
# compileSdk/targetSdk 36 (Android 16), minSdk 29 (Android 10)
RUN yes | sdkmanager --licenses > /dev/null \
    && sdkmanager \
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

# Hand ownership to vscode user so sdkmanager works from the terminal later
RUN chown -R vscode:vscode "${ANDROID_HOME}"

# Workspace-Verzeichnis mit korrektem Owner anlegen —
# Docker übernimmt diesen beim ersten Volume-Mount, verhindert EACCES-Fehler
RUN mkdir -p /workspace && chown vscode:vscode /workspace

# ── post-create script (baked into image so it's available without a bind-mount)
COPY post-create.sh /usr/local/share/devcontainer-post-create.sh
RUN chmod +x /usr/local/share/devcontainer-post-create.sh
COPY post-start.sh /usr/local/share/devcontainer-post-start.sh
RUN chmod +x /usr/local/share/devcontainer-post-start.sh

USER vscode
WORKDIR /workspace