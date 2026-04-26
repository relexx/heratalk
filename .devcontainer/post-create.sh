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
