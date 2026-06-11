#!/bin/bash
set -e

clean() {
  rm -fr *.pem *.key *.csr *-api-key
}

validate_csr() {
  local csr_file=$1

  if [[ ! -f "$csr_file" ]]; then
    echo "Error: CSR file $csr_file not found for validation."
    exit 1
  fi

  local csr_cn
  csr_cn=$(openssl req -in "$csr_file" -noout -subject | sed -n 's/.*[=,/]CN *=[ ]* \?\([^/,]*\).*/\1/p' | xargs)

  echo "Validating CSR..."
  echo "Expected Client Name: $client"
  echo "Found CN in CSR:     $csr_cn"

  if [[ "$csr_cn" != "$client" ]]; then
    echo "-----------------------------------------------------"
    echo " ERROR: CSR validation failed!"
    echo " The Common Name (CN) '$csr_cn' in the CSR does not match"
    echo " the expected client identifier '$client'."
    echo "-----------------------------------------------------"
    exit 1
  else
    echo "Success: CSR CN matches the client identifier."
  fi
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

  if [[ "$csr_required" == 1 ]]
  then
    echo "Do you wish to create a tar ball containing the client.pem to send to the client?"
    echo "(y)es, (n)o, or (c)ancel:"
    read create_tar_ball_user_input

    case $create_tar_ball_user_input in
      [yY])
        echo "User selected 'Yes'."
        create_tar_ball=1
        ;;
      [nN])
        echo "User selected 'No'."
        create_tar_ball=0
        ;;
      [cC])
        echo "User selected 'Cancel'."
        exit 1
        ;;
      *)
        echo "Invalid choice."
        exit 1
    esac

    if [[ "$create_tar_ball" == 1 ]]
    then
      echo "Do you wish to add the clients API key to the tar ball (i.e for new consumers)"
      echo "(y)es, (n)o, or (c)ancel:"
      read add_api_key_user_input
        case $add_api_key_user_input in
          [yY])
            echo "User selected 'Yes'."
            add_api_key=1
            ;;
          [nN])
            echo "User selected 'No'."
            add_api_key=0
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
  fi

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
        validate_csr "./csrs/$environment/$client/$environment-$client-client.csr"
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

  echo "Do you wish to upload the generated certificates to the S3 backup bucket?"
  echo "(y)es, (n)o, or (c)ancel:"
  read upload_s3_user_input
  case $upload_s3_user_input in
    [yY])
      upload_to_s3=1
      ;;
    [nN])
      upload_to_s3=0
      ;;
    [cC])
      echo "User selected 'Cancel'."
      exit 1
      ;;
    *)
      echo "Invalid choice."
      exit 1
  esac

  echo "-----------------------------------------------------"
  echo "   PLEASE REVIEW CHOICES BEFORE GENERATING CERTS"
  echo "-----------------------------------------------------"
  echo "Environment:            $environment"
  echo "Client Identifier:      $client"
  echo "Using supplied CSR?:    $( [[ "$csr_required" == 1 ]] && echo 'Yes' || echo 'No' )"

  if [[ "$csr_required" == 1 ]]; then
    echo "Create Tarball?:        $( [[ "$create_tar_ball" == 1 ]] && echo 'Yes' || echo 'No' )"
    if [[ "$create_tar_ball" == 1 ]]; then
      echo "Add API Key to Tarball: $( [[ "$add_api_key" == 1 ]] && echo 'Yes' || echo 'No' )"
    fi
  else
    echo "Client Organisation:    $organisation"
    echo "Client Region:          $region"
  fi

  echo "Upload Backups to S3?:  $( [[ "$upload_to_s3" == 1 ]] && echo 'Yes' || echo 'No' )"
  echo "-----------------------------------------------------"
  echo "Are you sure these options are correct? (y/n)"
  read confirmation

  case $confirmation in
    [yY])
      echo "Confirmation accepted. Proceeding..."
      ;;
    *)
      echo "Execution aborted by user."
      exit 1
      ;;
  esac
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

  validate_csr "$environment-$client-client.csr"

  openssl x509 -req -in $environment-$client-client.csr -CA truststore.pem -CAkey truststore.key -out $environment-$client-client.pem -days 365 -sha256 -CAcreateserial
}

clean_ca() {
  rm -fr truststore.pem truststore.key truststore.srl
}

success_message() {

  if [[ "$create_tar_ball" != 1 ]]
  then
    echo
    echo "Success: your client certificates have been generated in ./scripts/client_certificates"
  fi
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

generate_tar_ball(){
  if [[ "$create_tar_ball" == 1 ]]
  then
      if [[ "$add_api_key" == 1 ]]
      then
        get_api_key
        tar cvfz hmpps-integration-api-$client-$environment.tar.gz $environment-$client-api-key $environment-$client-client.pem
         echo "Success: hmpps-integration-api-$client-$environment.tar.gz containing $environment-$client-client.pem and $environment-$client-api-key has been generated in ./scripts/client_certificates"
      else
        tar cvfz hmpps-integration-api-$client-$environment.tar.gz $environment-$client-client.pem
        echo "Success: hmpps-integration-api-$client-$environment.tar.gz containing $environment-$client-client.pem has been generated in ./scripts/client_certificates"
      fi
      echo
      echo "To untar and check contents run: tar -xvf hmpps-integration-api-$client-$environment.tar.gz"
  fi
}

get_api_key() {
  api_key=`kubectl -n hmpps-integration-api-$environment get secrets consumer-api-keys -o json | jq -r .data.'"'$client'"' | base64 -d`
  echo -n $api_key > $environment-$client-api-key
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
  generate_tar_ball
  success_message

  if [[ "$upload_to_s3" == 1 ]]; then
    upload_backup
  else
    echo "Skipping S3 backup upload."
  fi

  clean_ca
  trap clean_ca EXIT
  trap clean_ca SIGINT
}

main
