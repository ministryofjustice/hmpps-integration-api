#!/bin/bash

set -o pipefail

requiredVars=("MTLS_KEY" "MTLS_CERT" "API_KEY")

# endpoints from file
baseUrl=("https://dev.integration-api.hmpps.service.justice.gov.uk")
expected200Endpoints=("$baseUrl/v1/persons/X828566")
expected403Endpoints=("")

echo -e "=========\n"

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

echo -n "Integration tests, expected 200 HTTP status code\n"
for(( i=0; i<${#expected200Endpoints[@]}; i++ ));
do

  expected_200_http_status_code=$(curl -s -o response.txt -w "%{http_code}" "${#expected200Endpoints[i]}" -H "x-api-key: ${API_KEY}" --cert /tmp/client.pem --key /tmp/client.key)

  if [[ $expected_200_http_status_code != "200" ]]; then
    echo -e "[Integration test for endpoint ${#expected200Endpoints[i]}] 📋 $expected_200_http_status_code - $(jq '.userMessage' response.txt)"
  fi
  echo

done

echo -n "Integration tests, expected 403 HTTP status code\n"
for(( i=0; i<${#expected403Endpoints[@]}; i++ ));
do

  expected_403_http_status_code=$(curl -s -o response.txt -w "%{http_code}" "${#expected403Endpoints[i]}")

  if [[ $expected_403_http_status_code != "403" ]]; then
    echo -e "[Integration test for endpoint ${#expected403Endpoints[i]}] 📋 $expected_403_http_status_code- $(jq '.userMessage' response.txt)"
  fi
echo
done

exit 1
