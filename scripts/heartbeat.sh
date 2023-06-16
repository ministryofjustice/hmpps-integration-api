#!/bin/bash

set -o pipefail

retry_attempts=5
retry_after_seconds=10
requiredVars=("MTLS_KEY" "MTLS_CERT" "SERVICE_URL" "API_KEY")

for str in "${requiredVars[@]}"; do
  if [[ -z "${!str}" ]]; then
    echo "CircleCI context variable was undefined: $str"
    fail=1
  fi
done

if [[ $fail == 1 ]]; then
  echo "**FAIL** Missing CircleCI context variable(s)"
  exit 1
fi

echo "Retrieving certificates from context";
echo -n "${MTLS_CERT}" | base64 --decode > /tmp/client.pem
echo -n "${MTLS_KEY}" | base64 --decode > /tmp/client.key
echo "Certificates retrieved";

for (( attempts=1;attempts<=retry_attempts;attempts++ ))
do
  echo "[Attempt ${attempts}] 🧑‍⚕️ Checking for a heartbeat..."
  curl --silent "${SERVICE_URL}" -H "x-api-key: ${API_KEY}" --cert /tmp/client.pem --key /tmp/client.key | grep firstName > /dev/null

  if [[ $? == 0 ]]; then
    echo "[Attempt ${attempts}] ✅ Success! Located firstName in response"
    exit 0
  else
    echo -e "[Attempt ${attempts}] 💔️ Failed! Unable to locate firstName in response"

    if [[ $attempts != "$retry_attempts" ]]; then
      sleep $retry_after_seconds
      echo -e "[Attempt ${attempts}] 🔁 Retrying..."
    fi
    echo
  fi
done

exit 1
