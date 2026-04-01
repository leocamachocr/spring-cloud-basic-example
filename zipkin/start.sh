#!/usr/bin/env bash
set -euo pipefail

ZIPKIN_VERSION="${ZIPKIN_VERSION:-3.5.1}"
ZIPKIN_JAR="zipkin-server-${ZIPKIN_VERSION}-exec.jar"
SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
JAR_PATH="${SCRIPT_DIR}/${ZIPKIN_JAR}"

# Descargar si no existe
"${SCRIPT_DIR}/download.sh"

echo "[zipkin] Iniciando Zipkin ${ZIPKIN_VERSION} en puerto 9411..."
exec java -jar "${JAR_PATH}" "$@"
