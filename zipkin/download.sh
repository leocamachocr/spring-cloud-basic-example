#!/usr/bin/env bash
set -euo pipefail

ZIPKIN_VERSION="${ZIPKIN_VERSION:-3.5.1}"
ZIPKIN_JAR="zipkin-server-${ZIPKIN_VERSION}-exec.jar"
ZIPKIN_URL="https://search.maven.org/remote_content?g=io.zipkin&a=zipkin-server&v=${ZIPKIN_VERSION}&c=exec"
DEST_DIR="$(cd "$(dirname "$0")" && pwd)"
DEST_FILE="${DEST_DIR}/${ZIPKIN_JAR}"

if [ -f "${DEST_FILE}" ]; then
  echo "[zipkin] JAR ya existe: ${DEST_FILE}"
  exit 0
fi

echo "[zipkin] Descargando Zipkin ${ZIPKIN_VERSION}..."
curl -fSL -o "${DEST_FILE}" "${ZIPKIN_URL}"
echo "[zipkin] Descargado en: ${DEST_FILE}"
