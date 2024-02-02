#!/bin/bash

set -e

ctrlo_api_key=$1
ctrlo_crn=$2
event_number=$3

test_ctrlo() {
  curl -H "x-api-key: $ctrlo_api_key" "https://dev.integration-api.hmpps.service.justice.gov.uk/v1/epf/person-details/$ctrlo_crn/$event_number" --cert ./cert.pem --key ./cert.key
  date >> /tmp/result
}

run_test() {
  echo > /tmp/result

  while true; do
    echo
    test_ctrlo &
    test_ctrlo &
    echo
    echo
    echo "running"
  done
}

run_test
