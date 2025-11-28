#!/bin/bash
set -e

clean() {
  rm -fr *.pem *.key *.csr
}

read_certificate_arguments() {
  echo "Environment: (dev, preprod or prod)"
  read environment

  echo "Client identifier (no spaces, lowercase) that will be used for authorisation: e.g. mapps"
  read client

  echo "Do you wish to use a CSR that has been supplied by the client?"
  echo "Selecting No will generate a CSR and private key"
  echo "(y)es, (n)o, or (c)ancel:"
  read user_input

  case $user_input in
    [yY])
      echo "User selected 'Yes'."
      csr_required=1
      ;;
    [nN])
      echo "User selected 'No'."
      csr_required=0
      ;;
    [cC])
      echo "User selected 'Cancel'."
      exit 1
      ;;
    *)
      echo "Invalid choice."
      exit 1
  esac

  if [[ "$csr_required" == 0 ]]
  then
    echo "Client organisation: e.g. Home Office"
    read organisation

    echo "Client region: e.g. London"
    read region

  else
    echo "Does the CSR exist in the following location?"
    echo "./csrs/$environment/$client/$environment-$client-client.csr"
    echo "(y)es, (n)o, or (c)ancel:"
    read does_csr_exist
    case $does_csr_exist in
      [yY])
        ;;
      [nN])
        echo "Please place the CSR at the following location."
        echo "./csrs/$environment/$client/$environment-$client-client.csr"
        exit 1
        ;;
      [cC])
        echo "User selected 'Cancel'."
        exit 1
        ;;
      *)
        echo "Invalid choice."
        exit 1
    esac
  fi
}

get_ca() {
  private_key=`kubectl get secret mutual-tls-auth -n hmpps-integration-api-$environment -o json |jq -r '.data."truststore-private-key"'`
  echo -n $private_key | base64 --decode > truststore.key
  public_key=`kubectl get secret mutual-tls-auth -n hmpps-integration-api-$environment -o json |jq -r '.data."truststore-public-key"'`
  echo -n $public_key | base64 --decode > truststore.pem
}

generate_client() {

  if [[ "$csr_required" == 0 ]]
  then
    openssl genrsa -out $environment-$client-client.key 2048
    openssl req -new -key $environment-$client-client.key -out $environment-$client-client.csr -subj "/C=GB/ST=$region/L=$region/O=$organisation/CN=$client"
  else
    cp ./csrs/$environment/$client/$environment-$client-client.csr .
  fi
  openssl x509 -req -in $environment-$client-client.csr -CA truststore.pem -CAkey truststore.key -out $environment-$client-client.pem -days 365 -sha256 -CAcreateserial
}

clean_ca() {
  rm -fr truststore.pem truststore.key truststore.srl
}

success_message() {
  echo
  echo "Success: your client certificates have been generated in ./scripts/client_certificates"
}

upload_backup() {
  access_key_id=$(kubectl get secret aws-services -n hmpps-integration-api-$environment -o json | jq -r '.data."api-gateway"' | base64 --decode | jq -r '."access-credentials"."access-key-id"')
  secret_access_key=$(kubectl get secret aws-services -n hmpps-integration-api-$environment -o json | jq -r '.data."api-gateway"' | base64 --decode | jq -r '."access-credentials"."secret-access-key"')
  aws configure set aws_access_key_id $access_key_id
  aws configure set aws_secret_access_key $secret_access_key
  bucket="hmpps-integration-api-$environment-certificates-backup"
  client_folder="$client"
  path="$bucket/$client_folder"
  file_path="./${environment}-${client}-client.pem"

  aws s3api put-object --bucket "$bucket" --key "$client_folder/" --acl private
  aws s3 cp "$file_path" "s3://$path/client.pem"
  aws s3 cp ./"$environment"-"$client"-client.pem "s3://$path/client.pem"
  aws s3 cp ./"$environment"-"$client"-client.csr "s3://$path/client.csr"

  if [[ "$csr_required" == 0 ]]
  then
    aws s3 cp ./"$environment"-"$client"-client.key "s3://$path/client.key"
  fi

  aws configure set aws_access_key_id ""
  aws configure set aws_secret_access_key ""
}

verify_deps() {
  aws --version
  kubectl version
}

main() {
  verify_deps
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
