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
