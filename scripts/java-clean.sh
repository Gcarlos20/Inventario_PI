#!/bin/sh

# Ejecuta Java fuera del entorno de bibliotecas heredado por VS Code Snap.
exec /usr/bin/env -i \
    HOME="${HOME:-/home/carlosev}" \
    PATH="/usr/bin:/bin" \
    JAVA_HOME="/usr/lib/jvm/java-21-openjdk-amd64" \
    DISPLAY="${DISPLAY:-}" \
    XAUTHORITY="${XAUTHORITY:-}" \
    XDG_RUNTIME_DIR="${XDG_RUNTIME_DIR:-}" \
    DBUS_SESSION_BUS_ADDRESS="${DBUS_SESSION_BUS_ADDRESS:-}" \
    DB_PASSWORD="${DB_PASSWORD:-}" \
    /usr/lib/jvm/java-21-openjdk-amd64/bin/java "$@"
