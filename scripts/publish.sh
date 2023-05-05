#!/bin/bash

set -e

AWS_DEFAULT_REGION=${AWS_DEFAULT_REGION} AWS_ACCESS_KEY_ID=${AWS_ACCESS_KEY_ID} AWS_SECRET_ACCESS_KEY=${AWS_SECRET_ACCESS_KEY} aws ecr get-login-password | docker login --username AWS --password-stdin 754256621582.dkr.ecr.eu-west-2.amazonaws.com

echo "we are about to tag"
docker tag hmpps-integration-api "${ECR_ENDPOINT}:${APP_VERSION}"
echo "we are about to push"
echo "${ECR_ENDPOINT}:${APP_VERSION}"
docker push "${ECR_ENDPOINT}:${APP_VERSION}"
