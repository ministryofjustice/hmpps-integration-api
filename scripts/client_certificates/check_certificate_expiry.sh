#!/bin/bash

read_certificate_arguments() {
  echo "Environment: (dev, preprod or prod)"
  read environment
  echo "Client identifier (no spaces, lowercase) that will be used for authorisation: e.g. mapps"
  read client
}

check_certificate_expiry() {
  access_key_id=$(kubectl get secret aws-services -n hmpps-integration-api-$environment -o json | jq -r '.data."api-gateway"' | base64 --decode | jq -r '."access-credentials"."access-key-id"')
  secret_access_key=$(kubectl get secret aws-services -n hmpps-integration-api-$environment -o json | jq -r '.data."api-gateway"' | base64 --decode | jq -r '."access-credentials"."secret-access-key"')
  aws configure set aws_access_key_id "$access_key_id"
  aws configure set aws_secret_access_key "$secret_access_key"
  bucket="hmpps-integration-api-$environment-certificates-backup"
  client_folder="$client"
  path="$bucket/$client_folder"
  local_dir="./scripts/client_certificates"
  certificate="$local_dir/$environment-$client-client.pem"
  days_before_expiry=30

  mkdir -p "$local_dir"

  aws s3 cp "s3://$path/client.pem" "$certificate"
  echo "Client certificate downloaded to: $certificate"

  expiration_date=$(openssl x509 -in "$certificate" -noout -enddate | awk -F= '{print $2}')

  if [ -z "$expiration_date" ]; then
    echo "Error: Could not extract the expiration date from the certificate."
    exit 1
  fi
  echo "Certificate Expiration Date: $expiration_date"

  expiration_date=$(date -jf "%b %e %T %Y %Z" "$expiration_date" +"%s" 2>/dev/null || date -d "$expiration_date" +"%s" 2>/dev/null)
  current_date=$(date +"%s")

  days_until_expiry=$(( (expiration_date - current_date) / 86400 ))

  if [ "$days_until_expiry" -lt "$days_before_expiry" ]; then
    echo "This certificate will expire in $days_until_expiry days. Please renew."
  else
    echo "This certificate is not close to expiration."
  fi
}

main() {
  read_certificate_arguments
  check_certificate_expiry
}

main
