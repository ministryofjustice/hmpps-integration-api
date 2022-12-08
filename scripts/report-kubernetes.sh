#!/bin/sh

# $# Gets the number of args passed
if [ $# -eq 0 ]
then
  echo "You must pass in an environment name e.g. bash ./scripts/report-kubernetes.sh development"
  exit
fi

environment=$1

echo "Ingress:"
echo "__________"
kubectl get ingress -n hmpps-integration-api-$environment
echo ""
echo "Services:"
echo "__________"
kubectl get service -n hmpps-integration-api-$environment
echo ""
echo "Deployments:"
echo "__________"
kubectl get deployment -n hmpps-integration-api-$environment
echo ""
echo "Pods:"
echo "__________"
kubectl get pod -n hmpps-integration-api-$environment
