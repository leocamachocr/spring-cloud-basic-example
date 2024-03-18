#!/bin/sh
APP_LOGS=../logs
APP=$1
PORT=$2
if [ -z "$PORT" ]
then
      PORT=8080 # Replace 8080 with your default port
fi

check_app_status() {
  url=$1"/actuator/health/liveness"
  app=$2
  while true; do
    # shellcheck disable=SC1083
    if [ "$(curl -s -o /dev/null -w ''%{http_code}'' "$url")" = "200" ]; then
      echo "$app"" ha iniciado correctamente en la dirección ""$1"
      break
    else
      echo "Verificando ""$app"
      sleep 1
    fi
  done
}





cd $APP
./gradlew clean build > $APP_LOGS/build-$APP-$PORT.log 2>&1
check_app_status "http://localhost:$PORT" $APP &
./gradlew bootRun --args='--server.port='$PORT > $APP_LOGS/$APP-$PORT.log 2>&1

