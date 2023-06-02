#!/bin/bash

set -o pipefail


#curl -s -o /dev/null -w "%{http_code}%" -L "https://google.com";

echo "Generating certificates";
./scripts/generate-client.certificate.sh "${CIRCLE_PROJECT_REPONAME}" "heartbeat";
echo "Certificates generated";

echo "Retrieving API key from Circle CI secret";
API_KEY=${HEARTBEAT_API_KEY}
echo "Got test fake api key: ${API_KEY}"

echo "fail on purpose"

exit 1
