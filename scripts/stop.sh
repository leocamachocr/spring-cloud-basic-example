#!/bin/bash

PORT="8080"
# Obtener el ID del proceso que utiliza el puerto 8080
PID=$(netstat -ano | awk '$2~/^.*:8080$/ {print $5}')

# Comprobar si hay un proceso en ejecución en el puerto 8080
if [ -z "$PID" ]
then
    echo "No se encontró ningún proceso en ejecución en el puerto ""$PORT"
else
    # Detener el proceso
    echo "Deteniendo proceso en el puerto 8080... ""$PID"
    taskkill  //F //PID "$PID"
    echo "Proceso detenido correctamente."
fi

PORT="8761"
# Obtener el ID del proceso que utiliza el puerto 8080
PID=$(netstat -ano | awk '$2~/^.*:8761$/ {print $5}')

# Comprobar si hay un proceso en ejecución en el puerto 8080
if [ -z "$PID" ]
then
    echo "No se encontró ningún proceso en ejecución en el puerto ""$PORT"
else
    # Detener el proceso
    echo "Deteniendo proceso en el puerto 8761... ""$PID"
    taskkill  //F //PID "$PID"
    echo "Proceso detenido correctamente."
fi

