#!/bin/bash

set -o pipefail

#curl -s -o /dev/null -w "%{http_code}%" -L "https://google.com";

echo "Retrieving certificates from context";
echo -n "${MTLS_CERT}" | base64 --decode > /tmp/client.pem
echo -n "${MTLS_KEY}" | base64 --decode > /tmp/client.key
echo "Certificates retrieved";

curl --silent "${SERVICE_URL}" -H "x-api-key: ${API_KEY}" --cert /tmp/client.pem --key /tmp/client.key | grep firstName
echo "Assertion: firstName field exists. Result was $?"
exit $?