#!/bin/bash

configure_aws_credentials() {
    local environment="$1"
    access_key_id=$(kubectl get secret aws-services -n hmpps-integration-api-"$environment" -o json | jq -r '.data."api-gateway"' | base64 --decode | jq -r '."access-credentials"."access-key-id"')
    secret_access_key=$(kubectl get secret aws-services -n hmpps-integration-api-"$environment" -o json | jq -r '.data."api-gateway"' | base64 --decode | jq -r '."access-credentials"."secret-access-key"')
    export AWS_ACCESS_KEY_ID="$access_key_id"
    export AWS_SECRET_ACCESS_KEY="$secret_access_key"
}

get_folders_from_s3() {
    local s3_bucket="$1"
    aws s3 ls "s3://$s3_bucket/" | grep 'PRE' | awk '{print $2}' | sed 's#/##'
}

get_certificate_from_s3() {
    local s3_bucket="$1"
    local client="$2"
    local s3_key="$client/client.pem"
    local file_path="./tmp/client.pem"

    aws s3 cp "s3://$s3_bucket/$s3_key" "$file_path"
    if [ $? -ne 0 ]; then
        echo "Failed to download certificate for $client from $s3_bucket."
        exit 1
    fi
}

get_certificate_expiry_date() {
    local file_path="$1"
    local expiry_date

    expiry_date=$(openssl x509 -in "$file_path" -noout -enddate | cut -d= -f2)
    if [ $? -ne 0 ]; then
        echo "Failed to read certificate expiry date."
        exit 1
    fi

    echo "$expiry_date"
}

convert_date_to_seconds() {
    local date_str="$1"
    if date --version >/dev/null 2>&1; then
        # GNU date
        date -d "$date_str" +%s
    else
        # BSD date (macOS)
        date -jf "%b %d %H:%M:%S %Y %Z" "$date_str" +%s 2>/dev/null || date -jf "%b %d %H:%M:%S %Y %Z" "$date_str" "+%s"
    fi
}

check_certificate_expiry() {
    local environment="$1"
    local client="$2"
    local slack_webhook_url="$3"
    local s3_bucket="hmpps-integration-api-$environment-certificates-backup"
    local file_path="./tmp/client.pem"

    get_certificate_from_s3 "$s3_bucket" "$client"

    local expiry_date
    expiry_date="$(get_certificate_expiry_date "$file_path")"

    local expiry_seconds
    expiry_seconds="$(convert_date_to_seconds "$expiry_date")"

    local current_seconds
    current_seconds="$(date +%s)"

    local difference=$((expiry_seconds - current_seconds))

    if [ "$difference" -le $((30 * 24 * 60 * 60)) ]; then
        message="**TEST** The certificate for $client in $environment will expire within the next 30 days (in $((difference / (24 * 60 * 60))) days)."
        curl -X POST -H 'Content-type: application/json' --data "{\"text\":\"$message\"}" "$slack_webhook_url"
    else
        message="**TEST** The certificate for $client in $environment is valid for more than 30 days and expires on $expiry_date."
        curl -X POST -H 'Content-type: application/json' --data "{\"text\":\"$message\"}" "$slack_webhook_url"
    fi
}

check_internal_certificate_expiry() {
    local environment="$1"
    local slack_webhook_url="$2"
    local file_path="./tmp/ca.crt"

    kubectl get secret "client-certificate-auth" -n "hmpps-integration-api-$environment" -o json | jq -r '.data."ca.crt"' | base64 --decode > "$file_path"

    local expiry_date
    expiry_date="$(get_certificate_expiry_date "$file_path")"

    local expiry_seconds
    expiry_seconds="$(convert_date_to_seconds "$expiry_date")"

    local current_seconds
    current_seconds="$(date +%s)"

    local difference=$((expiry_seconds - current_seconds))

    if [ "$difference" -le $((30 * 24 * 60 * 60)) ]; then
        message="**TEST** The internal certificate in $environment will expire within the next 30 days (in $((difference / (24 * 60 * 60))) days)."
        curl -X POST -H 'Content-type: application/json' --data "{\"text\":\"$message\"}" "$slack_webhook_url"
    else
        message="**TEST** The internal certificate in $environment is valid for more than 30 days and expires on $expiry_date."
        curl -X POST -H 'Content-type: application/json' --data "{\"text\":\"$message\"}" "$slack_webhook_url"
    fi
}

main() {
    environments=("dev" "preprod" "prod")
    slack_webhook_url=$(kubectl -n hmpps-integration-api-dev get secrets slack-webhook-url -o json | jq -r '.data."slack_webhook_url"' | base64 --decode)

    for environment in "${environments[@]}"; do
        configure_aws_credentials "$environment"
#        clients=$(get_folders_from_s3 "hmpps-integration-api-$environment-certificates-backup")
        clients=("ctrlo")
        for client in $clients; do
            check_certificate_expiry "$environment" "$client" "$slack_webhook_url"
        done
        check_internal_certificate_expiry "$environment" "$slack_webhook_url"
    done
}

main
