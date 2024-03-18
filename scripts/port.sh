#!/bin/sh

PORT=$1

PID=$(lsof -t -i:$PORT)

echo "The process ID of the process using port $PORT is $PID"