#!/bin/bash

set -o pipefail

requiredVars=("MTLS_KEY" "MTLS_CERT" "API_KEY")

GREEN='\033[0;32m'
RED='\033[0;31m'
NC='\033[0m' # No Color

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

baseUrl="https://dev.integration-api.hmpps.service.justice.gov.uk"
hmppsId="X828566"
prisonId="MDI"

echo -e "Beginning smoke tests\n"

# Full access smoke tests

all_endpoints=(
  "v1/persons/$hmppsId"
  "v1/epf/person-details/$hmppsId/1"
  "v1/persons/$hmppsId/licences/conditions"
  "v1/persons/$hmppsId/alerts"
  "v1/persons/$hmppsId/case-notes"
  "v1/persons/$hmppsId/contact"
  "v1/persons/$hmppsId/iep-level"
  "v1/persons/$hmppsId/person-responsible-officer"
  "v1/persons/$hmppsId/reported-adjudications"
  "v1/persons/$hmppsId/needs"
  "v1/persons/$hmppsId/risks/mappadetail"
  "v1/persons/$hmppsId/risks/scores"
  "v1/prison/$prisonId/prisoners/$hmppsId/non-associations"
  "v1/prison/$prisonId/visit/search"
)

echo -e "Beginning full access smoke tests - Should all return 200\n"
for endpoint in "${all_endpoints[@]}"
do
  http_status_code=$(curl -s -o response.txt -w "%{http_code}" "${baseUrl}/${endpoint}" -H "x-api-key: ${API_KEY}" --cert /tmp/client.pem --key /tmp/client.key)
  if [[ $http_status_code == "200" ]]; then
    echo -e "✅ ${GREEN}${endpoint}${NC}"
  else
    echo -e "${RED}❌ ${endpoint} returned $http_status_code - $(jq '.userMessage' response.txt)${NC}"
    fail=true
  fi
done
echo -e "Completed full access smoke tests\n"

# Limited access smoke tests

# TODO: Add limited access smoke tests

# No access smoke tests

echo -e "Beginning no access smoke tests - Should all return 403\n"
for endpoint in "${all_endpoints[@]}"
do
  http_status_code=$(curl -s -o response.txt -w "%{http_code}" "${baseUrl}/${endpoint}" --cert /tmp/client.pem --key /tmp/client.key)
  if [[ $http_status_code == "403" ]]; then
    echo -e "✅ ${GREEN}${endpoint}${NC}"
  else
    echo -e "${RED}❌ ${endpoint} returned $http_status_code - $(jq '.userMessage' response.txt)${NC}"
    fail=true
  fi
done
echo -e "Completed no access smoke tests\n"

echo -e "Completed smoke tests\n"

if [[ $fail == true ]]; then
  echo " 💔️️ Failed! Some tests have failed."
  exit 1
fi

exit 0
