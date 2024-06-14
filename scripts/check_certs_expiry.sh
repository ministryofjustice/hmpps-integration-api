#!/bin/bash

CERT_FILE="client_certificates/dev-event-service-client.pem"

if [ ! -f "$CERT_FILE" ]; then
  echo "Certificate file not found!"
  exit 1
fi

EXPIRY_DATE=$(openssl x509 -in "$CERT_FILE" -noout -enddate | cut -d= -f2)

date_to_seconds() {
  if date --version >/dev/null 2>&1; then
    # GNU date
    date -d "$1" +%s
  else
    # BSD date (macOS)
    date -jf "%b %d %H:%M:%S %Y %Z" "$1" +%s 2>/dev/null || date -jf "%b %d %H:%M:%S %Y %Z" "$1" "+%s"
  fi
}

EXPIRY_DATE_SECONDS=$(date_to_seconds "$EXPIRY_DATE")

if date --version >/dev/null 2>&1; then
  # GNU date
  CURRENT_DATE_SECONDS=$(date +%s)
  THIRTY_DAYS_SECONDS=$(date -d "+30 days" +%s)
else
  # BSD date (macOS)
  CURRENT_DATE_SECONDS=$(date +%s)
  THIRTY_DAYS_SECONDS=$(date -v +30d +%s)
fi

DIFFERENCE=$((EXPIRY_DATE_SECONDS - CURRENT_DATE_SECONDS))

if [ "$DIFFERENCE" -le $((30 * 24 * 60 * 60)) ]; then
  DAYS_LEFT=$((DIFFERENCE / (24 * 60 * 60)))
  echo "The certificate will expire on $EXPIRY_DATE in $DAYS_LEFT days."
else
  echo "The certificate is valid for more than 30 days."
fi
