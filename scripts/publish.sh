#!/bin/bash

set -e

aws ecr get-login-password --region ${AWS_DEFAULT_REGION} | docker login --username AWS --password-stdin ${AWS_ECR_REGISTRY_ID}.dkr.ecr.${AWS_DEFAULT_REGION}.amazonaws.com

echo "ecr endpoint"
echo $ECR_ENDPOINT

echo "we are about to tag"
docker tag hmpps-integration-api "${ECR_ENDPOINT}:${APP_VERSION}"
echo "we are about to push"
echo "${ECR_ENDPOINT}:${APP_VERSION}"
docker push "${ECR_ENDPOINT}:${APP_VERSION}"
