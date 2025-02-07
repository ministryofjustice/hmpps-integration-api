#!/bin/bash

# This script makes calls to test endpoints for the purpose of performance testing

if [[ -z "${API_KEY}" ]]; then
  echo "No API key set"
  exit 2
fi

BASE_URL="https://dev.integration-api.hmpps.service.justice.gov.uk/v1/performance-test"
CERT="$HOME/client_certificates/dev-bmadley-client.pem"
KEY="$HOME/client_certificates/dev-bmadley-client.key"

if [[ -z "${REQS_PER_SECOND}" ]]; then
  REQS_PER_SECOND=6
else
  REQS_PER_SECOND=$((REQS_PER_SECOND))
fi
if [[ -z "${TEST_DURATION_SECONDS}" ]]; then
  TEST_DURATION_SECONDS=300
else
  TEST_DURATION_SECONDS=$((TEST_DURATION_SECONDS))
fi

LOOPS_PER_SECOND=$((REQS_PER_SECOND/3))
TOTAL_LOOPS=$((LOOPS_PER_SECOND * TEST_DURATION_SECONDS))
DELAY=$((1000000 / LOOPS_PER_SECOND)) # In microseconds

START_TIME=$(date +%s)
reqs_performed=0

#while [[ $(date +%s) -lt $((START_TIME + TEST_DURATION_SECONDS)) ]]; do
#  curl -s --cert "$CERT" --key "$KEY" -X GET "$BASE_URL/test-1/LEI/G6980GG" -H "x-api-key: $API_KEY" &
#  curl -s --cert "$CERT" --key "$KEY" -X GET "$BASE_URL/test-2?first_name=Tom" -H "x-api-key: $API_KEY" &
#  curl -s --cert "$CERT" --key "$KEY" -X POST "$BASE_URL/test-3" -H "x-api-key: $API_KEY" -H "Content-Type: application/json" -d '{"type": "CANT", "description": "Canteen Purchase of £16.34", "amount": 1634, "clientTransactionId": "CL123212", "clientUniqueRef": "CLIENT121131-0_11"}' &
#
#  reqs_performed=$((reqs_performed + 1))
#  sleep $(bc <<< "scale=6; $DELAY / 1000000")
#done

for ((reqs=1; reqs <= TOTAL_LOOPS; reqs++)) do
  curl -s --cert "$CERT" --key "$KEY" -X GET "$BASE_URL/test-1/LEI/G6980GG" -H "x-api-key: $API_KEY" &
  curl -s --cert "$CERT" --key "$KEY" -X GET "$BASE_URL/test-2?first_name=Tom" -H "x-api-key: $API_KEY" &
  curl -s --cert "$CERT" --key "$KEY" -X POST "$BASE_URL/test-3/LEI/G6980GG" -H "x-api-key: $API_KEY" -H "Content-Type: application/json" -d '{"type": "CANT", "description": "Canteen Purchase of £16.34", "amount": 1634, "clientTransactionId": "CL123212", "clientUniqueRef": "CLIENT121131-0_11"}' &

  reqs_performed=$((reqs_performed + 1))
  sleep "$(bc <<< "scale=6; $DELAY / 1000000")"
done

wait

END_TIME=$(date +%s)
ACTUAL_REQS_PER_SECOND=$(bc <<< "scale=2; $((reqs_performed * 3)) / ($END_TIME - $START_TIME)")

echo "Target Rate: $REQS_PER_SECOND requests/second"
echo "Actual Rate: $ACTUAL_REQS_PER_SECOND requests/second"
echo "Total Requests: $reqs_performed"
echo "Duration: $TEST_DURATION_SECONDS seconds"
