#!/bin/bash

configure_aws_credentials() {
    local environment="$1"
    access_key_id=$(echo $AWS_CREDS | jq -r '."access-credentials"."access-key-id"')
    secret_access_key=$(echo $AWS_CREDS | jq -r '."access-credentials"."secret-access-key"')
    export AWS_ACCESS_KEY_ID="$access_key_id"
    export AWS_SECRET_ACCESS_KEY="$secret_access_key"
}

reset_aws_credentials() {
    export AWS_ACCESS_KEY_ID=""
    export AWS_SECRET_ACCESS_KEY=""
}

get_folders_from_s3() {
    local s3_bucket="$1"
    aws s3 ls "s3://$s3_bucket/" | grep 'PRE' | awk '{print $2}' | sed 's#/##'
}

get_certificate_from_s3() {
    local s3_bucket="$1"
    local client="$2"
    local file_path="$3"
    local s3_key="$client/client.pem"

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
        echo "Failed to read certificate expiry date from $file_path."
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

generate_message() {
    local difference="$1"
    local environment="$2"
    local certificate_name="$3"

    if [ "$difference" -le $(($EXPIRY_DAY_CHECK * 24 * 60 * 60)) ]; then
        echo "**ALERT ACTION REQUIRED** The certificate for $certificate_name in $environment will expire within the next $EXPIRY_DAY_CHECK days (in $((difference / (24 * 60 * 60))) days)."
    fi
}

check_certificate_expiry() {
    local certificate_source="$1"
    local source_details="$2"
    local file_path="$3"
    local environment="$4"
    local certificate_name="$5"
    local slack_webhook_url="$6"

    if [ "$certificate_source" == "s3" ]; then
        get_certificate_from_s3 "$source_details" "$certificate_name" "$file_path"
    fi

    local expiry_date
    expiry_date="$(get_certificate_expiry_date "$file_path")"

    local expiry_seconds
    expiry_seconds="$(convert_date_to_seconds "$expiry_date")"

    local current_seconds
    current_seconds="$(date +%s)"

    local difference=$((expiry_seconds - current_seconds))

    local message
    message=$(generate_message "$difference" "$environment" "$certificate_name")

    if [ -n "${message}" ]; then
      curl -X POST -H 'Content-type: application/json' --data "{\"text\":\"$message\"}" "$slack_webhook_url"
    fi
}

cleanup() {
    reset_aws_credentials
    rm -f /home/appuser/pem-certs/*.pem
}

trap cleanup EXIT

main() {
    slack_webhook_url=$(echo $SLACK_URL)
    configure_aws_credentials "$ENV"
    clients=$(get_folders_from_s3 "hmpps-integration-api-$ENV-certificates-backup")
    check_certificate_expiry "k8s" "client-certificate-auth" "/home/appuser/ca-cert/ca.crt" "$ENV" "internal" "$slack_webhook_url"
    for client in $clients; do
        check_certificate_expiry "s3" "hmpps-integration-api-$ENV-certificates-backup" "/home/appuser/pem-certs/client.pem" "$ENV" "$client" "$slack_webhook_url"
    done
}

main
