#!/bin/bash

set -e

environment=$1
client=$2

if [ -z $environment ]; then
  echo "Environment must be provided e.g. preprod"
  exit
fi


if [ -z $client ]; then
  echo "Client name must be provided e.g. mapps"
  exit
fi

clean() {
  rm -fr truststore.pem truststore.key $environment-$client-client.pem $environment-$client-client.key $environment-$client-client.csr
}

get_ca() {
  private_key=`kubectl get secret mutual-tls-auth -n hmpps-integration-api-$environment -o json |jq -r '.data."truststore-private-key"'`
  echo -n $private_key | base64 --decode > truststore.key
  public_key=`kubectl get secret mutual-tls-auth -n hmpps-integration-api-$environment -o json |jq -r '.data."truststore-public-key"'`
  echo -n $public_key | base64 --decode > truststore.pem
}

generate_client() {
  openssl genrsa -out $environment-$client-client.key 2048
  openssl req -new -key $environment-$client-client.key -out $environment-$client-client.csr -subj "/C=GB/ST=London/L=London/O=$client"
  openssl x509 -req -in $environment-$client-client.csr -CA truststore.pem -CAkey truststore.key -set_serial 01 -out $environment-$client-client.pem -days 365 -sha256
}

post_clean() {
  rm -fr truststore.pem truststore.key
}

main() {
  clean
  get_ca
  generate_client
  post_clean
}

main
