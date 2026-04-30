#!/usr/bin/env bash
# .devcontainer/post-create.sh
# Runs as the 'vscode' user after container creation.
# Baked into the image at /usr/local/share/devcontainer-post-create.sh —
# this ensures the script is available even without a bind-mount.
set -euo pipefail

echo "==> Git safe.directory"
git config --global --add safe.directory /workspace || true

# ── Claude Code (native installer, no Node.js required) ───────────────────────
echo "==> Installing Claude Code (native installer)..."
curl -fsSL https://claude.ai/install.sh | bash

# The native installer puts the binary in ~/.local/bin.
# Add it to PATH for interactive shells and the VS Code integrated terminal.
PROFILE_FILE="${HOME}/.bashrc"
if ! grep -q '\.local/bin' "${PROFILE_FILE}"; then
    echo 'export PATH="$HOME/.local/bin:$PATH"' >> "${PROFILE_FILE}"
fi

# ── Verify ─────────────────────────────────────────────────────────────────────
echo ""
echo "==> Installed versions:"
java -version 2>&1 | head -1
echo "Android SDK: $(sdkmanager --version 2>/dev/null | head -1)"
echo "Claude Code: $(${HOME}/.local/bin/claude --version 2>/dev/null || echo 'verify with: source ~/.bashrc && claude --version')"
gradle --version | head -3 || true
sdkmanager --list_installed | head -20
protoc --version

echo "==> Gradle wrapper pre-download"
if [ -x "./gradlew" ]; then
    ./gradlew --version || true
fi

echo ""
echo "==> Setup complete."
echo "    Tip: 'source ~/.bashrc' or open a new terminal to pick up the PATH update."