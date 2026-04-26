#!/usr/bin/env bash
# Runs on every container start. Reconnects ADB.
set -euo pipefail

echo "==> Starting ADB"
adb kill-server >/dev/null 2>&1 || true

if [ -n "${ADB_SERVER_SOCKET:-}" ]; then
    echo "==> ADB_SERVER_SOCKET=${ADB_SERVER_SOCKET}"
fi

adb devices || true
