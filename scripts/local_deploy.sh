#!/bin/bash

# This script will compile and deploy your current local version of the API
# Mainly used for code spikes and debugging, removing the need to commit and revert changes to Git
# Running the CI/CD pipeline will reset these changes

set -e

env=${1:-development}
namespace=hmpps-integration-api-$env
repo_host="754256621582.dkr.ecr.eu-west-2.amazonaws.com"
repo_url="${repo_host}/hmpps-integration-api-admin-team/hmpps-integration-api-$env-ecr"

authenticate_docker() {
  export AWS_ACCESS_KEY_ID=`kubectl get secret ecr-repo-hmpps-integration-api-$env -o json -n $namespace |jq -r '.data.access_key_id' |base64 --decode`
  export AWS_SECRET_ACCESS_KEY=`kubectl get secret ecr-repo-hmpps-integration-api-$env -o json -n $namespace |jq -r '.data.secret_access_key' |base64 --decode`
  export AWS_DEFAULT_REGION=eu-west-2

  aws ecr get-login-password --region eu-west-2 | docker login --username AWS --password-stdin $repo_host
}

build_image() {
  docker build --platform linux/amd64 -t $namespace .
  docker tag $namespace "${repo_url}:latest"
}

push_image() {
  docker push "${repo_url}:latest"
}

override_image_tag() {
  kubectl set image deployment/hmpps-integration-api hmpps-integration-api=${repo_url}:latest -n $namespace
}

deploy() {
  kubectl scale deployment/hmpps-integration-api --replicas=0 -n $namespace
  kubectl scale deployment/hmpps-integration-api --replicas=2 -n $namespace
}

main() {
  authenticate_docker
  build_image
  push_image
  override_image_tag
  deploy
}

main
