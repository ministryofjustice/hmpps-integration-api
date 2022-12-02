#!/bin/bash

#set -e 

docker build -t hmpps-integration-api ../

docker tag hmpps-integration-api 754256621582.dkr.ecr.eu-west-2.amazonaws.com/hmpps-integration-api-team/hmpps-integration-api-development-ecr

docker push 754256621582.dkr.ecr.eu-west-2.amazonaws.com/hmpps-integration-api-team/hmpps-integration-api-development-ecr

kubectl -n hmpps-integration-api-development delete -f ../kubectl_deploy

kubectl -n hmpps-integration-api-development apply -f ../kubectl_deploy


