#!/bin/bash

echo "Searching for running Spring Boot instances..."
echo "----------------------------------------"
printf " %-15s | %-7s | %s\n" "Service" "PID" "Command"
echo "----------------------------------------"

ps aux | grep -E 'spring.application.name=[^ ]+' | grep -v grep | while read -r line
do
    pid=$(echo "$line" | awk '{print $2}')
    cmd=$(echo "$line" | cut -d' ' -f11-)
    name=$(echo "$cmd" | grep -oE 'spring.application.name=[^ ]+' | cut -d= -f2)
    printf " %-15s | %-7s | %s\n" "${name:-<unknown>}" "$pid" "$cmd"
done

echo "----------------------------------------"
