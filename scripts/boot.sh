#!/bin/bash

set -e

echo "Yes we are booting!"

java -XX:+AlwaysActAsServerClassMachine -javaagent:/app/agent.jar -jar /app/app.jar