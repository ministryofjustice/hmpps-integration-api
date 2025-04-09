#!/bin/bash

set -o pipefail

requiredVars=("FULL_ACCESS_KEY" "FULL_ACCESS_CERT" "FULL_ACCESS_API_KEY" "LIMITED_ACCESS_KEY" "LIMITED_ACCESS_CERT" "LIMITED_ACCESS_API_KEY" "NO_ACCESS_KEY" "NO_ACCESS_CERT" "NO_ACCESS_API_KEY")

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
echo -n "${FULL_ACCESS_CERT}" | base64 --decode > /tmp/full_access.pem
echo -n "${FULL_ACCESS_KEY}" | base64 --decode > /tmp/full_access.key
echo -n "${LIMITED_ACCESS_CERT}" | base64 --decode > /tmp/limited_access.pem
echo -n "${LIMITED_ACCESS_KEY}" | base64 --decode > /tmp/limited_access.key
echo -n "${NO_ACCESS_CERT}" | base64 --decode > /tmp/no_access.pem
echo -n "${NO_ACCESS_KEY}" | base64 --decode > /tmp/no_access.key
echo -e "[Setup] Certificates retrieved\n";

# Endpoints

baseUrl="https://dev.integration-api.hmpps.service.justice.gov.uk"
hmppsId="A8451DY"
prisonId="MKI"
allowed_endpoints=(
  "/v1/hmpps/id/by-nomis-number/$hmppsId"
  "/v1/hmpps/id/nomis-number/by-hmpps-id/$hmppsId"
  "/v1/persons?first_name=john"
  "/v1/persons/$hmppsId"
  "/v1/persons/$hmppsId/addresses"
  "/v1/persons/$hmppsId/contacts"
  "/v1/persons/$hmppsId/iep-level"
  #"/v1/persons/$hmppsId/visitor/123456/restrictions"
  "/v1/persons/$hmppsId/visit-restrictions"
  "/v1/persons/$hmppsId/visit-orders"
  "/v1/persons/$hmppsId/visit/future"
  "/v1/persons/$hmppsId/alerts"
  "/v1/persons/$hmppsId/alerts/pnd"
  "/v1/persons/$hmppsId/case-notes"
  "/v1/persons/$hmppsId/name"
  "/v1/persons/$hmppsId/cell-location"
  "/v1/persons/$hmppsId/risks/categories"
  "/v1/persons/$hmppsId/sentences"
  "/v1/persons/$hmppsId/offences"
  "/v1/persons/$hmppsId/person-responsible-officer"
  "/v1/persons/$hmppsId/protected-characteristics"
  "/v1/persons/$hmppsId/reported-adjudications"
  "/v1/pnd/persons/$hmppsId/alerts"
  "/v1/prison/prisoners?first_name=john"
  "/v1/prison/prisoners/$hmppsId"
  "/v1/prison/$prisonId/prisoners/$hmppsId/balances"
  "/v1/prison/$prisonId/prisoners/$hmppsId/accounts/spends/balances"
  "/v1/prison/$prisonId/prisoners/$hmppsId/accounts/spends/transactions"
  "/v1/prison/$prisonId/prisoners/$hmppsId/transactions/canteen_test"
  #"/v1/prison/$prisonId/prisoners/$hmppsId/transactions"
  "/v1/prison/$prisonId/prisoners/$hmppsId/non-associations"
  "/v1/prison/$prisonId/visit/search?visitStatus=BOOKED"
  #"/v1/visit/[^/]*$"
  #"/v1/visit"
  #"/v1/visit/id/by-client-ref/AABDC234"
  #"/v1/visit/.*/cancel"
  "/v1/contacts/123456"
)
not_allowed_endpoints=(
  "/v1/epf/person-details/$hmppsId/1"
  "/v1/persons/$hmppsId/licences/conditions"
  "/v1/persons/$hmppsId/needs"
  "/v1/persons/$hmppsId/risks/mappadetail"
  "/v1/persons/$hmppsId/risks/scores"
  "/v1/persons/$hmppsId/plp-review-schedule"
  "/v1/persons/$hmppsId/plp-induction-schedule"
  "/v1/persons/$hmppsId/plp-induction-schedule/history"
  "/v1/persons/$hmppsId/status-information"
  "/v1/persons/$hmppsId/sentences/latest-key-dates-and-adjustments"
  "/v1/persons/$hmppsId/risks/serious-harm"
  "/v1/persons/$hmppsId/risks/scores"
  "/v1/persons/$hmppsId/risks/dynamic"
  "/v1/persons/$hmppsId/risk-management-plan"
  "/v1/persons/$hmppsId/images"
  "/v1/hmpps/reference-data"
  "/v1/hmpps/id/nomis-number/$hmppsId"
)
all_endpoints+=("${allowed_endpoints[@]}" "${not_allowed_endpoints[@]}")

echo -e "Beginning smoke tests\n"

# Full access smoke tests

echo -e "Beginning full access smoke tests - Should all return 200\n"
for endpoint in "${all_endpoints[@]}"
do
  http_status_code=$(curl -s -o response.txt -w "%{http_code}" "${baseUrl}${endpoint}" -H "x-api-key: ${FULL_ACCESS_API_KEY}" --cert /tmp/full_access.pem --key /tmp/full_access.key)
  if [[ $http_status_code == "200" ]]; then
    echo -e "${GREEN}✔ ${endpoint}${NC}"
  else
    echo -e "${RED}✗ ${endpoint} returned $http_status_code - $(jq '.userMessage' response.txt)${NC}"
    fail=true
  fi
done
echo
echo -e "Completed full access smoke tests\n"

# Limited access smoke tests

echo -e "Beginning limited access smoke tests\n"

echo -e "Limited access smoke tests - Should all return 200\n"
for endpoint in "${allowed_endpoints[@]}"
do
  http_status_code=$(curl -s -o response.txt -w "%{http_code}" "${baseUrl}${endpoint}" -H "x-api-key: ${LIMITED_ACCESS_API_KEY}" --cert /tmp/limited_access.pem --key /tmp/limited_access.key)
  if [[ $http_status_code == "200" ]]; then
    echo -e "${GREEN}✔ ${endpoint}${NC}"
  else
    echo -e "${RED}✗ ${endpoint} returned $http_status_code - $(jq '.userMessage' response.txt)${NC}"
    fail=true
  fi
done
echo

echo -e "Limited access smoke tests - Should all return 403\n"
for endpoint in "${not_allowed_endpoints[@]}"
do
  http_status_code=$(curl -s -o response.txt -w "%{http_code}" "${baseUrl}${endpoint}" -H "x-api-key: ${LIMITED_ACCESS_API_KEY}" --cert /tmp/limited_access.pem --key /tmp/limited_access.key)
  if [[ $http_status_code == "403" ]]; then
    echo -e "${GREEN}✔ ${endpoint}${NC}"
  else
    echo -e "${RED}✗ ${endpoint} returned $http_status_code - $(jq '.userMessage' response.txt)${NC}"
    fail=true
  fi
done

echo
echo -e "Completed limited access smoke tests\n"

# No access smoke tests

echo -e "Beginning no access smoke tests - Should all return 403\n"
for endpoint in "${all_endpoints[@]}"
do
  http_status_code=$(curl -s -o response.txt -w "%{http_code}" "${baseUrl}${endpoint}" -H "x-api-key: ${NO_ACCESS_API_KEY}" --cert /tmp/no_access.pem --key /tmp/no_access.key)
  if [[ $http_status_code == "403" ]]; then
    echo -e "${GREEN}✔ ${endpoint}${NC}"
  else
    echo -e "${RED}✗ ${endpoint} returned $http_status_code - $(jq '.userMessage' response.txt)${NC}"
    fail=true
  fi
done
echo
echo -e "Completed no access smoke tests\n"

echo -e "Completed smoke tests\n"

if [[ $fail == true ]]; then
  echo " 💔️️ Failed! Some tests have failed."
  exit 1
fi

exit 0
