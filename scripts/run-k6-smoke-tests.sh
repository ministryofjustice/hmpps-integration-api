#!/bin/bash
set -e

echo -e "\n[Setup] Retrieving certificates from context";
echo -n "$FULL_ACCESS_CERT" | base64 --decode > /tmp/full_access.pem
echo -n "$FULL_ACCESS_KEY" | base64 --decode > /tmp/full_access.key
echo -n "$LIMITED_ACCESS_CERT" | base64 --decode > /tmp/limited_access.pem
echo -n "$LIMITED_ACCESS_KEY" | base64 --decode > /tmp/limited_access.key
echo -n "$NO_ACCESS_CERT" | base64 --decode > /tmp/no_access.pem
echo -n "$NO_ACCESS_KEY" | base64 --decode > /tmp/no_access.key
echo -e "[Setup] Certificates retrieved\n"

echo -e "Beginning full access smoke tests - Should all return 200\n"
k6 run ./scripts/K6/dist/full-access-smoke-tests.js
echo -e "Completed full access smoke tests\n"

echo -e "Beginning limited access smoke tests - first endpoint should return 200, second should return 403\n"
k6 run ./scripts/K6/dist/limited-access-smoke-tests.js
echo -e "Completed limited access smoke tests\n"

echo -e "Beginning no access smoke tests\n"
echo -e "Consumer has certs but no endpoints associated to them so should return 403\n"
k6 run ./scripts/K6/dist/no-access-with-certs-smoke-tests.js

echo -e "Consumer has no certs so should not gain access to any endpoints\n"
k6 run ./scripts/K6/dist/no-access-without-certs-smoke-tests.js

echo -e "Completed no access smoke tests\n"
