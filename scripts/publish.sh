#!/bin/bash

set -e

echo "Logging into ECR."
aws ecr get-login-password --region ${AWS_DEFAULT_REGION} | docker login --username AWS --password-stdin ${AWS_ECR_REGISTRY_ID}.dkr.ecr.${AWS_DEFAULT_REGION}.amazonaws.com

echo "Tagging Docker image."
docker tag hmpps-integration-api "${ECR_ENDPOINT}:${APP_VERSION}"

echo "Pushing Docker image to ECR."
docker push "${ECR_ENDPOINT}:${APP_VERSION}"
