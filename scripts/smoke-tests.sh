#!/bin/bash

set -o pipefail

requiredVars=("MTLS_KEY" "MTLS_CERT" "API_KEY")

GREEN='\033[0;32m'
RED='\033[0;31m'
NC='\033[0m' # No Color

# endpoints from file
baseUrl="https://dev.integration-api.hmpps.service.justice.gov.uk"
hmppsId="X828566"
prisonId="MDI"
endpoints=(
  "$baseUrl/v1/persons/$hmppsId"
  "$baseUrl/v1/epf/person-details/$hmppsId/1"
  "$baseUrl/v1/persons/$hmppsId/licences/conditions"
  "$baseUrl/v1/persons/$hmppsId/alerts"
  "$baseUrl/v1/persons/$hmppsId/case-notes"
  "$baseUrl/v1/persons/$hmppsId/contact"
  "$baseUrl/v1/persons/$hmppsId/iep-level"
  "$baseUrl/v1/persons/$hmppsId/person-responsible-officer"
  "$baseUrl/v1/persons/$hmppsId/reported-adjudications"
  "$baseUrl/v1/persons/$hmppsId/needs"
  "$baseUrl/v1/persons/$hmppsId/risks/mappadetail"
  "$baseUrl/v1/persons/$hmppsId/risks/scores"
  "$baseUrl/v1/prison/$prisonId/prisoners/$hmppsId/non-associations"
  "$baseUrl/v1/prison/$prisonId/visit/search"
)

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

echo -e "Beginning smoke tests\n"
for endpoint in "${endpoints[@]}"
do
  echo -e "${endpoint}"

  expected_200_http_status_code=$(curl -s -o response.txt -w "%{http_code}" "${endpoint}" -H "x-api-key: ${API_KEY}" --cert /tmp/client.pem --key /tmp/client.key)
  if [[ $expected_200_http_status_code == "200" ]]; then
    echo -e "✅ ${GREEN}Success! $expected_200_http_status_code${NC}"
  else
    echo -e "❌ ${RED}Failed! $expected_200_http_status_code - $(jq '.userMessage' response.txt)${NC}"
    fail=true
  fi

  expected_403_http_status_code=$(curl -s -o response.txt -w "%{http_code}" "${endpoint}" --cert /tmp/client.pem --key /tmp/client.key)
  if [[ $expected_403_http_status_code == "403" ]]; then
    echo -e "✅ ${GREEN}Success! $expected_403_http_status_code${NC}"
  else
    echo -e "❌ ${RED}Failed! $expected_403_http_status_code - $(jq '.userMessage' response.txt)${NC}"
    fail=true
  fi
done
echo -e "Completed smoke tests\n"

if [[ $fail == true ]]; then
  echo " 💔️️ Failed! Some tests have failed."
  exit 1
fi

exit 0
