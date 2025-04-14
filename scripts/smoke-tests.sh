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

baseUrl="https://dev.integration-api.hmpps.service.justice.gov.uk"
timeout=2
hmppsId="A8451DY"
alternativeHmppsId="G6333VK"
plpHmppsId="A5502DZ"
deliusCrn="X725642"
risksCrn="X756352"
prisonId="MKI"
visitReference="qd-lh-gy-lx"
clientReference="123456"
contactId="1898610"

# Endpoints for testing full access

get_endpoints=(
  "/v1/hmpps/id/by-nomis-number/$hmppsId"
  "/v1/hmpps/id/nomis-number/by-hmpps-id/$hmppsId"
  "/v1/persons/$hmppsId/addresses"
  "/v1/persons/$hmppsId/contacts"
  "/v1/persons/$hmppsId/iep-level"
  "/v1/persons/$alternativeHmppsId/visit-orders"
  "/v1/persons/$hmppsId/visit-restrictions"
  "/v1/persons/$hmppsId/alerts"
  "/v1/persons/$hmppsId/alerts/pnd"
  "/v1/persons/$hmppsId/name"
  "/v1/persons/$hmppsId/cell-location"
  "/v1/persons/$hmppsId/risks/categories"
  "/v1/persons/$hmppsId/sentences"
  "/v1/persons/$hmppsId/offences"
  "/v1/persons/$hmppsId/reported-adjudications"
  "/v1/pnd/persons/$hmppsId/alerts"
  "/v1/prison/prisoners?first_name=john"
  "/v1/prison/prisoners/$hmppsId"
  "/v1/prison/$prisonId/prisoners/$hmppsId/balances"
  "/v1/prison/$prisonId/prisoners/$hmppsId/accounts/spends/balances"
  "/v1/prison/$prisonId/prisoners/$hmppsId/accounts/spends/transactions"
  "/v1/prison/$prisonId/prisoners/$hmppsId/transactions/canteen_test"
  "/v1/prison/$prisonId/prisoners/$hmppsId/non-associations"
  "/v1/contacts/123456"
  "/v1/persons?first_name=john"
  "/v1/persons/$deliusCrn"
  "/v1/persons/$hmppsId/licences/conditions"
  "/v1/persons/$hmppsId/needs"
  "/v1/persons/$hmppsId/risks/mappadetail"
  "/v1/persons/$hmppsId/risks/scores"
  "/v1/persons/$hmppsId/plp-induction-schedule"
  "/v1/persons/$hmppsId/plp-induction-schedule/history"
  "/v1/persons/$plpHmppsId/plp-review-schedule"
  "/v1/persons/$hmppsId/status-information"
  "/v1/persons/$hmppsId/sentences/latest-key-dates-and-adjustments"
  "/v1/persons/$hmppsId/risks/serious-harm"
  "/v1/persons/$hmppsId/risks/scores"
  "/v1/persons/$hmppsId/risks/dynamic"
#  "/v1/hmpps/reference-data" Currently 401 code from delius.
  "/v1/hmpps/id/nomis-number/$hmppsId"
  "/v1/persons/$hmppsId/visit/future"
  "/v1/visit/$visitReference"
  "/v1/visit/id/by-client-ref/$clientReference"
  "/v1/prison/$prisonId/visit/search?visitStatus=BOOKED"
  "/v1/persons/$deliusCrn/protected-characteristics"
  "/v1/epf/person-details/$deliusCrn/1"
  "/v1/persons/$risksCrn/risk-management-plan"
  "/v1/persons/$alternativeHmppsId/person-responsible-officer"
  "/v1/persons/$alternativeHmppsId/visitor/$contactId/restrictions"
)

broken_get_endpoints=(
# HMAI-427 Currently returning 404 as does not exist in probation. Will be solved when we add filter https://dsdmoj.atlassian.net/browse/HMAI-427
    "/v1/persons/$hmppsId/images"
# HMAI-442 Returns 403 https://dsdmoj.atlassian.net/jira/software/c/projects/HMAI/boards/1723/backlog?selectedIssue=HMAI-442
    "/v1/persons/$hmppsId/case-notes"
)

all_get_endpoints+=("${get_endpoints[@]}" "${broken_get_endpoints[@]}")
post_visit_endpoint="/v1/visit"
post_visit_data='{
  "prisonerId": "A8451DY",
  "prisonId": "MKI",
  "clientVisitReference": "123456",
  "visitRoom": "A1",
  "visitType": "SOCIAL",
  "visitRestriction": "OPEN",
  "startTimestamp": "2025-09-05T10:15:41",
  "endTimestamp": "2025-09-05T11:15:41",
  "visitNotes": [
   {
     "type": "VISITOR_CONCERN",
     "text": "Visitor is concerned their mother in law is coming!"
   }
  ],
  "visitContact": {
    "name": "John Smith",
    "telephone": "0987654321",
    "email": "john.smith@example.com"
  },
  "createDateTime": "2025-09-05T10:15:41",
  "visitorSupport": {
    "description": "Visually impaired assistance"
  }
}'
#       "visitors": [
#         {
#           "nomisPersonId": 654321,
#           "visitContact": true
#         }
#       ],

# Endpoints for testing of limited access (who is set up as if they have the private prison role) and no access consumers.
allowed_endpoint="/v1/persons/$hmppsId/name"
not_allowed_endpoint="/v1/persons?first_name=john"

echo -e "Beginning smoke tests\n"

# Full access smoke tests

echo -e "Beginning full access smoke tests - Should all return 200\n"

http_status_code=$(curl -s -o response.txt -w "%{http_code}" "${baseUrl}${post_visit_endpoint}" -X POST -H "x-api-key: ${FULL_ACCESS_API_KEY}" -H "Content-Type: application/json" -d "$post_visit_data" --cert /tmp/full_access.pem --key /tmp/full_access.key)
if [[ $http_status_code == "200" ]]; then
  echo -e "${GREEN}✔ ${post_visit_endpoint}${NC}"
else
  echo -e "${RED}✗ ${post_visit_endpoint} returned $http_status_code - $(jq '.userMessage' response.txt)${NC}"
  fail=true
fi

for endpoint in "${get_endpoints[@]}"
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

echo -e "Beginning limited access smoke tests - first endpoint should return 200, second should return 403\n"

  http_status_code=$(curl -s -o response.txt -w "%{http_code}" "${baseUrl}${allowed_endpoint}" -H "x-api-key: ${LIMITED_ACCESS_API_KEY}" --cert /tmp/limited_access.pem --key /tmp/limited_access.key)
  if [[ $http_status_code == "200" ]]; then
    echo -e "${GREEN}✔ ${allowed_endpoint} returned $http_status_code ${NC}"
  else
    echo -e "${RED}✗ ${allowed_endpoint} returned $http_status_code - $(jq '.userMessage' response.txt)${NC}"
    fail=true
  fi

  http_status_code=$(curl -s -o response.txt -w "%{http_code}" "${baseUrl}${not_allowed_endpoint}" -H "x-api-key: ${LIMITED_ACCESS_API_KEY}" --cert /tmp/limited_access.pem --key /tmp/limited_access.key)
  if [[ $http_status_code == "403" ]]; then
    echo -e "${GREEN}✔ ${not_allowed_endpoint} returned $http_status_code${NC}"
  else
    echo -e "${RED}✗ ${not_allowed_endpoint} returned $http_status_code - $(jq '.userMessage' response.txt)${NC}"
    fail=true
  fi

echo
echo -e "Completed limited access smoke tests\n"

# No access smoke tests

echo -e "Beginning no access smoke tests\n"
echo -e "Consumer has certs but no endpoints associated to them so should return 403\n"
  http_status_code=$(curl -s -o response.txt -w "%{http_code}" "${baseUrl}${not_allowed_endpoint}" -H "x-api-key: ${NO_ACCESS_API_KEY}" --cert /tmp/no_access.pem --key /tmp/no_access.key)
  if [[ $http_status_code == "403" ]]; then
    echo -e "${GREEN}✔ ${not_allowed_endpoint} returned $http_status_code${NC}"
  else
    echo -e "${RED}✗ ${not_allowed_endpoint} returned $http_status_code - $(jq '.userMessage' response.txt)${NC}"
    fail=true
  fi

echo
echo -e "Consumer has no certs so should not gain access to any endpoints\n"
  if curl -m "$timeout" -s --fail "${baseUrl}${allowed_endpoint}" > /dev/null 2>&1; then
    echo -e "${RED}✗ Successfully connected to ${baseUrl} (which is NOT what we wanted).${NC}"
  else
    echo -e "${GREEN}✔ Failed to connect to ${baseUrl} (as expected).${NC}"
  fi

echo
echo -e "Completed no access smoke tests\n"

echo -e "Check broken endpoints - Should return a 400, 403, 404 or 500 and be captured by a ticket in the backlog\n"
for endpoint in "${broken_get_endpoints[@]}"
do
  http_status_code=$(curl -s -o response.txt -w "%{http_code}" "${baseUrl}${endpoint}" -H "x-api-key: ${FULL_ACCESS_API_KEY}" --cert /tmp/full_access.pem --key /tmp/full_access.key)
  if [ "$http_status_code" -ge "400" ]; then
    echo -e "${GREEN}✔ ${endpoint} returned $http_status_code - $(jq '.userMessage' response.txt)${NC}${NC}"
  else
    echo -e "${RED}✗ ${endpoint} returned $http_status_code - $(jq '.userMessage' response.txt)${NC}"
    fail=true
  fi
done

echo -e "Completed smoke tests\n"

if [[ $fail == true ]]; then
  echo " 💔️️ Failed! Some tests have failed."
  exit 1
fi

exit 0
