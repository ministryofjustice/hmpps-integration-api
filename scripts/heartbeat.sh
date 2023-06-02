#!/bin/bash

set -o pipefail


#curl -s -o /dev/null -w "%{http_code}%" -L "https://google.com";

echo "Retrieving certificates from context";
echo -n "${MTLS_CERT}" > /tmp/client.pem &&
echo -n "${MTLS_KEY}" >  /tmp/client.key
echo "Certificates retrieved";

echo "Retrieving API key from Circle CI secret";
API_KEY=${API_KEY}

curl "https://dev.integration-api.hmpps.service.justice.gov.uk/v1/persons?first_name=Frank" -H "x-api-key: ${API_KEY}" --cert /tmp/client.pem --key /tmp/client.key |grep firstName &&
echo $?

echo "fail on purpose"

exit 1
