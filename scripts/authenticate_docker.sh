#!/bin/bash

set -eo pipefail

if [[ -n "${DOCKER_USERNAME}" && -n "${DOCKER_PASSWORD}" ]]; then
  docker login --username ${DOCKER_USERNAME} --password ${DOCKER_PASSWORD}
fi
