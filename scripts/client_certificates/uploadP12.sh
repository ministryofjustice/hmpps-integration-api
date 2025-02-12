#!/bin/bash
set -e
client="event-service"

generate_p12(){
  openssl pkcs12 -export -in "$environment"-"$client"-client.pem -inkey "$environment"-"$client"-client.key  -out "$environment"-"$client"-client.p12
}

upload_backup() {
  access_key_id=$(kubectl get secret aws-services -n hmpps-integration-api-$environment -o json | jq -r '.data."api-gateway"' | base64 --decode | jq -r '."access-credentials"."access-key-id"')
  secret_access_key=$(kubectl get secret aws-services -n hmpps-integration-api-$environment -o json | jq -r '.data."api-gateway"' | base64 --decode | jq -r '."access-credentials"."secret-access-key"')
  aws configure set aws_access_key_id $access_key_id
  aws configure set aws_secret_access_key $secret_access_key
  bucket="hmpps-integration-api-$environment-certificates-backup"
  client_folder="$client"
  path="$bucket/$client_folder"

  aws s3 cp ./"$environment"-"$client"-client.p12 "s3://$path/client.p12"

  aws configure set aws_access_key_id ""
  aws configure set aws_secret_access_key ""
}

main() {
  echo "When prompted for the password this needs to match the password in the event-service secret for the environment."
  echo "(This is found by running this command: kubectl -n hmpps-integration-api-{environment} get secrets certificate-store -o json | jq -r '.data.event_service_certificate_password' | base64 -d)"
  echo "-----"
  echo "Environment: (dev, preprod or prod)"

  read environment
  generate_p12
  upload_backup
}

main
