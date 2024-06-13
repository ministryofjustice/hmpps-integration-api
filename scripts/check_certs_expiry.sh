#!/bin/bash

# File to check
CERT_FILE="client_certificates/dev-event-service-client.pem"

if [ ! -f "$CERT_FILE" ]; then
  echo "Certificate file not found!"
  exit 1
fi

EXPIRY_DATE=$(openssl x509 -in "$CERT_FILE" -noout -enddate | cut -d= -f2)

# convert date to seconds
date_to_seconds() {
  if date --version >/dev/null 2>&1; then
    # GNU date
    date -d "$1" +%s
  else
    # BSD date (macOS)
    date -jf "%b %d %H:%M:%S %Y %Z" "$1" +%s 2>/dev/null || date -jf "%b %d %H:%M:%S %Y %Z" "$1" "+%s"
  fi
}

# Convert expiry date to seconds
EXPIRY_DATE_SECONDS=$(date_to_seconds "$EXPIRY_DATE")
THIRTY_DAYS_SECONDS=$(date_to_seconds "$(date -v +30d)")

if [ "$EXPIRY_DATE_SECONDS" -le "$THIRTY_DAYS_SECONDS" ]; then
  echo "The certificate will expire within the next 30 days."
else
  echo "The certificate is valid for more than 30 days."
fi
