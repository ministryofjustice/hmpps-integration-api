#!/bin/bash
set -e

clean() {
  rm -fr *.pem *.key *.csr
}

read_certificate_arguments() {
  echo "Environment: (dev, preprod or prod)"
  read environment
  echo "Client organisation: e.g. Home Office"
  read organisation
  echo "Client region: e.g. London"
  read region
  echo "Client identifier (no spaces, lowercase) that will be used for authorisation: e.g. mapps"
  read client
}

get_ca() {
  private_key=`kubectl get secret mutual-tls-auth -n hmpps-integration-api-$environment -o json |jq -r '.data."truststore-private-key"'`
  echo -n $private_key | base64 --decode > truststore.key
  public_key=`kubectl get secret mutual-tls-auth -n hmpps-integration-api-$environment -o json |jq -r '.data."truststore-public-key"'`
  echo -n $public_key | base64 --decode > truststore.pem
}

generate_client() {
  openssl genrsa -out $environment-$client-client.key 2048
  openssl req -new -key $environment-$client-client.key -out $environment-$client-client.csr -subj "/C=GB/ST=$region/L=$region/O=$organisation/CN=$client"
  openssl x509 -req -in $environment-$client-client.csr -CA truststore.pem -CAkey truststore.key -set_serial 01 -out $environment-$client-client.pem -days 365 -sha256
}

clean_ca() {
  rm -fr truststore.pem truststore.key
}

success_message() {
  echo
  echo "Success: your client certificates have been generated in ./scripts/client_certificates"
}

upload_backup() {
  access_key_id=$(kubectl get secret aws-services -n hmpps-integration-api-dev -o json | jq -r '.data."api-gateway"' | base64 --decode | jq -r '."access-credentials"."access-key-id"')
  secret_access_key=$(kubectl get secret aws-services -n hmpps-integration-api-dev -o json | jq -r '.data."api-gateway"' | base64 --decode | jq -r '."access-credentials"."secret-access-key"')
  bucket="hmpps-integration-api-$environment-certificates-backup"
  client_folder="$client"
  path="$bucket/$client_folder"
  file_path="./${environment}-${client}-client.pem"

  aws s3api put-object --bucket "$bucket" --key "$client_folder/" --acl private
  aws s3 cp "$file_path" "s3://$path/client.pem"
  aws s3 cp ./"$environment"-"$client"-client.pem "s3://$path/client.pem"
  aws s3 cp ./truststore.key "s3://$path/truststore.key"
  aws s3 cp ./truststore.pem "s3://$path/truststore.pem"
}

main() {
  clean
  read_certificate_arguments
  get_ca
  generate_client
  success_message
  upload_backup
  clean_ca
  trap clean_ca EXIT
  trap clean_ca SIGINT
}

main
