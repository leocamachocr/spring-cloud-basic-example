#!/bin/sh

# === Load .env variables into environment ===
ENV_FILE=".env"
if [ -f "$ENV_FILE" ]; then
  echo "Loadinv environment variables .env..."
  export $(grep -v '^#' "$ENV_FILE" | xargs)
fi


# === Configuration ===
APP_LOGS=../logs
APP=$1
PORT=$2

# === Help section ===
if [ -z "$APP" ]; then
  echo "Usage: $0 <app-folder> [port]"
  exit 1
fi

# === OS detection ===
is_windows() {
  case "$(uname -s)" in
    MINGW*|MSYS*|CYGWIN*) return 0 ;;
    *) return 1 ;;
  esac
}

# === Check if port is in use ===
is_port_in_use() {
  PORT_TO_CHECK=$1
  if is_windows; then
    netstat -ano | grep -q ":$PORT_TO_CHECK "
  else
    ss -tuln | grep -q ":$PORT_TO_CHECK" || netstat -tuln | grep -q ":$PORT_TO_CHECK"
  fi
}

# === Find a free random port in range 8000-9000 ===
get_random_free_port() {
  for i in $(seq 1 100); do
    CANDIDATE=$(shuf -i 8000-9000 -n 1)
    if ! is_port_in_use $CANDIDATE ; then
      echo $CANDIDATE
      return
    fi
  done
  echo "No free ports available in range 8000-9000"
  exit 1
}
get_random_free_port_for_debug() {
  for i in $(seq 1 100); do
    CANDIDATE=$(shuf -i 50000-60000 -n 1)
    if ! is_port_in_use $CANDIDATE ; then
      echo $CANDIDATE
      return
    fi
  done
  echo "No free ports available in range 5000-6000"
  exit 1
}

# === Wait until app is alive ===
check_app_status() {
  url=$1"/actuator/health/liveness"
  app=$2
  while true; do
    if [ "$(curl -s -o /dev/null -w ''%{http_code}'' "$url")" = "200" ]; then
      echo "$app has started correctly at $1"
      break
    else
      echo "Checking $app at $url..."
      sleep 1
    fi
  done
}

cd "$APP" || exit 1
mkdir -p "$APP_LOGS"
TIMESTAMP=$(date +%Y%m%d-%H%M%S)

# === Build the project and log output ===
BUILD_LOG="$APP_LOGS/build-$APP-$TIMESTAMP.log"
echo "Building $APP... Logs will be written to $BUILD_LOG"
./gradlew clean build > "$BUILD_LOG" 2>&1

# === Special case: Eureka service ===
if [ "$APP" = "eureka" ]; then
  PORT=8761
  if is_port_in_use $PORT ; then
    echo "Eureka is already running on port $PORT. Skipping duplicate instance."
    exit 0
  fi
  export SERVER_PORT=$PORT
  RUN_LOG="$APP_LOGS/$APP-$PORT-$TIMESTAMP.log"
  echo "Starting $APP on port $PORT... Logs: $RUN_LOG"
  nohup ./gradlew bootRun > "$RUN_LOG" 2>&1 &
  echo "$APP started on port $PORT with PID $!"
  check_app_status "http://localhost:$PORT" $APP
  exit 0
fi

# === For other services: pick or generate a port ===
if [ -z "$PORT" ]; then
  PORT=$(get_random_free_port)
  echo "No port provided. Using random free port: $PORT"
else
  echo "Using provided port: $PORT"
fi

# === Enable remote debugging ===
DEBUG_PORT=$(get_random_free_port_for_debug)
echo "Exponiendo puerto de depuración: $DEBUG_PORT"

# PASAR EL PUERTO DE DEPURACIÓN COMO PROPIEDAD DE SISTEMA A GRADLE
# Esto será capturado por el 'build.gradle' en la sección bootRun { jvmArgs [...] }
GRADLE_DEBUG_ARGS="-DdebugPort=$DEBUG_PORT"

export SERVER_PORT=$PORT
RUN_LOG="$APP_LOGS/$APP-$PORT-$TIMESTAMP.log"
echo "Starting $APP on port $PORT... Logs: $RUN_LOG"
nohup ./gradlew bootRun "$GRADLE_DEBUG_ARGS" > "$RUN_LOG" 2>&1 &
echo "$APP started on port $PORT with PID $!"


check_app_status "http://localhost:$PORT" $APP
