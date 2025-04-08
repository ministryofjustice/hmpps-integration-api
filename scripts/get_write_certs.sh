#!/bin/bash

set -o pipefail

retry_attempts=5
retry_after_seconds=10
requiredVars=("MTLS_KEY" "MTLS_CERT" "SERVICE_URL" "API_KEY")

echo "get_write_certs"
echo -e "=========\n"

echo "[Config] Maximum retry attempts: $retry_attempts"
echo "[Config] Retry after: $retry_after_seconds seconds"

for str in "${requiredVars[@]}"; do
  if [[ -z "${!str}" ]]; then
    echo "[Config] CircleCI context variable was undefined: $str"
    fail=1
  fi
done

if [[ $fail == 1 ]]; then
  echo "[Config] 💔️️ Failed! Missing CircleCI context variable(s)"
  exit 1
fi

echo -e "\n[Setup] Retrieving certificates from context";
echo -n "${MTLS_CERT}" | base64 --decode > /tmp/client.pem
echo -n "${MTLS_KEY}" | base64 --decode > /tmp/client.key
echo -e "[Setup] Certificates retrieved\n";

exit 1
