#!/bin/bash

set -o pipefail

requiredVars=("MTLS_KEY" "MTLS_CERT" "SERVICE_URL" "API_KEY")

for str in "${requiredVars[@]}"; do
  if [[ -z "${!str}" ]]; then
    echo "CircleCI context variable was undefined: $str"
    fail=1
  fi
done

if [ $fail = 1 ]; then
  echo "**FAIL** Missing CircleCI context variable(s)"
  exit 1
fi

echo "Retrieving certificates from context";
echo -n "${MTLS_CERT}" | base64 --decode > /tmp/client.pem
echo -n "${MTLS_KEY}" | base64 --decode > /tmp/client.key
echo "Certificates retrieved";

curl --silent "${SERVICE_URL}" -H "x-api-key: ${API_KEY}" --cert /tmp/client.pem --key /tmp/client.key | grep firstName > /dev/null

if [ $? -eq 0 ]; then
  echo "**SUCCESS** Located firstName in response"
  exit 0
else
  echo "**FAIL** Failed to locate firstName in response"
  exit 1
fi