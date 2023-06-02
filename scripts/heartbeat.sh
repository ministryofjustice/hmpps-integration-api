#!/bin/bash

set -o pipefail


#curl -s -o /dev/null -w "%{http_code}%" -L "https://google.com";

echo "Retrieving certificates from context";
CERT=${MTLS_CERT} > /tmp/client.pem
KEY=${MTLS_KEY} >  /tmp/client.key
echo "Certificates retrieved";

echo "Retrieving API key from Circle CI secret";
API_KEY=${API_KEY}

curl ${SERVICE_URL} -H "x-api-key: ${API_KEY}" --cert /tmp/client.pem --key /tmp/client.key |grep firstName &&
echo $?

echo "fail on purpose"

exit 1
