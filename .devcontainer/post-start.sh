#!/usr/bin/env bash
# Runs on every container start. Reconnects ADB if available on the host.
set -euo pipefail

echo "==> Checking ADB connectivity"

if [ -n "${ADB_SERVER_SOCKET:-}" ]; then
    echo "    ADB_SERVER_SOCKET=${ADB_SERVER_SOCKET}"
    if adb devices >/dev/null 2>&1; then
        echo "    ADB connected:"
        adb devices
    else
        echo "    ADB server not reachable at ${ADB_SERVER_SOCKET}."
        echo "    To connect a device, start ADB on the host first:"
        echo "      adb -a nodaemon server start"
        echo "    Then reopen the container or run: adb devices"
    fi
else
    echo "    ADB_SERVER_SOCKET not set — skipping remote ADB."
    echo "    Set it in devcontainer.json if you need device access."
fi
