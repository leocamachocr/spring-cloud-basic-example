#!/bin/sh
APP_LOGS=../logs
APP=$1
PORT=$2

check_app_status() {
  url=$1"/actuator/health/liveness"
  app=$2
  while true; do
    if [ "$(curl -s -o /dev/null -w ''%{http_code}'' "$url")" = "200" ]; then
      echo "$app"" has started correctly at the address ""$1"
      break
    else
      echo "Checking ""$app"
      sleep 1
    fi
  done
}

cd $APP
./gradlew clean build > $APP_LOGS/build-$APP-$PORT.log 2>&1

if [ -z "$PORT" ]
then
  ./gradlew bootRun
else
  export SERVER_PORT=$PORT
  ./gradlew bootRun
fi