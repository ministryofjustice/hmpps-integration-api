#!/bin/bash

check_certificate_file_exists() {
  if [ ! -f "$1" ]; then
    echo "Certificate file not found: $1"
    exit 1
  fi
}

get_certificate_expiry_date() {
  local cert_file="$1"
  local expiry_date

  expiry_date=$(openssl x509 -in "$cert_file" -noout -enddate | cut -d= -f2)
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
  local expiry_seconds="$1"
  local current_seconds="$2"
  local difference=$((expiry_seconds - current_seconds))

  if [ "$difference" -le $((30 * 24 * 60 * 60)) ]; then
    local days_left=$((difference / (24 * 60 * 60)))
    echo "The certificate will expire within the next 30 days (in $days_left days)."
  else
    echo "The certificate is valid for more than 30 days."
  fi
}

# Main function
main() {
  local cert_file="client_certificates/dev-notification-test-client.pem"

  check_certificate_file_exists "$cert_file"

  local expiry_date
  expiry_date=$(get_certificate_expiry_date "$cert_file")

  local expiry_date_seconds
  expiry_date_seconds=$(convert_date_to_seconds "$expiry_date")

  local current_date_seconds
  current_date_seconds=$(date +%s)

  echo "The certificate will expire on $expiry_date."

  check_certificate_expiry "$expiry_date_seconds" "$current_date_seconds"
}

main
