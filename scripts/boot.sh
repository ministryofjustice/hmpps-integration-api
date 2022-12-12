#!/bin/sh

set -ex

java -XX:+AlwaysActAsServerClassMachine -javaagent:/app/agent.jar -jar /app/app.jar
