#!/bin/bash

set -x

max_retry=20
tries=0

until $(curl --output /dev/null --silent --head --fail http://prison-api:8080/health); do
  if [ $tries -gt $max_retry ]; then
    echo ""
    exit 1
  fi
  ((tries=tries+1))

  printf '.'
  sleep 2
done
