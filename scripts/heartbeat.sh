#!/bin/bash

set -o pipefail

requiredVars=("MTLS_KEY" "MTLS_CERT" "SERVICE_URL" "API_KEY")

for str in requiredVars[@]; do
  if [[ -z "${!$str}" ]]; then
    echo "CIRCLECI Context variable was undefined: $str"
    exit 1
  else
    echo "Found secret $str"
  fi
done

echo "Retrieving certificates from context";
echo -n "${MTLS_CERT}" | base64 --decode > /tmp/client.pem
echo -n "${MTLS_KEY}" | base64 --decode > /tmp/client.key
echo "Certificates retrieved";

curl --silent "${SERVICE_URL}" -H "x-api-key: ${API_KEY}" --cert /tmp/client.pem --key /tmp/client.key | grep firstName

if [ $? -eq 0 ]; then
  echo "Located firstName in response"
  echo "**SUCCESS**"
fi

exit $?