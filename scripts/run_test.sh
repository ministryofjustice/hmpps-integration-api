#!/bin/bash

set -e

existing_person_id=A1234AL
curl http://hmpps-integration-api:8080/persons/${existing_person_id} > /tmp/result
expected_substring='"firstName":"DANIEL","lastName":"SMELLEY"'

echo "Running smoke test for GET /persons/${existing_person_id}..."
cat /tmp/result
grep $expected_substring /tmp/result > /dev/null

echo "Running smoke test for GET /persons/${existing_person_id}/images..."
curl http://hmpps-integration-api:8080/persons/${existing_person_id}/images > /tmp/result
expected_substring='"firstName":"DANIEL","lastName":"SMELLEY"'
cat /tmp/result
#grep $expected_substring /tmp/result > /dev/null
