#!/bin/sh
# Definir la ubicación de la aplicación Spring Boot
APP_LOGS=../logs
APP_HOME_GATEWAY=../gateway
APP_GATEWAY=gateway
APP_GATEWAY_PORT=8080

APP_HOME_EUREKA=../eureka
APP_EUREKA=eureka
APP_EUREKA_PORT=8761


check_app_status() {
  url=$1"/actuator/health/liveness"
  app=$2
  while true; do
    # shellcheck disable=SC1083
    if [ "$(curl -s -o /dev/null -w ''%{http_code}'' $url)" = "200" ]; then
      echo "$app"" ha iniciado correctamente en la dirección ""$1"
      break
    else
      echo "Verificando "$app
      sleep 1
    fi
  done
}


start_app(){
HOME=$1
APP=$2
PORT=$3
echo "Iniciando "$APP" en la ruta "$HOME"  en el puerto "$PORT
# Compilar la aplicación con Maven
cd $HOME
mvn clean package
# Iniciar la aplicación Spring Boot en segundo plano
./mvnw spring-boot:run  -Dspring-boot.run.arguments=--server.port=$PORT > $APP_LOGS/$APP.log 2>&1 &

check_app_status "http://localhost:$PORT" "Gateway"

# Mostrar un mensaje de que la aplicación está lista
echo "La aplicación $APP está lista para recibir solicitudes."
PID=$(netstat -ano | awk '$2~/^.*:$APP_GATEWAY_PORT$/ {print $5}')
echo "$APP"":""$PORT""->""$PID"

pwd
}
ROOT=$(pwd)
start_app $APP_HOME_EUREKA $APP_EUREKA $APP_EUREKA_PORT
cd ROOT
start_app $APP_HOME_GATEWAY $APP_GATEWAY $APP_GATEWAY_PORT
cd ROOT

