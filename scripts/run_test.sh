#!/bin/bash

set -e

curl http://hmpps-integration-api:8080/offences/all > /tmp/result
expected_substring="severityRanking"

grep $expected_substring /tmp/result