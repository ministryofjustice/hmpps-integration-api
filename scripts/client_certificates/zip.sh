#!/bin/bash
set -e

read_certificate_arguments() {
  echo "Environment: (dev, preprod or prod)"
  read environment
  echo "Client identifier (no spaces, lowercase) that will be used for authorisation: e.g. mapps"
  read client
}

get_api_key() {
  api_key=`kubectl -n hmpps-integration-api-$environment get secrets consumer-api-keys -o json | jq -r .data.$client | base64 -d`
  echo -n $api_key > $environment-$client-api-key
}

generate_symmetric_key() {
  # Create a symmetric key
  head /dev/urandom | sha256sum > symmetric.key
  # Encrypt with client's public key
  openssl pkeyutl -encrypt -pubin -inkey hmpps-integration-api-cred-exchange-public-key.pem -in symmetric.key -out symmetric.key.enc
}

zip_files() {
  # Create a tarball of the access credentials
  tar cvfz hmpps-integration-api-$environment.tar.gz $environment-$client-api-key $environment-$client-client.key $environment-$client-client.pem
  # Encrypt using symmetric key
  openssl enc -aes-256-cbc -pbkdf2 -iter 310000 -md sha256 -salt -in hmpps-integration-api-$environment.tar.gz -out hmpps-integration-api-$environment.tar.gz.enc -pass file:./symmetric.key
}

main() {
  read_certificate_arguments
  get_api_key
  generate_symmetric_key
  zip_files
}

main
