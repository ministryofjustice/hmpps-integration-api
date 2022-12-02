#!/bin/sh

set -ex

echo "Yes we are booting!"
env

java -XX:+AlwaysActAsServerClassMachine -javaagent:/app/agent.jar -jar /app/app.jar