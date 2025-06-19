#!/usr/bin/env bash

# Ir al directorio del script
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

# Cargar variables del archivo .env en el directorio raíz del proyecto
ENV_FILE="$SCRIPT_DIR/../.env"
if [ -f "$ENV_FILE" ]; then
  export $(grep ZIPKIN_PORT "$ENV_FILE" | xargs)
fi

ZIPKIN_PORT=${ZIPKIN_PORT:-9411}
ZIPKIN_JAR="$SCRIPT_DIR/../zipkin/zipkin-server-3.5.1-exec.jar"

# Verifica si el JAR existe
if [ ! -f "$ZIPKIN_JAR" ]; then
  echo "❌ No se encontró $ZIPKIN_JAR"
  exit 1
fi

# Inicia Zipkin
echo "🚀 Iniciando Zipkin en el puerto $ZIPKIN_PORT..."
exec java -jar "$ZIPKIN_JAR" --server.port="$ZIPKIN_PORT"
