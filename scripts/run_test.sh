#!/bin/bash

set -e

curl http://hmpps-integration-api:8080/persons/1 > /tmp/result
expected_substring="severityRanking"

cat /tmp/result
grep $expected_substring /tmp/result

