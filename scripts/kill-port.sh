#!/bin/bash

if [ -z "$1" ]; then
  echo "❌ Uso: ./kill-port-alt.sh <puerto>"
  exit 1
fi

PORT=$1

# Detectar el proceso que usa el puerto con netstat y awk
PID=$(netstat -anp 2>/dev/null | grep ":$PORT " | grep LISTEN | awk '{print $7}' | cut -d'/' -f1 | uniq)

if [ -z "$PID" ]; then
  echo "✅ No hay proceso usando el puerto $PORT"
else
  echo "🛑 Deteniendo proceso en el puerto $PORT (PID: $PID)..."
  kill -9 $PID
  echo "✅ Proceso detenido."
fi
